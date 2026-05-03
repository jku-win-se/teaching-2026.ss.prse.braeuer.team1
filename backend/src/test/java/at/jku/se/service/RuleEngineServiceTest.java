package at.jku.se.service;

import at.jku.se.entity.ActivityLog;
import at.jku.se.entity.Device;
import at.jku.se.entity.Notification;
import at.jku.se.entity.Room;
import at.jku.se.entity.Rule;
import at.jku.se.entity.User;
import at.jku.se.entity.enums.DeviceType;
import at.jku.se.entity.enums.TriggerType;
import at.jku.se.entity.enums.UserRole;
import at.jku.se.repository.ActivityLogRepository;
import at.jku.se.repository.NotificationRepository;
import at.jku.se.repository.RuleRepository;
import at.jku.se.websocket.DeviceEventBroadcaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RuleEngineService}.
 * Verifies trigger evaluation (EVENT, THRESHOLD), action application
 * (boolean and numeric values), and side effects (notification, activity log,
 * WebSocket broadcast).
 */
class RuleEngineServiceTest {

    private RuleEngineService service;
    private RuleRepository ruleRepo;
    private NotificationRepository notifRepo;
    private ActivityLogRepository activityLogRepo;
    private DeviceEventBroadcaster broadcaster;

    private User testUser;
    private Device triggerDevice;
    private Device actionDevice;

    @BeforeEach
    void setUp() {
        service = new RuleEngineService();
        ruleRepo = mock(RuleRepository.class);
        notifRepo = mock(NotificationRepository.class);
        activityLogRepo = mock(ActivityLogRepository.class);
        broadcaster = mock(DeviceEventBroadcaster.class);
        service.ruleRepo = ruleRepo;
        service.notifRepo = notifRepo;
        service.activityLogRepo = activityLogRepo;
        service.broadcaster = broadcaster;

        testUser = new User();
        testUser.id = 1L;
        testUser.email = "test@example.com";
        testUser.role = UserRole.OWNER;
        testUser.createdAt = LocalDateTime.now();

        Room room = new Room();
        room.id = 100L;
        room.name = "Wohnzimmer";

        triggerDevice = new Device();
        triggerDevice.id = 10L;
        triggerDevice.name = "Temperatursensor";
        triggerDevice.type = DeviceType.SENSOR;
        triggerDevice.room = room;
        triggerDevice.updatedAt = LocalDateTime.now();

        actionDevice = new Device();
        actionDevice.id = 20L;
        actionDevice.name = "Heizung";
        actionDevice.type = DeviceType.THERMOSTAT;
        actionDevice.room = room;
        actionDevice.updatedAt = LocalDateTime.now();
    }

    // ── shouldFire: EVENT trigger ─────────────────────────────────────────

    @Test
    void eventRule_alwaysFires() {
        Rule rule = createRule("Bewegung -> Licht an", TriggerType.EVENT,
                null, actionDevice, "true");
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(rule));

        service.evaluateRulesForDevice(triggerDevice);

