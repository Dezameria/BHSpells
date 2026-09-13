package net.offkung.bhspells.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class StarIceEffect extends MobEffect {
    public static final UUID FLIGHT_MODIFIER_UUID = UUID.fromString("f8d5b838-897d-304e-b5f7-f8c5b058a5b2");
    public static final ResourceLocation CAELUS_FALL_FLYING = ResourceLocation.fromNamespaceAndPath("caelus", "fall_flying");

    public StarIceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x99E2F9);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level().isClientSide && livingEntity.isFallFlying()) {
            if (livingEntity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, livingEntity.getX(), livingEntity.getY() + 0.2, livingEntity.getZ(), 2, 0.15, 0.15, 0.15, 0.02);
            }
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(livingEntity, attributeMap, amplifier);
        Attribute fallFlying = ForgeRegistries.ATTRIBUTES.getValue(CAELUS_FALL_FLYING);
        if (fallFlying != null) {
            AttributeInstance instance = attributeMap.getInstance(fallFlying);
            if (instance != null && !instance.hasModifier(getModifier())) {
                instance.addTransientModifier(getModifier());
            }
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(livingEntity, attributeMap, amplifier);
        Attribute fallFlying = ForgeRegistries.ATTRIBUTES.getValue(CAELUS_FALL_FLYING);
        if (fallFlying != null) {
            AttributeInstance instance = attributeMap.getInstance(fallFlying);
            if (instance != null) {
                instance.removeModifier(FLIGHT_MODIFIER_UUID);
            }
        }
    }

    public static AttributeModifier getModifier() {
        return new AttributeModifier(FLIGHT_MODIFIER_UUID, "Star Ice flight", 1.0D, AttributeModifier.Operation.ADDITION);
    }
}
