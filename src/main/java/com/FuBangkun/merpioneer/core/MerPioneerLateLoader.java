package com.FuBangkun.merpioneer.core;

import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.List;

public class MerPioneerLateLoader implements ILateMixinLoader {
    public static volatile boolean isModCompatLoaded = false;

    @Override
    public List<String> getMixinConfigs() {

        List<String> configs = new ArrayList<>();

        if (Loader.isModLoaded("galacticraftcore")) {
            configs.add("mixins.merpioneer.galacticraft.json");
        }

        if (Loader.isModLoaded("journeymap")) {
            configs.add("mixins.merpioneer.journeymap.json");
        }

        if (Loader.isModLoaded("xaerominimap")) {
            configs.add("mixins.merpioneer.xaerosminimap.json");
        }

        if (Loader.isModLoaded("thaumcraft")) {
            configs.add("mixins.merpioneer.thaumcraft.json");
        }

        isModCompatLoaded = true;

        return configs;
    }
}