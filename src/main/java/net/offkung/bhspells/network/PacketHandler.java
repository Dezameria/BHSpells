package net.offkung.bhspells.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.network.client.SwordDashPacket;
import net.offkung.bhspells.network.server.ScreenShakePacket;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "main"), () ->
                    PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public PacketHandler() {
    }

    public static void registerPackets() {
        int index = 0;
        INSTANCE.messageBuilder(ScreenShakePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT).encoder(ScreenShakePacket::encode).decoder(ScreenShakePacket::new).consumerNetworkThread(ScreenShakePacket::handle).add();
        INSTANCE.messageBuilder(SwordDashPacket.class, index++, NetworkDirection.PLAY_TO_SERVER).encoder(SwordDashPacket::encode).decoder(SwordDashPacket::new).consumerNetworkThread(SwordDashPacket::handle).add();
    }
}
