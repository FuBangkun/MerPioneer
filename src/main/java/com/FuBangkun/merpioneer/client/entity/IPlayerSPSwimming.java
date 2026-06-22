package com.FuBangkun.merpioneer.client.entity;

public interface IPlayerSPSwimming {
    boolean isActuallySneaking();

    boolean isForcedDown();

    boolean isUsingSwimmingAnimation();

    boolean isUsingSwimmingAnimation(float moveForward, float moveStrafe);

    boolean canSwim();

    boolean isMovingForward(float moveForward, float moveStrafe);

    boolean canPerformElytraTakeoff();
}