package net.offkung.bhspells.compat.epicfight.skills.phantom_dodge;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.offkung.bhspells.compat.epicfight.common.particle.AfterimageVfx;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Armatures;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry and random animation selection for Phantom Dodge skill.
 * Registers all 13 dodge animations from biped/spells/dodge/.
 */
public final class PhantomDodgeAnimations {
    public static final List<String> DODGE_PATHS = List.of(
            "biped/spells/dodge/dmcyamato_dodge_b",
            "biped/spells/dodge/dmcyamato_dodge_f",
            "biped/spells/dodge/dmcyamato_dodge_l",
            "biped/spells/dodge/dmcyamato_dodge_r",
            "biped/spells/dodge/dodge_b",
            "biped/spells/dodge/dodge_f",
            "biped/spells/dodge/hf_murasama_dodge_b",
            "biped/spells/dodge/hf_murasama_dodge_f",
            "biped/spells/dodge/perfect_dodge",
            "biped/spells/dodge/step_b",
            "biped/spells/dodge/step_f",
            "biped/spells/dodge/step_l",
            "biped/spells/dodge/step_r");

    private static final List<AnimationManager.AnimationAccessor<DodgeAnimation>> DODGE_ACCESSORS = new ArrayList<>();

    private PhantomDodgeAnimations() {
    }

    public static void registerAnimations(AnimationManager.AnimationBuilder builder) {
        DODGE_ACCESSORS.clear();
        for (String path : DODGE_PATHS) {
            AnimationManager.AnimationAccessor<DodgeAnimation> accessor = builder.nextAccessor(path,
                    acc -> new DodgeAnimation(0.0F, 0.12F, acc, 0.0F, 0.0F, Armatures.BIPED)
                            .addEvents(
                                    AnimationEvent.InTimeEvent.create(0.0F, (patch, anim, params) -> {
                                        LivingEntity entity = patch.getOriginal();
                                        AfterimageVfx.spawnWhiteAfterimage(entity.level(), entity);
                                    }, AnimationEvent.Side.CLIENT)));
            DODGE_ACCESSORS.add(accessor);
        }
    }

    public static AssetAccessor<? extends StaticAnimation> getRandomDodgeAnimation(RandomSource random) {
        if (DODGE_ACCESSORS.isEmpty()) {
            return null;
        }
        return DODGE_ACCESSORS.get(random.nextInt(DODGE_ACCESSORS.size()));
    }
}
