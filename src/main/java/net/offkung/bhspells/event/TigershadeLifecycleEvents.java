package net.offkung.bhspells.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.effect.TigershadeMarkEffect;
import net.offkung.bhspells.effect.TigershadeStanceEffect;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhspells.spells.ground.TigershadeTerrabreakSpell;

@Mod.EventBusSubscriber
public class TigershadeLifecycleEvents {
    private TigershadeLifecycleEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TigershadeTerrabreakSpell.syncHuntTarget(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            TigershadeTerrabreakSpell.tickActiveSlamDash(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        clearPlayerLinks(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        clearPlayerLinks(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity deceased = event.getEntity();
        TigershadeTerrabreakSpell.cancelActiveSlamDash(deceased);
        if (deceased.hasEffect(MobEffectsRegistry.TIGERSHADE_STANCE.get()) || deceased.getPersistentData().contains(TigershadeStanceEffect.TARGET_UUID_TAG)) {
            TigershadeTerrabreakSpell.clearHunt(deceased);
        }
        if (deceased.hasEffect(MobEffectsRegistry.TIGERSHADE_MARK.get()) || deceased.getPersistentData().contains(TigershadeMarkEffect.MARK_CASTER_UUID_TAG)) {
            TigershadeTerrabreakSpell.onMarkRemoved(deceased);
        }
    }

    private static void clearPlayerLinks(LivingEntity player) {
        TigershadeTerrabreakSpell.cancelActiveSlamDash(player);
        if (player.hasEffect(MobEffectsRegistry.TIGERSHADE_MARK.get()) || player.getPersistentData().contains(TigershadeMarkEffect.MARK_CASTER_UUID_TAG)) {
            TigershadeTerrabreakSpell.onMarkRemoved(player);
        }
        if (player.hasEffect(MobEffectsRegistry.TIGERSHADE_STANCE.get()) || player.getPersistentData().contains(TigershadeStanceEffect.TARGET_UUID_TAG)) {
            TigershadeTerrabreakSpell.clearHunt(player);
        }
    }
}
