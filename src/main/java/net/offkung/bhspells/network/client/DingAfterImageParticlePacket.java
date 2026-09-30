package net.offkung.bhspells.network.client;

import net.offkung.bhspells.registry.ParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client packet equivalent to Wukong's DingAfterImageParticle packet. */
public record DingAfterImageParticlePacket(int entityId) {
    public static void encode(DingAfterImageParticlePacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entityId);
    }

    public static DingAfterImageParticlePacket decode(FriendlyByteBuf buffer) {
        return new DingAfterImageParticlePacket(buffer.readInt());
    }

    public static void handle(DingAfterImageParticlePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> spawn(packet.entityId)));
        context.setPacketHandled(true);
    }

    private static void spawn(int entityId) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (minecraft.player == null || level == null) {
            return;
        }
        Entity entity = level.getEntity(entityId);
        if (entity instanceof LivingEntity living) {
            level.addParticle(ParticleRegistry.DING.get(), entity.getX(),
                    entity.getY() + living.getBbHeight() + 1.0D, entity.getZ(),
                    Double.longBitsToDouble(entity.getId()), 1.0D, 0.0D);
        }
    }
}
