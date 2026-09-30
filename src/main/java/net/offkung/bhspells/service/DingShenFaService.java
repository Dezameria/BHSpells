package net.offkung.bhspells.service;

import net.offkung.bhspells.compat.epicfight.EpicFightCompat;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.network.casting.CancelCastPacket;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Runtime port of Wukong's {@code BattleUnit.ding} /
 * {@code FashuDingshenfaSkill} behaviour for entities available in this mod.
 */
public final class DingShenFaService {
    public static final String TAG_DING = "ding";
    public static final String TAG_TRACKED_TARGET = "wukong_dingshen_target";
    public static final String TAG_TRACKED_TARGET_LIST = "bhspells:ding_tracked_targets";
    public static final String DATA_CASTER_UUID = "bhspells:ding_caster_uuid";

    private static final String DATA_ORIGINAL_MOVEMENT_SPEED = "bhspells:ding_original_movement_speed";
    private static final String DATA_ORIGINAL_NO_AI = "bhspells:ding_original_no_ai";
    private static final String DATA_ORIGINAL_AGGRESSIVE = "bhspells:ding_original_aggressive";
    public static final String DATA_ROTATION_Y = "bhspells:ding_rot_y";
    public static final String DATA_ROTATION_X = "bhspells:ding_rot_x";
    public static final String DATA_ROTATION_HEAD_Y = "bhspells:ding_rot_head_y";
    public static final String DATA_ROTATION_BODY_Y = "bhspells:ding_rot_body_y";
    public static final double MAX_TARGET_DISTANCE = 50.0D;
    public static final int IMMOBILIZE_DURATION_TICKS = 192;

    private DingShenFaService() {
    }

    public static boolean cast(LivingEntity caster) {
        return cast(caster, null);
    }

    /**
     * Executes Ding Shen Fa with support for Epic Fight battle target,
     * crosshair raycast targeting, configurable AoE multi-targeting, and Dodge evasion.
     */
    public static boolean cast(LivingEntity caster, @Nullable LivingEntity battleTarget) {
        if (caster == null || caster.level().isClientSide) {
            return false;
        }

        double multiRadius = SpellConfig.DingShenFa.getMultiTargetRadius();
        if (multiRadius > 0.0D) {
            return castMultiTarget(caster, battleTarget, multiRadius);
        }

        LivingEntity target = isValidTarget(caster, battleTarget) ? battleTarget : findTarget(caster);
        if (target == null) {
            return false;
        }

        // Dodge evasion: target evades Ding Shen Fa if actively playing an Epic Fight DodgeAnimation
        if (EpicFightCompat.isDodging(target)) {
            return false;
        }

        applyImmobilize(caster, target);
        return true;
    }

    private static boolean castMultiTarget(LivingEntity caster, @Nullable LivingEntity battleTarget, double radius) {
        Vec3 center;
        HitResult hit = Utils.raycastForEntity(caster.level(), caster, (float) MAX_TARGET_DISTANCE, true, 0.6F);
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living && isValidTarget(caster, living)) {
            center = living.position();
        } else if (isValidTarget(caster, battleTarget)) {
            center = battleTarget.position();
        } else if (hit instanceof BlockHitResult blockHit && blockHit.getType() != HitResult.Type.MISS) {
            center = blockHit.getLocation();
        } else {
            center = caster.position().add(caster.getLookAngle().scale(Math.min(10.0D, MAX_TARGET_DISTANCE)));
        }

