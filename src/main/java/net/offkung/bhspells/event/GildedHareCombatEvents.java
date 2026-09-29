package net.offkung.bhspells.event;

import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.client.particle.GildedHareVfx;
import net.offkung.bhspells.effect.GildedHareEffect;
import net.offkung.bhspells.effect.GildedHareMarkEffect;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhspells.spells.gold.GildedHareSpell;

@Mod.EventBusSubscriber
public class GildedHareCombatEvents {
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getAmount() <= 0.0F) {
            return;
        }

        DamageSource source = event.getSource();
        LivingEntity victim = event.getEntity();
        Entity directEntity = source.getDirectEntity();
        Entity attackerEntity = source.getEntity();

        if (!(attackerEntity instanceof LivingEntity attacker) || directEntity != attacker) {
            return;
        }

        if (source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.is(DamageTypeTags.BYPASSES_ARMOR)
                || source.is(DamageTypes.THORNS)
                || source.is(DamageTypes.MAGIC)) {
            return;
        }

        if (!attacker.hasEffect(MobEffectsRegistry.GILDED_HARE.get())) {
            return;
        }

        if (victim == attacker || !victim.isAlive() || DamageSources.isFriendlyFireBetween(attacker, victim)) {
            return;
        }

        long finisherCooldown = victim.getPersistentData().getLong(GildedHareMarkEffect.FINISHER_COOLDOWN_TICK_TAG);
        if (victim.level().getGameTime() < finisherCooldown) {
            if (victim.level() instanceof ServerLevel serverLevel) {
                GildedHareVfx.spawnKickImpactVfx(serverLevel, attacker, victim, 1);
            }
            return;
        }

        var activeMark = victim.getEffect(MobEffectsRegistry.GILDED_HARE_MARK.get());
        if (activeMark != null && activeMark.getAmplifier() >= 4) {
            if (victim.level() instanceof ServerLevel serverLevel) {
                GildedHareVfx.spawnKickImpactVfx(serverLevel, attacker, victim, 1);
            }
            return;
        }

        String storedOwner = victim.getPersistentData().getString(GildedHareMarkEffect.OWNER_UUID_TAG);
        String attackerUUID = attacker.getStringUUID();
        String lastTargetUUID = attacker.getPersistentData().getString(GildedHareEffect.CASTER_ACTIVE_TARGET_TAG);
        String victimUUID = victim.getStringUUID();

        boolean isOwnerMatch = attackerUUID.equals(storedOwner) || (storedOwner.isEmpty() && victimUUID.equals(lastTargetUUID));
        boolean isSameTarget = victimUUID.equals(lastTargetUUID);
        boolean hasActiveMark = activeMark != null;

        int currentCombo = victim.getPersistentData().getInt(GildedHareMarkEffect.COMBO_COUNT_TAG);
        if (currentCombo <= 0 && activeMark != null) {
            currentCombo = activeMark.getAmplifier() + 1;
        }

        int nextCombo = (isOwnerMatch && isSameTarget && hasActiveMark) ? currentCombo + 1 : 1;

        if (nextCombo < 5) {
            attacker.getPersistentData().putString(GildedHareEffect.CASTER_ACTIVE_TARGET_TAG, victimUUID);

            victim.removeEffect(MobEffectsRegistry.GILDED_HARE_MARK.get());

            victim.getPersistentData().putString(GildedHareMarkEffect.OWNER_UUID_TAG, attackerUUID);
            victim.getPersistentData().putInt(GildedHareMarkEffect.LAST_HIT_TICK_TAG, victim.tickCount);
            victim.getPersistentData().putInt(GildedHareMarkEffect.COMBO_COUNT_TAG, nextCombo);

            victim.addEffect(new MobEffectInstance(MobEffectsRegistry.GILDED_HARE_MARK.get(), GildedHareSpell.COMBO_WINDOW_TICKS, nextCombo - 1, false, false, true));

            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, GildedHareSpell.SLOWNESS_DURATION_TICKS, 0, false, false, true));

            if (victim.level() instanceof ServerLevel serverLevel) {
                GildedHareVfx.spawnKickImpactVfx(serverLevel, attacker, victim, nextCombo);
            }
        } else {
            attacker.getPersistentData().remove(GildedHareEffect.CASTER_ACTIVE_TARGET_TAG);
            GildedHareMarkEffect.clearComboData(victim);
            victim.getPersistentData().putLong(GildedHareMarkEffect.FINISHER_COOLDOWN_TICK_TAG, victim.level().getGameTime() + GildedHareSpell.FINISHER_COOLDOWN_TICKS);

            victim.removeEffect(MobEffectsRegistry.GILDED_HARE_MARK.get());

            victim.addEffect(new MobEffectInstance(MobEffectsRegistry.GILDED_HARE_MARK.get(), GildedHareSpell.STUN_DURATION_TICKS, 4, false, false, true));

            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, GildedHareSpell.STUN_DURATION_TICKS, 255, false, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, GildedHareSpell.STUN_DURATION_TICKS, 255, false, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.JUMP, GildedHareSpell.STUN_DURATION_TICKS, 200, false, false, false));
            victim.setDeltaMovement(0, 0, 0);
            victim.hurtMarked = true;

            victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, GildedHareSpell.BLINDNESS_DURATION_TICKS, 0, false, false, true));

            if (victim.level() instanceof ServerLevel serverLevel) {
                GildedHareVfx.spawnCocoonStunBurst(serverLevel, victim);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        var mark = entity.getEffect(MobEffectsRegistry.GILDED_HARE_MARK.get());
        if (mark != null && mark.getAmplifier() >= 4) {
            event.setCanceled(true);
            entity.setDeltaMovement(0, 0, 0);
            entity.hurtMarked = true;
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getPersistentData().contains(GildedHareEffect.CASTER_ACTIVE_TARGET_TAG)) {
            entity.getPersistentData().remove(GildedHareEffect.CASTER_ACTIVE_TARGET_TAG);
        }
        if (entity.getPersistentData().contains(GildedHareMarkEffect.OWNER_UUID_TAG) || entity.getPersistentData().contains(GildedHareMarkEffect.COMBO_COUNT_TAG)) {
            GildedHareMarkEffect.clearComboData(entity);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        cleanupPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        cleanupPlayer(event.getEntity());
    }

    private static void cleanupPlayer(Player player) {
        if (player.getPersistentData().contains(GildedHareEffect.CASTER_ACTIVE_TARGET_TAG)) {
            player.getPersistentData().remove(GildedHareEffect.CASTER_ACTIVE_TARGET_TAG);
        }
        if (player.getPersistentData().contains(GildedHareMarkEffect.OWNER_UUID_TAG) || player.getPersistentData().contains(GildedHareMarkEffect.COMBO_COUNT_TAG)) {
            GildedHareMarkEffect.clearComboData(player);
        }
    }
}
