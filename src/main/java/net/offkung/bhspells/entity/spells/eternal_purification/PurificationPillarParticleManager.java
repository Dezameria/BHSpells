package net.offkung.bhspells.entity.spells.eternal_purification;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.util.BHParticleHelper;

public class PurificationPillarParticleManager {
    private static final double PILLAR_WIDTH = 1.0F;
    private static final double PILLAR_HEIGHT = 3.0F;

    public static void playWaterfallCascade(Level level, Vec3 pillarPos, int age, double radiusMultiplier) {
        if (level.isClientSide) {
            int particlesPerTick = 4;

            for(int i = 0; i < particlesPerTick; ++i) {
                double startHeight = 3.2;
                double angle = level.random.nextDouble() * (double)2.0F * Math.PI;
                double baseRadius = (0.4 + level.random.nextDouble() * 0.3) * radiusMultiplier;
                double offsetX = Math.cos(angle) * baseRadius;
                double offsetZ = Math.sin(angle) * baseRadius;
                double fallProgress = ((double)age * (double)2.0F + (double)(i * 10)) % (double)80.0F / (double)80.0F;
                double currentHeight = startHeight - fallProgress * (double)3.5F;
                if (currentHeight > -0.3) {
                    double particleX = pillarPos.x + offsetX;
                    double particleY = pillarPos.y + currentHeight;
                    double particleZ = pillarPos.z + offsetZ;
                    double driftStrength = 0.01 / Math.max(radiusMultiplier, 1.0F);
                    double velocityX = -offsetX * driftStrength;
                    double velocityY = -0.06;
                    double velocityZ = -offsetZ * driftStrength;
                    level.addParticle(BHParticleHelper.PILLAR_GREEN_PASSIVE_ENCHANT, particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
                }
            }
        }

    }
}
