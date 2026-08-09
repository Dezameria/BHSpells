package net.offkung.bhspells.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.offkung.bhspells.client.event.ClientScreenShakeEvent;

import java.util.function.Supplier;

public class ScreenShakePacket {
    private final float power;
    private final double x;
    private final double y;
    private final double z;

    public ScreenShakePacket(float power, Vec3 center) {
        this.power = power;
        this.x = center.x;
        this.y = center.y;
        this.z = center.z;
    }

    public ScreenShakePacket(FriendlyByteBuf buf) {
        this.power = buf.readFloat();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    public static void encode(ScreenShakePacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.power);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
    }

    public static void handle(ScreenShakePacket msg, Supplier<NetworkEvent.Context> ctx) {
        (ctx.get()).enqueueWork(() -> handleClient(msg));
        (ctx.get()).setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(ScreenShakePacket msg) {
        ClientScreenShakeEvent.shake(msg.power);
    }
}
