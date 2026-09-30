package net.offkung.bhspells.pressure;

/**
 * Reusable gameplay reaction states based on normalized spiritual pressure intensity [0.0 - 1.0].
 */
public enum PressureReaction {
    NONE(0.0F),
    STAGGER(0.30F),
    CROUCH(0.45F),
    KNEEL(0.60F),
    KNOCKDOWN(0.85F);

    private final float threshold;

    PressureReaction(float threshold) {
        this.threshold = threshold;
    }

    public float getThreshold() {
        return threshold;
    }

    public static PressureReaction fromIntensity(float intensity) {
        if (intensity >= KNOCKDOWN.threshold) {
            return KNOCKDOWN;
        } else if (intensity >= KNEEL.threshold) {
            return KNEEL;
        } else if (intensity >= CROUCH.threshold) {
            return CROUCH;
        } else if (intensity >= STAGGER.threshold) {
            return STAGGER;
        }
        return NONE;
    }
}
