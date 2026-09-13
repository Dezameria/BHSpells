package net.offkung.bhspells.network.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.offkung.bhspells.entity.spells.blessing_snow.RadiusSnowRingEntity;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.network.server.BlessingSnowTargetGlowSyncPacket;

import java.util.List;
import java.util.function.Supplier;

public class BlessingSnowTargetSelectPacket {
    private final int targetEntityId;

    public BlessingSnowTargetSelectPacket(int targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    public BlessingSnowTargetSelectPacket(FriendlyByteBuf buf) {
        this.targetEntityId = buf.readInt();
    }

    public static void encode(BlessingSnowTargetSelectPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.targetEntityId);
    }

    public static void handle(BlessingSnowTargetSelectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            List<RadiusSnowRingEntity> rings = player.level().getEntitiesOfClass(
                    RadiusSnowRingEntity.class,
                    player.getBoundingBox().inflate(35.0),
                    ring -> ring.getOwner() == player && !ring.isRemoved()
            );

            if (rings.isEmpty()) return;

            RadiusSnowRingEntity ring = rings.get(0);
            Entity target = player.level().getEntity(msg.targetEntityId);

            if (target instanceof LivingEntity livingTarget && livingTarget != player && livingTarget.isAlive()) {
                double distance = player.distanceTo(livingTarget);
                if (distance <= ring.getRadius()) {
                    if (ring.hasHealTarget(livingTarget.getUUID())) {
                        ring.removeHealTarget(livingTarget.getUUID());
                        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new BlessingSnowTargetGlowSyncPacket(msg.targetEntityId, false));
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.PLAYERS, 0.6f, 0.8f);
                        player.displayClientMessage(Component.literal("§cDeselected " + livingTarget.getDisplayName().getString() + "!"), true);
                    } else {
                        // Select target
                        ring.addHealTarget(livingTarget, distance);
                        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new BlessingSnowTargetGlowSyncPacket(msg.targetEntityId, true));
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.2f);
                        player.displayClientMessage(Component.literal("§aSelected " + livingTarget.getDisplayName().getString() + " for healing!"), true);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
