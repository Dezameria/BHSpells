package net.offkung.bhspells.network.client;

import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.offkung.bhspells.entity.spells.six_petal_waltz.PetalWaltzSword;
import net.offkung.bhspells.event.SwordDashManager;

import java.util.function.Supplier;

public class SwordDashPacket {
    private final int swordEntityId;

    public SwordDashPacket(int swordEntityId) {
        this.swordEntityId = swordEntityId;
    }

    public SwordDashPacket(FriendlyByteBuf buf) {
        this.swordEntityId = buf.readInt();
    }

    public static void encode(SwordDashPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.swordEntityId);
    }

    public static void handle(SwordDashPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (player.level().getEntity(msg.swordEntityId) instanceof PetalWaltzSword sword) {
                if (sword.isOwnedBy(player) && !sword.isActivated() && !sword.isReturning()) {
                    if (!SwordDashManager.isDashing(player)) {
                        if (Utils.hasLineOfSight(player.level(), player, sword, true)) {
                            sword.activate();
                            SwordDashManager.startDash(player, sword);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
