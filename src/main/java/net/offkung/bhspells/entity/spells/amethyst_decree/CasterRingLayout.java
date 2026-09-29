package net.offkung.bhspells.entity.spells.amethyst_decree;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

public final class CasterRingLayout {
    private static final int SCATTER_COUNT = 176;
    private static final float SCATTER_MIN_SCALE = 0.5f;
    private static final float SCATTER_MAX_SCALE = 0.9f;
    private static final int SCATTER_RISE_STAGGER_TICKS = 14;

    private static final float BURST_MIN_SCALE = 1.5f;
    private static final float BURST_MAX_SCALE = 2.0f;
    private static final float BURST_TILT_DEG = 25.0f;
    private static final float BURST_TILT_JITTER_DEG = 10.0f;

    private static final int BURST_RING_COUNT = 18;
    private static final float BURST_RING_RADIUS_FRACTION = 0.75f;
    private static final float BURST_RING_ANGLE_JITTER_RAD = 0.18f;
    private static final float BURST_RING_RADIUS_JITTER_FRACTION = 0.12f;

    private static final int BURST_SCATTER_COUNT = 20;

    private CasterRingLayout() {
    }

    public static List<CrystalTransform> generateScatter(long seed, double radius, Level level, double originX, double originY, double originZ) {
        RandomSource random = RandomSource.create(seed);
        List<CrystalTransform> list = new ArrayList<>(SCATTER_COUNT);
        for (int i = 0; i < SCATTER_COUNT; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = Math.sqrt(random.nextDouble()) * radius;
            double x = originX + Math.cos(angle) * dist;
            double z = originZ + Math.sin(angle) * dist;
            double groundY = groundHeight(level, x, z);
            float scale = SCATTER_MIN_SCALE + random.nextFloat() * (SCATTER_MAX_SCALE - SCATTER_MIN_SCALE);
            float yaw = random.nextFloat() * 360.0f;
            int riseDelay = random.nextInt(SCATTER_RISE_STAGGER_TICKS);
            list.add(new CrystalTransform(x - originX, groundY - originY, z - originZ, yaw, 0.0f, scale, riseDelay, false));
        }
        return list;
    }

    public static List<CrystalTransform> generateBurstRing(long seed, double radius, Level level, double originX, double originY, double originZ) {
        RandomSource random = RandomSource.create(seed ^ 0x5DEECE66DL);
        List<CrystalTransform> list = new ArrayList<>(BURST_RING_COUNT);
        for (int i = 0; i < BURST_RING_COUNT; i++) {
            double baseAngle = (Math.PI * 2.0 / BURST_RING_COUNT) * i;
            double angle = baseAngle + (random.nextDouble() - 0.5) * BURST_RING_ANGLE_JITTER_RAD;
            double dist = radius * BURST_RING_RADIUS_FRACTION
                    * (1.0 + (random.nextDouble() - 0.5) * BURST_RING_RADIUS_JITTER_FRACTION);
            list.add(burstSpike(random, level, originX, originY, originZ, angle, dist));
        }
        return list;
    }

    public static List<CrystalTransform> generateBurstScatter(long seed, double radius, Level level, double originX, double originY, double originZ) {
        RandomSource random = RandomSource.create(seed ^ 0x27220A5D1L);
        List<CrystalTransform> list = new ArrayList<>(BURST_SCATTER_COUNT);
        for (int i = 0; i < BURST_SCATTER_COUNT; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = Math.sqrt(random.nextDouble()) * radius;
            list.add(burstSpike(random, level, originX, originY, originZ, angle, dist));
        }
        return list;
    }

    private static CrystalTransform burstSpike(RandomSource random, Level level, double originX, double originY, double originZ, double angle, double dist) {
        double x = originX + Math.cos(angle) * dist;
        double z = originZ + Math.sin(angle) * dist;
        double groundY = groundHeight(level, x, z);
        float scale = BURST_MIN_SCALE + random.nextFloat() * (BURST_MAX_SCALE - BURST_MIN_SCALE);
        float yaw = (float) Math.toDegrees(angle) + 90.0f;
        float tilt = BURST_TILT_DEG + (random.nextFloat() - 0.5f) * BURST_TILT_JITTER_DEG;
        return new CrystalTransform(x - originX, groundY - originY, z - originZ, yaw, tilt, scale, 0, true);
    }

    private static double groundHeight(Level level, double x, double z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
    }

    public static float positionOffset(float age, float riseStart, float riseTicks, float sinkStart, float sinkTicks) {
        if (age < riseStart) {
            return -1.0f;
        } else if (age < riseStart + riseTicks) {
            float f = (age - riseStart) / riseTicks;
            return ease(f) - 1.0f;
        } else if (age < sinkStart) {
            return 0.0f;
        } else if (age < sinkStart + sinkTicks) {
            float f = Mth.clamp((age - sinkStart) / sinkTicks, 0.0f, 1.0f);
            return -ease(f);
        } else {
            return -1.0f;
        }
    }

    private static float ease(float f) {
        return Mth.sin(f * (float) Math.PI) / (float) Math.PI + f;
    }
}
