package net.offkung.bhspells.event;

import com.gametechbc.traveloptics.entity.misc.TOScreenShakeEntity;
import com.hm.efn.registries.EFNMobEffectRegistry;
import com.p1nero.wukong.epicfight.animation.WukongAnimations;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.effect.WheelOfKarmaEffect;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhweapons.registry.AnimationRegistry;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

@Mod.EventBusSubscriber
public class WheelOfKarmaEvents {
    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (event.getEntity().hasEffect(MobEffectsRegistry.WHEEL_OF_KARMA.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        if (!target.hasEffect(MobEffectsRegistry.WHEEL_OF_KARMA.get())) {
            return;
        }

        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        // 1. Discard projectiles
        if (source.getDirectEntity() instanceof Projectile projectile) {
            CompoundTag tag = projectile.getPersistentData();
            if (!tag.getBoolean(WheelOfKarmaEffect.PROJECTILE_CHECKED_TAG)) {
                tag.putBoolean(WheelOfKarmaEffect.PROJECTILE_CHECKED_TAG, true);
                if (target.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(EpicFightParticles.HIT_BLUNT.get(), projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0, 0, 0, 0);
                    serverLevel.playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(), SoundRegistry.FORCE_IMPACT.get(), SoundSource.PLAYERS, 0.8f, 0.8f);
                }
                projectile.discard();
            }
            event.setCanceled(true);
            return;
        }

        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            event.setCanceled(true);
            return;
        }

        // 2. Invulnerable to damage hit by living entity, with blunt hit particle in the hit direction
        Entity directEntity = source.getDirectEntity();
        Entity trueSource = source.getEntity();
        LivingEntity attacker = directEntity instanceof LivingEntity livingDirect ? livingDirect : (trueSource instanceof LivingEntity livingAttacker ? livingAttacker : null);

