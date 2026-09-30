package net.offkung.bhspells.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * Shocking (ช็อคกิ้ง) Status Effect:
 * Slows movement speed by a fixed 25% and deals magic damage over time (DoT) every 20 ticks.
 */
public class ShockingEffect extends MobEffect {
    public static final UUID MOVEMENT_SLOW_UUID = UUID.fromString("c2a4e680-7b91-4d33-9e12-8f12a34b5678");
    public static final int DAMAGE_INTERVAL_TICKS = 20;
    public static final float BASE_DOT_DAMAGE = 2.0F;
    public static final float DOT_DAMAGE_PER_AMPLIFIER = 0.5F;

    public ShockingEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SLOW_UUID.toString(),
                -0.25D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        // Enforce fixed 25% slowness across all spell level amplifiers
        return modifier.getAmount();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % DAMAGE_INTERVAL_TICKS == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }

        float damage = BASE_DOT_DAMAGE + (float) amplifier * DOT_DAMAGE_PER_AMPLIFIER;
        entity.hurt(entity.damageSources().magic(), damage);
    }
}
