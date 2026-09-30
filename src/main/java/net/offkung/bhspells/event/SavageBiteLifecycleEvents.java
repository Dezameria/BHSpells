package net.offkung.bhspells.event;

import net.offkung.bhspells.service.SavageBiteManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SavageBiteLifecycleEvents {
    private SavageBiteLifecycleEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            SavageBiteManager.onServerTick();
        }
    }

    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.LivingEntity living) {
            if (SavageBiteManager.isCasterLatched(living) || SavageBiteManager.isTargetLatched(living)) {
                double dx = event.getTargetX() - living.getX();
                double dy = event.getTargetY() - living.getY();
                double dz = event.getTargetZ() - living.getZ();
                if ((dx * dx + dy * dy + dz * dz) > 256.0D) {
                    SavageBiteManager.cancelByEntity(living, "teleported");
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SavageBiteManager.cancelByEntity(event.getEntity(), "logged_out");
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        SavageBiteManager.cleanupOnServerStopping();
    }
}
