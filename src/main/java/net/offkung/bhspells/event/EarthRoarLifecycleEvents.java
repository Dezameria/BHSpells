package net.offkung.bhspells.event;

import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.service.EarthRoarDashManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BHSpells.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EarthRoarLifecycleEvents {
    private EarthRoarLifecycleEvents() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity != null && !entity.level().isClientSide() && EarthRoarDashManager.hasActiveDash(entity)) {
            EarthRoarDashManager.tickDash(entity);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        EarthRoarDashManager.cancelDash(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        EarthRoarDashManager.cancelDash(event.getEntity());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity != null) {
            EarthRoarDashManager.cancelDash(entity);
        }
    }
}
