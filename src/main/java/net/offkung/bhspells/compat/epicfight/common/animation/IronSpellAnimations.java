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

/**
 * Main registry and lookup for Epic Fight animations mapped to logical AnimationCue entries.
 * Delegates category-specific animations to modular animation builders.
 */
public final class IronSpellAnimations {
    private static final Map<AnimationCue, AssetAccessor<? extends StaticAnimation>> CUE_MAP = new EnumMap<>(AnimationCue.class);
    public static AnimationManager.AnimationAccessor<AttackAnimation> BLAZING_CHAKRA;
    public static AnimationManager.AnimationAccessor<yesman.epicfight.api.animation.types.ActionAnimation> DING_SHEN_FA;
    public static AnimationManager.AnimationAccessor<yesman.epicfight.api.animation.types.ActionAnimation> EARTH_ROAR;
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
            DING_SHEN_FA = net.offkung.bhspells.compat.epicfight.skills.dingshenfa.DingShenFaAnimations.registerDingShenFa(builder);
            if (DING_SHEN_FA != null) {
                CUE_MAP.put(AnimationCue.DING_SHEN_FA, DING_SHEN_FA);
            }
            EARTH_ROAR = net.offkung.bhspells.compat.epicfight.skills.earth_roar.EarthRoarAnimations.registerEarthRoar(builder);
            if (EARTH_ROAR != null) {
                CUE_MAP.put(AnimationCue.EARTH_ROAR, EARTH_ROAR);
            }
            net.offkung.bhspells.compat.epicfight.skills.phantom_dodge.PhantomDodgeAnimations.registerAnimations(builder);
        });

        CUE_MAP.put(AnimationCue.BOW_AIM, Animations.BIPED_BOW_AIM);
        CUE_MAP.put(AnimationCue.BOW_SHOOT, Animations.BIPED_BOW_SHOT);
        CUE_MAP.put(AnimationCue.AERIAL_SLAM, Animations.BIPED_DEMOLITION_LEAP);
        CUE_MAP.put(AnimationCue.WARP_PUNCH, Animations.BIPED_MOB_ONEHAND1);
        CUE_MAP.put(AnimationCue.UPPERCUT, Animations.BIPED_MOB_ONEHAND2);
        CUE_MAP.put(AnimationCue.PULL_KICK, Animations.BIPED_STEP_FORWARD);
        CUE_MAP.put(AnimationCue.SAVAGE_BITE, Animations.BIPED_SWIM);
        CUE_MAP.put(AnimationCue.EARTH_ROAR_CHARGE, Animations.BIPED_KNEEL);
        CUE_MAP.put(AnimationCue.EARTH_ROAR_DASH, Animations.BIPED_SNEAK);
        CUE_MAP.put(AnimationCue.EARTH_ROAR_KICK, Animations.BIPED_STEP_FORWARD);
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

            AnimationManager.AnimationAccessor<? extends StaticAnimation> byKey =
                AnimationManager.byKey(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, BlazingChakraAnimations.PATH_BLAZING_CHAKRA));
            if (byKey != null) {
                CUE_MAP.put(cue, byKey);
                return byKey;
            }
        }

        if (cue == AnimationCue.DING_SHEN_FA) {
            if (DING_SHEN_FA != null) {
                CUE_MAP.put(cue, DING_SHEN_FA);
                return DING_SHEN_FA;
            }

            AnimationManager.AnimationAccessor<? extends StaticAnimation> byKey =
                AnimationManager.byKey(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, net.offkung.bhspells.compat.epicfight.skills.dingshenfa.DingShenFaAnimations.PATH_DING_SHEN_FA));
            if (byKey != null) {
                CUE_MAP.put(cue, byKey);
                return byKey;
            }
        }

        if (cue == AnimationCue.EARTH_ROAR) {
            if (EARTH_ROAR != null) {
                CUE_MAP.put(cue, EARTH_ROAR);
                return EARTH_ROAR;
            }

            AnimationManager.AnimationAccessor<? extends StaticAnimation> byKey =
                AnimationManager.byKey(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, net.offkung.bhspells.compat.epicfight.skills.earth_roar.EarthRoarAnimations.PATH_EARTH_ROAR));
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

