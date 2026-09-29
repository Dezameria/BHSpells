package net.offkung.bhspells.effect;

import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.registry.ParticleRegistry;
import org.joml.Vector3f;

public class WhiteFlameBurnEffect extends MobEffect {
    private static final SparkParticleOptions WHITE_SPARKS = new SparkParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F));

    public WhiteFlameBurnEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFFFF);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();

        if (entity.getRemainingFireTicks() > 0) {
            entity.clearFire();
        }

        boolean isDamageTick = (entity.tickCount % 20 == 0);
        if (isDamageTick) {
            entity.hurt(entity.damageSources().onFire(), 2.0F + (float) amplifier);
        }

        if (level instanceof ServerLevel serverLevel) {
            double halfWidth = entity.getBbWidth() * 0.45;
            double halfHeight = entity.getBbHeight() * 0.45;
            double centerX = entity.getX();
            double centerY = entity.getY() + entity.getBbHeight() * 0.5;
            double centerZ = entity.getZ();

            if (isDamageTick) {
                serverLevel.sendParticles(ParticleRegistry.WHITE_FIRE_EMITTER.get(), centerX, centerY, centerZ, 8, halfWidth * 1.2, halfHeight * 0.8, halfWidth * 1.2, 0.08);
                serverLevel.sendParticles(ParticleRegistry.WHITE_FIRE.get(), centerX, centerY, centerZ, 6, halfWidth * 1.0, halfHeight * 0.8, halfWidth * 1.0, 0.06);
                serverLevel.sendParticles(ParticleRegistry.WHITE_EMBER.get(), centerX, centerY, centerZ, 8, halfWidth * 1.0, halfHeight * 0.8, halfWidth * 1.0, 0.06);
                serverLevel.sendParticles(WHITE_SPARKS, centerX, centerY, centerZ, 4, halfWidth * 0.8, halfHeight * 0.6, halfWidth * 0.8, 0.08);
            } else {
                if (entity.getRandom().nextFloat() < 0.35F) {
                    serverLevel.sendParticles(ParticleRegistry.WHITE_FIRE_EMITTER.get(), centerX, centerY, centerZ, 1, halfWidth * 0.7, halfHeight * 0.7, halfWidth * 0.7, 0.01);
                }
                if (entity.getRandom().nextFloat() < 0.20F) {
                    serverLevel.sendParticles(ParticleRegistry.WHITE_EMBER.get(), centerX, centerY, centerZ, 1, halfWidth * 0.7, halfHeight * 0.7, halfWidth * 0.7, 0.01);
                }
            }
        }
    }
}
