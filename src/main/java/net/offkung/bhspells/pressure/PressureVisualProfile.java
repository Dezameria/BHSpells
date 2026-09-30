package net.offkung.bhspells.pressure;

/**
 * Visual configuration profile for a pressure field.
 * Controls procedural streak geometry, density, colors, ground particle chance,
 * and whether to use camera-centric curtain rendering for torrential wide-domain effects.
 */
public record PressureVisualProfile(
        String styleId,
        int color,
        float minWidth,
        float maxWidth,
        float minLength,
        float maxLength,
        float speedMin,
        float speedMax,
        float baseAlpha,
        int streakCount,
        float groundImpactChance,
        boolean cameraCurtain,
        float curtainRadius
) {
    /**
     * Backward-compatible 11-parameter constructor defaulting cameraCurtain to false.
     */
    public PressureVisualProfile(
            String styleId,
            int color,
            float minWidth,
            float maxWidth,
            float minLength,
            float maxLength,
            float speedMin,
            float speedMax,
            float baseAlpha,
            int streakCount,
            float groundImpactChance
    ) {
        this(styleId, color, minWidth, maxWidth, minLength, maxLength, speedMin, speedMax, baseAlpha, streakCount, groundImpactChance, false, 40.0F);
    }

    public static final PressureVisualProfile SPIRITUAL_VIOLET = new PressureVisualProfile(
            "spiritual_violet",
            0x9933FF,
            0.02F, 0.06F,
            3.0F, 7.5F,
            0.8F, 1.6F,
            0.85F,
            240,
            0.06F,
            false,
            40.0F
    );

    public static final PressureVisualProfile REIATSU_PINK = new PressureVisualProfile(
            "reiatsu_pink",
            0xFF3399,
            0.02F, 0.07F,
            2.5F, 8.0F,
            0.9F, 1.8F,
            0.90F,
            250,
            0.08F,
            false,
            40.0F
    );

    public static final PressureVisualProfile REIATSU_CYAN = new PressureVisualProfile(
            "reiatsu_cyan",
            0x00E5FF,
            0.015F, 0.05F,
            3.0F, 7.0F,
            0.7F, 1.5F,
            0.85F,
            220,
            0.05F,
            false,
            40.0F
    );

    public static final PressureVisualProfile REIATSU_EMERALD = new PressureVisualProfile(
            "reiatsu_emerald",
            0x00FF88,
            0.02F, 0.065F,
            3.2F, 8.0F,
            0.85F, 1.7F,
            0.88F,
            230,
            0.07F,
            false,
            40.0F
    );

    /**
     * Evocation: Vengeful Pressure - Intense malice green deluge with camera curtain.
     */
    public static final PressureVisualProfile VENGEFUL_MALICE = new PressureVisualProfile(
            "vengeful_malice",
            0x22FF55,
            0.035F, 0.16F,
            8.0F, 26.0F,
            1.4F, 2.8F,
            0.95F,
            450,
            0.12F,
            true,
            48.0F
    );

    /**
     * Lightning: Tempest Reiatsu - Violent hot pink lightning storm deluge with camera curtain.
     */
    public static final PressureVisualProfile TEMPEST_LIGHTNING = new PressureVisualProfile(
            "tempest_lightning",
            0xFF1493,
            0.035F, 0.18F,
            9.0F, 30.0F,
            1.6F, 3.2F,
            0.95F,
            480,
            0.14F,
            true,
            48.0F
    );

    public static PressureVisualProfile withColor(PressureVisualProfile base, int newColor) {
        return new PressureVisualProfile(
                base.styleId(),
                newColor,
                base.minWidth(),
                base.maxWidth(),
                base.minLength(),
                base.maxLength(),
                base.speedMin(),
                base.speedMax(),
                base.baseAlpha(),
                base.streakCount(),
                base.groundImpactChance(),
                base.cameraCurtain(),
                base.curtainRadius()
        );
    }
}
