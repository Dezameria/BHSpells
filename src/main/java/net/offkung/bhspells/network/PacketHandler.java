package net.offkung.bhspells.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.network.client.BlessingSnowSetRadiusPacket;
import net.offkung.bhspells.network.client.BlessingSnowTargetSelectPacket;
import net.offkung.bhspells.network.client.DismountGoldenCloudPacket;
import net.offkung.bhspells.network.client.SwordDashPacket;
import net.offkung.bhspells.network.server.BlessingSnowSelectSyncPacket;
import net.offkung.bhspells.network.server.BlessingSnowTargetGlowSyncPacket;
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
        INSTANCE.messageBuilder(DismountGoldenCloudPacket.class, index++, NetworkDirection.PLAY_TO_SERVER).encoder(DismountGoldenCloudPacket::encode).decoder(DismountGoldenCloudPacket::new).consumerNetworkThread(DismountGoldenCloudPacket::handle).add();
        INSTANCE.messageBuilder(BlessingSnowSelectSyncPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT).encoder(BlessingSnowSelectSyncPacket::encode).decoder(BlessingSnowSelectSyncPacket::new).consumerNetworkThread(BlessingSnowSelectSyncPacket::handle).add();
        INSTANCE.messageBuilder(BlessingSnowSetRadiusPacket.class, index++, NetworkDirection.PLAY_TO_SERVER).encoder(BlessingSnowSetRadiusPacket::encode).decoder(BlessingSnowSetRadiusPacket::new).consumerNetworkThread(BlessingSnowSetRadiusPacket::handle).add();
        INSTANCE.messageBuilder(BlessingSnowTargetSelectPacket.class, index++, NetworkDirection.PLAY_TO_SERVER).encoder(BlessingSnowTargetSelectPacket::encode).decoder(BlessingSnowTargetSelectPacket::new).consumerNetworkThread(BlessingSnowTargetSelectPacket::handle).add();
        INSTANCE.messageBuilder(BlessingSnowTargetGlowSyncPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT).encoder(BlessingSnowTargetGlowSyncPacket::encode).decoder(BlessingSnowTargetGlowSyncPacket::new).consumerNetworkThread(BlessingSnowTargetGlowSyncPacket::handle).add();
    }
}