        List<LivingEntity> candidates = caster.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center.x - radius, center.y - radius, center.z - radius,
                         center.x + radius, center.y + radius, center.z + radius),
                entity -> isValidTarget(caster, entity) && entity.distanceToSqr(center) <= radius * radius);

        int count = 0;
        for (LivingEntity candidate : candidates) {
            if (EpicFightCompat.isDodging(candidate)) {
                continue;
            }
            applyImmobilize(caster, candidate);
            count++;
        }

        return count > 0;
    }

    private static boolean isValidTarget(LivingEntity caster, @Nullable LivingEntity target) {
        return target != null && target != caster && target.isAlive() && !target.isSpectator();
    }

    private static @Nullable LivingEntity findTarget(LivingEntity caster) {
        // 1. Crosshair raycast targeting
        HitResult hit = Utils.raycastForEntity(caster.level(), caster, (float) MAX_TARGET_DISTANCE, true, 0.6F);
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living && isValidTarget(caster, living)) {
            return living;
        }

        // 2. Fallback to nearest living entity within distance
        List<LivingEntity> nearby = caster.level().getEntitiesOfClass(
                LivingEntity.class,
                caster.getBoundingBox().inflate(MAX_TARGET_DISTANCE),
                entity -> isValidTarget(caster, entity));
        if (nearby.isEmpty()) {
            return null;
        }
        LivingEntity closest = null;
        double minDistanceSqr = Double.MAX_VALUE;
        for (LivingEntity entity : nearby) {
            double distSqr = entity.distanceToSqr(caster);
            if (distSqr < minDistanceSqr) {
                minDistanceSqr = distSqr;
                closest = entity;
            }
        }
        return closest;
    }

    public static void applyImmobilize(LivingEntity caster, LivingEntity target) {
        if (target.level().isClientSide) {
            return;
        }

        target.addTag(TAG_DING);
        saveAndZeroMovementSpeed(target);
        target.addEffect(new MobEffectInstance(MobEffectsRegistry.DING.get(), IMMOBILIZE_DURATION_TICKS, 0));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, IMMOBILIZE_DURATION_TICKS, 0));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, IMMOBILIZE_DURATION_TICKS, 4));

        target.stopUsingItem();
        if (target instanceof ServerPlayer serverPlayer) {
            CancelCastPacket.cancelCast(serverPlayer, false);
        }

        // Wukong applies this to Monster/EnderDragon. Apply it to every Mob
        // here so non-monster mobs cannot continue a navigation path during
        // the bind; the previous state is restored exactly on release.
        if (target instanceof Mob mob) {
            if (!mob.getPersistentData().contains(DATA_ORIGINAL_NO_AI)) {
                mob.getPersistentData().putBoolean(DATA_ORIGINAL_NO_AI, mob.isNoAi());
            }
            mob.setNoAi(true);
            mob.getNavigation().stop();
            mob.setTarget(null);
        }
        if (target instanceof Monster monster) {
            if (!monster.getPersistentData().contains(DATA_ORIGINAL_AGGRESSIVE)) {
                monster.getPersistentData().putBoolean(DATA_ORIGINAL_AGGRESSIVE, monster.isAggressive());
            }
            monster.setAggressive(false);
        }

        // Zero out horizontal velocity, preserving vertical velocity so entities can fall and land naturally
        Vec3 currentMovement = target.getDeltaMovement();
        target.setDeltaMovement(0.0D, currentMovement.y, 0.0D);

        // Lock rotation angles so their bodies/heads stay fixed
        target.getPersistentData().putFloat(DATA_ROTATION_Y, target.getYRot());
        target.getPersistentData().putFloat(DATA_ROTATION_X, target.getXRot());
        target.getPersistentData().putFloat(DATA_ROTATION_HEAD_Y, target.getYHeadRot());
        target.getPersistentData().putFloat(DATA_ROTATION_BODY_Y, target.yBodyRot);

        // Track caster-target relationship
        target.getPersistentData().putUUID(DATA_CASTER_UUID, caster.getUUID());
        trackTargetOnCaster(caster, target);

        PacketHandler.spawnDingAfterImage(target);
    }


    private static void trackTargetOnCaster(LivingEntity caster, LivingEntity target) {
        caster.getPersistentData().putUUID(TAG_TRACKED_TARGET, target.getUUID());

        ListTag list;
        if (caster.getPersistentData().contains(TAG_TRACKED_TARGET_LIST, Tag.TAG_LIST)) {
            list = caster.getPersistentData().getList(TAG_TRACKED_TARGET_LIST, Tag.TAG_STRING);
        } else {
            list = new ListTag();
            caster.getPersistentData().put(TAG_TRACKED_TARGET_LIST, list);
        }

        String targetUuidStr = target.getStringUUID();
        boolean alreadyTracked = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(targetUuidStr)) {
                alreadyTracked = true;
                break;
            }
        }
        if (!alreadyTracked) {
            list.add(StringTag.valueOf(targetUuidStr));
        }
    }

    /**
     * Releases all targets immobilized by the given caster (e.g. on Shift+Cast / cancellation).
     * Returns true if at least one target was released.
     */
    public static boolean releaseAllByCaster(LivingEntity caster) {
        if (caster == null || caster.level().isClientSide) {
            return false;
        }

        boolean releasedAny = false;
        Level level = caster.level();

        // 1. Release all tracked targets from caster list
        if (caster.getPersistentData().contains(TAG_TRACKED_TARGET_LIST, Tag.TAG_LIST)) {
            ListTag list = caster.getPersistentData().getList(TAG_TRACKED_TARGET_LIST, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                try {
                    UUID targetUuid = UUID.fromString(list.getString(i));
                    if (level instanceof ServerLevel serverLevel) {
                        Entity entity = serverLevel.getEntity(targetUuid);
                        if (entity instanceof LivingEntity living && isImmobilized(living)) {
                            release(living, false);
                            releasedAny = true;
                        }
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
            caster.getPersistentData().remove(TAG_TRACKED_TARGET_LIST);
        }

        // Also check single tracked target
        if (caster.getPersistentData().hasUUID(TAG_TRACKED_TARGET)) {
            UUID singleUuid = caster.getPersistentData().getUUID(TAG_TRACKED_TARGET);
            if (level instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(singleUuid);
                if (entity instanceof LivingEntity living && isImmobilized(living)) {
                    release(living, false);
                    releasedAny = true;
                }
            }
            caster.getPersistentData().remove(TAG_TRACKED_TARGET);
        }

        // 2. Fallback scan nearby 64 blocks for any entities tagged with TAG_DING owned by this caster
        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                caster.getBoundingBox().inflate(64.0D),
                entity -> isImmobilized(entity) && isOwnedByCaster(caster, entity));
        for (LivingEntity target : nearby) {
            release(target, false);
            releasedAny = true;
        }

        return releasedAny;
    }

    private static boolean isOwnedByCaster(LivingEntity caster, LivingEntity target) {
        if (target.getPersistentData().hasUUID(DATA_CASTER_UUID)) {
            return caster.getUUID().equals(target.getPersistentData().getUUID(DATA_CASTER_UUID));
        }
        return false;
    }

    /** Explicit early release, for example if another game system removes Ding. */
    public static void release(LivingEntity target, boolean ignoredFromDamage) {
        if (target == null || target.level().isClientSide) {
            return;
        }

        if (!target.getTags().contains(TAG_DING)) {
            return;
        }

        if (target.hasEffect(MobEffectsRegistry.DING.get())) {
            target.removeEffect(MobEffectsRegistry.DING.get());
        } else {
            cleanupAfterEffect(target);
        }
    }

    /** Called once by {@link net.offkung.bhspells.effect.DingEffect} after expiry or removal. */
    public static void cleanupAfterEffect(LivingEntity target) {
        if (target == null || target.level().isClientSide || !target.getTags().contains(TAG_DING)) {
            return;
        }

        target.removeTag(TAG_DING);
        target.getPersistentData().remove(DATA_CASTER_UUID);
        restoreMovementSpeed(target);
        if (target.hasEffect(MobEffects.GLOWING)) {
            target.removeEffect(MobEffects.GLOWING);
        }
        if (target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }

        if (target instanceof Mob mob) {
            boolean originalNoAi = mob.getPersistentData().getBoolean(DATA_ORIGINAL_NO_AI);
            mob.getPersistentData().remove(DATA_ORIGINAL_NO_AI);
            mob.setNoAi(originalNoAi);
        }
        if (target instanceof Monster monster) {
            boolean originalAggressive = monster.getPersistentData().getBoolean(DATA_ORIGINAL_AGGRESSIVE);
            monster.getPersistentData().remove(DATA_ORIGINAL_AGGRESSIVE);
            monster.setAggressive(originalAggressive);
        }

        target.getPersistentData().remove(DATA_ROTATION_Y);
        target.getPersistentData().remove(DATA_ROTATION_X);
        target.getPersistentData().remove(DATA_ROTATION_HEAD_Y);
        target.getPersistentData().remove(DATA_ROTATION_BODY_Y);
        clearFrozenHeadPose(target);
    }


    private static void saveAndZeroMovementSpeed(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null && !entity.getPersistentData().contains(DATA_ORIGINAL_MOVEMENT_SPEED)) {
            entity.getPersistentData().putDouble(DATA_ORIGINAL_MOVEMENT_SPEED, attribute.getBaseValue());
            attribute.setBaseValue(0.0D);
        }
    }

    private static void restoreMovementSpeed(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (entity.getPersistentData().contains(DATA_ORIGINAL_MOVEMENT_SPEED)) {
            double originalSpeed = entity.getPersistentData().getDouble(DATA_ORIGINAL_MOVEMENT_SPEED);
            entity.getPersistentData().remove(DATA_ORIGINAL_MOVEMENT_SPEED);
            if (attribute != null) {
                attribute.setBaseValue(originalSpeed);
            }
        }
    }

    /** Invoked by the active Ding effect only; stops horizontal drift while allowing natural gravity like Wukong. */
    public static void enforceFreeze(LivingEntity entity) {
        if (!isImmobilized(entity)) {
            return;
        }
        Vec3 motion = entity.getDeltaMovement();
        if (motion.x != 0.0D || motion.z != 0.0D) {
            entity.setDeltaMovement(0.0D, motion.y, 0.0D);
        }

        if (!(entity instanceof Player) && entity.getPersistentData().contains(DATA_ROTATION_Y)) {
            float yRot = entity.getPersistentData().getFloat(DATA_ROTATION_Y);
            float xRot = entity.getPersistentData().getFloat(DATA_ROTATION_X);
            float headY = entity.getPersistentData().getFloat(DATA_ROTATION_HEAD_Y);
            float bodyY = entity.getPersistentData().getFloat(DATA_ROTATION_BODY_Y);
            entity.setYRot(yRot);
            entity.setXRot(xRot);
            entity.setYHeadRot(headY);
            entity.yBodyRot = bodyY;
            entity.yRotO = yRot;
            entity.xRotO = xRot;
            entity.yHeadRotO = headY;
            entity.yBodyRotO = bodyY;
        }

        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.setTarget(null);
        }
    }

    /**
     * Immutable snapshot of the head orientation at the moment an entity became immobilized.
     */
    public static final class FrozenHeadPose {
        public final float headYRot;
        public final float pitch;
        public final float netHeadYaw;

        public FrozenHeadPose(float headYRot, float pitch, float netHeadYaw) {
            this.headYRot = headYRot;
            this.pitch = pitch;
            this.netHeadYaw = netHeadYaw;
        }
    }

    private static final java.util.Map<Integer, FrozenHeadPose> FROZEN_HEAD_POSES = new java.util.concurrent.ConcurrentHashMap<>();

    public static @Nullable FrozenHeadPose getOrCreateFrozenHeadPose(LivingEntity entity, float currentModelYRot) {
        if (!isImmobilized(entity)) {
            FROZEN_HEAD_POSES.remove(entity.getId());
            return null;
        }

        return FROZEN_HEAD_POSES.computeIfAbsent(entity.getId(), id -> {
            float headY = entity.getYHeadRot();
            float pitch = entity.getXRot();
            float bodyY = entity.yBodyRot;
            float netHeadYaw = net.minecraft.util.Mth.wrapDegrees(headY - bodyY);
            return new FrozenHeadPose(headY, pitch, netHeadYaw);
        });
    }

    public static void clearFrozenHeadPose(LivingEntity entity) {
        if (entity != null) {
            FROZEN_HEAD_POSES.remove(entity.getId());
        }
    }


    public static boolean isImmobilized(@Nullable LivingEntity entity) {
        return entity != null && (entity.getTags().contains(TAG_DING) || entity.hasEffect(MobEffectsRegistry.DING.get()));
    }
}

