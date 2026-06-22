package com.FuBangkun.merpioneer; // 请根据你的项目实际包路径修改

import com.FuBangkun.merpioneer.network.NetworkHandler;
import com.FuBangkun.merpioneer.network.SyncTailStylePacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class MerPioneerCapability {
    @CapabilityInject(TailStyleData.class)
    public static Capability<TailStyleData> TAIL_STYLE_CAPABILITY = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(TailStyleData.class, new Capability.IStorage<TailStyleData>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<TailStyleData> capability, TailStyleData instance, EnumFacing side) {
                if (instance == null) return new NBTTagCompound();
                return instance.saveToNBT();
            }

            @Override
            public void readNBT(Capability<TailStyleData> capability, TailStyleData instance, EnumFacing side, NBTBase nbt) {
                if (instance != null && nbt instanceof NBTTagCompound) {
                    instance.loadFromNBT((NBTTagCompound) nbt);
                }
            }
        }, TailStyleData::new);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(
                    new ResourceLocation("merpioneer", "tail_style"),
                    new TailStyleProvider()
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer oldPlayer = event.getOriginal();
        EntityPlayer newPlayer = event.getEntityPlayer();

        if (oldPlayer.hasCapability(TAIL_STYLE_CAPABILITY, null) && newPlayer.hasCapability(TAIL_STYLE_CAPABILITY, null)) {
            TailStyleData oldCap = oldPlayer.getCapability(TAIL_STYLE_CAPABILITY, null);
            TailStyleData newCap = newPlayer.getCapability(TAIL_STYLE_CAPABILITY, null);

            if (oldCap != null && newCap != null) {
                NBTTagCompound nbt = oldCap.saveToNBT();
                newCap.loadFromNBT(nbt);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            TailStyleData cap = player.getCapability(TAIL_STYLE_CAPABILITY, null);
            if (cap != null) {
                NetworkHandler.INSTANCE.sendTo(new SyncTailStylePacket(player.getEntityId(), cap.saveToNBT()), player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            TailStyleData cap = player.getCapability(TAIL_STYLE_CAPABILITY, null);
            if (cap != null) {
                NetworkHandler.INSTANCE.sendTo(new SyncTailStylePacket(player.getEntityId(), cap.saveToNBT()), player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntityPlayerMP) {
            EntityPlayerMP targetPlayer = (EntityPlayerMP) event.getTarget();
            TailStyleData cap = targetPlayer.getCapability(TAIL_STYLE_CAPABILITY, null);
            if (cap != null) {
                EntityPlayerMP watcher = (EntityPlayerMP) event.getEntityPlayer();
                NetworkHandler.INSTANCE.sendTo(new SyncTailStylePacket(targetPlayer.getEntityId(), cap.saveToNBT()), watcher);
            }
        }
    }

    public static class TailStyleProvider implements ICapabilitySerializable<NBTTagCompound> {
        private final TailStyleData instance = new TailStyleData();

        @Override
        public boolean hasCapability(@Nonnull Capability<?> cap, @Nullable EnumFacing side) {
            return cap == TAIL_STYLE_CAPABILITY;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
            return cap == TAIL_STYLE_CAPABILITY ? TAIL_STYLE_CAPABILITY.cast(this.instance) : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return instance.saveToNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            if (nbt != null) {
                instance.loadFromNBT(nbt);
            }
        }
    }
}