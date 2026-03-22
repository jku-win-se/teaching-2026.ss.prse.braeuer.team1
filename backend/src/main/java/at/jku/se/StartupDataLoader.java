package at.jku.se;

import at.jku.se.entity.ActivityLog;
import at.jku.se.entity.Device;
import at.jku.se.entity.EnergyLog;
import at.jku.se.entity.Notification;
import at.jku.se.entity.Room;
import at.jku.se.entity.Rule;
import at.jku.se.entity.Scene;
import at.jku.se.entity.SceneDeviceState;
import at.jku.se.entity.Schedule;
import at.jku.se.entity.User;
import at.jku.se.entity.enums.DeviceType;
import at.jku.se.entity.enums.TriggerType;
import at.jku.se.entity.enums.UserRole;
import org.mindrot.jbcrypt.BCrypt;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

/**
 * Loads realistic sample data on every application startup.
 * Only runs when the database is empty (no users exist yet).
 * Because {@code quarkus.hibernate-orm.database.generation=drop-and-create} is active,
 * the schema is always fresh and this loader always populates it.
 *
 * <p>Test credentials:
 * <ul>
 *   <li>Owner: alice@example.com / password123</li>
 *   <li>Member: bob@example.com / password123</li>
 * </ul>
 */
@ApplicationScoped
public class StartupDataLoader {

    private static final Logger LOG = Logger.getLogger(StartupDataLoader.class);
    private static final String TEST_PASSWORD = "password123";

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        if (User.count() > 0) {
            LOG.info("Database already has data — skipping seed.");
            return;
        }
        LOG.info("Seeding database with test data...");

        // ── Users ──────────────────────────────────────────────────────────────
        User alice = createUser("alice@example.com", TEST_PASSWORD, UserRole.OWNER);

        // ── Rooms ──────────────────────────────────────────────────────────────
        Room livingRoom = createRoom("Living Room", alice);
        Room kitchen = createRoom("Kitchen", alice);

        // ── Devices ────────────────────────────────────────────────────────────
        Device mainLight = createDevice("Main Light", DeviceType.SWITCH, livingRoom, 60.0);
        mainLight.switchedOn = false;

        Device ceilingDimmer = createDevice("Ceiling Dimmer", DeviceType.DIMMER, livingRoom, 100.0);
        ceilingDimmer.level = 80.0;

        Device thermostat = createDevice("Smart Thermostat", DeviceType.THERMOSTAT, livingRoom, 500.0);
        thermostat.level = 21.0;

        Device motionSensor = createDevice("Motion Sensor", DeviceType.SENSOR, livingRoom, 2.0);
        motionSensor.level = 0.0;

        Device kitchenLight = createDevice("Kitchen Light", DeviceType.SWITCH, kitchen, 60.0);
        kitchenLight.switchedOn = true;

        Device windowBlind = createDevice("Window Blind", DeviceType.BLIND, kitchen, 50.0);
        windowBlind.level = 75.0;

        // ── Schedules ──────────────────────────────────────────────────────────
        Schedule morningLights = createSchedule(
                "Morning Lights", "0 7 * * MON-FRI", mainLight, "true", true, alice);
        createSchedule(
                "Night Lights Off", "0 23 * * *", mainLight, "false", true, alice);
        createSchedule(
                "Kitchen Light Evening", "0 18 * * *", kitchenLight, "true", true, alice);

        // ── Rules ──────────────────────────────────────────────────────────────
        createRule("Motion activates hallway light",
                TriggerType.THRESHOLD, "sensor value >= 1",
                motionSensor, 1.0, mainLight, "true", true, alice);
        createRule("Cold activates heating",
                TriggerType.THRESHOLD, "temperature < 18",
                thermostat, 18.0, thermostat, "22.0", true, alice);
        createRule("Midnight lights off",
                TriggerType.TIME_BASED, "0 0 * * *",
                null, null, mainLight, "false", true, alice);

        // ── Scenes ─────────────────────────────────────────────────────────────
        Scene movieNight = createScene("Movie Night", alice);
        addSceneState(movieNight, ceilingDimmer, null, 20.0);
        addSceneState(movieNight, windowBlind, null, 0.0);
        addSceneState(movieNight, mainLight, false, null);

        Scene goodMorning = createScene("Good Morning", alice);
        addSceneState(goodMorning, mainLight, true, null);
        addSceneState(goodMorning, ceilingDimmer, null, 100.0);
        addSceneState(goodMorning, windowBlind, null, 100.0);

        // ── Activity Logs ──────────────────────────────────────────────────────
        createActivityLog(mainLight, "alice@example.com",
                "State updated: switchedOn=true",
                LocalDateTime.now().minusHours(3));
        createActivityLog(kitchenLight, "alice@example.com",
                "State updated: switchedOn=true",
                LocalDateTime.now().minusHours(1));
        createActivityLog(thermostat, "Rule:Cold activates heating",
                "State updated: level=22.0",
                LocalDateTime.now().minusMinutes(30));
        createActivityLog(ceilingDimmer, "Scene:Movie Night",
                "Scene 'Movie Night' activated: level=20.0",
                LocalDateTime.now().minusMinutes(10));

