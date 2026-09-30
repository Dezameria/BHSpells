package net.offkung.bhspells.compat.epicfight.pressure;

import net.offkung.bhspells.compat.api.AnimationCue;
import net.offkung.bhspells.compat.api.AnimationRequest;
import net.offkung.bhspells.compat.epicfight.common.animation.EpicFightAnimationPlayer;
import net.offkung.bhspells.pressure.PressureReaction;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * Controller executing Epic Fight reaction animations under strict priority rules.
 * Never interrupts death, active dodges, or high-priority skill actions.
 */
class PressureAnimationController {
    static void handleReaction(LivingEntity entity, PressureReaction reaction) {
        if (!entity.isAlive() || entity.level().isClientSide) {
            return;
        }

        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
        if (patch == null || patch.getAnimator() == null) {
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
            }
        }

        AnimationCue cue = switch (reaction) {
            case STAGGER -> AnimationCue.PRESSURE_STAGGER;
            case CROUCH, KNEEL -> AnimationCue.PRESSURE_KNEEL;
            case KNOCKDOWN -> AnimationCue.PRESSURE_KNOCKDOWN;
            case NONE -> null;
        };

        if (cue == null) {
            return;
        }

        float transition = switch (reaction) {
            case STAGGER -> 0.1F;
            case CROUCH, KNEEL -> 0.15F;
            case KNOCKDOWN -> 0.2F;
            case NONE -> 0.1F;
        };

        EpicFightAnimationPlayer.play(AnimationRequest.of(entity, cue, transition));
    }
}
