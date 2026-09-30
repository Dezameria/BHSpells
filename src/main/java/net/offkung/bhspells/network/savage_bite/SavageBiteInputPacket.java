package net.offkung.bhspells.network.savage_bite;

import net.offkung.bhspells.service.SavageBiteManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client-to-Server packet conveying movement steering intent or jump cancellation.
 */
public class SavageBiteInputPacket {
    private final float forward;
    private final float strafe;
    private final boolean cancelRequested;

    public SavageBiteInputPacket(float forward, float strafe, boolean cancelRequested) {
        float safeForward = Float.isFinite(forward) ? forward : 0.0F;
        float safeStrafe = Float.isFinite(strafe) ? strafe : 0.0F;
        this.forward = Mth.clamp(safeForward, -1.0F, 1.0F);
        this.strafe = Mth.clamp(safeStrafe, -1.0F, 1.0F);
        this.cancelRequested = cancelRequested;
    }

    public static void encode(SavageBiteInputPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.forward);
        buf.writeFloat(msg.strafe);
        buf.writeBoolean(msg.cancelRequested);
    }

    public static SavageBiteInputPacket decode(FriendlyByteBuf buf) {
        return new SavageBiteInputPacket(buf.readFloat(), buf.readFloat(), buf.readBoolean());
    }

    public static void handle(SavageBiteInputPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                return;
            }
            if (msg.cancelRequested) {
                SavageBiteManager.cancelByCaster(sender, "jump_cancel");
            } else {
                SavageBiteManager.updateSteeringInput(sender, msg.forward, msg.strafe);
            }
        });
        ctx.setPacketHandled(true);
    }
}
