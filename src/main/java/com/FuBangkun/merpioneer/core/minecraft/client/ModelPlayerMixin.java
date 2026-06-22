package com.FuBangkun.merpioneer.core.minecraft.client;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPlayer.class)
public class ModelPlayerMixin {
    @Unique
    private boolean shouldHideLegs = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        ModelPlayer model = (ModelPlayer) (Object) this;
        if (entityIn instanceof EntityPlayer) {
            this.shouldHideLegs = true;

            model.bipedLeftLeg.showModel = false;
            model.bipedRightLeg.showModel = false;
        } else {
            this.shouldHideLegs = false;
        }
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/ModelRenderer;render(F)V"
            )
    )
    private void redirectLegwearRender(ModelRenderer modelRenderer, float scale) {
        ModelPlayer model = (ModelPlayer) (Object) this;
        if (this.shouldHideLegs && (modelRenderer == model.bipedLeftLegwear || modelRenderer == model.bipedRightLegwear)) {
            return;
        }
        modelRenderer.render(scale);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderEnd(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        ModelPlayer model = (ModelPlayer) (Object) this;
        model.bipedLeftLeg.showModel = true;
        model.bipedRightLeg.showModel = true;
        this.shouldHideLegs = false;
    }
}
