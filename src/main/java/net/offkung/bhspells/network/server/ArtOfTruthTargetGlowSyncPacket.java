package net.offkung.bhspells.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.offkung.bhspells.client.event.ArtOfTruthClientHandler;

import java.util.function.Supplier;

public class ArtOfTruthTargetGlowSyncPacket {
    private final int targetEntityId;
    private final boolean glowing;

    public ArtOfTruthTargetGlowSyncPacket(int targetEntityId, boolean glowing) {
        this.targetEntityId = targetEntityId;
        this.glowing = glowing;
    }

    public ArtOfTruthTargetGlowSyncPacket(FriendlyByteBuf buf) {
        this.targetEntityId = buf.readInt();
        this.glowing = buf.readBoolean();
    }

    public static void encode(ArtOfTruthTargetGlowSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.targetEntityId);
        buf.writeBoolean(msg.glowing);
    }

    public static void handle(ArtOfTruthTargetGlowSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(msg));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(ArtOfTruthTargetGlowSyncPacket msg) {
        ArtOfTruthClientHandler.setTargetGlowing(msg.targetEntityId, msg.glowing);
    }
}
