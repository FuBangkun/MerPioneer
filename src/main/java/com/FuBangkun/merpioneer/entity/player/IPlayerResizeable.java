package com.FuBangkun.merpioneer.entity.player;

import com.FuBangkun.merpioneer.entity.EntitySize;
import com.FuBangkun.merpioneer.entity.Pose;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPlayerResizeable {
    boolean canSwim();

    void updateSwimming();

    boolean getEyesInWaterPlayer();

    float getWaterVision();

    float getWidth();

    float getHeight();

    EntitySize getSize(Pose poseIn);

    void recalculateSize();

    boolean isResizingAllowed();

    boolean isActuallySneaking();

    float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn);

    Pose getPose();

    void setPose(Pose poseIn);

    boolean isPoseClear(Pose poseIn);

    boolean getShouldBeDead();

    boolean isSwimming();

    void setSwimming(boolean flag);

    boolean isActuallySwimming();

    @SideOnly(Side.CLIENT)
    boolean isVisuallySwimming();

    float getSwimAnimation(float partialTicks);

    boolean canForceCrawling();

    boolean isForcingCrawling();

    void setForcingCrawling(boolean flag);
}