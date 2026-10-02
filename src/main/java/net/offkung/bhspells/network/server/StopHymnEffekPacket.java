package net.offkung.bhspells.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.offkung.bhspells.client.event.HymnofPurificationClientEvents;

import java.util.function.Supplier;

public class StopHymnEffekPacket {
    private final int casterEntityId;

    public StopHymnEffekPacket(int casterEntityId) {
        this.casterEntityId = casterEntityId;
    }

    public StopHymnEffekPacket(FriendlyByteBuf buf) {
        this.casterEntityId = buf.readInt();
    }

    public static void encode(StopHymnEffekPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.casterEntityId);
    }

    public static void handle(StopHymnEffekPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(msg));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(StopHymnEffekPacket msg) {
        HymnofPurificationClientEvents.stopClientEffek(msg.casterEntityId);
    }
}
