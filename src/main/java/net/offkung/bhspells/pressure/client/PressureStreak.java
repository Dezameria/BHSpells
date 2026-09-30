package net.offkung.bhspells.pressure.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Procedural streak in a spiritual pressure field / torrential deluge domain.
 * Supports full 3D volumetric distribution across heaven and earth ("ทั่วฟ้าดิน"), continuous downward motion,
 * dynamic lifecycle (condensation -> deluge rush -> ground impact/dissipation), and ground caching.
 */
public class PressureStreak {
    private final float offsetX;
    private final float offsetZ;
    private final float width;
    private final float length;
    private final float speed;
    private final float baseAlpha;
    private final float phaseOffset;
    private final float verticalSpawnOffset;
    private final float fallDistance;

    private int cachedGroundY = Integer.MIN_VALUE;
    private int lastCycle = -1;

    public PressureStreak(float offsetX, float offsetZ, float width, float length,
                          float speed, float baseAlpha, float phaseOffset, float heightOffset) {
        this(offsetX, offsetZ, width, length, speed, baseAlpha, phaseOffset, heightOffset + 18.0F, 32.0F);
    }

    public PressureStreak(float offsetX, float offsetZ, float width, float length,
                          float speed, float baseAlpha, float phaseOffset, float verticalSpawnOffset, float fallDistance) {
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.width = width;
        this.length = length;
        this.speed = speed;
        this.baseAlpha = baseAlpha;
        this.phaseOffset = phaseOffset;
        this.verticalSpawnOffset = verticalSpawnOffset;
        this.fallDistance = fallDistance;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetZ() {
        return offsetZ;
    }

    public float getWidth() {
        return width;
    }

    public float getLength() {
        return length;
    }

    public float getSpeed() {
        return speed;
    }

    public float getPhaseOffset() {
        return phaseOffset;
    }

    public float getVerticalSpawnOffset() {
        return verticalSpawnOffset;
    }

    public float getFallDistance() {
        return fallDistance;
    }

    public float getHeightOffset() {
        return verticalSpawnOffset;
    }

    /**
     * Calculates the 3D vertical span [baseY, topY] and resulting alpha for the current animation cycle.
     * The streak plunges downwards from heaven to earth with high-velocity supernatural momentum.
     *
     * @param gameTimeSeconds current fractional game time in seconds
     * @param refY reference center Y (camera Y for curtain mode, or domain center Y)
     * @param groundY cached terrain surface Y under this streak
     * @param outY 2-element array returning [baseY, topY]
     * @return alpha multiplier in [0.0, 1.0]
     */
    public float calculateMotionAndAlpha(float gameTimeSeconds, double refY, int groundY, double[] outY) {
        float time = gameTimeSeconds * speed + phaseOffset;
        int currentCycle = (int) Math.floor(time);
        float progress = time - currentCycle;

        float alphaMultiplier;
        if (progress < 0.12F) {
            // Rapid condensation in upper sky
            alphaMultiplier = progress / 0.12F;
        } else if (progress < 0.75F) {
            // Full torrential deluge stream
            alphaMultiplier = 1.0F;
        } else {
            // Fade-out towards cycle completion
            alphaMultiplier = (1.0F - progress) / 0.25F;
        }

        // Downward travel
        double drop = progress * fallDistance;
        double topY = refY + verticalSpawnOffset - drop;
        double baseY = topY - length;

        // Ground clamping: streak compresses and slams into ground rather than piercing underground
        if (baseY < groundY) {
            baseY = groundY;
            if (topY <= groundY) {
                alphaMultiplier = 0.0F;
            } else {
                float remainingRatio = (float) ((topY - groundY) / length);
                alphaMultiplier *= Math.min(1.0F, remainingRatio * 1.4F);
            }
        }

        outY[0] = baseY;
        outY[1] = topY;
        return baseAlpha * Math.max(0.0F, Math.min(1.0F, alphaMultiplier));
    }

    /**
     * Backward-compatible helper for length and alpha without full 3D motion.
     */
    public float calculateAlphaAndLength(float gameTimeSeconds, float[] outLength) {
        float time = gameTimeSeconds * speed + phaseOffset;
        int currentCycle = (int) Math.floor(time);
        float progress = time - currentCycle;

        float alphaMultiplier;
        if (progress < 0.15F) {
            alphaMultiplier = progress / 0.15F;
        } else if (progress < 0.70F) {
            alphaMultiplier = 1.0F;
        } else {
            alphaMultiplier = (1.0F - progress) / 0.30F;
        }

        outLength[0] = length;
        return baseAlpha * alphaMultiplier;
    }

    public int getOrUpdateGroundY(ClientLevel level, int blockX, int blockZ, float gameTimeSeconds) {
        int currentCycle = (int) Math.floor(gameTimeSeconds * speed + phaseOffset);
        if (cachedGroundY == Integer.MIN_VALUE || currentCycle != lastCycle) {
            lastCycle = currentCycle;
            if (level.hasChunkAt(blockX >> 4, blockZ >> 4)) {
                cachedGroundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, blockX, blockZ);
            } else {
                cachedGroundY = 64;
            }
        }
        return cachedGroundY;
    }
}
