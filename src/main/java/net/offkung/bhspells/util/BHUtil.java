package net.offkung.bhspells.util;

import com.gametechbc.traveloptics.init.TravelopticsParticles;
import com.github.L_Ender.cataclysm.client.particle.LightningParticle;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.DustParticleOptions;
import net.offkung.bhspells.registry.ParticleRegistry;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BHUtil {
    public static boolean isAlly(LivingEntity owner, LivingEntity target) {
        return owner.getTeam() != null && owner.getTeam().isAlliedTo(target.getTeam());
    }

    public static boolean isTamed(LivingEntity target) {
        if (target instanceof TamableAnimal tamableAnimal) {
            return tamableAnimal.isTame();
        } else {
            return false;
        }
    }

    public static boolean isAllyOrSelf(LivingEntity owner, LivingEntity target) {
        if (owner == null || target == null) {
            return false;
        }
        if (owner == target || owner.getUUID().equals(target.getUUID())) {
            return true;
        }
        if (isAlly(owner, target) || owner.isAlliedTo(target)) {
            return true;
        }
        if (target instanceof TamableAnimal tamableAnimal && owner.getUUID().equals(tamableAnimal.getOwnerUUID())) {
            return true;
        }
        if (DamageSources.isFriendlyFireBetween(owner, target)) {
            return true;
        }
        return false;
    }

    public static Vector3f hexToVector3f(String hexColor) {
        String cleanHex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
        int rgb = Integer.parseInt(cleanHex, 16);
        float red = (float)(rgb >> 16 & 255) / 255.0F;
        float green = (float)(rgb >> 8 & 255) / 255.0F;
        float blue = (float)(rgb & 255) / 255.0F;
        return new Vector3f(red, green, blue);
    }

    public static Vector3f hexToVector3f(int hexColor) {
        float red = (float)(hexColor >> 16 & 255) / 255.0F;
        float green = (float)(hexColor >> 8 & 255) / 255.0F;
        float blue = (float)(hexColor & 255) / 255.0F;
        return new Vector3f(red, green, blue);
    }

    public static void createSphereParticles(ServerLevel level, Vec3 center, ParticleOptions particleOptions, double radius, double minSpeed, double maxSpeed, int particleCount) {
        RandomSource random = level.random;
        for (int i = 0; i < particleCount; i++) {
            double dirX;
            double dirY;
            double dirZ;
            double lenSq;
            do {
                dirX = random.nextDouble() * 2.0 - 1.0;
                dirY = random.nextDouble() * 2.0 - 1.0;
                dirZ = random.nextDouble() * 2.0 - 1.0;
                lenSq = dirX * dirX + dirY * dirY + dirZ * dirZ;
            } while (lenSq > 1.0 || lenSq < 1.0E-6);
            double inv = 1.0 / Math.sqrt(lenSq);
            dirX *= inv;
            dirY *= inv;
            dirZ *= inv;

            double speed = minSpeed + random.nextDouble() * (maxSpeed - minSpeed);
            MagicManager.spawnParticles(level, particleOptions, center.x + dirX * radius, center.y + dirY * radius, center.z + dirZ * radius, 0, dirX, dirY, dirZ, speed, true);
        }
    }

    public static void createHorizontalRingParticles(ServerLevel level, Vec3 center, ParticleOptions particleOptions, double radius, double minSpeed, double maxSpeed, int particleCount) {
        RandomSource random = level.random;
        double angleStep = Math.PI * 2.0 / particleCount;
        for (int i = 0; i < particleCount; i++) {
            double angle = angleStep * i;
            double dirX = Math.cos(angle);
            double dirZ = Math.sin(angle);

            double speed = minSpeed + random.nextDouble() * (maxSpeed - minSpeed);
            MagicManager.spawnParticles(level, particleOptions, center.x + dirX * radius, center.y, center.z + dirZ * radius, 0, dirX, 0, dirZ, speed, true);
        }
    }

    public static void spawnImpactSparks(ServerLevel level, Vec3 center, ParticleOptions particleOptions, int count, double minSpeed, double maxSpeed, float verticalSpread) {
        RandomSource random = level.random;
        float clampedSpread = Mth.clamp(verticalSpread, 0.0F, 1.0F);

        for (int i = 0; i < count; i++) {
            double yaw = random.nextDouble() * Math.PI * 2.0;
            double roll = random.nextDouble();
            double pitch = roll * roll * (Math.PI / 2.0) * clampedSpread;

            double horizontal = Math.cos(pitch);
            double dirX = horizontal * Math.cos(yaw);
            double dirY = Math.sin(pitch);
            double dirZ = horizontal * Math.sin(yaw);

            double speed = minSpeed + random.nextDouble() * (maxSpeed - minSpeed);
            MagicManager.spawnParticles(level, particleOptions, center.x, center.y, center.z, 0, dirX * speed, dirY * speed, dirZ * speed, 1.0, true);
        }
    }

    public static void spawnRedThunderAura(ServerLevel level, LivingEntity entity, int count) {
        RandomSource random = level.random;
        LightningParticle.OrbData particle = new LightningParticle.OrbData(255, 0, 0);
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat() * ((float) Math.PI * 2F);
            float horizontal = random.nextFloat() * (entity.getBbWidth() * 0.6F + 0.2F);
            double ox = Mth.cos(angle) * horizontal;
            double oz = Mth.sin(angle) * horizontal;
            double oy = random.nextFloat() * entity.getBbHeight();
            double toX = Mth.cos(angle) * 1.2F;
            double toY = random.nextFloat() * 0.8F;
            double toZ = Mth.sin(angle) * 1.2F;
            level.sendParticles(particle, entity.getX() + ox, entity.getY() + oy, entity.getZ() + oz, 0, toX, toY, toZ, 1.0);
        }
    }

    public static void createHexagramParticle(ParticleOptions particleOptions, ServerLevel level, Vec3 center, float interval, float radius, float yRot) {
        createHexagramParticle(particleOptions, level, center, interval, radius, yRot, 0, 0.01F, 0);
    }

    public static void createHexagramParticle(ParticleOptions particleOptions, Level level, Vec3 center, float interval, float radius, float yRot, float xSpeed, float ySpeed, float zSpeed) {
        for (Vec3 pos : generateHexagram(center, radius, interval, yRot)) {
            level.addParticle(particleOptions, pos.x, pos.y, pos.z, xSpeed, ySpeed, zSpeed);
        }
    }

    public static List<Vec3> generateHexagram(Vec3 center, double radius, double interval, double yRot) {
        double rad = Math.toRadians(yRot);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        List<Vec3> vertices = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            double dx = radius * Math.cos(angle);
            double dz = radius * Math.sin(angle);
            double rx = dx * cos - dz * sin;
            double rz = dx * sin + dz * cos;

            vertices.add(new Vec3(center.x + rx, center.y, center.z + rz));
        }

        int[][] connections = {{0, 2}, {2, 4}, {4, 0}, {1, 3}, {3, 5}, {5, 1}};

        Set<Vec3> path = new LinkedHashSet<>();
        for (int[] edge : connections) {
            Vec3 start = vertices.get(edge[0]);
            Vec3 end = vertices.get(edge[1]);

            double dx = end.x - start.x;
            double dz = end.z - start.z;
            double length = Math.hypot(dx, dz);

            int steps = (int) Math.ceil(length / interval);
            for (int i = 0; i <= steps; i++) {
                double ratio = i / (double) steps;
                path.add(new Vec3(start.x + dx * ratio, center.y, start.z + dz * ratio));
            }
        }
        return new ArrayList<>(path);
    }

    public static void spawnAquaFlowerGroundWave(Level level, Vec3 center, float radius, int tickCount) {
        // Circular rotation angle (~0.08 radians per tick)
        double rotSpeed = 0.08;
        double baseAngle = tickCount * rotSpeed;

        // 6 spiral rotating wave arms across the ground within the 20-block radius
        int numArms = 6;
        double armStep = (Math.PI * 2.0) / numArms;
        double spiralTightness = -0.15; // gentle curvature forming rotating swirling wave

        RandomSource random = level.random;

        for (int arm = 0; arm < numArms; arm++) {
            double armBaseAngle = baseAngle + arm * armStep;
            for (double r = 0.8; r <= radius; r += 0.85) {
                double angle = armBaseAngle + (r * spiralTightness);
                double dirX = Math.cos(angle);
                double dirZ = Math.sin(angle);

                // Slight lateral spread to give wave arms realistic width/stream thickness
                double lateral = (random.nextDouble() - 0.5) * 0.45;
                double perpX = -dirZ * lateral;
                double perpZ = dirX * lateral;

                double x = center.x + dirX * r + perpX;
                double y = center.y + 0.08;
                double z = center.z + dirZ * r + perpZ;

                // Tangential circular velocity (rotating in circular, not spreading out)
                double tangentialSpeed = 0.04;
                double vx = -dirZ * tangentialSpeed;
                double vz = dirX * tangentialSpeed;

                MagicManager.spawnParticles(level, ACParticleRegistry.WATER_FOAM.get(), x, y, z, 0, vx, 0.01, vz, 1.0, false);

                if (random.nextInt(3) == 0) {
                    MagicManager.spawnParticles(level, TravelopticsParticles.WATER_PARTICLE.get(), x, y, z, 1, vx, 0.02, vz, tangentialSpeed, false);
                }
                if (random.nextInt(4) == 0) {
                    MagicManager.spawnParticles(level, ParticleTypes.SPLASH, x, y, z, 1, vx * 1.2, 0.04 + random.nextDouble() * 0.03, vz * 1.2, tangentialSpeed * 1.2, false);
                }
                if (random.nextInt(6) == 0) {
                    MagicManager.spawnParticles(level, ParticleRegistry.BUBBLE_SPLASH_PARTICLE.get(), x, y + 0.05, z, 1, vx, 0.03, vz, tangentialSpeed, false);
                }
            }
        }

        Vector3f fromColor = new Vector3f(0.2f, 0.28f, 0.52f);
        Vector3f toColor = new Vector3f(1f, 1f, 1f);

        DustColorTransitionOptions blueDustTransition = new DustColorTransitionOptions(fromColor, toColor, 0.5f);

        // 4 continuous ripples spaced evenly across the radius to fill all radial gaps
        float waveSpeed = 0.35f;
        for (int ri = 0; ri < 4; ri++) {
            float rippleR = (tickCount * waveSpeed + (radius * (ri / 4.0f))) % radius;
            int rippleCount = Math.max(18, (int) (rippleR * 2.8f));
            spawnGroundRipple(level, center, rippleR, blueDustTransition, rippleCount, baseAngle);
        }
    }

    private static void spawnGroundRipple(Level level, Vec3 center, float r, ParticleOptions dust, int count, double rotationOffset) {
        if (r <= 0.6f) return;
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0 / count) * i + rotationOffset * 0.5 + (random.nextDouble() - 0.5) * 0.1;
            double dirX = Math.cos(angle);
            double dirZ = Math.sin(angle);
            double x = center.x + dirX * r;
            double z = center.z + dirZ * r;
            double y = center.y + 0.08;
            // Swirling circular velocity to match "rotating in circular, not spreading out"
            double tangentialSpeed = 0.035;
            double vx = -dirZ * tangentialSpeed;
            double vz = dirX * tangentialSpeed;
            MagicManager.spawnParticles(level, dust, x, y, z, 0, vx, 0.01, vz, 1.0, false);
            if (i % 4 == 0) {
                MagicManager.spawnParticles(level, ParticleTypes.SPLASH, x, y, z, 1, vx * 1.4, 0.04 + random.nextDouble() * 0.03, vz * 1.4, tangentialSpeed * 1.2, false);
            }
            if (i % 7 == 0) {
                MagicManager.spawnParticles(level, ParticleRegistry.BUBBLE_SPLASH_PARTICLE.get(), x, y + 0.05, z, 1, vx, 0.03, vz, tangentialSpeed, false);
            }
        }
    }

    public static void spawnAquaFlowerShield(Level level, LivingEntity entity, int tickCount) {
        double width = entity.getBbWidth();
        double height = entity.getBbHeight();
        double radius = Math.max(width * 0.8, height * 0.55) + 0.25;
        double centerX = entity.getX();
        double centerY = entity.getY() + height * 0.5;
        double centerZ = entity.getZ();

        DustParticleOptions blueDust = new DustParticleOptions(new Vector3f(0.66f, 0.87f, 1f), 0.7f);

        int cycleTicks = 20;
        int particlesPerRing = 25;

        // Rising layer sweeping in order from bottom pole (-pi/2) to top pole (+pi/2) every 1 second, matching SEA_STAFF_CAST
        int step = tickCount % cycleTicks;
        float progress = step / (float) (cycleTicks - 1);
        double phi = -Math.PI / 2.0 + progress * Math.PI;
        double rh = radius * Math.cos(phi);
        double y = centerY + radius * Math.sin(phi);

        for (int i = 0; i < particlesPerRing; i++) {
            double theta = (Math.PI * 2.0 / particlesPerRing) * i;
            double px = centerX + rh * Math.cos(theta);
            double pz = centerZ + rh * Math.sin(theta);

            MagicManager.spawnParticles(level, blueDust, px, y, pz, 1, 0, 0, 0, 0, false);
        }

        // Vertical meridian ring to clearly delineate the 3D full sphere
        if (tickCount % 2 == 0) {
            int meridianCount = 12;
            for (int i = 0; i < meridianCount; i++) {
                double angle = (Math.PI * 2.0 / meridianCount) * i;
                double px = centerX + radius * Math.cos(angle);
                double py = centerY + radius * Math.sin(angle);
                MagicManager.spawnParticles(level, blueDust, px, py, centerZ, 1, 0, 0, 0, 0, false);
            }
        }
    }

    public static void spawnProjectileInterceptParticle(Level level, Vec3 pos) {
        MagicManager.spawnParticles(level, TravelopticsParticles.WATER_DROP_PARTICLE.get(), pos.x, pos.y, pos.z, 20, 0.2, 0.2, 0.2, 0.1, false);
        MagicManager.spawnParticles(level, ParticleTypes.SPLASH, pos.x, pos.y, pos.z, 15, 0.2, 0.2, 0.2, 0.1, false);
        MagicManager.spawnParticles(level, ParticleRegistry.BUBBLE_SPLASH_PARTICLE.get(), pos.x, pos.y, pos.z, 10, 0.15, 0.15, 0.15, 0.05, false);

        level.playSound(null, pos.x, pos.y, pos.z, SoundRegistry.FORCE_IMPACT.get(), SoundSource.NEUTRAL, 1.0f, 1.5f);
    }

    public static void spawnRisingSakuraParticles(Level level, Vec3 center, float radius, int count) {
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            double dist = Math.sqrt(random.nextDouble()) * radius;
            double angle = random.nextDouble() * Math.PI * 2.0;

            double px = center.x + Math.cos(angle) * dist;
            double pz = center.z + Math.sin(angle) * dist;
            double py = center.y + 0.08 + random.nextDouble() * 0.15;

            // Upward velocity with slight drifting motion
            double vx = (random.nextDouble() - 0.5) * 0.02;
            double vy = 0.08 + random.nextDouble() * 0.08;
            double vz = (random.nextDouble() - 0.5) * 0.02;

            MagicManager.spawnParticles(level, ParticleRegistry.SPLATTER_SAKURA.get(), px, py, pz, 0, vx, vy, vz, 1.0, false);
        }
    }
}