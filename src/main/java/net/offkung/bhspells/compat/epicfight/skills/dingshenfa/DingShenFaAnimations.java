package net.offkung.bhspells.compat.epicfight.skills.dingshenfa;

import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.service.DingShenFaService;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.gameasset.Armatures;

public final class DingShenFaAnimations {
    public static final String PATH_DING_SHEN_FA = "biped/spells/ding_shen_fa";
    public static final float DEFAULT_TRANSITION_TIME = 0.14F;
    public static final float DEFAULT_SPEED_MODIFIER = 2.0F;
    public static final boolean DEFAULT_SHOULD_MOVE = true;
    public static final float DEFAULT_DING_TIMESTAMP = 0.0F;

    private DingShenFaAnimations() {
    }

    public static AnimationManager.AnimationAccessor<ActionAnimation> registerDingShenFa(AnimationManager.AnimationBuilder builder) {
        return registerDingShenFa(builder, PATH_DING_SHEN_FA);
    }

    public static AnimationManager.AnimationAccessor<ActionAnimation> registerDingShenFa(AnimationManager.AnimationBuilder builder, String path) {
        return registerDingShenFa(builder, path, DEFAULT_TRANSITION_TIME, DEFAULT_SPEED_MODIFIER, DEFAULT_SHOULD_MOVE, DEFAULT_DING_TIMESTAMP);
    }

    /**
     * Fully configurable registration supporting both Root Motion (SpecialActionAnimation)
     * and stationary casts, custom playback speeds, and flexible event timestamps.
     */
    public static AnimationManager.AnimationAccessor<ActionAnimation> registerDingShenFa(
            AnimationManager.AnimationBuilder builder,
            String path,
            float transitionTime,
            float speedMultiplier,
            boolean shouldMove,
            float dingTimestamp) {
        return builder.nextAccessor(path, accessor -> {
            ActionAnimation animation = new SpecialActionAnimation(
                    0.0F,
                    transitionTime,
                    accessor,
                    Armatures.BIPED,
                    shouldMove
            );

            animation.addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER,
                    (anim, patch, speed, prev, next) -> speedMultiplier);

            // Wukong triggers ding at start (0.0s / ON_BEGIN) and plays sound at 0.0s
            if (dingTimestamp <= 0.0F) {
                animation.addEvents(
                        AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS,
                        AnimationEvent.SimpleEvent.create((patch, anim, params) -> {
                            DingShenFaService.cast(patch.getOriginal(), patch.getTarget());
                        }, AnimationEvent.Side.SERVER)
                );
            } else {
                animation.addEvents(AnimationEvent.InTimeEvent.create(dingTimestamp, (patch, anim, params) -> {
                    DingShenFaService.cast(patch.getOriginal(), patch.getTarget());
                }, AnimationEvent.Side.SERVER));
            }

            animation.addEvents(AnimationEvent.InTimeEvent.create(0.0F, (patch, anim, params) -> {
                patch.playSound(BHSoundRegistry.XULI_DING_SOU.get(), 1.0F, 1.0F);
            }, AnimationEvent.Side.SERVER));

            return animation;
        });
    }
}
