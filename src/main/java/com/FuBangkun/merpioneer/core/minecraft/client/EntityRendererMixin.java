package com.FuBangkun.merpioneer.core.minecraft.client;

import com.FuBangkun.merpioneer.config.ConfigHandler;
import com.FuBangkun.merpioneer.integration.IntegrationManager;
import com.FuBangkun.merpioneer.util.math.MathHelperNew;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    private float thirdPersonDistancePrev;

    @Unique
    private float eyeHeight;
    @Unique
    private float previousEyeHeight;
    @Unique
    private float entityEyeHeight;
    @Unique
    private float partialTicks;

    @Redirect(
            method = "updateLightmap",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;isPotionActive(Lnet/minecraft/potion/Potion;)Z"
            )
    )
    private boolean redirectIsPotionActiveForLightmap(net.minecraft.client.entity.EntityPlayerSP player, net.minecraft.potion.Potion potion) {
        if (potion == MobEffects.NIGHT_VISION && ConfigHandler.MISCELLANEOUS_CONFIG.enableNightVision && player.isInsideOfMaterial(Material.WATER)) {
            return true;
        }
        return player.isPotionActive(potion);
    }

    @Redirect(
            method = "updateFogColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z"
            )
    )
    private boolean redirectIsPotionActiveForFog(EntityLivingBase entity, net.minecraft.potion.Potion potion) {
        if (potion == MobEffects.NIGHT_VISION && ConfigHandler.MISCELLANEOUS_CONFIG.enableNightVision && entity.isInsideOfMaterial(Material.WATER)) {
            return true;
        }
        return entity.isPotionActive(potion);
    }

    @Inject(
            method = "getNightVisionBrightness",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGetNightVisionBrightness(EntityLivingBase entitylivingbaseIn, float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (ConfigHandler.MISCELLANEOUS_CONFIG.enableNightVision && entitylivingbaseIn != null && entitylivingbaseIn.isInsideOfMaterial(Material.WATER)) {
            cir.setReturnValue(1.0F);
        }
    }

    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void orientCamera(float partialTicks, CallbackInfo callbackInfo) {
        // field for passing on partialTicks, workaround as @ModifyVariable is unable to handle method arguments in Mixin <0.8
        this.partialTicks = partialTicks;
    }

    @ModifyVariable(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevPosX:D", ordinal = 0, opcode = Opcodes.GETFIELD), ordinal = 1)
    public float getEyeHeight(float eyeHeight) {
        Entity entity = this.mc.getRenderViewEntity();

        // Do not apply eye height patch if the camera is not a player, or if Random Patches is installed
        if (!(entity instanceof EntityPlayer) || IntegrationManager.isRandomPatchesEnabled()) {
            return eyeHeight;
        }

        // need to do it like this to prevent crash with wings mod
        this.entityEyeHeight = eyeHeight;
        return MathHelperNew.lerp(this.partialTicks, this.previousEyeHeight, this.eyeHeight);
    }

    @Inject(method = "updateRenderer", at = @At("TAIL"))
    public void updateRenderer(CallbackInfo callbackInfo) {
        this.interpolateHeight();
    }

    @Unique
    private void interpolateHeight() {
        this.previousEyeHeight = this.eyeHeight;
        this.eyeHeight += (this.entityEyeHeight - this.eyeHeight) * 0.5F;
    }

    // Backport start - Camera logic from modern versions
    @Redirect(
            method = {"updateFogColor", "setupFog", "getFOVModifier"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockStateAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/state/IBlockState;"
            )
    )
    private IBlockState getBlockStateAtCameraForFog(World world, Entity entity, float partialTicks) {
        IBlockState cameraWaterState = this.getCameraWaterState(world, entity, partialTicks);
        if (cameraWaterState != null) {
            return cameraWaterState;
        }

        IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(world, entity, partialTicks);
        if (state.getMaterial() == Material.WATER) {
            return Blocks.AIR.getDefaultState();
        }
        return state;
    }

    @Redirect(
            method = "updateFogColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;getFogColor(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;"
            )
    )
    private Vec3d getFogColor(Block block, World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks) {
        Vec3d cameraPos = this.getCameraPosition(entity, partialTicks);
        BlockPos cameraBlockPos = new BlockPos(cameraPos);
        IBlockState cameraState = world.getBlockState(cameraBlockPos);
        if (this.isWaterAtCamera(world, cameraBlockPos, cameraState, cameraPos)) {
            return cameraState.getBlock().getFogColor(world, cameraBlockPos, cameraState, entity, originalColor, partialTicks);
        }
        if (state.getMaterial() == Material.WATER) {
            return originalColor;
        }
        return block.getFogColor(world, pos, state, entity, originalColor, partialTicks);
    }

    @Unique
    private IBlockState getCameraWaterState(World world, Entity entity, float partialTicks) {
        Vec3d cameraPos = this.getCameraPosition(entity, partialTicks);
        BlockPos blockPos = new BlockPos(cameraPos);
        IBlockState state = world.getBlockState(blockPos);
        return this.isWaterAtCamera(world, blockPos, state, cameraPos) ? state : null;
    }

    @Unique
    private boolean isWaterAtCamera(World world, BlockPos blockPos, IBlockState state, Vec3d cameraPos) {
        if (state.getMaterial() != Material.WATER) {
            return false;
        }

        return cameraPos.y < (double) blockPos.getY() + this.getWaterHeight(world, blockPos, state);
    }

    @Unique
    private Vec3d getCameraPosition(Entity entity, float partialTicks) {
        double x = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
        double y = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks;
        double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
        Vec3d eyePos = new Vec3d(x, y + (double) this.getCameraEyeHeight(entity, partialTicks), z);
        if (this.mc.gameSettings.thirdPersonView <= 0 || this.mc.gameSettings.debugCamEnable) {
            return eyePos;
        }

        return this.getThirdPersonCameraPosition(entity, partialTicks, eyePos);
    }

    @Unique
    private Vec3d getThirdPersonCameraPosition(Entity entity, float partialTicks, Vec3d eyePos) {
        double cameraDistance = this.thirdPersonDistancePrev + (4.0F - this.thirdPersonDistancePrev) * partialTicks;
        float yaw = entity.rotationYaw;
        float pitch = entity.rotationPitch;
        if (this.mc.gameSettings.thirdPersonView == 2) {
            pitch += 180.0F;
        }

        double xOffset = (double) (-MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)) * cameraDistance;
        double zOffset = (double) (MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)) * cameraDistance;
        double yOffset = (double) (-MathHelper.sin(pitch * 0.017453292F)) * cameraDistance;

        for (int i = 0; i < 8; ++i) {
            float xJitter = (float) ((i & 1) * 2 - 1) * 0.1F;
            float yJitter = (float) ((i >> 1 & 1) * 2 - 1) * 0.1F;
            float zJitter = (float) ((i >> 2 & 1) * 2 - 1) * 0.1F;
            Vec3d from = eyePos.add(xJitter, yJitter, zJitter);
            Vec3d to = new Vec3d(eyePos.x - xOffset + (double) xJitter + (double) zJitter, eyePos.y - yOffset + (double) yJitter, eyePos.z - zOffset + (double) zJitter);
            RayTraceResult result = this.mc.world.rayTraceBlocks(from, to);
            if (result != null) {
                double hitDistance = result.hitVec.distanceTo(eyePos);
                if (hitDistance < cameraDistance) {
                    cameraDistance = hitDistance;
                }
            }
        }

        xOffset = (double) (-MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)) * cameraDistance;
        zOffset = (double) (MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)) * cameraDistance;
        yOffset = (double) (-MathHelper.sin(pitch * 0.017453292F)) * cameraDistance;
        return new Vec3d(eyePos.x - xOffset, eyePos.y - yOffset, eyePos.z - zOffset);
    }

    @Unique
    private float getCameraEyeHeight(Entity entity, float partialTicks) {
        if (entity instanceof EntityPlayer && !IntegrationManager.isRandomPatchesEnabled()) {
            return MathHelperNew.lerp(partialTicks, this.previousEyeHeight, this.eyeHeight);
        }
        return entity.getEyeHeight();
    }

    @Unique
    private float getWaterHeight(World world, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            float filled = ((IFluidBlock) block).getFilledPercentage(world, pos);
            return filled < 0.0F ? filled + 1.0F : filled;
        }
        if (block instanceof BlockLiquid) {
            return BlockLiquid.getBlockLiquidHeight(state, world, pos);
        }
        float height = block.getBlockLiquidHeight(world, pos, state, Material.WATER);
        return height > 0.0F ? height : 1.0F;
    }
    // Backport end - Camera logic from modern versions

    /**
     * This mixin is marked as not required, as some mods patch this themselves.
     */
    @Redirect(
            method = "renderWorldPass",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;isInsideOfMaterial(Lnet/minecraft/block/material/Material;)Z",
                    ordinal = 0
            ),
            require = 0,
            expect = 0
    )
    private boolean ignoreWater(Entity entity, Material material) {
        /* 1.13 removed this check */
        if (material == Material.WATER) {
            return false;
        }
        return entity.isInsideOfMaterial(material);
    }
}