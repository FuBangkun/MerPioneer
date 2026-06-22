package com.FuBangkun.merpioneer.integration.thaumicaugmentation;

import com.FuBangkun.merpioneer.integration.IntegrationManager;
import thecodex6824.thaumicaugmentation.client.internal.TAHooksClient;

public class ThaumicAugmentationIntegration {
    public static void register() {
        IntegrationManager.elytraOpenHooks.add(TAHooksClient::checkElytra);
    }
}
