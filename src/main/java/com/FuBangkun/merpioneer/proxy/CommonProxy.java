package com.FuBangkun.merpioneer.proxy;

import com.FuBangkun.merpioneer.MerPioneer;
import com.FuBangkun.merpioneer.Tags;
import com.FuBangkun.merpioneer.biome.BiomeWaterFogColors;
import com.FuBangkun.merpioneer.block.BlockBubbleColumn;
import com.FuBangkun.merpioneer.config.ConfigHandler;
import com.FuBangkun.merpioneer.core.MerPioneerLateLoader;
import com.FuBangkun.merpioneer.handler.CommonHandler;
import com.FuBangkun.merpioneer.integration.IntegrationManager;
import com.FuBangkun.merpioneer.integration.hats.HatsIntegration;
import com.FuBangkun.merpioneer.integration.witchery.WitcheryResurrectedIntegration;
import com.FuBangkun.merpioneer.network.NetworkHandler;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber
public class CommonProxy {
    @GameRegistry.ObjectHolder("merpioneer:bubble_column")
    public static BlockBubbleColumn BUBBLE_COLUMN;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        if (ConfigHandler.MISCELLANEOUS_CONFIG.bubbleColumns) {
            event.getRegistry().register(new BlockBubbleColumn());
        }
    }

    public void preInit(FMLPreInitializationEvent event) {
        IntegrationManager.loadCompat();
        NetworkHandler.registerMessages(Tags.MOD_ID);
        MinecraftForge.EVENT_BUS.register(new CommonHandler());
    }

    public void init() {
    }

    public void onMappings() {
    }

    public void postInit() {
        if (IntegrationManager.isHatsEnabled()) {
            HatsIntegration.register();
        }

        if (IntegrationManager.isWitcheryResurrectedEnabled()) {
            WitcheryResurrectedIntegration.register();
        }

        if (!MerPioneerLateLoader.isModCompatLoaded) {
            MerPioneer.LOGGER.error("Please consider installing MixinBooter to ensure compatibility with more mods");
        }

        BiomeWaterFogColors.recomputeColors();
    }
}