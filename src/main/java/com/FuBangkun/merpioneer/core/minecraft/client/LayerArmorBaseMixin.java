package com.FuBangkun.merpioneer.core.minecraft.client;

import com.FuBangkun.merpioneer.MerPioneerCapability;
import com.FuBangkun.merpioneer.TailStyleData;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerArmorBase.class)
public class LayerArmorBaseMixin {
    @Inject(
            method = "renderArmorLayer(Lnet/minecraft/entity/EntityLivingBase;FFFFFFFLnet/minecraft/inventory/EntityEquipmentSlot;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderArmorLayer(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn, CallbackInfo ci) {
        if (slotIn == EntityEquipmentSlot.LEGS || slotIn == EntityEquipmentSlot.FEET) {
            ci.cancel();
            return;
        }

        if (slotIn == EntityEquipmentSlot.CHEST && entityLivingBaseIn instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLivingBaseIn;
            TailStyleData cap = player.getCapability(MerPioneerCapability.TAIL_STYLE_CAPABILITY, null);

            if (cap != null && cap.isHasBra()) {
                ci.cancel();
            }
        }
    }
}