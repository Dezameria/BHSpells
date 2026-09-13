package net.offkung.bhspells.event;

import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.effect.DemonicSpinEffect;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import yesman.epicfight.api.forgeevent.EntityStunEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mod.EventBusSubscriber
public class DemonicSpinEvents {
    private static final float DAMAGE_REDUCTION_MULTIPLIER = 0.60f; // 40% reduction

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!target.hasEffect(MobEffectsRegistry.DEMONIC_SPIN.get())) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)) {
            return;
        }
        event.setAmount(event.getAmount() * DAMAGE_REDUCTION_MULTIPLIER);
    }

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (event.getEntity().hasEffect(MobEffectsRegistry.DEMONIC_SPIN.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        if (!target.hasEffect(MobEffectsRegistry.DEMONIC_SPIN.get())) {
            return;
        }
        if (event.getSource().getDirectEntity() instanceof Projectile projectile) {
            CompoundTag tag = projectile.getPersistentData();
            if (!tag.getBoolean(DemonicSpinEffect.PROJECTILE_CHECKED_TAG)) {
                tag.putBoolean(DemonicSpinEffect.PROJECTILE_CHECKED_TAG, true);
                if (target.getRandom().nextFloat() < DemonicSpinEffect.PROJECTILE_DISCARD_CHANCE) {
                    if (target.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(EpicFightParticles.HIT_BLUNT.get(), projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0, 0, 0, 0);
                        serverLevel.playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(), SoundRegistry.FORCE_IMPACT.get(), SoundSource.PLAYERS, 0.8f, 0.8f);
                    }
                    projectile.discard();
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect() == MobEffectsRegistry.DEMONIC_SPIN.get()) {
            stopSpinAnimation(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        if (event.getEffect() == MobEffectsRegistry.DEMONIC_SPIN.get()) {
            stopSpinAnimation(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().hasEffect(MobEffectsRegistry.DEMONIC_SPIN.get())) {
            stopSpinAnimation(event.getEntity());
        }
    }

    public static void stopSpinAnimation(LivingEntity entity) {
        if (entity.level().isClientSide) {
            return;
        }
        LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
        if (entityPatch != null) {
            entityPatch.playAnimationSynchronized(Animations.OFF_ANIMATION_HIGHEST, 0.0f);
        }
    }
}
