package com.FuBangkun.merpioneer.config;

import com.FuBangkun.merpioneer.Tags;
import com.FuBangkun.merpioneer.biome.BiomeWaterFogColors;
import com.FuBangkun.merpioneer.client.handler.FogHandler;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Tags.MOD_ID)
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class ConfigHandler {
    @Config.Name("Blocks")
    @Config.Comment("Block-related config options (must match server).")
    public static final BlocksConfig BLOCKS_CONFIG = new BlocksConfig();
    @Config.Name("Movement")
    @Config.Comment("Movement related config options.")
    public static final MovementConfig MOVEMENT_CONFIG = new MovementConfig();
    @Config.Name("Miscellaneous")
    @Config.Comment("Config options for various features of the mod.")
    public static final MiscellaneousConfig MISCELLANEOUS_CONFIG = new MiscellaneousConfig();
    @Config.Name("Integration")
    @Config.Comment("Control compatibility settings for individual mods.")
    public static final IntegrationConfig INTEGRATION_CONFIG = new IntegrationConfig();
    @Config.Name("FirstPersonView")
    public static final FPVConfig FPV_CONFIG = new FPVConfig();
    @Config.Name("Push Player Out Of Blocks")
    @Config.Comment({
            "STANDARD - The player will occasionally be pushed out of certain spaces. Collisions are evaluated for full cubes only, non-full cubes are ignored. This is the default behavior up to Minecraft 1.12.",
            "APPROXIMATE - The player can move into more spaces, but will still be pushed out of some. Collisions are evaluated for full cubes only, non-full cubes are ignored.",
            "EXACT - The player can move into all spaces as expected. Collisions are evaluated for all types of cubes. This is the default behavior in Minecraft 1.13 and onwards."
    })
    public static PlayerBlockCollisions playerBlockCollisions = PlayerBlockCollisions.APPROXIMATE;

    @SubscribeEvent
    public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent evt) {
        if (evt.getModID().equals(Tags.MOD_ID)) {
            ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
            BiomeWaterFogColors.recomputeColors();
            FogHandler.recomputeBlacklist();
        }
    }

    public enum PlayerBlockCollisions {
        STANDARD, APPROXIMATE, EXACT
    }

    public enum WaterFogMode {
        AA_EXP2, VANILLA_LINEAR
    }

    public static class MovementConfig {
        @Config.Name("Easy Elytra Takeoff")
        @Config.Comment("Taking off with an elytra from the ground is now far easier like in Minecraft 1.15 and onwards.")
        public boolean easyElytraTakeoff = true;

        @Config.Name("No Double Tab Sprinting")
        @Config.Comment("Prevent sprinting from being triggered by double tapping the walk forward key.")
        public boolean noDoubleTapSprinting = true;

        @Config.Name("Sideways Sprinting")
        @Config.Comment("Enables sprinting to the left and right.")
        public boolean sidewaysSprinting = true;

        @Config.Name("Sideways Swimming")
        @Config.Comment("Enables swimming to the left and right.")
        public boolean sidewaysSwimming = true;

        @Config.Name("Enable Crawling")
        @Config.Comment("Enables crawling to prevent suffocation. Note that if you disable this there will probably be behavioral differences from 1.13.")
        public boolean enableCrawling = true;

        @Config.Name("Enable Toggle Crawling")
        @Config.Comment("Enables a keybind to toggle crawling.")
        public boolean enableToggleCrawling = true;

        @Config.Name("New Projectile Behavior")
        @Config.Comment("Modify projectile behavior to be closer to that of newer versions (fixes MC-73884 and allows bubble columns to work with ender pearls).")
        public boolean newProjectileBehavior = true;

        @Config.Name("New Climbing Behavior")
        @Config.Comment("Allow climbing vines and climbing by pressing jump.")
        public boolean newClimbingBehavior = true;

        @Config.Name("Swim Speed Multiplier")
        @Config.Comment("Swim speed multiplier.")
        @Config.RangeDouble(min = 1.0, max = 10.0)
        public double swimSpeedMultiplier = 3.0;
    }

    public static class BlocksConfig {
        @Config.Name("Seagrass")
        @Config.Comment("Allow seagrass to generate in the world.")
        public boolean seagrass = true;

        @Config.Name("Brighter Water")
        @Config.Comment("Make water only reduce light level by 1 per Y-level, instead of 3.")
        public boolean brighterWater = true;

        @Config.Name("New Water")
        @Config.Comment("Use the new water rendering in 1.13+.")
        public boolean newWaterColors = true;

        @Config.Name("New Water Fog")
        @Config.Comment("Use the new fog rendering in 1.13+.")
        public boolean newWaterFog = true;

        @Config.Name("New Water Fog Render Mode")
        @Config.Comment("Water fog render mode, available options: AA_EXP2, VANILLA_LINEAR")
        public WaterFogMode waterFogMode = WaterFogMode.AA_EXP2;
    }

    public static class MiscellaneousConfig {
        @Config.Name("Replenish Air Slowly")
        @Config.Comment("Replenish air slowly when out of water instead of immediately.")
        public boolean slowAirReplenish = true;

        @Config.Name("Sneaking Dismounts Parrots")
        @Config.Comment("Parrots no longer leave the players shoulders as easily, instead the player needs to press the sneak key.")
        public boolean sneakingForParrots = true;

        @Config.Name("Eating Animation")
        @Config.Comment("Animate eating in third-person view.")
        public boolean eatingAnimation = true;

        @Config.Name("Bubble Columns")
        @Config.Comment("Enable bubble columns.")
        public boolean bubbleColumns = true;

        @Config.Name("Custom Biome Water Colors")
        @Config.Comment("Allows overriding the water and fog colors for a biome. Specify each entry like this (without quotes) - 'modname:biome,color,fogcolor'")
        public String[] customBiomeWaterColors = new String[]{};

        @Config.Name("WorldProvider Fog Blacklist")
        @Config.Comment("List of WorldProviders in which fog should be disabled.")
        public String[] providerFogBlacklist = new String[]{"thebetweenlands.common.world.WorldProviderBetweenlands"};

        @Config.Name("Floating Items")
        @Config.Comment("Whether or not items should float in water like in 1.13+.")
        public boolean floatingItems = true;

        @Config.Name("Night Vision")
        @Config.Comment("Enable night vision in the water.")
        public boolean enableNightVision = true;
    }

    public static class IntegrationConfig {
        private static final String COMPAT_DESCRIPTION = "Only applies when the mod is installed. Disable when there are issues with the mod.";

        @Config.Name("Applied Energistics 2 Integration")
        @Config.Comment(COMPAT_DESCRIPTION)
        @Config.RequiresMcRestart
        public boolean ae2Integration = true;

        @Config.Name("Betweenlands Integration")
        @Config.Comment(COMPAT_DESCRIPTION)
        @Config.RequiresMcRestart
        public boolean betweenlandsIntegration = true;

        @Config.Name("Chiseled Me Integration")
        @Config.Comment(COMPAT_DESCRIPTION)
        @Config.RequiresMcRestart
        public boolean chiseledMeIntegration = true;

        @Config.Name("Ender IO Integration")
        @Config.Comment(COMPAT_DESCRIPTION)
        @Config.RequiresMcRestart
        public boolean enderIoIntegration = true;

        @Config.Name("Random Patches Integration")
        @Config.Comment(COMPAT_DESCRIPTION)
        public boolean randomPatchesIntegration = true;

        @Config.Name("Mo' Bends Integration")
        @Config.Comment(COMPAT_DESCRIPTION)
        @Config.RequiresMcRestart
        public boolean moBendsIntegration = true;

        @Config.Name("Wings Integration")
        @Config.Comment(COMPAT_DESCRIPTION)
        public boolean wingsIntegration = true;

        @Config.Name("ArtemisLib Integration")
        @Config.Comment(COMPAT_DESCRIPTION)
        @Config.RequiresMcRestart
        public boolean artemisLibIntegration = true;

        @Config.Name("Morph Integration")
        @Config.Comment(COMPAT_DESCRIPTION)
        public boolean morphIntegration = true;

        @Config.Name("Hats Integration")
        @Config.Comment(COMPAT_DESCRIPTION)
        @Config.RequiresMcRestart
        public boolean hatsIntegration = true;

        @Config.Name("Thaumic Augmentation Integration")
        @Config.Comment(COMPAT_DESCRIPTION)
        public boolean thaumicAugmentationIntegration = true;

        @Config.Name("Trinkets and Baubles Integration")
        @Config.Comment(COMPAT_DESCRIPTION)
        public boolean trinketsAndBaublesIntegration = true;

        @Config.Name("Witchery: Resurrected Integration")
        @Config.Comment(COMPAT_DESCRIPTION)
        public boolean witcheryResurrectedIntegration = true;
    }

    public static class FPVConfig {
        @Config.Name("Enable First Person View")
        @Config.Comment("Enable First Person View")
        public boolean enableFPV = true;

        @Config.Name("Enable Helmet Rendering")
        @Config.Comment("Enable Helmet Rendering")
        public boolean enableHelm = false;

        @Config.Name("Enable Chestplate Rendering")
        @Config.Comment("Enable Chestplate Rendering")
        public boolean enableChest = true;

        @Config.Name("Enable Head Rendering")
        @Config.Comment("Enable Head Rendering")
        public boolean enableHead = false;

        @Config.Name("Enable Body Rendering")
        @Config.Comment("Enable Body Rendering")
        public boolean enableBody = true;

        @Config.Name("Enable Arms Rendering")
        @Config.Comment("Enable Arms Rendering")
        public boolean enableArms = true;

        @Config.Name("Enable Cape Rendering")
        @Config.Comment("Enable Cape Rendering")
        public boolean enableCape = true;

        @Config.Name("Enable Universal Clipping")
        @Config.Comment("Enable Universal Clipping (fixes head visibility with animation mods)")
        public boolean enableClipping = true;

        @Config.Name("Clipping Plane Height")
        @Config.Comment("Clipping Plane Height (relative to player feet)")
        public double clippingHeight = 1.5;
    }
}