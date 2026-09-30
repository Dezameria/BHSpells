package net.offkung.bhspells.network.savage_bite;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server-to-Client packet syncing Savage Bite lifecycle state.
 */
public class SavageBiteStatePacket {
    public static final byte STATE_END = 0;
    public static final byte STATE_LUNGING = 1;
    public static final byte STATE_LATCHED = 2;

    private final java.util.UUID casterUuid;
    private final int casterId;
    private final int targetId;
    private final byte state;

    public SavageBiteStatePacket(java.util.UUID casterUuid, int casterId, int targetId, byte state) {
        this.casterUuid = casterUuid;
        this.casterId = casterId;
        this.targetId = targetId;
        this.state = state;
    }

    public static void encode(SavageBiteStatePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.casterUuid);
        buf.writeInt(msg.casterId);
        buf.writeInt(msg.targetId);
        buf.writeByte(msg.state);
    }

    public static SavageBiteStatePacket decode(FriendlyByteBuf buf) {
        return new SavageBiteStatePacket(buf.readUUID(), buf.readInt(), buf.readInt(), buf.readByte());
    }

    public static void handle(SavageBiteStatePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg));
        });
        ctx.setPacketHandled(true);
    }

    private static void handleClient(SavageBiteStatePacket msg) {
        Minecraft mc = Minecraft.getInstance();
        LivingEntity livingCaster = null;
        LivingEntity livingTarget = null;
        if (mc.level != null) {
            Entity caster = mc.level.getEntity(msg.casterId);
            if (caster instanceof LivingEntity lc) {
                livingCaster = lc;
            }
            Entity target = mc.level.getEntity(msg.targetId);
            if (target instanceof LivingEntity lt) {
                livingTarget = lt;
            }
        }

        net.offkung.bhspells.client.event.SavageBiteClientEvents.updateClientState(
                msg.casterUuid,
                livingCaster,
                livingTarget,
                msg.state
        );
    }
}
