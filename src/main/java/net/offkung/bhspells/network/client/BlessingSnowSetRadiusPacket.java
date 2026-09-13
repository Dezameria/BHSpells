package net.offkung.bhspells.network.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.network.NetworkEvent;
import net.offkung.bhspells.registry.MobEffectsRegistry;

import java.util.function.Supplier;

public class BlessingSnowSetRadiusPacket {
    private final int radius;

    public BlessingSnowSetRadiusPacket(int radius) {
        this.radius = radius;
    }

    public BlessingSnowSetRadiusPacket(FriendlyByteBuf buf) {
        this.radius = buf.readInt();
    }

    public static void encode(BlessingSnowSetRadiusPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.radius);
    }

    public static void handle(BlessingSnowSetRadiusPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (msg.radius == 5 || msg.radius == 10 || msg.radius == 20 || msg.radius == 30) {
                player.getPersistentData().putInt("BlessingSnowRadius", msg.radius);

                int amplifier = switch (msg.radius) {
                    case 10 -> 1;
                    case 20 -> 2;
                    case 30 -> 3;
                    default -> 0;
                };
                player.removeEffect(MobEffectsRegistry.BLESSING_SNOW_CHECK.get());
                player.addEffect(new MobEffectInstance(MobEffectsRegistry.BLESSING_SNOW_CHECK.get(), 60, amplifier, false, false, true));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
