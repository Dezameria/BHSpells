package net.offkung.bhspells.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class AttackSpeedBoostEffect extends MobEffect {
    private static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("6b6b1e2e-6f2b-4c2d-9b3e-2a4c1e0c9a11");

    public AttackSpeedBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xADD8E6);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER_UUID.toString(), 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
