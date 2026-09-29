package net.offkung.bhspells.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class GaleFallImpactEffect extends MobEffect {
    public GaleFallImpactEffect() {
        super(MobEffectCategory.HARMFUL, 0x4a7c59);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            if (entity.onGround() && entity.tickCount > 2) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 1, false, false, true));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, false, true));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, false, true));

                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8f, 1.3f);
                if (entity.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, entity.getX(), entity.getY() + 0.1, entity.getZ(), 12, 0.4, 0.1, 0.4, 0.04);
                }

                entity.removeEffect(this);
            }
        }
    }
}