        verify(notifRepo, times(1)).persist(any(Notification.class));
        verify(activityLogRepo, times(1)).persist(any(ActivityLog.class));
    }

    // ── shouldFire: THRESHOLD trigger ─────────────────────────────────────

    @Test
    void thresholdRule_firesWhenLevelBelowThreshold() {
        Rule rule = createRule("Temp niedrig -> Heizung",
                TriggerType.THRESHOLD, 18.0, actionDevice, "22.0");
        triggerDevice.level = 15.0;
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(rule));

        service.evaluateRulesForDevice(triggerDevice);

        verify(notifRepo, times(1)).persist(any(Notification.class));
    }

    @Test
    void thresholdRule_firesWhenLevelEqualsThreshold() {
        Rule rule = createRule("Temp Schwelle", TriggerType.THRESHOLD,
                18.0, actionDevice, "22.0");
        triggerDevice.level = 18.0;
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(rule));

        service.evaluateRulesForDevice(triggerDevice);

        verify(notifRepo, times(1)).persist(any(Notification.class));
    }

    @Test
    void thresholdRule_doesNotFireWhenLevelAboveThreshold() {
        Rule rule = createRule("Temp niedrig -> Heizung",
                TriggerType.THRESHOLD, 18.0, actionDevice, "22.0");
        triggerDevice.level = 25.0;
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(rule));

        service.evaluateRulesForDevice(triggerDevice);

        verify(notifRepo, never()).persist(any(Notification.class));
        verify(activityLogRepo, never()).persist(any(ActivityLog.class));
    }

    @Test
    void thresholdRule_doesNotFireWhenDeviceLevelIsNull() {
        Rule rule = createRule("Temp Schwelle", TriggerType.THRESHOLD,
                18.0, actionDevice, "22.0");
        triggerDevice.level = null;
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(rule));

        service.evaluateRulesForDevice(triggerDevice);

        verify(notifRepo, never()).persist(any(Notification.class));
    }

    @Test
    void thresholdRule_doesNotFireWhenThresholdValueIsNull() {
        Rule rule = createRule("Temp Schwelle", TriggerType.THRESHOLD,
                null, actionDevice, "22.0");
        triggerDevice.level = 15.0;
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(rule));

        service.evaluateRulesForDevice(triggerDevice);

        verify(notifRepo, never()).persist(any(Notification.class));
    }

    // ── applyAction: boolean values ───────────────────────────────────────

    @Test
    void booleanAction_setsSwitchedOnTrue() {
        Rule rule = createRule("Licht an", TriggerType.EVENT,
                null, actionDevice, "true");
        actionDevice.switchedOn = false;
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(rule));

        service.evaluateRulesForDevice(triggerDevice);

        assertTrue(actionDevice.switchedOn);
    }

    @Test
    void booleanAction_setsSwitchedOnFalse() {
        Rule rule = createRule("Licht aus", TriggerType.EVENT,
                null, actionDevice, "false");
        actionDevice.switchedOn = true;
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(rule));

        service.evaluateRulesForDevice(triggerDevice);

        assertFalse(actionDevice.switchedOn);
    }

    // ── applyAction: numeric values ───────────────────────────────────────

    @Test
    void numericAction_setsLevel() {
        Rule rule = createRule("Heizung auf 22", TriggerType.EVENT,
                null, actionDevice, "22.5");
        actionDevice.level = 18.0;
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(rule));

        service.evaluateRulesForDevice(triggerDevice);

        assertEquals(22.5, actionDevice.level);
    }

    // ── applyAction: invalid value ────────────────────────────────────────

    @Test
    void invalidAction_doesNotThrow() {
        Rule rule = createRule("Kaputte Aktion", TriggerType.EVENT,
                null, actionDevice, "not-a-number");
        Double levelBefore = actionDevice.level;
        Boolean switchedBefore = actionDevice.switchedOn;
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(rule));

        // Must not throw
        service.evaluateRulesForDevice(triggerDevice);

        // Device state for boolean/level fields must remain unchanged
        assertEquals(levelBefore, actionDevice.level);
        assertEquals(switchedBefore, actionDevice.switchedOn);
    }

    // ── Side effects ──────────────────────────────────────────────────────

    @Test
    void firingRule_persistsActivityLogWithRuleEngineActor() {
        Rule rule = createRule("Bewegung -> Licht", TriggerType.EVENT,
                null, actionDevice, "true");
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(rule));

        service.evaluateRulesForDevice(triggerDevice);

        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepo).persist(captor.capture());
        ActivityLog log = captor.getValue();
        assertEquals("rule-engine", log.actor);
        assertEquals(actionDevice, log.device);
        assertNotNull(log.timestamp);
        assertTrue(log.description.contains("Bewegung -> Licht"));
    }

    @Test
    void firingRule_createsNotificationForRuleOwner() {
        Rule rule = createRule("Heizung -> 22", TriggerType.EVENT,
                null, actionDevice, "22.0");
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(rule));

        service.evaluateRulesForDevice(triggerDevice);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifRepo).persist(captor.capture());
        Notification n = captor.getValue();
        assertEquals(testUser, n.user);
        assertFalse(n.read);
        assertNotNull(n.createdAt);
        assertTrue(n.message.contains("Heizung -> 22"));
    }

    @Test
    void firingRule_broadcastsDeviceUpdate() {
        Rule rule = createRule("Bewegung -> Licht", TriggerType.EVENT,
                null, actionDevice, "true");
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(rule));

        service.evaluateRulesForDevice(triggerDevice);

        verify(broadcaster, times(1)).broadcastDeviceUpdate(any());
    }

    // ── Multiple rules / no rules ─────────────────────────────────────────

    @Test
    void noMatchingRules_noSideEffects() {
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of());

        service.evaluateRulesForDevice(triggerDevice);

        verify(notifRepo, never()).persist(any(Notification.class));
        verify(activityLogRepo, never()).persist(any(ActivityLog.class));
        verify(broadcaster, never()).broadcastDeviceUpdate(any());
    }

    @Test
    void multipleMatchingRules_allFire() {
        Rule r1 = createRule("Regel A", TriggerType.EVENT, null, actionDevice, "true");
        Rule r2 = createRule("Regel B", TriggerType.EVENT, null, actionDevice, "false");
        when(ruleRepo.findActiveByTriggerDevice(triggerDevice)).thenReturn(List.of(r1, r2));

        service.evaluateRulesForDevice(triggerDevice);

        verify(notifRepo, times(2)).persist(any(Notification.class));
        verify(activityLogRepo, times(2)).persist(any(ActivityLog.class));
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Rule createRule(String name, TriggerType triggerType,
                            Double thresholdValue, Device actionDev,
                            String actionValue) {
        Rule r = new Rule();
        r.id = 1L;
        r.name = name;
        r.triggerType = triggerType;
        r.triggerThresholdValue = thresholdValue;
        r.actionDevice = actionDev;
        r.actionValue = actionValue;
        r.active = true;
        r.user = testUser;
        return r;
    }
}