        if (attacker != null && attacker != target) {
            if (target.level() instanceof ServerLevel serverLevel) {
                Vec3 targetCenter = target.position().add(0, target.getBbHeight() * 0.6, 0);
                Vec3 attackerCenter = attacker.position().add(0, attacker.getBbHeight() * 0.6, 0);
                Vec3 diff = attackerCenter.subtract(targetCenter);
                Vec3 dir = diff.lengthSqr() > 1.0E-4 ? diff.normalize() : target.getLookAngle();

                double dist = targetCenter.distanceTo(attackerCenter);
                double offset = Math.min(1.0, Math.max(target.getBbWidth() * 0.5 + 0.2, dist * 0.4));
                Vec3 hitPos = targetCenter.add(dir.scale(offset));

                serverLevel.sendParticles(EpicFightParticles.HIT_BLUNT.get(), hitPos.x, hitPos.y, hitPos.z, 1, 0, 0, 0, 0);
                serverLevel.playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundRegistry.FORCE_IMPACT.get(), SoundSource.PLAYERS, 0.8f, 0.8f);
            }
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (!target.hasEffect(MobEffectsRegistry.WHEEL_OF_KARMA.get())) {
            return;
        }

        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        if (source.is(DamageTypeTags.IS_PROJECTILE) || source.getDirectEntity() instanceof Projectile) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            return;
        }

        Entity directEntity = source.getDirectEntity();
        Entity trueSource = source.getEntity();
        LivingEntity attacker = directEntity instanceof LivingEntity livingDirect ? livingDirect : (trueSource instanceof LivingEntity livingAttacker ? livingAttacker : null);

        if (attacker != null && attacker != target) {
            event.setCanceled(true);
            event.setAmount(0.0F);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }

        CompoundTag data = entity.getPersistentData();
        if (!data.getBoolean(WheelOfKarmaEffect.SMASH_ACTIVE_TAG)) {
            return;
        }

        int slamTicks = data.getInt(WheelOfKarmaEffect.SLAM_TICKS_TAG) + 1;
        data.putInt(WheelOfKarmaEffect.SLAM_TICKS_TAG, slamTicks);

        if (data.getBoolean(WheelOfKarmaEffect.SLAM_PENDING_TAG)) {
            if (!entity.onGround() || slamTicks > 3) {
                data.putBoolean(WheelOfKarmaEffect.WAS_IN_AIR_TAG, true);
            }

            boolean wasInAir = data.getBoolean(WheelOfKarmaEffect.WAS_IN_AIR_TAG);
            boolean hitGround = wasInAir && (entity.onGround() || entity.verticalCollision);

            // Trigger ground slam upon landing (or timeout fallback)
            if (hitGround || slamTicks >= 30) {
                data.remove(WheelOfKarmaEffect.SLAM_PENDING_TAG);
                data.remove(WheelOfKarmaEffect.WAS_IN_AIR_TAG);

                Vec3 slamPos = entity.position();
                HitResult groundCheck = entity.level().clip(new ClipContext(
                        slamPos.add(0, 1.0, 0),
                        slamPos.add(0, -3.0, 0),
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        entity
                ));
                if (groundCheck.getType() == HitResult.Type.BLOCK) {
                    slamPos = groundCheck.getLocation();
                }

                Vec3 fractureCenter = new Vec3(slamPos.x, slamPos.y - 0.1, slamPos.z);
                LevelUtil.circleSlamFracture(entity, entity.level(), fractureCenter, 4.0, false, false, true);
                TOScreenShakeEntity.createScreenShake(entity.level(), slamPos, 6.0F, 0.07F, 10, 0, 5, true);

                // Apply slowness + knockdown to entities in fracture radius
                double fractureRadius = 4.0;
                AABB fractureArea = new AABB(
                        slamPos.x - fractureRadius, slamPos.y - 1.0, slamPos.z - fractureRadius,
                        slamPos.x + fractureRadius, slamPos.y + 3.0, slamPos.z + fractureRadius
                );
                for (LivingEntity target : entity.level().getEntitiesOfClass(LivingEntity.class, fractureArea, t -> t != entity && t.isAlive())) {
                    double distSq = target.distanceToSqr(slamPos.x, slamPos.y, slamPos.z);
                    if (distSq <= fractureRadius * fractureRadius) {
                        target.addEffect(new MobEffectInstance(EFNMobEffectRegistry.STOP.get(), 40, 1, false, true));
                        LivingEntityPatch<?> targetPatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
                        if (targetPatch != null) {
                            targetPatch.playAnimationSynchronized(Animations.BIPED_KNOCKDOWN, 0.0f);
                        }
                    }
                }
            }
        }

        boolean slamFinished = slamTicks >= 60;
        if (slamTicks > 15 && !data.getBoolean(WheelOfKarmaEffect.SLAM_PENDING_TAG)) {
            LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
            if (patch != null && patch.getAnimator().getPlayer(WukongAnimations.SMASH_CHARGED0).isEmpty()) {
                slamFinished = true;
            }
        }

        if (slamFinished) {
            cleanup(entity);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity victim = event.getEntity();
        if (victim.hasEffect(MobEffectsRegistry.WHEEL_OF_KARMA.get()) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (source.is(DamageTypeTags.IS_PROJECTILE) || source.getDirectEntity() instanceof Projectile) {
                event.setCanceled(true);
                event.setAmount(0.0F);
                return;
            }
            Entity directEntity = source.getDirectEntity();
            Entity trueSource = source.getEntity();
            LivingEntity attacker = directEntity instanceof LivingEntity livingDirect ? livingDirect : (trueSource instanceof LivingEntity livingAttacker ? livingAttacker : null);
            if (attacker != null && attacker != victim) {
                event.setCanceled(true);
                event.setAmount(0.0F);
                return;
            }
        }

        boolean isSmash = false;
        if (source instanceof EpicFightDamageSource epicSource) {
            if (epicSource.getAnimation() == WukongAnimations.SMASH_CHARGED0) {
                isSmash = true;
            }
        }
        if (!isSmash && source.getEntity() instanceof LivingEntity attacker) {
            if (attacker.getPersistentData().getBoolean(WheelOfKarmaEffect.SMASH_ACTIVE_TAG)) {
                LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(attacker, LivingEntityPatch.class);
                if (patch != null && patch.getAnimator().getPlayer(WukongAnimations.SMASH_CHARGED0).isPresent()) {
                    isSmash = true;
                }
            }
        }

        if (isSmash) {
            LivingEntity target = event.getEntity();
            LivingEntityPatch<?> targetPatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
            if (targetPatch != null) {
                targetPatch.playAnimationSynchronized(Animations.BIPED_KNOCKDOWN, 0.0f);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        if (event.getEffect() == MobEffectsRegistry.WHEEL_OF_KARMA.get()) {
            LivingEntity entity = event.getEntity();
            if (entity.isAlive()) {
                triggerSlam(entity);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect() == MobEffectsRegistry.WHEEL_OF_KARMA.get()) {
            LivingEntity entity = event.getEntity();
            triggerSlam(entity);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().hasEffect(MobEffectsRegistry.WHEEL_OF_KARMA.get()) || event.getEntity().getPersistentData().getBoolean(WheelOfKarmaEffect.SMASH_ACTIVE_TAG)) {
            LivingEntity entity = event.getEntity();
            stopSpinAnimation(entity);
            cleanup(entity);
        }
    }

    public static void triggerSlam(LivingEntity entity) {
        if (entity.level().isClientSide || !entity.isAlive()) {
            return;
        }

        CompoundTag persistentData = entity.getPersistentData();
        if (persistentData.getBoolean(WheelOfKarmaEffect.SMASH_ACTIVE_TAG)) {
            return;
        }
        persistentData.putBoolean(WheelOfKarmaEffect.FINISHED_TAG, true);
        persistentData.putBoolean(WheelOfKarmaEffect.SMASH_ACTIVE_TAG, true);
        persistentData.putBoolean(WheelOfKarmaEffect.SLAM_PENDING_TAG, true);
        persistentData.putInt(WheelOfKarmaEffect.SLAM_TICKS_TAG, 0);
        persistentData.putBoolean(WheelOfKarmaEffect.WAS_IN_AIR_TAG, false);

        LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
        if (entityPatch != null && !entityPatch.isLogicalClient()) {
            entityPatch.playAnimationSynchronized(WukongAnimations.SMASH_CHARGED0, 0.0f);
        }
    }

    public static void stopSpinAnimation(LivingEntity entity) {
        if (entity.level().isClientSide) {
            return;
        }
        LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
        if (entityPatch != null) {
            entityPatch.stopPlaying(AnimationRegistry.STAFF_CHARYBDIS_LOOP_FAST);
            entityPatch.playAnimationSynchronized(Animations.OFF_ANIMATION_HIGHEST, 0.0f);
        }
    }

    public static void cleanup(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        persistentData.remove(WheelOfKarmaEffect.FINISHED_TAG);
        persistentData.remove(WheelOfKarmaEffect.SMASH_ACTIVE_TAG);
        persistentData.remove(WheelOfKarmaEffect.SLAM_PENDING_TAG);
        persistentData.remove(WheelOfKarmaEffect.SLAM_TICKS_TAG);
        persistentData.remove(WheelOfKarmaEffect.WAS_IN_AIR_TAG);
    }
}
