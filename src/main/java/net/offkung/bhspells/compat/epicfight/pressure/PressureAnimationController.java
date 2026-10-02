package net.offkung.bhspells.compat.epicfight.pressure;

import net.offkung.bhspells.compat.api.AnimationCue;
import net.offkung.bhspells.compat.api.AnimationRequest;
import net.offkung.bhspells.compat.epicfight.common.animation.EpicFightAnimationPlayer;
import net.offkung.bhspells.pressure.PressureReaction;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller executing Epic Fight reaction animations under strict priority rules.
 * Never interrupts death, active dodges, or high-priority skill actions.
 * Resets back to idle when a pressure reaction ends.
 */
class PressureAnimationController {
    private static final Map<UUID, AnimationCue> ACTIVE_PRESSURE_ANIMATIONS = new ConcurrentHashMap<>();
    static void handleReaction(LivingEntity entity, PressureReaction oldReaction, PressureReaction newReaction) {
        if (!entity.isAlive() || entity.level().isClientSide) {
            return;
        }

        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
        if (patch == null || patch.getAnimator() == null) {
            return;
        }

        // Handle reaction ending (NONE): Reset to idle only if currently playing a pressure reaction animation that this controller tracked
        if (newReaction == PressureReaction.NONE) {
            AnimationCue activeCue = ACTIVE_PRESSURE_ANIMATIONS.remove(entity.getUUID());
            if (activeCue != null) {
                var player = patch.getAnimator().getPlayerFor(null);
                if (player != null && player.getAnimation() != null) {
                    AssetAccessor<? extends DynamicAnimation> activeAccessor = player.getAnimation();
                    if (activeAccessor != null) {
                        DynamicAnimation activeAnim = activeAccessor.get();
                        if (isPressureAnimation(activeAnim)) {
                            patch.playAnimationSynchronized(Animations.BIPED_IDLE, 0.15F);
                        }
                    }
                }
            }
            return;
        }

        // Check active animation priority
        var player = patch.getAnimator().getPlayerFor(null);
        if (player != null && player.getAnimation() != null) {
            AssetAccessor<? extends DynamicAnimation> activeAccessor = player.getAnimation();
            if (activeAccessor != null) {
                DynamicAnimation activeAnim = activeAccessor.get();
                // Do not override active dodge animation
                if (activeAnim instanceof DodgeAnimation) {
                    return;
                }
                // Do not interrupt ongoing skill actions or attack animations unless already playing a pressure reaction
                if ((activeAnim instanceof ActionAnimation || activeAnim instanceof AttackAnimation) && !isPressureAnimation(activeAnim)) {
                    return;
                }
                // Do not downgrade an active knockdown with a weaker stagger or kneel
                if (activeAnim == Animations.BIPED_KNOCKDOWN.get() && newReaction != PressureReaction.KNOCKDOWN) {
                    return;
                }
            }
        }

        AnimationCue cue = switch (newReaction) {
            case STAGGER -> AnimationCue.PRESSURE_STAGGER;
            case CROUCH, KNEEL -> AnimationCue.PRESSURE_KNEEL;
            case KNOCKDOWN -> AnimationCue.PRESSURE_KNOCKDOWN;
            case NONE -> null;
        };

        if (cue == null) {
            return;
        }

        float transition = switch (newReaction) {
            case STAGGER -> 0.1F;
            case CROUCH, KNEEL -> 0.15F;
            case KNOCKDOWN -> 0.2F;
            case NONE -> 0.1F;
        };

        ACTIVE_PRESSURE_ANIMATIONS.put(entity.getUUID(), cue);
        EpicFightAnimationPlayer.play(AnimationRequest.of(entity, cue, transition));
    }

    private static boolean isPressureAnimation(DynamicAnimation anim) {
        if (anim == null) {
            return false;
        }
        return anim == Animations.BIPED_HIT_SHORT.get()
                || anim == Animations.BIPED_KNEEL.get()
                || anim == Animations.BIPED_KNOCKDOWN.get();
    }
}
