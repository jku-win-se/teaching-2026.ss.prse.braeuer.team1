package at.jku.se.dto.response;

/**
 * Response DTO describing a scheduling or rule conflict (FR-15).
 * Returned by the conflict detection endpoint so users can identify
 * and resolve contradictory automations.
 */
public class ConflictResponse {

    /** The type of conflict: SCHEDULE_SCHEDULE, RULE_RULE, or RULE_SCHEDULE. */
    public String conflictType;

    /** Human-readable description of the conflict in German. */
    public String message;

    /** ID of the first conflicting automation. */
    public Long sourceId;

    /** Name of the first conflicting automation. */
    public String sourceName;

    /** Kind of the first automation (e.g., "SCHEDULE" or "RULE"). */
    public String sourceKind;

    /** ID of the second conflicting automation. */
    public Long targetId;

    /** Name of the second conflicting automation. */
    public String targetName;

    /** Kind of the second automation. */
    public String targetKind;

    /** ID of the device both automations target. */
    public Long deviceId;

    /** Name of the device both automations target. */
    public String deviceName;
}
