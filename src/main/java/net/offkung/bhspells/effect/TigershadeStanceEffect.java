package net.offkung.bhspells.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class TigershadeStanceEffect extends MobEffect {
    public static final String TARGET_UUID_TAG = "TigershadeTargetUUID";
    public static final String TARGET_DIM_TAG = "TigershadeTargetDim";

    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("c5942f27-84d7-425b-80a6-16f5bf167c91");
    private static final UUID STRENGTH_MODIFIER_UUID = UUID.fromString("e85741dc-3392-4217-a068-07e0c4bfa640");

    public TigershadeStanceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B5A2B);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID.toString(), 0.40D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, STRENGTH_MODIFIER_UUID.toString(), 3.0D, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 3 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        var level = entity.level();
        if (level instanceof ServerLevel serverLevel) {
            double x = entity.getX();
            double y = entity.getY();
            double z = entity.getZ();

            serverLevel.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(1.0F, 0.65F, 0.15F), 1.2F), x, y + 0.5, z, 3, 0.35, 0.4, 0.35, 0.02);
            serverLevel.sendParticles(ParticleTypes.FLAME, x, y + 0.2, z, 1, 0.2, 0.15, 0.2, 0.01);
        }
    }
}
