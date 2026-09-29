package net.offkung.bhspells.client.particle;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class JadeAuraVfx {
    public static final Vector3f COLOR_DARK_JUNGLE = new Vector3f(0.118F, 0.125F, 0.122F);
    public static final Vector3f COLOR_MEDIUM_JUNGLE = new Vector3f(0.098F, 0.227F, 0.192F);
    public static final Vector3f COLOR_DEEP_CYAN = new Vector3f(0.114F, 0.424F, 0.380F);
    public static final Vector3f COLOR_VERDIGRIS = new Vector3f(0.243F, 0.725F, 0.659F);
    public static final Vector3f COLOR_FOREST_GREEN = new Vector3f(0.353F, 0.639F, 0.443F);

    public static final int[] COLOR_DARK_JUNGLE_RGB = new int[]{30, 32, 31};
    public static final int[] COLOR_MEDIUM_JUNGLE_RGB = new int[]{25, 58, 49};
    public static final int[] COLOR_DEEP_CYAN_RGB = new int[]{29, 108, 97};
    public static final int[] COLOR_VERDIGRIS_RGB = new int[]{62, 185, 168};
    public static final int[] COLOR_FOREST_GREEN_RGB = new int[]{90, 163, 113};

    public static final int[] COLOR_ELECTRIC_CORE_RGB = new int[]{210, 255, 238};
    public static final int[] COLOR_LUMINOUS_VERDIGRIS_RGB = new int[]{105, 248, 220};
    public static final int[] COLOR_RADIANT_JADE_RGB = new int[]{135, 250, 180};
    public static final int[] COLOR_ELECTRIC_GLOW_RGB = new int[]{50, 218, 185};

    public static final Vector3f[] JADE_PALETTE = new Vector3f[]{
            COLOR_VERDIGRIS,
            COLOR_FOREST_GREEN,
            COLOR_DEEP_CYAN,
            COLOR_MEDIUM_JUNGLE,
            COLOR_DARK_JUNGLE
    };

    public static void spawnCastBurst(ServerLevel level, LivingEntity target) {
        double cx = target.getX();
        double cy = target.getY();
        double cz = target.getZ();

        int[] ringSamples = new int[]{16, 24, 32};
        double[] ringRadii = new double[]{0.6D, 1.2D, 1.8D};
        double[] ringSpeeds = new double[]{0.25D, 0.40D, 0.55D};

        for (int r = 0; r < ringRadii.length; r++) {
            double radius = ringRadii[r];
            int count = ringSamples[r];
            double speed = ringSpeeds[r];
            Vector3f ringColor = JADE_PALETTE[r % JADE_PALETTE.length];

            for (int i = 0; i < count; i++) {
                double angle = (2.0D * Math.PI / count) * i;
                double px = cx + Math.cos(angle) * radius;
                double pz = cz + Math.sin(angle) * radius;
                double py = cy + 0.15D;

                double vx = Math.cos(angle) * speed;
                double vz = Math.sin(angle) * speed;
                double vy = 0.04D;

                level.sendParticles(new DustParticleOptions(ringColor, 1.35F), px, py, pz, 0, vx, vy, vz, 1.0D);
            }
        }

        for (int i = 0; i < 20; i++) {
            double angle = target.getRandom().nextDouble() * 2.0D * Math.PI;
            double dist = 0.25D + target.getRandom().nextDouble() * 0.5D;
            double px = cx + Math.cos(angle) * dist;
            double pz = cz + Math.sin(angle) * dist;
            double py = cy + 0.1D + target.getRandom().nextDouble() * (target.getBbHeight() * 0.6D);

            Vector3f filamentColor = JADE_PALETTE[target.getRandom().nextInt(JADE_PALETTE.length)];
            level.sendParticles(new DustParticleOptions(filamentColor, 1.2F), px, py, pz, 0, Math.cos(angle) * 0.05D, 0.22D, Math.sin(angle) * 0.05D, 1.0D);
        }

        level.playSound(null, cx, cy, cz, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.2F, 1.4F);
        level.playSound(null, cx, cy, cz, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, 1.2F);
    }

    public static void spawnSustainedAura(LivingEntity entity) {
        Level level = entity.level();
        if (level == null || !level.isClientSide) {
            return;
        }

        double time = entity.tickCount * 0.18D;
        double height = entity.getBbHeight();
        double radius = Math.max(0.55D, entity.getBbWidth() * 0.75D);

        for (int i = 0; i < 2; i++) {
            double angle = time + (i * Math.PI);
            double px = entity.getX() + Math.cos(angle) * radius;
            double pz = entity.getZ() + Math.sin(angle) * radius;
            double py = entity.getY() + 0.25D + ((Math.sin(time * 0.7D + (i * Math.PI)) + 1.0D) * 0.5D) * (height * 0.75D);

            Vector3f color = (i == 0) ? COLOR_VERDIGRIS : COLOR_FOREST_GREEN;
            if (entity.getRandom().nextFloat() < 0.3F) {
                color = COLOR_DEEP_CYAN;
            }

            level.addParticle(new DustParticleOptions(color, 0.85F), px, py, pz, 0.01D, 0.03D, 0.01D);
        }
    }
}
