package net.offkung.bhspells.compat.avalon.particle;

import com.merlin204.avalon.network.NetworkHandler;
import com.merlin204.avalon.network.server.ShakeCameraPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.offkung.bhspells.compat.api.CompatResult;
import net.offkung.bhspells.compat.api.VfxCue;
import net.offkung.bhspells.compat.api.VfxRequest;

public final class AvalonVfx {
    private AvalonVfx() {
    }

    public static CompatResult spawnVfx(VfxRequest request) {
        if (request == null || request.level().isClientSide) {
            return CompatResult.UNSUPPORTED;
        }

        if (request.cue() == VfxCue.AVALON_ENERGY) {
            if (request.source() instanceof Player player) {
                try {
                    NetworkHandler.sendToClient(new ShakeCameraPacket(10, 0.4F, 1.2F, player.position(), 16.0F), (ServerPlayer) player);
                    return CompatResult.APPLIED;
                } catch (Throwable ignored) {
                }
            }
        }

        return CompatResult.UNSUPPORTED;
    }
}
