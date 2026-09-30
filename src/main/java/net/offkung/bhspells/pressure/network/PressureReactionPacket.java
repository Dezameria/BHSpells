package net.offkung.bhspells.pressure.network;

import net.offkung.bhspells.pressure.PressureReaction;
import net.offkung.bhspells.pressure.client.ClientPressureManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Dispatched only when an entity's pressure reaction state transitions (e.g. NONE -> STAGGER -> KNEEL).
 */
public final class PressureReactionPacket {
    private final int entityId;
    private final byte reactionOrdinal;

    public PressureReactionPacket(int entityId, PressureReaction reaction) {
        this.entityId = entityId;
        this.reactionOrdinal = (byte) reaction.ordinal();
    }

    private PressureReactionPacket(int entityId, byte reactionOrdinal) {
        this.entityId = entityId;
        this.reactionOrdinal = reactionOrdinal;
    }

    public static void encode(PressureReactionPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId);
        buf.writeByte(packet.reactionOrdinal);
    }

    public static PressureReactionPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        byte reactionOrdinal = buf.readByte();
        return new PressureReactionPacket(entityId, reactionOrdinal);
    }

    public static void handle(PressureReactionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> {
                    PressureReaction[] values = PressureReaction.values();
                    int ordinal = packet.reactionOrdinal & 0xFF;
                    PressureReaction reaction = (ordinal < values.length) ? values[ordinal] : PressureReaction.NONE;
                    ClientPressureManager.setEntityReaction(packet.entityId, reaction);
                }));
        context.setPacketHandled(true);
    }
}
