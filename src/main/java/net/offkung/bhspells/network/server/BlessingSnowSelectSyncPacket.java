package net.offkung.bhspells.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.offkung.bhspells.client.event.BlessingSnowClientHandler;

import java.util.function.Supplier;

public class BlessingSnowSelectSyncPacket {
    private final boolean active;
    private final int radius;

    public BlessingSnowSelectSyncPacket(boolean active, int radius) {
        this.active = active;
        this.radius = radius;
    }

    public BlessingSnowSelectSyncPacket(FriendlyByteBuf buf) {
        this.active = buf.readBoolean();
        this.radius = buf.readInt();
    }

    public static void encode(BlessingSnowSelectSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.active);
        buf.writeInt(msg.radius);
    }

    public static void handle(BlessingSnowSelectSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(msg));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(BlessingSnowSelectSyncPacket msg) {
        BlessingSnowClientHandler.setSelectingRadius(msg.active, msg.radius);
    }
}
