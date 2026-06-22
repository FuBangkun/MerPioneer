package com.FuBangkun.merpioneer.network;

import com.FuBangkun.merpioneer.config.ConfigHandler;
import com.FuBangkun.merpioneer.network.message.PacketSendKey;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
    public static SimpleNetworkWrapper INSTANCE = null;

    public static void registerMessages(String channelName) {
        int packetId = 0;
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
        if (ConfigHandler.MOVEMENT_CONFIG.enableToggleCrawling) {
            INSTANCE.registerMessage(PacketSendKey.Handler.class, PacketSendKey.class, packetId++, Side.SERVER);
        }
        INSTANCE.registerMessage(SyncTailStylePacket.Handler.class, SyncTailStylePacket.class, packetId++, Side.SERVER);
        INSTANCE.registerMessage(SyncTailStylePacket.Handler.class, SyncTailStylePacket.class, packetId++, Side.CLIENT);
    }
}