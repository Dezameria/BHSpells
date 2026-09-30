package net.offkung.bhspells.pressure.client;

/**
 * Aggregated screen pressure state calculated from all active fields influencing the local player.
 */
public record ScreenPressureState(
        float totalPressure,
        int primaryColor,
        float primaryStrength,
        int secondaryColor,
        float secondaryStrength,
        float dominantDirX,
        float dominantDirY,
        float interference,
        int sourceCount
) {
    public static final ScreenPressureState EMPTY = new ScreenPressureState(
            0.0F, 0, 0.0F, 0, 0.0F, 0.0F, 0.0F, 0.0F, 0
    );

    public boolean isActive() {
        return totalPressure > 0.005F;
    }
}
