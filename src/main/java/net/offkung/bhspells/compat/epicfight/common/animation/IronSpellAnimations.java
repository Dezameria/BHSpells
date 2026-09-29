package net.offkung.bhspells.compat.epicfight.common.animation;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.compat.api.AnimationCue;
import net.offkung.bhspells.compat.epicfight.skills.blazing_chakra.BlazingChakraAnimations;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Animations;

import java.util.EnumMap;
import java.util.Map;

public final class IronSpellAnimations {
    private static final Map<AnimationCue, AssetAccessor<? extends StaticAnimation>> CUE_MAP = new EnumMap<>(AnimationCue.class);
    public static AnimationManager.AnimationAccessor<AttackAnimation> BLAZING_CHAKRA;
    private static boolean registered;

    private IronSpellAnimations() {
    }

    public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
        if (registered) {
            return;
        }
        registered = true;

        event.newBuilder(BHSpells.MODID, builder -> {
            BLAZING_CHAKRA = BlazingChakraAnimations.registerBlazingChakra(builder, BlazingChakraAnimations.PATH_BLAZING_CHAKRA);
            if (BLAZING_CHAKRA != null) {
                CUE_MAP.put(AnimationCue.BLAZING_CHAKRA, BLAZING_CHAKRA);
            }
        });

        CUE_MAP.put(AnimationCue.BOW_AIM, Animations.BIPED_BOW_AIM);
        CUE_MAP.put(AnimationCue.BOW_SHOOT, Animations.BIPED_BOW_SHOT);
        CUE_MAP.put(AnimationCue.AERIAL_SLAM, Animations.BIPED_DEMOLITION_LEAP);
        CUE_MAP.put(AnimationCue.WARP_PUNCH, Animations.BIPED_MOB_ONEHAND1);
        CUE_MAP.put(AnimationCue.UPPERCUT, Animations.BIPED_MOB_ONEHAND2);
        CUE_MAP.put(AnimationCue.PULL_KICK, Animations.BIPED_STEP_FORWARD);
    }

    public static AssetAccessor<? extends StaticAnimation> getAnimationForCue(AnimationCue cue) {
        AssetAccessor<? extends StaticAnimation> animation = CUE_MAP.get(cue);
        if (animation != null) {
            return animation;
        }

        if (cue == AnimationCue.BLAZING_CHAKRA) {
            if (BLAZING_CHAKRA != null) {
                CUE_MAP.put(cue, BLAZING_CHAKRA);
                return BLAZING_CHAKRA;
            }

            AnimationManager.AnimationAccessor<? extends StaticAnimation> byKey = AnimationManager.byKey(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, BlazingChakraAnimations.PATH_BLAZING_CHAKRA));
            if (byKey != null) {
                CUE_MAP.put(cue, byKey);
                return byKey;
            }
        }

        return null;
    }

    public static void setCustomCueMapping(AnimationCue cue, AssetAccessor<? extends StaticAnimation> animation) {
        CUE_MAP.put(cue, animation);
    }
}
