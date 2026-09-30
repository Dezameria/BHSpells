package net.offkung.bhspells.pressure;

import java.util.UUID;

/**
 * Common data model representing an active pressure field or torrential domain.
 */
public record PressureFieldData(
        UUID fieldId,
        UUID ownerUuid,
        String sourceSpellId,
        int spellLevel,
        PressureAnchor anchor,
        float radius,
        int durationTicks,
        long startGameTime,
        long seed,
        PressureVisualProfile visualProfile,
        float intensity
) {
    /**
     * Backward-compatible constructor for legacy/reference invocations.
     */
    public PressureFieldData(
            UUID fieldId,
            UUID ownerUuid,
            PressureAnchor anchor,
            float radius,
            int durationTicks,
            long startGameTime,
            long seed,
            PressureVisualProfile visualProfile,
            float intensity
    ) {
        this(fieldId, ownerUuid, "bhspells:spiritual_pressure", 1, anchor, radius, durationTicks, startGameTime, seed, visualProfile, intensity);
    }

    public boolean isPersistent() {
        return durationTicks <= 0 || durationTicks == Integer.MAX_VALUE;
    }

    public int getColor() {
        return visualProfile.color();
    }
}
