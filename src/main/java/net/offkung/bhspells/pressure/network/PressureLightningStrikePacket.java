package net.offkung.bhspells.pressure.network;

import net.offkung.bhspells.pressure.client.ClientPressureManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from server to nearby clients when a Tempest Reiatsu lightning strike occurs.
 */
public final class PressureLightningStrikePacket {
    private final Vec3 position;
    private final int color;

    public PressureLightningStrikePacket(Vec3 position, int color) {
        this.position = position;
        this.color = color;
    }

    public static void encode(PressureLightningStrikePacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.position.x);
        buf.writeDouble(packet.position.y);
        buf.writeDouble(packet.position.z);
        buf.writeInt(packet.color);
    }

    public static PressureLightningStrikePacket decode(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        int color = buf.readInt();
        return new PressureLightningStrikePacket(new Vec3(x, y, z), color);
    }

    public static void handle(PressureLightningStrikePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientPressureManager.handleLightningStrike(packet.position, packet.color);
        }));
        ctx.setPacketHandled(true);
    }
}
