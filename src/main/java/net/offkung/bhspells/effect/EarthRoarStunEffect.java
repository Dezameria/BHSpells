package net.offkung.bhspells.effect;

import net.offkung.bhspells.service.EarthRoarStunService;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

/**
 * Visual and client-recognized stun effect for Earth Roar.
 * Authoritative control-lock and AI suppression are managed independently by {@link EarthRoarStunService}.
 */
public class EarthRoarStunEffect extends MobEffect {
    public EarthRoarStunEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        // Authoritative stun lifecycle is owned by EarthRoarStunService timer;
        // only clean up if the authoritative stun service has expired.
        if (!EarthRoarStunService.isStunned(entity)) {
            EarthRoarStunService.cleanupStun(entity);
        }
    }
}
