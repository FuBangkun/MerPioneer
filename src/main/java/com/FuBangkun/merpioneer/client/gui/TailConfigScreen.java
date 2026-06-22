package com.FuBangkun.merpioneer.client.gui;

import com.FuBangkun.merpioneer.MerPioneerCapability;
import com.FuBangkun.merpioneer.TailStyleData;
import com.FuBangkun.merpioneer.network.NetworkHandler;
import com.FuBangkun.merpioneer.network.SyncTailStylePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;

public class TailConfigScreen extends GuiScreen {
    private static final String[] TEXTURE_OPTIONS = new String[]{
            "merpioneer:textures/tail/tail.png",
            "merpioneer:textures/tail/tail_ariel.png",
            "merpioneer:textures/tail/tail_h2o.png"
    };
    private boolean hasBra;
    private boolean hasGradient;
    private int tailColor = 0xFFFFFF;
    private int braColor = 0xFFFFFF;
    private int gradientColor = 0xFFFFFF;
    private String selectedTexture = "merpioneer:textures/tail/tail.png";
    private GuiTextField tailColorField;
    private GuiTextField braColorField;
    private GuiTextField gradientColorField;

    @Override
    public void initGui() {
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (player != null) {
            TailStyleData cap = player.getCapability(MerPioneerCapability.TAIL_STYLE_CAPABILITY, null);
            if (cap != null) {
                hasBra = cap.isHasBra();
                hasGradient = cap.isHasGradient();
                tailColor = cap.getTailColor();
                braColor = cap.getBraColor();
                gradientColor = cap.getGradientColor();

                if (cap.getTexture() != null) {
                    selectedTexture = cap.getTexture();
                }
            }

            this.buttonList.add(new GuiButton(1, width / 2 - 100, height / 2 - 80, 200, 20, I18n.format("gui.merpioneer.bra") + ": " + hasBra));
            this.buttonList.add(new GuiButton(2, width / 2 - 100, height / 2 - 55, 200, 20, getStyleLabelByTexture()));

            tailColorField = new GuiTextField(0, fontRenderer, width / 2 - 20, height / 2 - 30, 120, 20);
            braColorField = new GuiTextField(1, fontRenderer, width / 2 - 20, height / 2 - 5, 120, 20);
            gradientColorField = new GuiTextField(2, fontRenderer, width / 2 - 20, height / 2 + 45, 120, 20);

            tailColorField.setText(toHex(tailColor));
            braColorField.setText(toHex(braColor));
            gradientColorField.setText(toHex(gradientColor));

            this.buttonList.add(new GuiButton(3, width / 2 - 100, height / 2 + 20, 200, 20, I18n.format("gui.merpioneer.gradient") + ": " + hasGradient));
            this.buttonList.add(new GuiButton(4, width / 2 - 100, height / 2 + 70, 200, 20, I18n.format("gui.merpioneer.save")));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        EntityPlayer player = Minecraft.getMinecraft().player;

        switch (button.id) {

            case 1:
                hasBra = !hasBra;
                button.displayString = I18n.format("gui.merpioneer.bra") + ": " + hasBra;
                break;

            case 2:
                cycleTexture(button);
                break;

            case 3:
                hasGradient = !hasGradient;
                button.displayString = I18n.format("gui.merpioneer.gradient") + ": " + hasGradient;
                break;

            case 4:
                tailColor = parseColor(tailColorField.getText(), tailColor);
                braColor = parseColor(braColorField.getText(), braColor);
                gradientColor = parseColor(gradientColorField.getText(), gradientColor);

                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setInteger("tailColor", tailColor);
                nbt.setBoolean("hasBra", hasBra);
                nbt.setInteger("braColor", braColor);
                nbt.setBoolean("hasGradient", hasGradient);
                nbt.setInteger("gradientColor", gradientColor);
                nbt.setString("texture", selectedTexture);

                TailStyleData cap = player.getCapability(MerPioneerCapability.TAIL_STYLE_CAPABILITY, null);
                if (cap != null) {
                    cap.loadFromNBT(nbt);
                }

                NetworkHandler.INSTANCE.sendToServer(new SyncTailStylePacket(nbt));

                Minecraft.getMinecraft().displayGuiScreen(null);
                break;
        }
    }

    private void cycleTexture(GuiButton button) {
        int idx = 0;
        for (int i = 0; i < TEXTURE_OPTIONS.length; i++) {
            if (TEXTURE_OPTIONS[i].equals(selectedTexture)) {
                idx = i;
                break;
            }
        }
        idx = (idx + 1) % TEXTURE_OPTIONS.length;
        selectedTexture = TEXTURE_OPTIONS[idx];

        button.displayString = getStyleLabelByIndex(idx);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        tailColorField.textboxKeyTyped(typedChar, keyCode);
        braColorField.textboxKeyTyped(typedChar, keyCode);
        gradientColorField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        tailColorField.mouseClicked(mouseX, mouseY, mouseButton);
        braColorField.mouseClicked(mouseX, mouseY, mouseButton);
        gradientColorField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        tailColorField.updateCursorCounter();
        braColorField.updateCursorCounter();
        gradientColorField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawCenteredString(fontRenderer, I18n.format("gui.merpioneer.tail_config.title"), width / 2, 20, 0xFFFFFF);

        drawString(fontRenderer, I18n.format("gui.merpioneer.tail_color"), width / 2 - 100, height / 2 - 24, 0xFFFFFF);

        drawString(fontRenderer, I18n.format("gui.merpioneer.bra_color"), width / 2 - 100, height / 2 + 1, 0xFFFFFF);

        drawString(fontRenderer, I18n.format("gui.merpioneer.gradient_color"), width / 2 - 100, height / 2 + 51, 0xFFFFFF);

        tailColorField.drawTextBox();
        braColorField.drawTextBox();
        gradientColorField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int parseColor(String text, int fallback) {
        try {
            if (text.startsWith("#")) text = text.substring(1);
            return Integer.parseInt(text, 16) & 0xFFFFFF;
        } catch (Exception e) {
            return fallback;
        }
    }

    private String toHex(int color) {
        StringBuilder hex = new StringBuilder(Integer.toHexString(color & 0xFFFFFF).toUpperCase());
        while (hex.length() < 6) hex.insert(0, "0");
        return hex.toString();
    }

    private String getStyleLabelByIndex(int idx) {
        switch (idx) {
            case 1:
                return I18n.format("gui.merpioneer.style.ariel");
            case 2:
                return I18n.format("gui.merpioneer.style.h2o");
            default:
                return I18n.format("gui.merpioneer.style.default");
        }
    }

    private String getStyleLabelByTexture() {
        int idx = 0;
        for (int i = 0; i < TEXTURE_OPTIONS.length; i++) {
            if (TEXTURE_OPTIONS[i].equals(selectedTexture)) {
                idx = i;
                break;
            }
        }
        return getStyleLabelByIndex(idx);
    }
}