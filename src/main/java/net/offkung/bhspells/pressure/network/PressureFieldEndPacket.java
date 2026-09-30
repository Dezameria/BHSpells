package net.offkung.bhspells.pressure.network;

import net.offkung.bhspells.pressure.client.ClientPressureManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet sent from server to clients when a spiritual pressure field expires or is cancelled.
 */
public final class PressureFieldEndPacket {
    private final UUID fieldId;

    public PressureFieldEndPacket(UUID fieldId) {
        this.fieldId = fieldId;
    }

    public static void encode(PressureFieldEndPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.fieldId);
    }

    public static PressureFieldEndPacket decode(FriendlyByteBuf buf) {
        return new PressureFieldEndPacket(buf.readUUID());
    }

    public static void handle(PressureFieldEndPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPressureManager.removeField(packet.fieldId)));
        context.setPacketHandled(true);
    }
}
