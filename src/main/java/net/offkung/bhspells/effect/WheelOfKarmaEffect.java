package net.offkung.bhspells.effect;

import com.p1nero.wukong.epicfight.animation.WukongAnimations;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.offkung.bhspells.event.WheelOfKarmaEvents;
import net.offkung.bhweapons.registry.AnimationRegistry;
import org.joml.Vector3f;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public class WheelOfKarmaEffect extends MobEffect {
    public static final double RADIUS = 10.0;
    public static final double RADIUS_SQR = RADIUS * RADIUS;
    public static final Vector3f BLUE_BLASTWAVE = new Vector3f(0.12f, 0.55f, 1.0f);
    public static final String PROJECTILE_CHECKED_TAG = "bhspells:wheel_of_karma_checked";
    public static final String FINISHED_TAG = "bhspells:wheel_of_karma_finished";
    public static final String SMASH_ACTIVE_TAG = "bhspells:wheel_of_karma_smash_active";
    public static final String SLAM_PENDING_TAG = "bhspells:wheel_of_karma_slam_pending";
    public static final String SLAM_TICKS_TAG = "bhspells:wheel_of_karma_slam_ticks";
    public static final String WAS_IN_AIR_TAG = "bhspells:wheel_of_karma_was_in_air";

    public WheelOfKarmaEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700);
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

        // If smash has already been triggered, don't tick loop or particles
        if (livingEntity.getPersistentData().getBoolean(SMASH_ACTIVE_TAG)) {
            return;
        }

        // 1. Animation loop & trigger slam when duration reaches end
        MobEffectInstance instance = livingEntity.getEffect(this);
        if (instance != null && instance.getDuration() <= 1) {
            WheelOfKarmaEvents.triggerSlam(livingEntity);
            return;
        } else if (instance != null) {
            LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(livingEntity, LivingEntityPatch.class);
            if (entityPatch != null && !entityPatch.isLogicalClient()) {
                if (entityPatch.getAnimator().getPlayer(AnimationRegistry.STAFF_CHARYBDIS_LOOP_FAST).isEmpty()) {
                    entityPatch.playAnimationSynchronized(AnimationRegistry.STAFF_CHARYBDIS_LOOP_FAST, 0.0f);
                }
            }
        }

        // 2. Blue blastwave effect every 1 second (20 ticks) while active
        if (instance != null && instance.getDuration() % 20 == 0) {
            MagicManager.spawnParticles(livingEntity.level(), new BlastwaveParticleOptions(BLUE_BLASTWAVE, (float) RADIUS), livingEntity.getX(), livingEntity.getY() + 0.15, livingEntity.getZ(), 1, 0, 0, 0, 0, true);
        }

        // 3. Discard any projectile in radius of 10 blocks around the caster
        AABB area = livingEntity.getBoundingBox().inflate(RADIUS);
        List<Projectile> projectiles = livingEntity.level().getEntitiesOfClass(Projectile.class, area, p -> p.isAlive() && p.getOwner() != livingEntity && p.distanceToSqr(livingEntity) <= RADIUS_SQR);

        for (Projectile projectile : projectiles) {
            CompoundTag tag = projectile.getPersistentData();
            if (!tag.getBoolean(PROJECTILE_CHECKED_TAG)) {
                tag.putBoolean(PROJECTILE_CHECKED_TAG, true);
                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(EpicFightParticles.HIT_BLUNT.get(), projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0, 0, 0, 0);
                    serverLevel.playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(), SoundRegistry.FORCE_IMPACT.get(), SoundSource.PLAYERS, 1.1f, 0.8f);
                }
                projectile.discard();
            }
        }
    }
}
