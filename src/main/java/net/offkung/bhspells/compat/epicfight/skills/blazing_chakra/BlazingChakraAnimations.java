package net.offkung.bhspells.compat.epicfight.skills.blazing_chakra;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.offkung.bhspells.compat.CompatMods;
import net.offkung.bhspells.compat.epicfight.common.particle.AfterimageVfx;
import net.offkung.bhspells.compat.epicfight.common.particle.FractureVfx;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.damagesource.StunType;

public final class BlazingChakraAnimations {
    public static final String PATH_BLAZING_CHAKRA = "biped/spells/blazing_chakra";

    private BlazingChakraAnimations() {
    }

    public static AnimationManager.AnimationAccessor<AttackAnimation> registerBlazingChakra(AnimationManager.AnimationBuilder builder) {
        return registerBlazingChakra(builder, PATH_BLAZING_CHAKRA);
    }

    public static AnimationManager.AnimationAccessor<AttackAnimation> registerBlazingChakra(AnimationManager.AnimationBuilder builder, String path) {
        if (CompatMods.isAvalonLoaded()) {
            return builder.nextAccessor(path, AvalonBlazingChakraAnimationBuilder::buildBlazingChakra);
        }

        return builder.nextAccessor(path, accessor ->
                new AttackAnimation(
                        0.1F,
                        59.0F / 60.0F,
                        70.0F / 60.0F,
                        100.0F / 60.0F,
                        2.0F,
                        InteractionHand.MAIN_HAND,
                        BlazingChakraColliders.IMPACT,
                        Armatures.BIPED.get().rootJoint,
                        accessor,
                        Armatures.BIPED
                )
                        .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                        .addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_BIG.get())
                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
                        .addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLADE)
                        .addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100.0F))
                        .addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(10.0F))
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (anim, patch, speed, prev, next) -> Mth.clamp(speed, 0.85F, 1.45F))
                        .addEvents(
                                AnimationEvent.InTimeEvent.create(0.0F, (patch, anim, params) -> {
                                    LivingEntity entity = patch.getOriginal();
                                    AfterimageVfx.spawnWhiteAfterimage(entity.level(), entity);
                                }, AnimationEvent.Side.CLIENT),

                                AnimationEvent.InTimeEvent.create(0.0F, (patch, anim, params) -> {
                                    LivingEntity entity = patch.getOriginal();
                                    entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 5, false, false, false));
                                }, AnimationEvent.Side.BOTH),

                                AnimationEvent.InTimeEvent.create(0.2F, (patch, anim, params) -> {
                                    LivingEntity entity = patch.getOriginal();
                                    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.5F, 1.0F);
                                }, AnimationEvent.Side.SERVER),

                                AnimationEvent.InTimeEvent.create(57.0F / 60.0F, (patch, anim, params) -> {
                                    LivingEntity entity = patch.getOriginal();
                                    FractureVfx.trySpawnFracture(entity, entity.level(), entity.position(), 2, 4, 5.0D);
                                }, AnimationEvent.Side.SERVER),

                                AnimationEvent.InTimeEvent.create(59.0F / 60.0F, (patch, anim, params) -> {
                                    LivingEntity entity = patch.getOriginal();
                                    FractureVfx.triggerCameraShake(entity, 60, 4.0F);
                                }, AnimationEvent.Side.SERVER),

                                AnimationEvent.InTimeEvent.create(1.1F, (patch, anim, params) -> {
                                    BlazingChakraVfx.spawnImpactClientVfx(patch);
                                }, AnimationEvent.Side.CLIENT),

                                AnimationEvent.InTimeEvent.create(1.1F, (patch, anim, params) -> {
                                    LivingEntity entity = patch.getOriginal();
                                    if (entity.level() instanceof ServerLevel level) {
                                        BlazingChakraVfx.applyShockwaveDamage(level, entity, entity.position(), BlazingChakraVfx.MAX_RANGE);
                                    }
                                }, AnimationEvent.Side.SERVER),

                                AnimationEvent.InTimeEvent.create(1.1F, (patch, anim, params) -> {
                                    LivingEntity entity = patch.getOriginal();
                                    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5F, 1.0F);
                                }, AnimationEvent.Side.SERVER),

                                // 10. 1.85s SERVER: Secondary expanding 4-ring spiral shockwave
                                AnimationEvent.InTimeEvent.create(1.85F, (patch, anim, params) -> {
                                    LivingEntity entity = patch.getOriginal();
                                    if (entity.level() instanceof ServerLevel level) {
                                        BlazingChakraVfx.spawnSpiralShockwave(level, entity);
                                    }
                                }, AnimationEvent.Side.SERVER)
                        )
                        .addStateRemoveOld(EntityState.MOVEMENT_LOCKED, true)
                        .addStateRemoveOld(EntityState.CAN_SWITCH_HAND_ITEM, false)
                        .addStateRemoveOld(EntityState.INACTION, true)
        );
    }
}
