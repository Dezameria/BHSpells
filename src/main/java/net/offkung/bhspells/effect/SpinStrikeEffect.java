package net.offkung.bhspells.effect;

import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.BHSpellRegistry;

import java.util.List;

public class SpinStrikeEffect extends MobEffect {
    public SpinStrikeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4DD9EB);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player player) {
            player.startAutoSpinAttack(10);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        var level = entity.level();

        if (entity instanceof Player player) {
            player.startAutoSpinAttack(10);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ(), 4, 0.3, 0.3, 0.3, 0.03);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ(), 4, 0.3, 0.3, 0.3, 0.05);
        }

        Vec3 forward = entity.getLookAngle();
        double yMotion = entity.onGround() && forward.y < 0 ? 0.05 : forward.y;
        Vec3 dashVelocity = new Vec3(forward.x, yMotion, forward.z).normalize().scale(1.4);
        entity.setDeltaMovement(dashVelocity);
        entity.hurtMarked = true;

        AABB hitBox = entity.getBoundingBox().inflate(1.2, 0.6, 1.2);
        List<Entity> targets = level.getEntities(entity, hitBox);

        for (Entity target : targets) {
            if (target instanceof LivingEntity livingTarget && target.isAlive() && !target.isAlliedTo(entity) && target != entity) {
                if (livingTarget.invulnerableTime <= 0 && livingTarget.hurtTime <= 0) {
                    var damageSource = BHSpellRegistry.SPIN_STRIKE.get().getDamageSource(entity);
                    DamageSources.applyDamage(livingTarget, (float) amplifier, damageSource);
                    livingTarget.invulnerableTime = 20;

                    Vec3 away = livingTarget.position().subtract(entity.position());
                    Vec3 knockback = new Vec3(away.x, 0.2, away.z).normalize().scale(0.6);
                    livingTarget.setDeltaMovement(knockback);
                    livingTarget.hurtMarked = true;
                }
            }
        }

        if (entity.horizontalCollision) {
            entity.removeEffect(this);
        }
        entity.fallDistance = 0;
    }
}
