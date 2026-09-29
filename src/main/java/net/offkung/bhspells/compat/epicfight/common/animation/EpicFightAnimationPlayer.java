package net.offkung.bhspells.compat.epicfight.common.animation;

import net.minecraft.world.entity.LivingEntity;
import net.offkung.bhspells.compat.api.AnimationCue;
import net.offkung.bhspells.compat.api.AnimationRequest;
import net.offkung.bhspells.compat.api.CompatResult;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public final class EpicFightAnimationPlayer {
    private EpicFightAnimationPlayer() {
    }

    public static CompatResult play(AnimationRequest request) {
        LivingEntity entity = request.entity();
        if (entity == null || entity.level().isClientSide) {
            return CompatResult.UNSUPPORTED;
        }

        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
        if (patch == null) {
            return CompatResult.UNSUPPORTED;
        }

        AssetAccessor<? extends StaticAnimation> animation = IronSpellAnimations.getAnimationForCue(request.cue());
        if (animation == null) {
            return CompatResult.UNAVAILABLE;
        }

        if (request.synchronize()) {
            patch.playAnimationSynchronized(animation, request.transitionDuration());
        } else {
            patch.playAnimation(animation, request.transitionDuration());
        }

        return CompatResult.APPLIED;
    }

    public static CompatResult play(LivingEntity entity, AnimationCue cue, float transitionDuration) {
        return play(AnimationRequest.of(entity, cue, transitionDuration));
    }
}
