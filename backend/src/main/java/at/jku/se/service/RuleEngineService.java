package at.jku.se.service;

import at.jku.se.entity.ActivityLog;
import at.jku.se.entity.Device;
import at.jku.se.entity.Notification;
import at.jku.se.entity.Rule;
import at.jku.se.entity.enums.TriggerType;
import at.jku.se.mapper.DeviceMapper;
import at.jku.se.repository.ActivityLogRepository;
import at.jku.se.repository.NotificationRepository;
import at.jku.se.repository.RuleRepository;
import at.jku.se.websocket.DeviceEventBroadcaster;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Evaluates active EVENT and THRESHOLD rules when a device state changes (FR-12).
 * Fires matching rules, applies their action to the target device, and creates
 * an in-app notification for the rule owner.
 */
@ApplicationScoped
public class RuleEngineService {

    private static final Logger LOG = Logger.getLogger(RuleEngineService.class.getName());

    @Inject
    RuleRepository ruleRepo;

    @Inject
    NotificationRepository notifRepo;

    @Inject
    ActivityLogRepository activityLogRepo;

    @Inject
    DeviceEventBroadcaster broadcaster;

    /**
     * Evaluates all active EVENT and THRESHOLD rules whose trigger device matches
     * the given device. Called after every device state update.
     *
     * @param changedDevice the device whose state just changed
     */
    @Transactional
    public void evaluateRulesForDevice(Device changedDevice) {
        List<Rule> rules = ruleRepo.findActiveByTriggerDevice(changedDevice);
        for (Rule rule : rules) {
            if (shouldFire(rule, changedDevice)) {
                applyAction(rule);
                createNotification(rule, "Rule '" + rule.name + "' fired successfully.");
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Rule fired: " + rule.name);
                }
            }
        }
    }

    private boolean shouldFire(Rule rule, Device device) {
        if (rule.triggerType == TriggerType.EVENT) {
            return true;
        }
        if (rule.triggerType == TriggerType.THRESHOLD) {
            return device.level != null
                    && rule.triggerThresholdValue != null
                    && device.level <= rule.triggerThresholdValue;
        }
        return false;
    }

    private void applyAction(Rule rule) {
        Device target = rule.actionDevice;
        String val = rule.actionValue;
        if ("true".equals(val) || "false".equals(val)) {
            target.switchedOn = Boolean.parseBoolean(val);
        } else {
            try {
                target.level = Double.parseDouble(val);
            } catch (NumberFormatException e) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.warning("Invalid actionValue '" + val + "' for rule: " + rule.name);
                }
            }
        }
        target.updatedAt = LocalDateTime.now();

        ActivityLog log = new ActivityLog();
        log.device = target;
        log.actor = "rule-engine";
        log.description = "Rule '" + rule.name + "' set " + target.name + " to " + val;
        log.timestamp = target.updatedAt;
        activityLogRepo.persist(log);

        broadcaster.broadcastDeviceUpdate(DeviceMapper.toResponse(target));
    }

    private void createNotification(Rule rule, String message) {
        Notification n = new Notification();
        n.user = rule.user;
        n.message = message;
        n.createdAt = LocalDateTime.now();
        n.read = false;
        notifRepo.persist(n);
    }
}
