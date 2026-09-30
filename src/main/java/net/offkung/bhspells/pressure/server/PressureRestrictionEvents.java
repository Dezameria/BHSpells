package net.offkung.bhspells.pressure.server;

import net.offkung.bhspells.BHSpells;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BHSpells.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PressureRestrictionEvents {
    private PressureRestrictionEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            ServerPressureManager.tickLevel(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (ServerPressureManager.isRooted(entity)) {
            Vec3 delta = entity.getDeltaMovement();
            entity.setDeltaMovement(delta.x, Math.min(0.0D, delta.y), delta.z);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity living) {
            if (!ServerPressureManager.isRooted(living)) {
                living.removeTag(ServerPressureManager.TAG_ROOTED);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity != null) {
            ServerPressureManager.stopAllByOwner(entity.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerPressureManager.syncToPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerPressureManager.onPlayerLoggedOut(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerPressureManager.stopAllByOwner(player.getUUID());
            ServerPressureManager.syncToPlayer(player);
        }
    }
}
