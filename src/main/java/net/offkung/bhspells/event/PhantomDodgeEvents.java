package net.offkung.bhspells.event;

import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.compat.epicfight.EpicFightCompat;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server-authoritative damage interception and charge consumption for Phantom Dodge.
 */
@Mod.EventBusSubscriber(modid = BHSpells.MODID)
public final class PhantomDodgeEvents {
    private PhantomDodgeEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.isCanceled()) {
            return;
        }

        // Do not intercept damage types that bypass invulnerability (e.g. void / kill)
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        LivingEntity target = event.getEntity();
        if (target == null || target.level().isClientSide) {
            return;
        }

        MobEffectInstance effect = target.getEffect(MobEffectsRegistry.PHANTOM_DODGE.get());
        if (effect == null) {
            return;
        }

        // Authoritatively negate the attack
        event.setCanceled(true);

        int currentAmp = effect.getAmplifier();
        int remainingDuration = effect.getDuration();

        // Consume one charge
        if (currentAmp > 0) {
            target.removeEffect(MobEffectsRegistry.PHANTOM_DODGE.get());
            target.addEffect(new MobEffectInstance(MobEffectsRegistry.PHANTOM_DODGE.get(), remainingDuration, currentAmp - 1, false, false, true));
        } else {
            target.removeEffect(MobEffectsRegistry.PHANTOM_DODGE.get());
        }

        // Trigger Epic Fight dodge animation + afterimage
        EpicFightCompat.triggerPhantomDodge(target);

        // Play subtle evasive whoosh feedback
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.4F);
    }
}
