package com.FuBangkun.merpioneer.core.minecraft.client;

import com.FuBangkun.merpioneer.client.model.IModelBipedSwimming;
import com.FuBangkun.merpioneer.entity.player.IPlayerResizeable;
import com.FuBangkun.merpioneer.integration.IntegrationManager;
import com.FuBangkun.merpioneer.integration.mobends.MoBendsIntegration;
import com.FuBangkun.merpioneer.util.math.MathHelperNew;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public abstract class RenderPlayerMixin extends RenderLivingBase<AbstractClientPlayer> {
    public RenderPlayerMixin(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    @Inject(method = "renderRightArm", at = @At("HEAD"))
    public void renderRightArm(AbstractClientPlayer clientPlayer, CallbackInfo callbackInfo) {
        ModelPlayer modelplayer = (ModelPlayer) this.getMainModel();
        ((IModelBipedSwimming) modelplayer).setSwimAnimation(0.0F);
    }

    @Inject(method = "renderLeftArm", at = @At("HEAD"))
    public void renderLeftArm(AbstractClientPlayer clientPlayer, CallbackInfo callbackInfo) {
        ModelPlayer modelplayer = (ModelPlayer) this.getMainModel();
        ((IModelBipedSwimming) modelplayer).setSwimAnimation(0.0F);
    }

    @Inject(method = "applyRotations*", at = @At("TAIL"))
    protected void applyRotations(AbstractClientPlayer entityLiving, float p_77043_2_, float rotationYaw, float partialTicks, CallbackInfo callbackInfo) {
        if (!entityLiving.isElytraFlying()) {
            if (!IntegrationManager.isMoBendsEnabled()) {
                float f = ((IPlayerResizeable) entityLiving).getSwimAnimation(partialTicks);
                float f3 = entityLiving.isInWater() ? -90.0F - entityLiving.rotationPitch : -90.0F;
                float f4 = MathHelperNew.lerp(f, 0.0F, f3);
                GlStateManager.rotate(f4, 1.0F, 0.0F, 0.0F);
            }

            if (((IPlayerResizeable) entityLiving).isActuallySwimming()) {
                if (!IntegrationManager.isMoBendsEnabled()) {
                    GlStateManager.translate(0.0F, -1.0F, 0.3F);
                } else {
                    MoBendsIntegration.applyRotations((RenderPlayer) (Object) this, entityLiving);
                }
            }
        }
    }
}