package at.jku.se.service;

import at.jku.se.dto.response.ConflictResponse;
import at.jku.se.entity.Device;
import at.jku.se.entity.Rule;
import at.jku.se.entity.Schedule;
import at.jku.se.entity.User;
import at.jku.se.entity.enums.DeviceType;
import at.jku.se.entity.enums.TriggerType;
import at.jku.se.entity.enums.UserRole;
import at.jku.se.repository.RuleRepository;
import at.jku.se.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConflictDetectionServiceTest {

    private ConflictDetectionService service;
    private ScheduleRepository scheduleRepo;
    private RuleRepository ruleRepo;

    private User testUser;
    private Device lightDevice;
    private Device thermostatDevice;

    @BeforeEach
    void setUp() {
        service = new ConflictDetectionService();
        scheduleRepo = mock(ScheduleRepository.class);
        ruleRepo = mock(RuleRepository.class);
        service.scheduleRepo = scheduleRepo;
        service.ruleRepo = ruleRepo;

        testUser = new User();
        testUser.id = 1L;
        testUser.email = "test@example.com";
        testUser.role = UserRole.OWNER;
        testUser.createdAt = LocalDateTime.now();

        lightDevice = new Device();
        lightDevice.id = 10L;
        lightDevice.name = "Wohnzimmer Licht";
        lightDevice.type = DeviceType.SWITCH;
        lightDevice.updatedAt = LocalDateTime.now();

        thermostatDevice = new Device();
        thermostatDevice.id = 20L;
        thermostatDevice.name = "Wohnzimmer Thermostat";
        thermostatDevice.type = DeviceType.THERMOSTAT;
        thermostatDevice.updatedAt = LocalDateTime.now();
    }

    // ── Schedule vs Schedule ──────────────────────────────────────────

    @Test
    void scheduleConflict_sameDeviceSameCronDifferentAction() {
        Schedule s1 = createSchedule(1L, "Morgens an", "0 7 * * *", lightDevice, "true", true);
        Schedule s2 = createSchedule(2L, "Morgens aus", "0 7 * * *", lightDevice, "false", true);
        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of(s1, s2));
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of());

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        assertEquals(1, conflicts.size());
        assertEquals("SCHEDULE_SCHEDULE", conflicts.get(0).conflictType);
        assertTrue(conflicts.get(0).message.contains("Wohnzimmer Licht"));
    }

    @Test
    void noConflict_sameDeviceSameCronSameAction() {
        Schedule s1 = createSchedule(1L, "Morgens an", "0 7 * * *", lightDevice, "true", true);
        Schedule s2 = createSchedule(2L, "Morgens an 2", "0 7 * * *", lightDevice, "true", true);
        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of(s1, s2));
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of());

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        assertEquals(0, conflicts.size());
    }

    @Test
    void noConflict_sameDeviceDifferentCron() {
        Schedule s1 = createSchedule(1L, "Morgens", "0 7 * * *", lightDevice, "true", true);
        Schedule s2 = createSchedule(2L, "Abends", "0 22 * * *", lightDevice, "false", true);
        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of(s1, s2));
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of());

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        assertEquals(0, conflicts.size());
    }

    @Test
    void noConflict_differentDeviceSameCron() {
        Schedule s1 = createSchedule(1L, "Licht an", "0 7 * * *", lightDevice, "true", true);
        Schedule s2 = createSchedule(2L, "Heizung an", "0 7 * * *", thermostatDevice, "22.0", true);
        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of(s1, s2));
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of());

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        assertEquals(0, conflicts.size());
    }

    @Test
    void noConflict_inactiveSchedulesIgnored() {
        Schedule s1 = createSchedule(1L, "Morgens an", "0 7 * * *", lightDevice, "true", false);
        Schedule s2 = createSchedule(2L, "Morgens aus", "0 7 * * *", lightDevice, "false", true);
        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of(s1, s2));
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of());

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        assertEquals(0, conflicts.size());
    }

    // ── Rule vs Rule ──────────────────────────────────────────────────

    @Test
    void ruleConflict_sameDeviceSameTriggerTypeDifferentAction() {
        Rule r1 = createRule(1L, "Temp niedrig → heizen", TriggerType.THRESHOLD,
                lightDevice, "true", true);
        Rule r2 = createRule(2L, "Temp niedrig → aus", TriggerType.THRESHOLD,
                lightDevice, "false", true);
        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of());
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of(r1, r2));

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        assertEquals(1, conflicts.size());
        assertEquals("RULE_RULE", conflicts.get(0).conflictType);
    }

    @Test
    void noRuleConflict_differentTriggerTypes() {
        Rule r1 = createRule(1L, "Zeitregel", TriggerType.TIME_BASED,
                lightDevice, "true", true);
        Rule r2 = createRule(2L, "Schwellenregel", TriggerType.THRESHOLD,
                lightDevice, "false", true);
        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of());
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of(r1, r2));

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        assertEquals(0, conflicts.size());
    }

    @Test
    void noRuleConflict_sameActionValue() {
        Rule r1 = createRule(1L, "Regel A", TriggerType.EVENT, lightDevice, "true", true);
        Rule r2 = createRule(2L, "Regel B", TriggerType.EVENT, lightDevice, "true", true);
        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of());
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of(r1, r2));

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        assertEquals(0, conflicts.size());
    }

    @Test
    void noRuleConflict_inactiveRulesIgnored() {
        Rule r1 = createRule(1L, "Regel A", TriggerType.THRESHOLD,
                lightDevice, "true", false);
        Rule r2 = createRule(2L, "Regel B", TriggerType.THRESHOLD,
                lightDevice, "false", true);
        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of());
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of(r1, r2));

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        assertEquals(0, conflicts.size());
    }

    // ── Rule vs Schedule (cross-entity) ───────────────────────────────

    @Test
    void crossConflict_timeBasedRuleAndScheduleSameDeviceSameCron() {
        Rule r = createRule(3L, "Zeitregel aus", TriggerType.TIME_BASED,
                lightDevice, "false", true);
        r.triggerCondition = "0 7 * * *";
        Schedule s = createSchedule(1L, "Morgens an", "0 7 * * *", lightDevice, "true", true);
        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of(s));
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of(r));

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        assertEquals(1, conflicts.size());
        assertEquals("RULE_SCHEDULE", conflicts.get(0).conflictType);
        assertTrue(conflicts.get(0).message.contains("Wohnzimmer Licht"));
    }

    @Test
    void noCrossConflict_thresholdRuleAndSchedule() {
        Rule r = createRule(3L, "Schwelle", TriggerType.THRESHOLD,
                lightDevice, "false", true);
        r.triggerCondition = "0 7 * * *";
        Schedule s = createSchedule(1L, "Morgens an", "0 7 * * *", lightDevice, "true", true);
        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of(s));
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of(r));

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        assertEquals(0, conflicts.size());
    }

    @Test
    void noCrossConflict_sameActionValue() {
        Rule r = createRule(3L, "Zeitregel an", TriggerType.TIME_BASED,
                lightDevice, "true", true);
        r.triggerCondition = "0 7 * * *";
        Schedule s = createSchedule(1L, "Morgens an", "0 7 * * *", lightDevice, "true", true);
        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of(s));
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of(r));

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        assertEquals(0, conflicts.size());
    }

    // ── findAllConflicts without user filter ───────────────────────────

    @Test
    void findAllConflicts_withoutUserFilter() {
        Schedule s1 = createSchedule(1L, "A", "0 7 * * *", lightDevice, "true", true);
        Schedule s2 = createSchedule(2L, "B", "0 7 * * *", lightDevice, "false", true);
        when(scheduleRepo.listAll()).thenReturn(List.of(s1, s2));
        when(ruleRepo.listAll()).thenReturn(List.of());

        List<ConflictResponse> conflicts = service.findAllConflicts(null);

        assertEquals(1, conflicts.size());
    }

    // ── checkScheduleConflicts ─────────────────────────────────────────

    @Test
    void checkScheduleConflicts_findsConflict() {
        Schedule existing = createSchedule(1L, "Existing", "0 7 * * *", lightDevice, "true", true);
        Schedule newSchedule = createSchedule(null, "New", "0 7 * * *", lightDevice, "false", true);

        var query = mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
        when(scheduleRepo.find(eq("device = ?1 AND cronExpression = ?2 AND active = true"),
                any(Device.class), eq("0 7 * * *"))).thenReturn(query);
        when(query.list()).thenReturn(List.of(existing));

        List<ConflictResponse> conflicts = service.checkScheduleConflicts(newSchedule, null);

        assertEquals(1, conflicts.size());
    }

    @Test
    void checkScheduleConflicts_excludesOwnId() {
        Schedule existing = createSchedule(1L, "Existing", "0 7 * * *", lightDevice, "true", true);
        Schedule updating = createSchedule(1L, "Updating", "0 7 * * *", lightDevice, "false", true);

        var query = mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
        when(scheduleRepo.find(eq("device = ?1 AND cronExpression = ?2 AND active = true"),
                any(Device.class), eq("0 7 * * *"))).thenReturn(query);
        when(query.list()).thenReturn(List.of(existing));

        List<ConflictResponse> conflicts = service.checkScheduleConflicts(updating, 1L);

        assertEquals(0, conflicts.size());
    }

    // ── checkRuleConflicts ──────────────────────────────────────────────

    @Test
    void checkRuleConflicts_findsConflict() {
        Rule existing = createRule(1L, "Existing", TriggerType.THRESHOLD,
                lightDevice, "true", true);
        Rule newRule = createRule(null, "New", TriggerType.THRESHOLD,
                lightDevice, "false", true);

        var query = mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
        when(ruleRepo.find(eq("actionDevice = ?1 AND active = true"),
                any(Device.class))).thenReturn(query);
        when(query.list()).thenReturn(List.of(existing));

        List<ConflictResponse> conflicts = service.checkRuleConflicts(newRule, null);

        assertEquals(1, conflicts.size());
    }

    @Test
    void checkRuleConflicts_excludesOwnId() {
        Rule existing = createRule(1L, "Existing", TriggerType.THRESHOLD,
                lightDevice, "true", true);
        Rule updating = createRule(1L, "Updating", TriggerType.THRESHOLD,
                lightDevice, "false", true);

        var query = mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
        when(ruleRepo.find(eq("actionDevice = ?1 AND active = true"),
                any(Device.class))).thenReturn(query);
        when(query.list()).thenReturn(List.of(existing));

        List<ConflictResponse> conflicts = service.checkRuleConflicts(updating, 1L);

        assertEquals(0, conflicts.size());
    }

    // ── Multiple conflicts ──────────────────────────────────────────────

    @Test
    void multipleConflictsAcrossCategories() {
        Schedule s1 = createSchedule(1L, "S1", "0 7 * * *", lightDevice, "true", true);
        Schedule s2 = createSchedule(2L, "S2", "0 7 * * *", lightDevice, "false", true);
        Rule r1 = createRule(1L, "R1", TriggerType.THRESHOLD, thermostatDevice, "22.0", true);
        Rule r2 = createRule(2L, "R2", TriggerType.THRESHOLD, thermostatDevice, "18.0", true);
        Rule r3 = createRule(3L, "R3", TriggerType.TIME_BASED, lightDevice, "false", true);
        r3.triggerCondition = "0 7 * * *";

        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of(s1, s2));
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of(r1, r2, r3));

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        assertEquals(3, conflicts.size());
        long scheduleConflicts = conflicts.stream()
                .filter(c -> "SCHEDULE_SCHEDULE".equals(c.conflictType)).count();
        long ruleConflicts = conflicts.stream()
                .filter(c -> "RULE_RULE".equals(c.conflictType)).count();
        long crossConflicts = conflicts.stream()
                .filter(c -> "RULE_SCHEDULE".equals(c.conflictType)).count();
        assertEquals(1, scheduleConflicts);
        assertEquals(1, ruleConflicts);
        assertEquals(1, crossConflicts);
    }

    @Test
    void conflictResponse_containsCorrectFields() {
        Schedule s1 = createSchedule(1L, "Morgens an", "0 7 * * *", lightDevice, "true", true);
        Schedule s2 = createSchedule(2L, "Morgens aus", "0 7 * * *", lightDevice, "false", true);
        when(scheduleRepo.findByUser(testUser)).thenReturn(List.of(s1, s2));
        when(ruleRepo.findByUser(testUser)).thenReturn(List.of());

        List<ConflictResponse> conflicts = service.findAllConflicts(testUser);

        ConflictResponse c = conflicts.get(0);
        assertEquals(1L, c.sourceId);
        assertEquals("Morgens an", c.sourceName);
        assertEquals("SCHEDULE", c.sourceKind);
        assertEquals(2L, c.targetId);
        assertEquals("Morgens aus", c.targetName);
        assertEquals("SCHEDULE", c.targetKind);
        assertEquals(10L, c.deviceId);
        assertEquals("Wohnzimmer Licht", c.deviceName);
    }

    // ── Helper methods ──────────────────────────────────────────────────

    private Schedule createSchedule(Long id, String name, String cron,
                                    Device device, String actionValue, boolean active) {
        Schedule s = new Schedule();
        s.id = id;
        s.name = name;
        s.cronExpression = cron;
        s.device = device;
        s.actionValue = actionValue;
        s.active = active;
        s.user = testUser;
        return s;
    }

    private Rule createRule(Long id, String name, TriggerType triggerType,
                            Device actionDevice, String actionValue, boolean active) {
        Rule r = new Rule();
        r.id = id;
        r.name = name;
        r.triggerType = triggerType;
        r.actionDevice = actionDevice;
        r.actionValue = actionValue;
        r.active = active;
        r.user = testUser;
        return r;
    }
}
