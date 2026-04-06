package at.jku.se.service;

import at.jku.se.dto.response.ConflictResponse;
import at.jku.se.entity.Rule;
import at.jku.se.entity.Schedule;
import at.jku.se.entity.User;
import at.jku.se.entity.enums.TriggerType;
import at.jku.se.repository.RuleRepository;
import at.jku.se.repository.ScheduleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Detects scheduling and rule conflicts that could send contradictory
 * commands to the same device (FR-15).
 *
 * <p>Three conflict categories are checked:
 * <ol>
 *   <li><b>SCHEDULE_SCHEDULE</b> — two active schedules target the same device
 *       with the same cron expression but different action values.</li>
 *   <li><b>RULE_RULE</b> — two active rules target the same action device
 *       with contradictory action values.</li>
 *   <li><b>RULE_SCHEDULE</b> — an active time-based rule and an active schedule
 *       target the same device at the same time with different actions.</li>
 * </ol>
 */
@ApplicationScoped
public class ConflictDetectionService {

    private static final Logger LOG = Logger.getLogger(ConflictDetectionService.class.getName());

    @Inject
    ScheduleRepository scheduleRepo;

    @Inject
    RuleRepository ruleRepo;

    /**
     * Returns all detected conflicts, optionally scoped to a single user.
     *
     * @param user if non-null, only automations owned by this user are checked
     * @return list of conflict descriptions (empty if none)
     */
    public List<ConflictResponse> findAllConflicts(User user) {
        List<Schedule> schedules = user != null
                ? scheduleRepo.findByUser(user)
                : scheduleRepo.listAll();
        List<Rule> rules = user != null
                ? ruleRepo.findByUser(user)
                : ruleRepo.listAll();

        List<ConflictResponse> conflicts = new ArrayList<>();
        conflicts.addAll(findScheduleConflicts(schedules));
        conflicts.addAll(findRuleConflicts(rules));
        conflicts.addAll(findCrossConflicts(schedules, rules));

        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Conflict detection found " + conflicts.size() + " conflicts");
        }
        return conflicts;
    }

    /**
     * Checks whether a specific schedule conflicts with existing active schedules.
     *
     * @param schedule   the schedule to check
     * @param excludeId  ID to exclude from comparison (for updates), or null
     * @return list of conflicts (empty if none)
     */
    public List<ConflictResponse> checkScheduleConflicts(Schedule schedule, Long excludeId) {
        List<Schedule> existing = scheduleRepo.find(
                "device = ?1 AND cronExpression = ?2 AND active = true",
                schedule.device, schedule.cronExpression).list();

        List<ConflictResponse> conflicts = new ArrayList<>();
        for (Schedule other : existing) {
            if (Objects.equals(other.id, excludeId)) {
                continue;
            }
            if (!other.actionValue.equals(schedule.actionValue)) {
                conflicts.add(buildScheduleScheduleConflict(schedule, other));
            }
        }
        return conflicts;
    }

    /**
     * Checks whether a specific rule conflicts with existing active rules
     * that target the same device.
     *
     * @param rule       the rule to check
     * @param excludeId  ID to exclude from comparison (for updates), or null
     * @return list of conflicts (empty if none)
     */
    public List<ConflictResponse> checkRuleConflicts(Rule rule, Long excludeId) {
        List<Rule> existing = ruleRepo.find(
                "actionDevice = ?1 AND active = true",
                rule.actionDevice).list();

        List<ConflictResponse> conflicts = new ArrayList<>();
        for (Rule other : existing) {
            if (Objects.equals(other.id, excludeId)) {
                continue;
            }
            if (!other.actionValue.equals(rule.actionValue)
                    && other.triggerType == rule.triggerType) {
                conflicts.add(buildRuleRuleConflict(rule, other));
            }
        }
        return conflicts;
    }

    // ── Schedule vs Schedule ────────────────────────────────────────────

    private List<ConflictResponse> findScheduleConflicts(List<Schedule> schedules) {
        List<ConflictResponse> conflicts = new ArrayList<>();
        List<Schedule> active = schedules.stream()
                .filter(s -> Boolean.TRUE.equals(s.active))
                .toList();

        for (int i = 0; i < active.size(); i++) {
            for (int j = i + 1; j < active.size(); j++) {
                Schedule a = active.get(i);
                Schedule b = active.get(j);
                if (a.device.id.equals(b.device.id)
                        && a.cronExpression.equals(b.cronExpression)
                        && !a.actionValue.equals(b.actionValue)) {
                    conflicts.add(buildScheduleScheduleConflict(a, b));
                }
            }
        }
        return conflicts;
    }

    // ── Rule vs Rule ────────────────────────────────────────────────────

    private List<ConflictResponse> findRuleConflicts(List<Rule> rules) {
        List<ConflictResponse> conflicts = new ArrayList<>();
        List<Rule> active = rules.stream()
                .filter(r -> Boolean.TRUE.equals(r.active))
                .toList();

        for (int i = 0; i < active.size(); i++) {
            for (int j = i + 1; j < active.size(); j++) {
                Rule a = active.get(i);
                Rule b = active.get(j);
                if (a.actionDevice.id.equals(b.actionDevice.id)
                        && !a.actionValue.equals(b.actionValue)
                        && a.triggerType == b.triggerType) {
                    conflicts.add(buildRuleRuleConflict(a, b));
                }
            }
        }
        return conflicts;
    }

    // ── Rule vs Schedule (cross-entity) ─────────────────────────────────

    private List<ConflictResponse> findCrossConflicts(List<Schedule> schedules,
                                                      List<Rule> rules) {
        List<ConflictResponse> conflicts = new ArrayList<>();
        List<Schedule> activeSchedules = schedules.stream()
                .filter(s -> Boolean.TRUE.equals(s.active))
                .toList();
        List<Rule> activeTimeRules = rules.stream()
                .filter(r -> Boolean.TRUE.equals(r.active))
                .filter(r -> r.triggerType == TriggerType.TIME_BASED)
                .toList();

        for (Schedule s : activeSchedules) {
            for (Rule r : activeTimeRules) {
                if (s.device.id.equals(r.actionDevice.id)
                        && s.cronExpression.equals(r.triggerCondition)
                        && !s.actionValue.equals(r.actionValue)) {
                    conflicts.add(buildRuleScheduleConflict(r, s));
                }
            }
        }
        return conflicts;
    }

    // ── DTO builders ────────────────────────────────────────────────────

    private ConflictResponse buildScheduleScheduleConflict(Schedule a, Schedule b) {
        ConflictResponse c = new ConflictResponse();
        c.conflictType = "SCHEDULE_SCHEDULE";
        c.message = "Zeitpläne \"" + a.name + "\" und \"" + b.name
                + "\" steuern dasselbe Gerät (" + a.device.name
                + ") zur gleichen Zeit mit unterschiedlichen Werten ("
                + a.actionValue + " vs " + b.actionValue + ").";
        c.sourceId = a.id;
        c.sourceName = a.name;
        c.sourceKind = "SCHEDULE";
        c.targetId = b.id;
        c.targetName = b.name;
        c.targetKind = "SCHEDULE";
        c.deviceId = a.device.id;
        c.deviceName = a.device.name;
        return c;
    }

    private ConflictResponse buildRuleRuleConflict(Rule a, Rule b) {
        ConflictResponse c = new ConflictResponse();
        c.conflictType = "RULE_RULE";
        c.message = "Regeln \"" + a.name + "\" und \"" + b.name
                + "\" steuern dasselbe Gerät (" + a.actionDevice.name
                + ") mit unterschiedlichen Werten ("
                + a.actionValue + " vs " + b.actionValue + ").";
        c.sourceId = a.id;
        c.sourceName = a.name;
        c.sourceKind = "RULE";
        c.targetId = b.id;
        c.targetName = b.name;
        c.targetKind = "RULE";
        c.deviceId = a.actionDevice.id;
        c.deviceName = a.actionDevice.name;
        return c;
    }

    private ConflictResponse buildRuleScheduleConflict(Rule rule, Schedule schedule) {
        ConflictResponse c = new ConflictResponse();
        c.conflictType = "RULE_SCHEDULE";
        c.message = "Regel \"" + rule.name + "\" und Zeitplan \"" + schedule.name
                + "\" steuern dasselbe Gerät (" + schedule.device.name
                + ") zur gleichen Zeit mit unterschiedlichen Werten ("
                + rule.actionValue + " vs " + schedule.actionValue + ").";
        c.sourceId = rule.id;
        c.sourceName = rule.name;
        c.sourceKind = "RULE";
        c.targetId = schedule.id;
        c.targetName = schedule.name;
        c.targetKind = "SCHEDULE";
        c.deviceId = schedule.device.id;
        c.deviceName = schedule.device.name;
        return c;
    }
}
