package com.FuBangkun.merpioneer.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.world.storage.MapData;

import javax.annotation.Nonnull;

public class LayerFirstPersonMap implements LayerRenderer<AbstractClientPlayer> {
    private static final float RIGHT_MAP_TX = 0.0F;
    private static final float RIGHT_MAP_TY = 0.6F;
    private static final float RIGHT_MAP_TZ = -0.1F;
    private static final float RIGHT_MAP_RX = -90.0F;
    private static final float RIGHT_MAP_RY = 180.0F;
    private static final float RIGHT_MAP_RZ = 180.0F;
    private static final float RIGHT_MAP_SCALE = 0.006F;

    private static final float LEFT_MAP_TX = 0.0F;
    private static final float LEFT_MAP_TY = 0.6F;
    private static final float LEFT_MAP_TZ = -0.1F;
    private static final float LEFT_MAP_RX = -90.0F;
    private static final float LEFT_MAP_RY = 180.0F;
    private static final float LEFT_MAP_RZ = 180.0F;
    private static final float LEFT_MAP_SCALE = 0.006F;

    private static final float MAP_PIVOT_X = -64.0F;
    private static final float MAP_PIVOT_Y = -128.0F;
    private static final float MAP_PIVOT_Z = 0.0F;

    private final RenderPlayer renderPlayer;

    public LayerFirstPersonMap(RenderPlayer renderPlayer) {
        this.renderPlayer = renderPlayer;
    }

    public void doRenderLayer(@Nonnull AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (FPVRenderHandler.isRenderingFirstPerson && player.equals(Minecraft.getMinecraft().player)) {
            ItemStack mainHand = FPVRenderHandler.currentRenderingMapMain;
            ItemStack offHand = FPVRenderHandler.currentRenderingMapOff;
            boolean hasMap = !mainHand.isEmpty() && mainHand.getItem() instanceof ItemMap;
            boolean hasOffMap = !offHand.isEmpty() && offHand.getItem() instanceof ItemMap;
            if (hasMap || hasOffMap) {
                ModelPlayer model = this.renderPlayer.getMainModel();
                EnumHandSide mainHandSide = player.getPrimaryHand();
                if (hasMap) {
                    boolean isLeft = mainHandSide == EnumHandSide.LEFT;
                    this.renderMapInHand(player, isLeft ? model.bipedLeftArm : model.bipedRightArm, mainHand, isLeft);
                }

                if (hasOffMap) {
                    boolean isLeft = mainHandSide == EnumHandSide.RIGHT;
                    this.renderMapInHand(player, isLeft ? model.bipedLeftArm : model.bipedRightArm, offHand, isLeft);
                }

            }
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void renderMapInHand(EntityPlayer player, ModelRenderer arm, ItemStack stack, boolean leftHand) {
        GlStateManager.pushMatrix();
        arm.postRender(0.0625F);

        if (leftHand) {
            GlStateManager.translate(LEFT_MAP_TX, LEFT_MAP_TY, LEFT_MAP_TZ);
            GlStateManager.rotate(LEFT_MAP_RX, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(LEFT_MAP_RY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(LEFT_MAP_RZ, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(LEFT_MAP_SCALE, LEFT_MAP_SCALE, 1.0F);
        } else {
            GlStateManager.translate(RIGHT_MAP_TX, RIGHT_MAP_TY, RIGHT_MAP_TZ);
            GlStateManager.rotate(RIGHT_MAP_RX, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(RIGHT_MAP_RY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(RIGHT_MAP_RZ, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(RIGHT_MAP_SCALE, RIGHT_MAP_SCALE, 1.0F);
        }

        GlStateManager.translate(MAP_PIVOT_X, MAP_PIVOT_Y, MAP_PIVOT_Z);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        MapData mapdata = ((ItemMap) stack.getItem()).getMapData(stack, player.world);
        if (mapdata != null) {
            Minecraft.getMinecraft().entityRenderer.getMapItemRenderer().renderMap(mapdata, false);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    public boolean shouldCombineTextures() {
        return false;
    }
}