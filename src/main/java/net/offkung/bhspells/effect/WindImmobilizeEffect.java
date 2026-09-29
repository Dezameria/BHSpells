package net.offkung.bhspells.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class WindImmobilizeEffect extends MobEffect {
    public static final UUID IMMOBILIZE_SPEED_UUID = UUID.fromString("b3a1a9e2-51c3-4d64-8e12-70b5550a1122");

    public WindImmobilizeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC2F0C2);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, IMMOBILIZE_SPEED_UUID.toString(), -1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        livingEntity.setDeltaMovement(Vec3.ZERO);
        livingEntity.hasImpulse = true;
        livingEntity.fallDistance = 0;

        if (livingEntity instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
        }

        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            Vec3 look = livingEntity.getLookAngle().normalize();
            double backDist = 0.7;
            double px = livingEntity.getX() - look.x * backDist;
            double py = livingEntity.getY() + livingEntity.getBbHeight() * 0.55 - look.y * 0.3;
            double pz = livingEntity.getZ() - look.z * backDist;
            serverLevel.sendParticles(ParticleTypes.CLOUD, px, py, pz, 1, 0.08, 0.08, 0.08, 0.01);
        }
    }
}
