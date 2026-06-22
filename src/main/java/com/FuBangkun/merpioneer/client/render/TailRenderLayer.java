package com.FuBangkun.merpioneer.client.render;

import com.FuBangkun.merpioneer.MerPioneerCapability;
import com.FuBangkun.merpioneer.TailStyleData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class TailRenderLayer implements LayerRenderer<AbstractClientPlayer> {
    private final RenderPlayer renderPlayer;
    private final TailModel tailModel;

    public TailRenderLayer(RenderPlayer renderPlayer) {
        this.renderPlayer = renderPlayer;
        this.tailModel = new TailModel();
    }

    @Override
    public void doRenderLayer(@Nonnull AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (player.isInvisible()) {
            return;
        }

        if (player.hasCapability(MerPioneerCapability.TAIL_STYLE_CAPABILITY, null)) {
            TailStyleData style = player.getCapability(MerPioneerCapability.TAIL_STYLE_CAPABILITY, null);

            if (style != null) {
                GlStateManager.pushMatrix();

                this.tailModel.updatePose(
                        player,
                        this.renderPlayer.getMainModel(),
                        ageInTicks
                );

                this.tailModel.renderTail(
                        scale,
                        style,
                        this.renderPlayer
                );

                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}