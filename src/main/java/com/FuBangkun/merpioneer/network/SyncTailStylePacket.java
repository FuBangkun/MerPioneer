package com.FuBangkun.merpioneer.network;

import com.FuBangkun.merpioneer.MerPioneerCapability;
import com.FuBangkun.merpioneer.TailStyleData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SyncTailStylePacket implements IMessage {
    private NBTTagCompound nbt;
    private int playerId; // 新增：用来标识这个数据属于哪个玩家（同步给周围人时需要）

    public SyncTailStylePacket() {
    }

    // 客户端发给服务端用这个（默认自己）
    public SyncTailStylePacket(NBTTagCompound nbt) {
        this.nbt = nbt;
        this.playerId = -1;
    }

    // 服务端同步给客户端用这个
    public SyncTailStylePacket(int playerId, NBTTagCompound nbt) {
        this.nbt = nbt;
        this.playerId = playerId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, nbt);
    }

    public static class Handler implements IMessageHandler<SyncTailStylePacket, IMessage> {
        @Override
        public IMessage onMessage(SyncTailStylePacket message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                // ---------------- 服务端收到客户端的保存请求 ----------------
                ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                    EntityPlayerMP player = ctx.getServerHandler().player;
                    if (player != null) {
                        TailStyleData cap = player.getCapability(MerPioneerCapability.TAIL_STYLE_CAPABILITY, null);
                        if (cap != null) {
                            cap.loadFromNBT(message.nbt);

                            // 【核心修复 1】服务器收到新数据后，立刻广播给周围所有人（包括自己）
                            NetworkHandler.INSTANCE.sendToAllTracking(new SyncTailStylePacket(player.getEntityId(), message.nbt), player);
                            // 同时也发给自己（确保完全同步）
                            NetworkHandler.INSTANCE.sendTo(new SyncTailStylePacket(player.getEntityId(), message.nbt), player);
                        }
                    }
                });
            } else {
                // ---------------- 客户端收到服务端的同步请求 ----------------
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    EntityPlayer clientPlayer = Minecraft.getMinecraft().player;
                    if (clientPlayer == null) return;

                    // 根据 playerId 找到对应的玩家世界实体（有可能是自己，也有可能是别人）
                    EntityPlayer targetPlayer = (message.playerId == -1 || message.playerId == clientPlayer.getEntityId())
                            ? clientPlayer
                            : (EntityPlayer) clientPlayer.world.getEntityByID(message.playerId);

                    if (targetPlayer != null) {
                        TailStyleData cap = targetPlayer.getCapability(MerPioneerCapability.TAIL_STYLE_CAPABILITY, null);
                        if (cap != null) {
                            cap.loadFromNBT(message.nbt);
                        }
                    }
                });
            }
            return null;
        }
    }
}