package net.offkung.bhspells.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * Server-authoritative gameplay effect applied to entities caught within a Spiritual Pressure field.
 * Attributes scale with amplifier (stagger -> kneel -> knockdown).
 */
public class SpiritualPressureEffect extends MobEffect {
    public static final UUID MOVEMENT_SLOW_UUID = UUID.fromString("6a8b7c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d");
    public static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("7b9c8d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6e");

    public SpiritualPressureEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SLOW_UUID.toString(),
                -0.20D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_UUID.toString(),
                -0.15D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }
}
