package net.offkung.bhspells.network.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.offkung.bhspells.entity.spells.jade_cluster.JadeClusterEntity;

import java.util.function.Supplier;

public class ExplodeJadeClusterPacket {
    private final int clusterEntityId;

    public ExplodeJadeClusterPacket(int clusterEntityId) {
        this.clusterEntityId = clusterEntityId;
    }

    public ExplodeJadeClusterPacket(FriendlyByteBuf buf) {
        this.clusterEntityId = buf.readInt();
    }

    public static void encode(ExplodeJadeClusterPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.clusterEntityId);
    }

    public static void handle(ExplodeJadeClusterPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (player.level().getEntity(msg.clusterEntityId) instanceof JadeClusterEntity cluster) {
                if (cluster.isAlive() && cluster.isOwnedBy(player)) {
                    if (player.distanceToSqr(cluster) <= 55.0 * 55.0) {
                        cluster.explode();
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
