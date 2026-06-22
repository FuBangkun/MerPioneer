package com.FuBangkun.merpioneer.handler;

import com.FuBangkun.merpioneer.config.ConfigHandler;
import com.FuBangkun.merpioneer.entity.IRockableBoat;
import net.minecraft.entity.item.EntityBoat;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommonHandler {
    @SubscribeEvent
    public void onEntityConstructing(EntityEvent.EntityConstructing event) {
        if (event.getEntity() instanceof EntityBoat) {
            if (ConfigHandler.MISCELLANEOUS_CONFIG.bubbleColumns) {
                ((IRockableBoat) event.getEntity()).doRegisterData();
            }
        }
    }
}