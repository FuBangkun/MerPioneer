package com.FuBangkun.merpioneer.util;

import com.FuBangkun.merpioneer.config.ConfigHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class Keybindings {
    public static KeyBinding forceCrawling = null;
    public static KeyBinding openTailGUI = null;

    public static void register() {
        if (ConfigHandler.MOVEMENT_CONFIG.enableToggleCrawling) {
            forceCrawling = new KeyBinding("key.merpioneer.toggle_crawling", Keyboard.KEY_C, "key.categories.merpioneer");
            openTailGUI = new KeyBinding("key.merpioneer.open_gui", Keyboard.KEY_G, "key.categories.merpioneer");
            ClientRegistry.registerKeyBinding(forceCrawling);
            ClientRegistry.registerKeyBinding(openTailGUI);
        }
    }
}