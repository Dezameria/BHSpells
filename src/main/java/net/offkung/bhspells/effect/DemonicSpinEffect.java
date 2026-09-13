package net.offkung.bhspells.effect;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.event.DemonicSpinEvents;
import net.offkung.bhweapons.registry.AnimationRegistry;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public class DemonicSpinEffect extends MobEffect {
    public static final double RADIUS = 5.0;
    public static final double RADIUS_SQR = RADIUS * RADIUS;
    public static final float PROJECTILE_DISCARD_CHANCE = 0.40f;
    public static final String PROJECTILE_CHECKED_TAG = "bhspells:demonic_spin_checked";

    public DemonicSpinEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x631D76);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (livingEntity.level().isClientSide) {
            return;
        }

        MobEffectInstance instance = livingEntity.getEffect(this);
        if (instance != null && instance.getDuration() <= 1) {
            DemonicSpinEvents.stopSpinAnimation(livingEntity);
        } else if (instance != null) {
            LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(livingEntity, LivingEntityPatch.class);
            if (entityPatch != null && !entityPatch.isLogicalClient()) {
                if (entityPatch.getAnimator().getPlayer(AnimationRegistry.STAFF_SPIN_TWOHAND_LOOP_FAST).isEmpty()) {
                    entityPatch.playAnimationSynchronized(AnimationRegistry.STAFF_SPIN_TWOHAND_LOOP_FAST, 0.0f);
                }
            }
        }

        AABB area = livingEntity.getBoundingBox().inflate(RADIUS);

        // 1. Chance of 40% to discard projectiles in radius of 5 blocks
        List<Projectile> projectiles = livingEntity.level().getEntitiesOfClass(Projectile.class, area, p -> p.isAlive() && p.getOwner() != livingEntity && p.distanceToSqr(livingEntity) <= RADIUS_SQR);

        for (Projectile projectile : projectiles) {
            CompoundTag tag = projectile.getPersistentData();
            if (!tag.getBoolean(PROJECTILE_CHECKED_TAG)) {
                tag.putBoolean(PROJECTILE_CHECKED_TAG, true);
                if (livingEntity.getRandom().nextFloat() < PROJECTILE_DISCARD_CHANCE) {
                    if (livingEntity.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(EpicFightParticles.HIT_BLUNT.get(), projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0, 0, 0, 0);
                        serverLevel.playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(), SoundRegistry.FORCE_IMPACT.get(), SoundSource.PLAYERS, 1.1f, 0.8f);
                    }
                    projectile.discard();
                }
            }
        }

        // 2. If any entities try to approach caster in 5 blocks, push them away
        List<LivingEntity> targets = livingEntity.level().getEntitiesOfClass(LivingEntity.class, area, target -> target != livingEntity && target.isAlive() && !target.isSpectator() && !livingEntity.isAlliedTo(target) && !DamageSources.isFriendlyFireBetween(livingEntity, target) && target.distanceToSqr(livingEntity) <= RADIUS_SQR);

        for (LivingEntity target : targets) {
            Vec3 diff = target.position().subtract(livingEntity.position());
            double distHorizontal = diff.horizontalDistance();
            Vec3 pushDir;
            if (distHorizontal < 1.0E-4) {
                pushDir = new Vec3(1.0, 0.0, 0.0);
            } else {
                pushDir = new Vec3(diff.x / distHorizontal, 0.0, diff.z / distHorizontal);
            }

            double pushStrength = 0.6;
            Vec3 pushVelocity = new Vec3(pushDir.x * pushStrength, 0.2, pushDir.z * pushStrength);
            target.setDeltaMovement(pushVelocity);
            target.hurtMarked = true;
            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }
            if (livingEntity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(EpicFightParticles.HIT_BLUNT.get(), target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 1, 0, 0, 0, 0);
                serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), SoundRegistry.FORCE_IMPACT.get(), SoundSource.PLAYERS, 1.1f, 0.8f);
            }
        }
    }
}
