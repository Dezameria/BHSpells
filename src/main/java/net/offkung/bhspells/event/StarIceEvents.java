package net.offkung.bhspells.event;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.registry.MobEffectsRegistry;

@Mod.EventBusSubscriber
public class StarIceEvents {
    private static final String SPELL_ID = "bhspells:star_ice";
    public static final String CAST_TICK_TAG = "StarIceCastTick";
    public static final String AIRBORNE_TAG = "StarIceAirborne";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        if (!player.hasEffect(MobEffectsRegistry.STAR_ICE.get())) {
            player.getPersistentData().remove(CAST_TICK_TAG);
            player.getPersistentData().remove(AIRBORNE_TAG);
            return;
        }

        if (!player.getPersistentData().contains(CAST_TICK_TAG)) {
            player.getPersistentData().putInt(CAST_TICK_TAG, player.tickCount);
        }

        int castTick = player.getPersistentData().getInt(CAST_TICK_TAG);

        if (!player.onGround() || player.isFallFlying()) {
            player.getPersistentData().putBoolean(AIRBORNE_TAG, true);
        }

        // Flight enforcement: if the player has STAR_ICE, is not on ground,
        // but somehow lost the fall flying flag (e.g. updateFallFlying cleared it),
        // force re-engage flight immediately.
        if (!player.onGround() && !player.isFallFlying()) {
            player.setOnGround(false);
            player.startFallFlying();
        }

        // During the grace period, also enforce flight even if still "on ground"
        // (player might not have physically left the block yet after launch)
        boolean pastGracePeriod = (player.tickCount - castTick) > 20;
        if (!pastGracePeriod && !player.isFallFlying()) {
            player.setOnGround(false);
            player.startFallFlying();
        }

        // Landing check: when touching the ground after takeoff and grace period
        if (pastGracePeriod && player.onGround()) {
            player.removeEffect(MobEffectsRegistry.STAR_ICE.get());
            if (player.isFallFlying()) {
                player.stopFallFlying();
            }
            player.getPersistentData().remove(CAST_TICK_TAG);
            player.getPersistentData().remove(AIRBORNE_TAG);

            MagicData magicData = MagicData.getPlayerMagicData(player);
            if (magicData.getPlayerRecasts().hasRecastForSpell(SPELL_ID)) {
                var recast = magicData.getPlayerRecasts().getRecastInstance(SPELL_ID);
                if (recast != null) {
                    magicData.getPlayerRecasts().removeRecast(recast, RecastResult.USED_ALL_RECASTS);
                }
            }
            return;
        }

        // Flight check: while flying, apply/refresh STAR_ICE every 2 seconds (40 ticks)
        if (player.isFallFlying()) {
            if (player.tickCount % 40 == 0) {
                player.addEffect(new MobEffectInstance(MobEffectsRegistry.STAR_ICE.get(), 60, 0, false, false, true));
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.2, player.getZ(), 6, 0.2, 0.2, 0.2, 0.05);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && player.hasEffect(MobEffectsRegistry.STAR_ICE.get())) {
            if (event.getSource().is(DamageTypeTags.IS_FALL) || event.getSource().is(DamageTypes.FLY_INTO_WALL)) {
                event.setCanceled(true);
            }
        }
    }
}
