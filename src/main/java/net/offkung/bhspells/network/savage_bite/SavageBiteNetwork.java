package net.offkung.bhspells.network.savage_bite;

import net.offkung.bhspells.BHSpells;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.Optional;

public final class SavageBiteNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            BHSpells.id("savage_bite"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private SavageBiteNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                0,
                SavageBiteStatePacket.class,
                SavageBiteStatePacket::encode,
                SavageBiteStatePacket::decode,
                SavageBiteStatePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                1,
                SavageBiteInputPacket.class,
                SavageBiteInputPacket::encode,
                SavageBiteInputPacket::decode,
                SavageBiteInputPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public static void sendState(LivingEntity caster, @Nullable LivingEntity target, byte state) {
        if (caster.level().isClientSide) {
            return;
        }
        int targetId = target != null ? target.getId() : -1;
        SavageBiteStatePacket packet = new SavageBiteStatePacket(caster.getUUID(), caster.getId(), targetId, state);
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> caster), packet);
    }

    public static void sendInput(float forward, float strafe, boolean cancelRequested) {
        CHANNEL.sendToServer(new SavageBiteInputPacket(forward, strafe, cancelRequested));
    }
}
