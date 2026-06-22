package com.FuBangkun.merpioneer;

import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;

public class TailStyleData {
    @Getter
    private boolean hasBra;
    @Getter
    private boolean hasGradient;
    @Getter
    private int tailColor = 0xFFFFFF;
    @Getter
    private int braColor = 0xFFFFFF;
    @Getter
    private int gradientColor = 0xFFFFFF;
    @Getter
    private String texture = "merpioneer:textures/tail/tail.png";

    public void loadFromNBT(NBTTagCompound nbt) {
        this.tailColor = nbt.getInteger("tailColor");
        this.hasBra = nbt.getBoolean("hasBra");
        this.braColor = nbt.getInteger("braColor");
        this.hasGradient = nbt.getBoolean("hasGradient");
        this.gradientColor = nbt.getInteger("gradientColor");
        if (nbt.hasKey("texture")) {
            this.texture = nbt.getString("texture");
        }
    }

    public NBTTagCompound saveToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("tailColor", tailColor);
        nbt.setBoolean("hasBra", hasBra);
        nbt.setInteger("braColor", braColor);
        nbt.setBoolean("hasGradient", hasGradient);
        nbt.setInteger("gradientColor", gradientColor);
        nbt.setString("texture", texture);
        return nbt;
    }
}