        // ── Energy Logs ────────────────────────────────────────────────────────
        // Simulate hourly readings for today
        LocalDateTime base = LocalDate.now().atTime(6, 0);
        for (int hour = 0; hour < 14; hour++) {
            createEnergyLog(mainLight, base.plusHours(hour), 6.0);     // 60W for 6 min ≈ 6 Wh
            createEnergyLog(ceilingDimmer, base.plusHours(hour), 10.0);
            createEnergyLog(kitchenLight, base.plusHours(hour), 6.0);
        }
        // Add a few from yesterday for the weekly totals
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atTime(20, 0);
        createEnergyLog(mainLight, yesterday, 60.0);
        createEnergyLog(kitchenLight, yesterday, 30.0);

        // ── Notifications ──────────────────────────────────────────────────────
        createNotification(alice,
                "Rule 'Motion activates hallway light' fired successfully.", false);
        createNotification(alice,
                "Rule 'Cold activates heating' fired successfully.", false);
        createNotification(alice,
                "Scene 'Movie Night' was activated (3 device(s) updated).", true);

        // ── Vacation Mode ──────────────────────────────────────────────────────
        createVacationMode(alice,
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(14),
                morningLights, false);

        LOG.info("Test data seeded successfully. "
                + "Login: alice@example.com / " + TEST_PASSWORD
                + " (OWNER) | bob@example.com / " + TEST_PASSWORD + " (MEMBER)");
    }

    private User createUser(String email, String password, UserRole role) {
        User u = new User();
        u.email = email;
        u.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        u.role = role;
        u.createdAt = LocalDateTime.now();
        u.persist();
        return u;
    }

    private Room createRoom(String name, User owner) {
        Room r = new Room();
        r.name = name;
        r.user = owner;
        r.persist();
        return r;
    }

    private Device createDevice(String name, DeviceType type, Room room, Double powerWatt) {
        Device d = new Device();
        d.name = name;
        d.type = type;
        d.room = room;
        d.powerConsumptionWatt = powerWatt;
        d.updatedAt = LocalDateTime.now();
        d.persist();
        return d;
    }

    private Schedule createSchedule(String name, String cron, Device device,
                                     String actionValue, boolean active, User user) {
        Schedule s = new Schedule();
        s.name = name;
        s.cronExpression = cron;
        s.device = device;
        s.actionValue = actionValue;
        s.active = active;
        s.user = user;
        s.persist();
        return s;
    }

    private void createRule(String name, TriggerType triggerType, String triggerCondition,
                             Device triggerDevice, Double thresholdValue,
                             Device actionDevice, String actionValue,
                             boolean active, User user) {
        Rule r = new Rule();
        r.name = name;
        r.triggerType = triggerType;
        r.triggerCondition = triggerCondition;
        r.triggerDevice = triggerDevice;
        r.triggerThresholdValue = thresholdValue;
        r.actionDevice = actionDevice;
        r.actionValue = actionValue;
        r.active = active;
        r.user = user;
        r.persist();
    }

    private Scene createScene(String name, User user) {
        Scene s = new Scene();
        s.name = name;
        s.user = user;
        s.persist();
        return s;
    }

    private void addSceneState(Scene scene, Device device,
                                Boolean targetSwitchedOn, Double targetLevel) {
        SceneDeviceState state = new SceneDeviceState();
        state.scene = scene;
        state.device = device;
        state.targetSwitchedOn = targetSwitchedOn;
        state.targetLevel = targetLevel;
        state.persist();
    }

    private void createActivityLog(Device device, String actor,
                                    String description, LocalDateTime timestamp) {
        ActivityLog log = new ActivityLog();
        log.device = device;
        log.actor = actor;
        log.description = description;
        log.timestamp = timestamp;
        log.persist();
    }

    private void createEnergyLog(Device device, LocalDateTime timestamp, double wh) {
        EnergyLog log = new EnergyLog();
        log.device = device;
        log.timestamp = timestamp;
        log.consumptionWh = wh;
        log.persist();
    }

    private void createNotification(User user, String message, boolean read) {
        Notification n = new Notification();
        n.user = user;
        n.message = message;
        n.createdAt = LocalDateTime.now();
        n.read = read;
        n.persist();
    }

    private void createVacationMode(User user, LocalDate start, LocalDate end,
                                     Schedule schedule, boolean active) {
        at.jku.se.entity.VacationMode vm = new at.jku.se.entity.VacationMode();
        vm.user = user;
        vm.startDate = start;
        vm.endDate = end;
        vm.schedule = schedule;
        vm.active = active;
        vm.persist();
    }
}
