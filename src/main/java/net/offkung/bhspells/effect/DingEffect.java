package net.offkung.bhspells.effect;

import net.offkung.bhspells.service.DingShenFaService;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class DingEffect extends MobEffect {
    public DingEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // This runs inside the active-effect update, independently of Forge's
        // living-tick event ordering, so a frozen mob cannot get one AI move
        // or one knockback frame between server ticks.
        DingShenFaService.enforceFreeze(entity);

        // Match Wukong's every-five-tick wax-off pulse without a global
        // LivingTickEvent handler. Only entities with Ding execute this.
        MobEffectInstance ding = entity.getEffect(MobEffectsRegistry.DING.get());
        if (!entity.level().isClientSide && ding != null && ding.getDuration() % 5 == 0
                && entity.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.WAX_OFF,
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                    3, 0.15D, 0.25D, 0.15D, 0.02D);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        // Wukong's DingEndEvent emits these two bursts whenever the Ding
        // effect ends, including natural expiry.
        if (entity.level() instanceof ServerLevel level) {
            double y = entity.getY() + 1.0D;
            level.sendParticles(ParticleTypes.GLOW, entity.getX(), y, entity.getZ(), 12,
                    0.25D, 0.5D, 0.25D, 0.05D);
            level.sendParticles(ParticleTypes.WAX_OFF, entity.getX(), y, entity.getZ(), 12,
                    0.25D, 0.5D, 0.25D, 0.05D);
        }
        DingShenFaService.cleanupAfterEffect(entity);
    }
}
