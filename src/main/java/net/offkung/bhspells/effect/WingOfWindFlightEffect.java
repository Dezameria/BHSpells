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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class WingOfWindFlightEffect extends MobEffect {
    public static final UUID FLIGHT_MODIFIER_UUID = UUID.fromString("9a2e6f43-7a14-41d3-9821-2e61dfbd3401");
    public static final ResourceLocation CAELUS_FALL_FLYING = ResourceLocation.fromNamespaceAndPath("caelus", "fall_flying");

    public WingOfWindFlightEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x98E4B0);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level().isClientSide && livingEntity.isFallFlying()) {
            if (livingEntity.level() instanceof ServerLevel serverLevel) {
                Vec3 look = livingEntity.getLookAngle().normalize();
                double backDist = 0.8;
                double px = livingEntity.getX() - look.x * backDist;
                double py = livingEntity.getY() + 0.2 - look.y * 0.4;
                double pz = livingEntity.getZ() - look.z * backDist;
                serverLevel.sendParticles(ParticleTypes.CLOUD, px, py, pz, 1, 0.08, 0.08, 0.08, 0.01);
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
        return new AttributeModifier(FLIGHT_MODIFIER_UUID, "Wing of Wind flight", 1.0D, AttributeModifier.Operation.ADDITION);
    }
}
