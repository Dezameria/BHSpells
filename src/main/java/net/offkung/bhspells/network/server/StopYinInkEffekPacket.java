package net.offkung.bhspells.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.offkung.bhspells.client.event.YinInkCascadeClientEvents;

import java.util.function.Supplier;

public class StopYinInkEffekPacket {
    private final int casterEntityId;

    public StopYinInkEffekPacket(int casterEntityId) {
        this.casterEntityId = casterEntityId;
    }

    public StopYinInkEffekPacket(FriendlyByteBuf buf) {
        this.casterEntityId = buf.readInt();
    }

    public static void encode(StopYinInkEffekPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.casterEntityId);
    }

    public static void handle(StopYinInkEffekPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(msg));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(StopYinInkEffekPacket msg) {
        YinInkCascadeClientEvents.stopClientEffek(msg.casterEntityId);
    }
}
