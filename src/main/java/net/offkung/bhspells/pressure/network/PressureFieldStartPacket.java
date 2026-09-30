package net.offkung.bhspells.pressure.network;

import net.offkung.bhspells.pressure.PressureAnchor;
import net.offkung.bhspells.pressure.PressureFieldData;
import net.offkung.bhspells.pressure.PressureVisualProfile;
import net.offkung.bhspells.pressure.client.ClientPressureManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet sent from server to clients when a new spiritual pressure field begins or needs syncing.
 */
public final class PressureFieldStartPacket {
    private final PressureFieldData fieldData;

    public PressureFieldStartPacket(PressureFieldData fieldData) {
        this.fieldData = fieldData;
    }

    public static void encode(PressureFieldStartPacket packet, FriendlyByteBuf buf) {
        PressureFieldData data = packet.fieldData;
        buf.writeUUID(data.fieldId());
        buf.writeUUID(data.ownerUuid());
        buf.writeUtf(data.sourceSpellId() != null ? data.sourceSpellId() : "bhspells:spiritual_pressure");
        buf.writeVarInt(data.spellLevel());

        boolean isFollowing = data.anchor().isFollowing();
        buf.writeBoolean(isFollowing);
        if (isFollowing) {
            buf.writeVarInt(data.anchor().getEntityId());
        } else {
            Vec3 pos = data.anchor().getFixedPosition();
            if (pos != null) {
                buf.writeDouble(pos.x);
                buf.writeDouble(pos.y);
                buf.writeDouble(pos.z);
            } else {
                buf.writeDouble(0.0);
                buf.writeDouble(0.0);
                buf.writeDouble(0.0);
            }
        }

        buf.writeFloat(data.radius());
        buf.writeVarInt(data.durationTicks());
        buf.writeLong(data.startGameTime());
        buf.writeLong(data.seed());
        buf.writeFloat(data.intensity());

        PressureVisualProfile profile = data.visualProfile();
        buf.writeUtf(profile.styleId());
        buf.writeInt(profile.color());
        buf.writeFloat(profile.minWidth());
        buf.writeFloat(profile.maxWidth());
        buf.writeFloat(profile.minLength());
        buf.writeFloat(profile.maxLength());
        buf.writeFloat(profile.speedMin());
        buf.writeFloat(profile.speedMax());
        buf.writeFloat(profile.baseAlpha());
        buf.writeVarInt(profile.streakCount());
        buf.writeFloat(profile.groundImpactChance());
        buf.writeBoolean(profile.cameraCurtain());
        buf.writeFloat(profile.curtainRadius());
    }

    public static PressureFieldStartPacket decode(FriendlyByteBuf buf) {
        UUID fieldId = buf.readUUID();
        UUID ownerUuid = buf.readUUID();
        String sourceSpellId = buf.readUtf();
        int spellLevel = buf.readVarInt();

        boolean isFollowing = buf.readBoolean();
        PressureAnchor anchor;
        if (isFollowing) {
            int entityId = buf.readVarInt();
            anchor = PressureAnchor.follow(ownerUuid, entityId);
        } else {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            anchor = PressureAnchor.stationary(new Vec3(x, y, z));
        }

        float radius = buf.readFloat();
        int durationTicks = buf.readVarInt();
        long startGameTime = buf.readLong();
        long seed = buf.readLong();
        float intensity = buf.readFloat();

        String styleId = buf.readUtf();
        int color = buf.readInt();
        float minWidth = buf.readFloat();
        float maxWidth = buf.readFloat();
        float minLength = buf.readFloat();
        float maxLength = buf.readFloat();
        float speedMin = buf.readFloat();
        float speedMax = buf.readFloat();
        float baseAlpha = buf.readFloat();
        int streakCount = buf.readVarInt();
        float groundImpactChance = buf.readFloat();
        boolean cameraCurtain = buf.readBoolean();
        float curtainRadius = buf.readFloat();

        PressureVisualProfile profile = new PressureVisualProfile(
                styleId, color, minWidth, maxWidth, minLength, maxLength,
                speedMin, speedMax, baseAlpha, streakCount, groundImpactChance,
                cameraCurtain, curtainRadius
        );

        PressureFieldData fieldData = new PressureFieldData(
                fieldId, ownerUuid, sourceSpellId, spellLevel, anchor, radius, durationTicks, startGameTime, seed, profile, intensity
        );

        return new PressureFieldStartPacket(fieldData);
    }

    public static void handle(PressureFieldStartPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientPressureManager.addOrUpdateField(packet.fieldData);
        }));
        context.setPacketHandled(true);
    }
}
