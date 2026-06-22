package com.FuBangkun.merpioneer.core.minecraft;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World {
    private WorldServerMixin(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Shadow
    protected abstract void createBonusChest();

    @Unique
    private BlockPos findSeaFloor(World world, int x, int z) {
        int sea = world.getSeaLevel();
        for (int y = sea; y > 0; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            IBlockState state = world.getBlockState(pos);
            IBlockState above = world.getBlockState(pos.up());
            if (state.isFullBlock() && above.getMaterial() == Material.WATER) {
                return pos.up();
            }
        }
        return new BlockPos(x, sea - 10, z);
    }

    @Inject(method = "createSpawnPosition", at = @At("HEAD"), cancellable = true)
    private void redirectCreateSpawnPosition(WorldSettings settings, CallbackInfo ci) {
        WorldProvider provider = this.provider;
        if (!provider.canRespawnHere()) {
            this.getWorldInfo().setSpawn(BlockPos.ORIGIN.up(provider.getAverageGroundLevel()));
            ci.cancel();
            return;
        }
        this.findingSpawnPoint = true;
        BiomeProvider biomeprovider = provider.getBiomeProvider();
        List<Biome> oceanBiomes = Collections.singletonList(Biomes.OCEAN);
        Random random = new Random(this.getSeed());
        BlockPos blockpos = biomeprovider.findBiomePosition(0, 0, 2560, oceanBiomes, random);
        int spawnX = 0;
        int spawnZ = 0;
        int spawnY = findSeaFloor(this, spawnX, spawnZ).getY();
        if (blockpos != null) {
            spawnX = blockpos.getX();
            spawnZ = blockpos.getZ();
        }
        this.getWorldInfo().setSpawn(new BlockPos(spawnX, spawnY, spawnZ));
        this.findingSpawnPoint = false;
        if (settings.isBonusChestEnabled()) {
            this.createBonusChest();
        }
        ci.cancel();
    }
}