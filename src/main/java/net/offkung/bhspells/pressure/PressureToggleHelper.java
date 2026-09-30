package net.offkung.bhspells.pressure;

import net.offkung.bhspells.pressure.client.ClientPressureManager;
import net.offkung.bhspells.pressure.server.ServerPressureManager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * Dedicated-server safe helper for checking toggle stance domain state across logical sides.
 */
public final class PressureToggleHelper {
    private PressureToggleHelper() {
    }

    public static boolean isDomainActive(Player player, String spellId) {
        if (player.level().isClientSide()) {
            return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> ClientPressureManager.hasActiveFieldByOwnerAndSpell(player.getUUID(), spellId));
        } else {
            return ServerPressureManager.hasActiveField(player.getUUID(), spellId);
        }
    }
}
