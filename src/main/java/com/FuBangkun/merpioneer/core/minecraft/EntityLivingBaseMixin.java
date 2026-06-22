package com.FuBangkun.merpioneer.core.minecraft;

import com.FuBangkun.merpioneer.config.ConfigHandler;
import com.FuBangkun.merpioneer.entity.player.IPlayerResizeable;
import com.FuBangkun.merpioneer.proxy.CommonProxy;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin extends Entity {
    @Unique
    private static final DamageSource LAND_SUFFOCATION = new DamageSource("land_suffocation").setDamageBypassesArmor();

    public EntityLivingBaseMixin(World worldIn) {
        super(worldIn);
    }

    @Accessor(value = "isJumping")
    public abstract boolean isJumping();

    @Unique
    private float getAirSpeedMultiplier() {
        float air = Math.max(0, Math.min(300, this.getAir()));
        float airPercentage = air / 300.0F;
        return 0.25F + 0.75F * airPercentage;
    }

    @ModifyVariable(method = "travel", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float modifyStrafe(float original) {
        if ((EntityLivingBase) (Object) this instanceof EntityPlayer && !this.isInWater()) {
            return original * getAirSpeedMultiplier();
        }
        return original;
    }

    @ModifyVariable(method = "travel", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private float modifyVertical(float original) {
        if ((EntityLivingBase) (Object) this instanceof EntityPlayer && !this.isInWater()) {
            return original * getAirSpeedMultiplier();
        }
        return original;
    }

    @ModifyVariable(method = "travel", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private float modifyForward(float original) {
        if ((EntityLivingBase) (Object) this instanceof EntityPlayer && !this.isInWater()) {
            return original * getAirSpeedMultiplier();
        }
        return original;
    }

    @Inject(method = "jump", at = @At("TAIL"))
    private void heavilyNerfLandJump(CallbackInfo ci) {
        if ((EntityLivingBase) (Object) this instanceof EntityPlayer && !this.isInWater()) {
            float air = Math.max(0, Math.min(300, this.getAir()));
            double jumpMultiplier = 0.4D + 0.6D * (air / 300.0D);
            this.motionX *= jumpMultiplier;
            this.motionY *= jumpMultiplier;
            this.motionZ *= jumpMultiplier;
        }
    }

    @Inject(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;move(Lnet/minecraft/entity/MoverType;DDD)V",
                    ordinal = 2
            )
    )
    private void modifySwimSpeed(float strafe, float vertical, float forward, CallbackInfo ci) {
        if ((EntityLivingBase) (Object) this instanceof EntityPlayer && this.isInWater()) {
            double multiplier = ConfigHandler.MOVEMENT_CONFIG.swimSpeedMultiplier;
            this.motionX *= multiplier;
            this.motionZ *= multiplier;
        }
    }

    @WrapOperation(
            method = "onEntityUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;isInsideOfMaterial(Lnet/minecraft/block/material/Material;)Z"
            )
    )
    private boolean checkBubbleAndWaterBreath(EntityLivingBase instance, Material material, Operation<Boolean> original) {
        if (material == Material.WATER) {
            if (instance instanceof EntityPlayer) {
                return !instance.isInsideOfMaterial(Material.WATER);
            }

            return isLosingAir();
        }

        return original.call(instance, material);
    }

    @Unique
    private boolean isLosingAir() {
        if (ConfigHandler.MISCELLANEOUS_CONFIG.bubbleColumns && this.world.getBlockState(new BlockPos(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ)).getBlock() == CommonProxy.BUBBLE_COLUMN) {
            return false; // Pretend not to be in water
        }
        return this.isInsideOfMaterial(Material.WATER);
    }

    @ModifyArg(method = "onEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;setAir(I)V"), index = 0)
    private int getNewAirValue(int original) {
        if (ConfigHandler.MISCELLANEOUS_CONFIG.slowAirReplenish && original == 300 && this.getAir() >= -20) {
            boolean shouldReplenish;
            if ((Object) this instanceof EntityPlayer) {
                shouldReplenish = this.isInsideOfMaterial(Material.WATER);
            } else {
                shouldReplenish = !isLosingAir();
            }

            if (shouldReplenish) {
                int oldAirValue = Math.max(this.getAir(), 0);
                return Math.min(oldAirValue + 4, 300);
            }
        }
        return original;
    }

    @Redirect(
            method = "onEntityUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"
            )
    )
    private void redirectSpawnParticle(World world, EnumParticleTypes particleType, double x, double y, double z, double mx, double my, double mz, int[] auth) {
        if (particleType == EnumParticleTypes.WATER_BUBBLE) {
            return;
        }
        world.spawnParticle(particleType, x, y, z, mx, my, mz, auth);
    }

    @Redirect(
            method = "onEntityUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"
            )
    )
    private boolean redirectDrownDamage(EntityLivingBase entity, DamageSource source, float amount) {
        if (source == DamageSource.DROWN && entity instanceof EntityPlayer) {
            if (!entity.isInsideOfMaterial(Material.WATER)) {
                return entity.attackEntityFrom(LAND_SUFFOCATION, amount);
            }
        }
        return entity.attackEntityFrom(source, amount);
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isSneaking()Z"))
    public boolean isSneaking(EntityLivingBase entity) {
        if (entity instanceof IPlayerResizeable) {
            return ((IPlayerResizeable) entity).isActuallySneaking();
        }
        return this.isSneaking();
    }

    @Redirect(method = "travel", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/EntityLivingBase;collidedHorizontally:Z", ordinal = 1))
    private boolean isJumpingOnLadder(EntityLivingBase instance) {
        if (ConfigHandler.MOVEMENT_CONFIG.newClimbingBehavior) {
            return instance.collidedHorizontally || ((EntityLivingBaseMixin) (Object) instance).isJumping();
        } else {
            return instance.collidedHorizontally;
        }
    }
}