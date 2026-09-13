package net.offkung.bhspells.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.offkung.bhspells.client.event.BlessingSnowClientHandler;

import java.util.function.Supplier;

public class BlessingSnowTargetGlowSyncPacket {
    private final int targetEntityId;
    private final boolean glowing;

    public BlessingSnowTargetGlowSyncPacket(int targetEntityId, boolean glowing) {
        this.targetEntityId = targetEntityId;
        this.glowing = glowing;
    }

    public BlessingSnowTargetGlowSyncPacket(FriendlyByteBuf buf) {
        this.targetEntityId = buf.readInt();
        this.glowing = buf.readBoolean();
    }

    public static void encode(BlessingSnowTargetGlowSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.targetEntityId);
        buf.writeBoolean(msg.glowing);
    }

    public static void handle(BlessingSnowTargetGlowSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(msg));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(BlessingSnowTargetGlowSyncPacket msg) {
        BlessingSnowClientHandler.setTargetGlowing(msg.targetEntityId, msg.glowing);
    }
}
