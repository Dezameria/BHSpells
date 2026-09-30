package net.offkung.bhspells.compat.epicfight.skills.earth_roar;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * Dedicated builder and registry for Earth Roar's Epic Fight animation.
 * Features client-side periodic joint tracking for the right leg (thighR, legR, kneeR)
 * during the energy-gathering phase (0.4s - 0.8s).
 */
public final class EarthRoarAnimations {
    public static final String PATH_EARTH_ROAR = "biped/spells/earth_roar";
    public static final float DEFAULT_TRANSITION_TIME = 0.15F;

    private EarthRoarAnimations() {}

    public static AnimationManager.AnimationAccessor<ActionAnimation> registerEarthRoar(AnimationManager.AnimationBuilder builder) {
        return registerEarthRoar(builder, PATH_EARTH_ROAR);
    }

    public static AnimationManager.AnimationAccessor<ActionAnimation> registerEarthRoar(AnimationManager.AnimationBuilder builder, String path) {
        return builder.nextAccessor(path, accessor -> {
            ActionAnimation animation = new ActionAnimation(
                    DEFAULT_TRANSITION_TIME,
                    accessor,
                    Armatures.BIPED
            );

            // Phase 2: Client-side right leg energy gathering VFX (0.4s to 0.8s)
            animation.addEvents(
                    AnimationEvent.InPeriodEvent.create(0.4F, 0.8F, (patch, anim, params) -> {
                        spawnRightLegParticles(patch);
                    }, AnimationEvent.Side.CLIENT)
            );

            return animation;
        });
    }

    private static void spawnRightLegParticles(LivingEntityPatch<?> patch) {
        if (patch == null) {
            return;
        }

        LivingEntity entity = patch.getOriginal();
        if (entity == null || !entity.level().isClientSide) {
            return;
        }

        Pose currentPose = patch.getAnimator().getPose(0.0F);
        if (currentPose == null) {
            return;
        }

        Joint kneeJoint = Armatures.BIPED.get().searchJointByName("kneeR");
        if (kneeJoint == null) {
            kneeJoint = Armatures.BIPED.get().searchJointByName("legR");
        }

        if (kneeJoint != null) {
            OpenMatrix4f legTransform = Armatures.BIPED.get().getBoundTransformFor(currentPose, kneeJoint);
            OpenMatrix4f rot = new OpenMatrix4f();
            rot.rotate((float) -Math.toRadians(entity.getYRot() + 180.0F), new Vec3f(0.0F, 1.0F, 0.0F));
            OpenMatrix4f.mul(legTransform, rot, legTransform);

            double worldX = legTransform.m30 + entity.getX();
            double worldY = legTransform.m31 + entity.getY() + 0.3D;
            double worldZ = legTransform.m32 + entity.getZ();

            entity.level().addParticle(ParticleTypes.CRIT,
                    worldX, worldY, worldZ,
                    (Math.random() - 0.5D) * 0.1D, 0.05D, (Math.random() - 0.5D) * 0.1D);
            entity.level().addParticle(ParticleTypes.POOF,
                    worldX, worldY, worldZ,
                    0, 0.02D, 0);
        }
    }
}
