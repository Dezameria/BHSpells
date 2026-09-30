package net.offkung.bhspells.client.event;

import net.offkung.bhspells.network.savage_bite.SavageBiteNetwork;
import net.offkung.bhspells.network.savage_bite.SavageBiteStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SavageBiteClientEvents {
    private static final Map<UUID, ClientBiteState> CLIENT_SESSIONS = new ConcurrentHashMap<>();
    private static int lastInputSendTick = 0;
    private static float lastForward = 0.0F;
    private static float lastStrafe = 0.0F;

    public record ClientBiteState(UUID casterUuid, @Nullable UUID targetUuid, byte state) {}

    private SavageBiteClientEvents() {
    }

    public static void updateClientState(UUID casterUuid, @Nullable LivingEntity caster, @Nullable LivingEntity target, byte state) {
        if (state == SavageBiteStatePacket.STATE_END) {
            CLIENT_SESSIONS.remove(casterUuid);
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getUUID().equals(casterUuid)) {
                mc.gui.setOverlayMessage(Component.translatable("ui.bhspells.savage_bite_cancelled"), false);
            }
        } else {
            CLIENT_SESSIONS.put(casterUuid, new ClientBiteState(casterUuid, target != null ? target.getUUID() : null, state));
        }
    }

    public static boolean isLatched(LivingEntity entity) {
        ClientBiteState state = CLIENT_SESSIONS.get(entity.getUUID());
        return state != null && state.state() == SavageBiteStatePacket.STATE_LATCHED;
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        LocalPlayer player = (LocalPlayer) event.getEntity();
        ClientBiteState state = CLIENT_SESSIONS.get(player.getUUID());
        if (state == null) {
            return;
        }

        // Spacebar jump cancellation allowed in both LUNGING and LATCHED states
        if (event.getInput().jumping) {
            event.getInput().jumping = false;
            SavageBiteNetwork.sendInput(0.0F, 0.0F, true);
            return;
        }

        if (state.state() != SavageBiteStatePacket.STATE_LATCHED) {
            return;
        }

        float forward = event.getInput().forwardImpulse;
        float strafe = event.getInput().leftImpulse;

        // Bounded heartbeat (every 5 ticks) or on input delta
        int currentTicks = player.tickCount;
        boolean changed = Math.abs(forward - lastForward) > 0.05F || Math.abs(strafe - lastStrafe) > 0.05F;
        if (changed || (currentTicks - lastInputSendTick >= 5)) {
            lastForward = forward;
            lastStrafe = strafe;
            lastInputSendTick = currentTicks;
            SavageBiteNetwork.sendInput(forward, strafe, false);
        }
    }

    @SubscribeEvent
    public static void onClientLogout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        CLIENT_SESSIONS.clear();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            CLIENT_SESSIONS.clear();
        }
    }
}
