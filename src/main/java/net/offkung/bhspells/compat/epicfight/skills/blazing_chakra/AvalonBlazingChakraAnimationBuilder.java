package net.offkung.bhspells.compat.epicfight.skills.blazing_chakra;

import com.hm.efn.gameasset.EFNAnimations;
import com.hm.efn.gameasset.EFNExtraDamageInstance;
import com.merlin204.avalon.epicfight.animations.AvalonAttackAnimation;
import com.merlin204.avalon.util.AvalonAnimationUtils;
import com.merlin204.avalon.util.AvalonEventUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.offkung.bhspells.compat.epicfight.common.particle.AfterimageVfx;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.BasicAttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.ExtraDamageInstance;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Set;

public final class AvalonBlazingChakraAnimationBuilder {
    private AvalonBlazingChakraAnimationBuilder() {
    }

    @SuppressWarnings("unchecked")
    static AttackAnimation buildBlazingChakra(AnimationManager.AnimationAccessor<?> accessor) {
        AnimationManager.AnimationAccessor<? extends BasicAttackAnimation> basicAccessor =
                (AnimationManager.AnimationAccessor<? extends BasicAttackAnimation>) accessor;

        return new AvalonAttackAnimation(
                0.1F,
                basicAccessor,
                Armatures.BIPED,
                1.0F,
                1.0F,
                AvalonAnimationUtils.createSimplePhase(
                        59,
                        70,
                        100,
                        InteractionHand.MAIN_HAND,
                        1.0F,
                        2.0F,
                        Armatures.BIPED.get().rootJoint,
                        BlazingChakraColliders.IMPACT
                ))
                .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                .addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_BIG.get())
                .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
                .addProperty(
                        AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER,
                        ValueModifier.setter(100.0F)
                )
                .addProperty(
                        AnimationProperty.AttackPhaseProperty.EXTRA_DAMAGE,
                        Set.of(
                                EFNExtraDamageInstance.LOST_HEALTH_DAMAGE_WITH_SCALING_CAP.create(
                                        new float[] {0.2F, 50.0F, 100.0F}
                                ),
                                ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create(new float[0])
                        )
                )
                .addProperty(
                        AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER,
                        ValueModifier.setter(100.0F)
                )
                .addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLADE)
                .addProperty(
                        AnimationProperty.AttackPhaseProperty.SOURCE_TAG,
                        Set.of(EpicFightDamageTypeTags.WEAPON_INNATE, EpicFightDamageTypeTags.GUARD_PUNCTURE)
                )
                .newTimePair(0.0F, Float.MAX_VALUE)
                .addProperty(
                        AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER,
                        EFNAnimations.ATTACK_SPEED_CAP_MEEN
                )
                .addEvents(
                        AnimationEvent.InTimeEvent.create(59.0F / 60.0F, (patch, anim, params) -> {
                            BlazingChakraVfx.spawnImpactClientVfx(patch);
                        }, AnimationEvent.Side.CLIENT),

                        AnimationEvent.InTimeEvent.create(59.0F / 60.0F, (patch, anim, params) -> {
                            LivingEntity entity = patch.getOriginal();
                            if (entity.level() instanceof ServerLevel level) {
                                BlazingChakraVfx.applyShockwaveDamage(level, entity, entity.position(), BlazingChakraVfx.MAX_RANGE);
                            }
                        }, AnimationEvent.Side.SERVER),

                        AnimationEvent.InTimeEvent.create(1.85F, (patch, anim, params) -> {
                            LivingEntity entity = patch.getOriginal();
                            if (entity.level() instanceof ServerLevel level) {
                                BlazingChakraVfx.spawnSpiralShockwave(level, entity);
                            }
                        }, AnimationEvent.Side.SERVER),

                        AnimationEvent.InTimeEvent.create(0.0F, (patch, anim, params) -> {
                            LivingEntity entity = patch.getOriginal();
                            AfterimageVfx.spawnWhiteAfterimage(entity.level(), entity);
                        }, AnimationEvent.Side.CLIENT),

                        AnimationEvent.InTimeEvent.create(0.0F, (patch, anim, params) ->
                                patch.getOriginal().addEffect(
                                        new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 5, false, false, false)
                                ), AnimationEvent.Side.BOTH),

                        // 6. 0.2s SERVER: Pre-attack Trident Thunder Sound
                        AnimationEvent.InTimeEvent.create(0.2F, (patch, anim, params) ->
                                        patch.playSound(SoundEvents.TRIDENT_THUNDER, 1.5F, 0.0F, 0.0F),
                                AnimationEvent.Side.SERVER),

                        // 7. 1.1s SERVER: Impact Explosion Sound
                        AnimationEvent.InTimeEvent.create(59.0F / 60.0F, (patch, anim, params) ->
                                        patch.playSound(SoundEvents.GENERIC_EXPLODE, 1.5F, 0.0F, 0.0F),
                                AnimationEvent.Side.SERVER),

                        // 8. Frame 57 (~0.95s): Ground crack fracture
                        AvalonEventUtils.simpleGroundSplit(57, 0.0D, 0.0D, 0.0D, 0.0D, 5.0F, true),

                        // 9. Frame 59 (~0.98s): Camera shake
                        AvalonEventUtils.simpleCameraShake(59, 60, 4.0F, 4.0F, 4.0F)
                )
                .newTimePair(0.0F, Float.MAX_VALUE)
                .addStateRemoveOld(EntityState.MOVEMENT_LOCKED, true)
                .addStateRemoveOld(EntityState.CAN_SWITCH_HAND_ITEM, false)
                .addStateRemoveOld(EntityState.INACTION, true);
    }
}
