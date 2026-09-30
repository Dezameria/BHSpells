package net.offkung.bhspells.pressure.network;

import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.pressure.PressureFieldData;
import net.offkung.bhspells.pressure.PressureReaction;
import net.offkung.bhspells.pressure.server.ServerPressureField;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.UUID;

public final class PressureNetwork {
    private static final String PROTOCOL_VERSION = "2";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            BHSpells.id("pressure"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private PressureNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                0,
                PressureFieldStartPacket.class,
                PressureFieldStartPacket::encode,
                PressureFieldStartPacket::decode,
                PressureFieldStartPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                1,
                PressureFieldEndPacket.class,
                PressureFieldEndPacket::encode,
                PressureFieldEndPacket::decode,
                PressureFieldEndPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                2,
                PressureReactionPacket.class,
                PressureReactionPacket::encode,
                PressureReactionPacket::decode,
                PressureReactionPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                3,
                PressureLightningStrikePacket.class,
                PressureLightningStrikePacket::encode,
                PressureLightningStrikePacket::decode,
                PressureLightningStrikePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void broadcastStartField(ServerPressureField field) {
        Vec3 center = field.getCurrentCenter();
        CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        center.x, center.y, center.z,
                        field.getData().radius() + 96.0D,
                        field.getLevel().dimension()
                )),
                new PressureFieldStartPacket(field.getData())
        );
    }

    public static void sendStartFieldToPlayer(ServerPlayer player, PressureFieldData data) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PressureFieldStartPacket(data));
    }

    public static void sendEndFieldToPlayer(ServerPlayer player, UUID fieldId) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PressureFieldEndPacket(fieldId));
    }

    public static void broadcastEndField(ServerLevel level, UUID fieldId, Vec3 center, float radius) {
        CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        center.x, center.y, center.z,
                        radius + 96.0D,
                        level.dimension()
                )),
                new PressureFieldEndPacket(fieldId)
        );
    }

    public static void broadcastReaction(LivingEntity entity, PressureReaction reaction) {
        CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new PressureReactionPacket(entity.getId(), reaction)
        );
    }

    public static void broadcastLightningStrike(ServerLevel level, Vec3 pos, int color, double radius) {
        CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        pos.x, pos.y, pos.z,
                        radius + 64.0D,
                        level.dimension()
                )),
                new PressureLightningStrikePacket(pos, color)
        );
    }
}
