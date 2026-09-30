package net.offkung.bhspells.compat.epicfight.skills.dingshenfa;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;

/**
 * Extended ActionAnimation supporting Root Motion (shouldMove) and flexible transitions,
 * ported from Wukong's SpecialActionAnimation for animations like {@code fashu_magicarts_dsf_start}.
 */
public class SpecialActionAnimation extends ActionAnimation {
    private final boolean shouldMove;

    public SpecialActionAnimation(float transitionTime, AnimationManager.AnimationAccessor<? extends ActionAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        this(0.0F, transitionTime, accessor, armature, true);
    }

    public SpecialActionAnimation(float preDelay, float transitionTime, AnimationManager.AnimationAccessor<? extends ActionAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        this(preDelay, transitionTime, accessor, armature, true);
    }

    public SpecialActionAnimation(float preDelay, float transitionTime, AnimationManager.AnimationAccessor<? extends ActionAnimation> accessor, AssetAccessor<? extends Armature> armature, boolean shouldMove) {
        super(preDelay, transitionTime, accessor, armature);
        this.shouldMove = shouldMove;
    }

    @Override
    protected boolean shouldMove(float currentTime) {
        return this.shouldMove;
    }
}
