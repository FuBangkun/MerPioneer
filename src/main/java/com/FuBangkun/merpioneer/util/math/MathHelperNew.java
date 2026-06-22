package com.FuBangkun.merpioneer.util.math;

public class MathHelperNew {
    public static float lerp(float pct, float start, float end) {
        return start + pct * (end - start);
    }
}