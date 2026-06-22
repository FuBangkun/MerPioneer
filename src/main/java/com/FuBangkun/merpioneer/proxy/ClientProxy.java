package com.FuBangkun.merpioneer.proxy;

import com.FuBangkun.merpioneer.block.BlockBubbleColumn;
import com.FuBangkun.merpioneer.client.gui.TailConfigScreen;
import com.FuBangkun.merpioneer.client.handler.AirMeterHandler;
import com.FuBangkun.merpioneer.client.handler.FogHandler;
import com.FuBangkun.merpioneer.client.model.WaterResourcePack;
import com.FuBangkun.merpioneer.client.render.LayerFirstPersonMap;
import com.FuBangkun.merpioneer.client.render.FPVRenderHandler;
import com.FuBangkun.merpioneer.client.render.TailRenderLayer;
import com.FuBangkun.merpioneer.config.ConfigHandler;
import com.FuBangkun.merpioneer.entity.player.IPlayerResizeable;
import com.FuBangkun.merpioneer.integration.IntegrationManager;
import com.FuBangkun.merpioneer.integration.artemislib.ArtemisLibIntegration;
import com.FuBangkun.merpioneer.integration.enderio.EnderIOIntegration;
import com.FuBangkun.merpioneer.integration.mobends.MoBendsIntegration;
import com.FuBangkun.merpioneer.integration.thaumicaugmentation.ThaumicAugmentationIntegration;
import com.FuBangkun.merpioneer.network.NetworkHandler;
import com.FuBangkun.merpioneer.network.message.PacketSendKey;
import com.FuBangkun.merpioneer.optifine.OptifineHelper;
import com.FuBangkun.merpioneer.util.Keybindings;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        if (ConfigHandler.MISCELLANEOUS_CONFIG.bubbleColumns) {
            ModelLoader.setCustomStateMapper(CommonProxy.BUBBLE_COLUMN, new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockBubbleColumn.DRAG).build());
        }
    }

    @SubscribeEvent
    public static void registerTextures(TextureStitchEvent.Pre event) {
        if (ConfigHandler.BLOCKS_CONFIG.newWaterColors) {
            TextureMap map = event.getMap();
            /* Register the custom 1.13-style texture used by most in-world renderers */
            map.registerSprite(new ResourceLocation("merpioneer:blocks/water_still"));
            map.registerSprite(new ResourceLocation("merpioneer:blocks/water_flow"));
        }
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.KeyInputEvent event) {
        // null check for hot reload
        if (ConfigHandler.MOVEMENT_CONFIG.enableToggleCrawling && Keybindings.forceCrawling != null && Keybindings.forceCrawling.isPressed()) {
            IPlayerResizeable player = (IPlayerResizeable) Minecraft.getMinecraft().player;
            if (player != null) {
                if (player.canForceCrawling())
                    NetworkHandler.INSTANCE.sendToServer(new PacketSendKey(PacketSendKey.KeybindPacket.TOGGLE_CRAWLING));
                else {
                    ((EntityPlayerSP) player).sendMessage(new TextComponentTranslation("chat.merpioneer.cannot_toggle_crawling"));
                }
            }
        }
        if (Keybindings.openTailGUI.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new TailConfigScreen());
            }
        }
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(new AirMeterHandler());
        MinecraftForge.EVENT_BUS.register(new FogHandler());
        MinecraftForge.EVENT_BUS.register(new FPVRenderHandler());

        if (ConfigHandler.BLOCKS_CONFIG.newWaterColors) {
            List<IResourcePack> packs = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "field_110449_ao");
            packs.add(new WaterResourcePack(event.getSourceFile()));
            FMLClientHandler.instance().refreshResources(VanillaResourceType.TEXTURES);
            OptifineHelper.init();
        }
    }

    @Override
    public void init() {
        Keybindings.register();
        for (RenderPlayer render : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()) {
            render.addLayer(new LayerFirstPersonMap(render));
        }
    }

    @Override
    public void onMappings() {
        if (OptifineHelper.isOFPresent) {
            OptifineHelper.reloadBlockAliases();
        }
    }

    @Override
    public void postInit() {
        super.postInit();

        Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();

        for (RenderPlayer renderer : skinMap.values()) {
            renderer.addLayer(new TailRenderLayer(renderer));
        }

        FogHandler.recomputeBlacklist();

        if (IntegrationManager.isMoBendsEnabled()) {
            MoBendsIntegration.register();
        }

        if (IntegrationManager.isArtemisLibEnabled()) {
            ArtemisLibIntegration.register();
        }

        if (IntegrationManager.isEnderIoEnabled()) {
            EnderIOIntegration.register();
        }

        if (IntegrationManager.isThaumicAugmentationEnabled()) {
            ThaumicAugmentationIntegration.register();
        }
    }
}