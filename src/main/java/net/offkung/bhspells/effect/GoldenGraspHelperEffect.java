package net.offkung.bhspells.effect;

import com.gametechbc.traveloptics.util.TravelopticsParticleHelper;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.mcreator.dungeonsandcombat.init.DungeonsAndCombatModParticleTypes;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.offkung.bhspells.registry.ParticleRegistry;
import org.joml.Vector3f;

public class GoldenGraspHelperEffect extends MobEffect {
    public GoldenGraspHelperEffect() {
        super(MobEffectCategory.HARMFUL, 9636843);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            DustColorTransitionOptions goldDust = new DustColorTransitionOptions(new Vector3f(1f, 0.68f, 0f), new Vector3f(1f, 0.85f, 0.44f), 2.5f);
            ParticleOptions[] particleTypes = new ParticleOptions[]{ParticleRegistry.GOLDEN_CRIT.get(), DungeonsAndCombatModParticleTypes.BLESSED_SPARKLE.get(), goldDust};
            int[] spawnPercentages = new int[]{30, 30, 40};
            int totalParticles = 8;

            for(int i = 0; i < totalParticles; ++i) {
                int randomValue = entity.getRandom().nextInt(100);
                ParticleOptions selectedParticle = null;
                int cumulativePercentage = 0;

                for(int j = 0; j < particleTypes.length; ++j) {
                    cumulativePercentage += spawnPercentages[j];
                    if (randomValue < cumulativePercentage) {
                        selectedParticle = particleTypes[j];
                        break;
                    }
                }

                if (selectedParticle != null) {
                    AABB boundingBox = entity.getBoundingBox();
                    double x = boundingBox.minX + (boundingBox.maxX - boundingBox.minX) * entity.getRandom().nextDouble();
                    double y = boundingBox.minY + (boundingBox.maxY - boundingBox.minY) * entity.getRandom().nextDouble();
                    double z = boundingBox.minZ + (boundingBox.maxZ - boundingBox.minZ) * entity.getRandom().nextDouble();
                    MagicManager.spawnParticles(entity.level(), selectedParticle, x, y, z, 1, 0.2, 0.2, 0.2, 0.1, true);
                }
            }
        }

    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
