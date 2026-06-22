package com.FuBangkun.merpioneer.client.render;

import com.FuBangkun.merpioneer.config.ConfigHandler;
import com.FuBangkun.merpioneer.entity.player.IPlayerResizeable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.DoubleBuffer;

public class FPVRenderHandler {
    public static boolean isRenderingFirstPerson = false;
    public static ItemStack currentRenderingMapMain;
    public static ItemStack currentRenderingMapOff;
    private static DataParameter<Byte> PLAYER_MODEL_FLAG_CACHE;

    static {
        currentRenderingMapMain = ItemStack.EMPTY;
        currentRenderingMapOff = ItemStack.EMPTY;
    }

    private final DoubleBuffer clippingBuffer = BufferUtils.createDoubleBuffer(4);

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        if (ConfigHandler.FPV_CONFIG.enableFPV) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (ConfigHandler.FPV_CONFIG.enableFPV) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.player;
            if (mc.gameSettings.thirdPersonView == 0 && player != null && !player.isSpectator()) {
                this.renderFirstPersonPlayer(player, event.getPartialTicks());
            }
        }
    }

    private void renderFirstPersonPlayer(EntityPlayer player, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        Render<? extends EntityPlayer> render = mc.getRenderManager().getEntityRenderObject(player);
        if (render instanceof RenderPlayer) {
            if (player instanceof AbstractClientPlayer) {
                RenderPlayer renderPlayer = (RenderPlayer) render;
                AbstractClientPlayer clientPlayer = (AbstractClientPlayer) player;
                GlStateManager.pushMatrix();
                double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
                double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
                double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
                double dx = d0 - mc.getRenderManager().viewerPosX;
                double dy = d1 - mc.getRenderManager().viewerPosY;
                double dz = d2 - mc.getRenderManager().viewerPosZ;
                float renderYawOffset = this.interpolateRotation(player.prevRenderYawOffset, player.renderYawOffset, partialTicks);
                double bodyOffset = 0.25F;

                IPlayerResizeable r = (IPlayerResizeable) player;
                float swimAnim = r.getSwimAnimation(partialTicks);

                if (r.isActuallySwimming() || swimAnim > 0.0F) {
                    bodyOffset += 0.65 * (double) swimAnim;
                }

                float pitchRad = (float) Math.toRadians(player.rotationPitch);
                float yawRad = (float) Math.toRadians(renderYawOffset);

                double pitchFactor = Math.abs(Math.sin(pitchRad));
                double antiClipOffset = (swimAnim > 0.0F) ? 0.18F * pitchFactor : 0.04F * pitchFactor;

                dx += (bodyOffset + antiClipOffset) * Math.sin(yawRad);
                dz -= (bodyOffset + antiClipOffset) * Math.cos(yawRad);

                if (swimAnim > 0.0F) {
                    if (player.rotationPitch < 0) {
                        dy -= 0.15F * Math.cos(pitchRad);
                    } else {
                        dy -= 0.08F * pitchFactor;
                    }
                }

                if (player.isElytraFlying()) {
                    GlStateManager.translate(0.0F, -0.6, 0.0F);
                }

                if (player.isRiding()) {
                    GlStateManager.translate(0.0F, 0.4, 0.0F);
                }

                GlStateManager.translate(dx, dy, dz);
                isRenderingFirstPerson = true;
                int i = player.getBrightnessForRender();
                int j = i % 65536;
                int k = i / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
                mc.entityRenderer.enableLightmap();
                GlStateManager.enableLighting();
                GlStateManager.enableRescaleNormal();
                RenderHelper.enableStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                ItemStack helmet = player.inventory.armorItemInSlot(3);
                ItemStack chest = player.inventory.armorItemInSlot(2);
                ItemStack mainHand = player.getHeldItemMainhand();
                ItemStack offHand = player.getHeldItemOffhand();
                currentRenderingMapMain = mainHand;
                currentRenderingMapOff = offHand;
                if (!ConfigHandler.FPV_CONFIG.enableHelm) {
                    player.inventory.armorInventory.set(3, ItemStack.EMPTY);
                }

                if (!ConfigHandler.FPV_CONFIG.enableChest) {
                    player.inventory.armorInventory.set(2, ItemStack.EMPTY);
                }

                boolean hasMap = !mainHand.isEmpty() && mainHand.getItem() instanceof ItemMap;
                boolean hasOffMap = !offHand.isEmpty() && offHand.getItem() instanceof ItemMap;
                if (hasMap) {
                    player.inventory.mainInventory.set(player.inventory.currentItem, ItemStack.EMPTY);
                }

                if (hasOffMap) {
                    player.inventory.offHandInventory.set(0, ItemStack.EMPTY);
                }

                byte originalModelParts = 0;
                boolean capeHidden = false;
                if (!ConfigHandler.FPV_CONFIG.enableCape && player.isWearing(EnumPlayerModelParts.CAPE)) {
                    try {
                        DataParameter<Byte> modelPartsParam = this.getPlayerModelFlag();
                        if (modelPartsParam != null) {
                            originalModelParts = player.getDataManager().get(modelPartsParam);
                            int mask = EnumPlayerModelParts.CAPE.getPartMask();
                            byte newModelParts = (byte) (originalModelParts & ~mask);
                            player.getDataManager().set(modelPartsParam, newModelParts);
                            capeHidden = true;
                        }
                    } catch (Exception ignored) {
                    }
                }

                boolean clippingActive = ConfigHandler.FPV_CONFIG.enableClipping;
                if (clippingActive) {
                    this.setupClippingPlane(ConfigHandler.FPV_CONFIG.clippingHeight);
                }

                renderPlayer.doRender(clientPlayer, 0.0F, 0.0F, 0.0F, 0.0F, partialTicks);
                if (clippingActive) {
                    this.disableClippingPlane();
                }

                if (hasMap) {
                    player.inventory.mainInventory.set(player.inventory.currentItem, mainHand);
                }

                if (hasOffMap) {
                    player.inventory.offHandInventory.set(0, offHand);
                }

                RenderHelper.disableStandardItemLighting();
                player.inventory.armorInventory.set(3, helmet);
                player.inventory.armorInventory.set(2, chest);
                if (capeHidden) {
                    try {
                        DataParameter<Byte> modelPartsParam = this.getPlayerModelFlag();
                        if (modelPartsParam != null) {
                            player.getDataManager().set(modelPartsParam, originalModelParts);
                        }
                    } catch (Exception ignored) {
                    }
                }

                mc.entityRenderer.disableLightmap();
                isRenderingFirstPerson = false;
                GlStateManager.popMatrix();
            }
        }
    }

    private void setupClippingPlane(double height) {
        this.clippingBuffer.clear();
        this.clippingBuffer.put(0.0F).put(-1.0F).put(0.0F).put(height);
        this.clippingBuffer.flip();
        GL11.glClipPlane(12288, this.clippingBuffer);
        GL11.glEnable(12288);
    }

    private void disableClippingPlane() {
        GL11.glDisable(12288);
    }

    private DataParameter<Byte> getPlayerModelFlag() {
        if (PLAYER_MODEL_FLAG_CACHE == null) {
            try {
                PLAYER_MODEL_FLAG_CACHE = ObfuscationReflectionHelper.getPrivateValue(EntityPlayer.class, null, "field_184827_bp");
            } catch (Exception ignored) {
            }
        }

        return PLAYER_MODEL_FLAG_CACHE;
    }

    private float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
        float f = yawOffset - prevYawOffset;

        while (f < -180.0F) {
            f += 360.0F;
        }

        while (f >= 180.0F) {
            f -= 360.0F;
        }

        return prevYawOffset + partialTicks * f;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (isRenderingFirstPerson && event.getEntityPlayer().equals(Minecraft.getMinecraft().player)) {
            ModelPlayer model = event.getRenderer().getMainModel();
            if (!ConfigHandler.FPV_CONFIG.enableHead) {
                model.bipedHead.isHidden = true;
                model.bipedHeadwear.isHidden = true;
            }

            if (!ConfigHandler.FPV_CONFIG.enableHelm) {
                model.bipedHeadwear.isHidden = true;
            }

            if (!ConfigHandler.FPV_CONFIG.enableBody) {
                model.bipedBody.isHidden = true;
                model.bipedBodyWear.isHidden = true;
            }

            if (!ConfigHandler.FPV_CONFIG.enableArms) {
                model.bipedRightArm.isHidden = true;
                model.bipedLeftArm.isHidden = true;
                model.bipedRightArmwear.isHidden = true;
                model.bipedLeftArmwear.isHidden = true;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (isRenderingFirstPerson && event.getEntityPlayer().equals(Minecraft.getMinecraft().player)) {
            ModelPlayer model = event.getRenderer().getMainModel();
            model.bipedHead.isHidden = false;
            model.bipedHeadwear.isHidden = false;
            model.bipedBody.isHidden = false;
            model.bipedBodyWear.isHidden = false;
            model.bipedRightArm.isHidden = false;
            model.bipedLeftArm.isHidden = false;
            model.bipedRightArmwear.isHidden = false;
            model.bipedLeftArmwear.isHidden = false;
        }
    }
}
