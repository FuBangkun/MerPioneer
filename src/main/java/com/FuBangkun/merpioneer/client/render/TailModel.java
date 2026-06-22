package com.FuBangkun.merpioneer.client.render;

import com.FuBangkun.merpioneer.TailStyleData;
import com.FuBangkun.merpioneer.entity.player.IPlayerResizeable;
import com.FuBangkun.merpioneer.util.math.MathHelperNew;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class TailModel extends ModelBase {
    private static final float r = (float) (Math.PI / 180);
    private final ModelRenderer main;
    private final ModelRenderer waist;
    private final ModelRenderer bra;
    private final ModelRenderer tail1;
    private final ModelRenderer tail2;
    private final ModelRenderer tail3;
    private final ModelRenderer tail4;
    private final ModelRenderer tail5;
    private final ModelRenderer tail6;
    private final ModelRenderer tail7;
    private final ModelRenderer fin1;
    private final ModelRenderer fin2;
    private final ModelRenderer[] tailParts;

    public TailModel() {
        this.textureWidth = 48;
        this.textureHeight = 61;

        // 主躯干和衣服（整数尺寸直接用原生 addBox 即可）
        main = new ModelRenderer(this);
        main.addBox(-4, 0, -2, 8, 12, 4);

        waist = new ModelRenderer(this, 24, 0);
        waist.addBox(-4, 0, -2, 8, 12, 4);

        bra = new ModelRenderer(this, 24, 16);
        bra.addBox(-4, 0, -2, 8, 12, 4);

        // ==========================================
        // 核心改造：使用 Tessellator 画出精确的浮点盒子
        // ==========================================
        tail1 = new ModelRenderer(this, 0, 0);
        addPreciseBox(tail1, 0, 0, -4F, 0F, -2F, 8F, 4F, 4F);
        tail1.setRotationPoint(0, 12, 0);

        tail2 = new ModelRenderer(this, 0, 8);
        addPreciseBox(tail2, 0, 8, -3.75F, 0F, -1.75F, 7.5F, 3F, 3.5F);
        tail2.setRotationPoint(0, 4, 0);

        tail3 = new ModelRenderer(this, 0, 15);
        addPreciseBox(tail3, 0, 15, -3.5F, 0F, -1.5F, 7F, 2F, 3F);
        tail3.setRotationPoint(0, 3, 0);

        tail4 = new ModelRenderer(this, 0, 20);
        addPreciseBox(tail4, 0, 20, -3.25F, 0F, -1.25F, 6.5F, 2F, 2.5F);
        tail4.setRotationPoint(0, 2, 0);

        tail5 = new ModelRenderer(this, 0, 25);
        addPreciseBox(tail5, 0, 25, -3F, 0F, -1F, 6F, 2F, 2F);
        tail5.setRotationPoint(0, 2, 0);

        tail6 = new ModelRenderer(this, 0, 29);
        addPreciseBox(tail6, 0, 29, -2.75F, 0F, -0.75F, 5.5F, 2F, 1.5F);
        tail6.setRotationPoint(0, 2, 0);

        tail7 = new ModelRenderer(this, 0, 33);
        addPreciseBox(tail7, 0, 33, -2.5F, 0F, -0.5F, 5F, 2F, 1F);
        tail7.setRotationPoint(0, 2, 0);

        fin1 = new ModelRenderer(this, 0, 36);
        addPreciseBox(fin1, 0, 36, -11.5F, 0F, 0.02F, 23F, 24F, 1F);
        fin1.setRotationPoint(0, 2, 0);

        fin2 = new ModelRenderer(this, 0, 36);
        addPreciseBox(fin2, 0, 36, -11.5F, 0F, -0.04F, 23F, 24F, 1F);
        fin2.rotateAngleY = 180 * r;

        this.tailParts = new ModelRenderer[]{tail1, tail2, tail3, tail4, tail5, tail6, tail7, fin1};
        fin1.addChild(fin2);
    }

    private static float sine(int a, int b, float t) {
        return (float) ((double) (-(b - a)) / 2 * (Math.cos(Math.PI * t) - 1) + a);
    }

    private static float angle(float a, int b, float c) {
        float d = ((a - b) * c) % 40;
        float e = ((a - b) * c) % 80;
        return e < 40 ? sine(-20, 20, d / 40) : sine(20, -20, d / 40);
    }

    private static float angleWithAmp(float a, int b, float c, int amp) {
        float d = ((a - b) * c) % 40;
        float e = ((a - b) * c) % 80;
        return e < 40 ? sine(-amp, amp, d / 40) : sine(amp, -amp, d / 40);
    }

    private void swimPose(float age) {
        tail1.rotateAngleX = tail2.rotateAngleX = angle(age, 0, 2.5F) * r;
        tail3.rotateAngleX = angle(age, 5, 2.5F) * r;
        tail4.rotateAngleX = tail5.rotateAngleX = angle(age, 10, 2.5F) * r;
        tail6.rotateAngleX = tail7.rotateAngleX = fin1.rotateAngleX = angle(age, 15, 2.5F) * r;
    }

    private void beachedPose(float age) {
        float speed = 10.0F;
        int amp = 4;
        tail1.rotateAngleX = tail2.rotateAngleX = angleWithAmp(age, 0, speed, amp) * r;
        tail3.rotateAngleX = angleWithAmp(age, 5, speed, amp) * r;
        tail4.rotateAngleX = tail5.rotateAngleX = angleWithAmp(age, 10, speed, amp) * r;
        tail6.rotateAngleX = tail7.rotateAngleX = fin1.rotateAngleX = angleWithAmp(age, 15, speed, amp) * r;
    }

    private void idlePose(float angle) {
        tail1.rotateAngleX = angle * r;
        tail2.rotateAngleX = tail3.rotateAngleX = tail4.rotateAngleX = tail5.rotateAngleX = tail6.rotateAngleX = tail7.rotateAngleX = fin1.rotateAngleX = 0;
    }

    private void landPose() {
        tail1.rotateAngleX = tail2.rotateAngleX = tail3.rotateAngleX = tail4.rotateAngleX = tail5.rotateAngleX = tail6.rotateAngleX = tail7.rotateAngleX = 14 * r;
        fin1.rotateAngleX = 0;
    }

    public void updatePose(EntityPlayer player, ModelBiped bipedModel, float ageInTicks) {
        this.setModelAttributes(bipedModel);
        ModelBase.copyModelAngles(bipedModel.bipedBody, this.main);
        if (player.isPlayerSleeping()) {
            idlePose(0);
        } else if (player.isRiding()) {
            idlePose(-90);
        } else if (!player.isInWater() && player.getAir() <= 0) {
            beachedPose(ageInTicks);
        } else if (player.onGround && !((IPlayerResizeable) player).isSwimming()) {
            landPose();
        } else {
            swimPose(ageInTicks);
        }
    }

    public void renderTail(float scale, TailStyleData style, RenderPlayer renderPlayer) {
        int tc = style.getTailColor();
        float tailRed = (tc >> 16 & 255) / 255.0F;
        float tailGreen = (tc >> 8 & 255) / 255.0F;
        float tailBlue = (tc & 255) / 255.0F;

        int gc = style.getGradientColor();
        float gradRed = (gc >> 16 & 255) / 255.0F;
        float gradGreen = (gc >> 8 & 255) / 255.0F;
        float gradBlue = (gc & 255) / 255.0F;

        boolean hasGradient = style.isHasGradient();

        renderPlayer.bindTexture(new ResourceLocation(style.getTexture()));

        GlStateManager.pushMatrix();
        this.main.postRender(scale);
        GlStateManager.scale(1.01F, 1.01F, 1.01F);
        GlStateManager.translate(0, -0.00375F, 0);

        GlStateManager.color(tailRed, tailGreen, tailBlue, 1.0F);
        this.waist.render(scale);

        if (style.isHasBra()) {
            int bc = style.getBraColor();
            GlStateManager.color((bc >> 16 & 255) / 255.0F, (bc >> 8 & 255) / 255.0F, (bc & 255) / 255.0F, 1.0F);
            this.bra.render(scale);
        }

        renderTailJoints(scale, hasGradient, tailRed, tailGreen, tailBlue, gradRed, gradGreen, gradBlue);
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderTailJoints(float scale, boolean hasGradient, float tr, float tg, float tb, float gr, float gg, float gb) {
        GlStateManager.pushMatrix();
        for (int i = 0; i < tailParts.length; i++) {
            ModelRenderer part = tailParts[i];

            if (hasGradient) {
                if (i < 7) {
                    float delta = i / 7.0F;
                    GlStateManager.color(MathHelperNew.lerp(delta, tr, gr), MathHelperNew.lerp(delta, tg, gg), MathHelperNew.lerp(delta, tb, gb), 1.0F);
                } else {
                    GlStateManager.color(gr, gg, gb, 1.0F);
                }
            } else {
                GlStateManager.color(tr, tg, tb, 1.0F);
            }

            GlStateManager.translate(part.rotationPointX * scale, part.rotationPointY * scale, part.rotationPointZ * scale);
            if (part.rotateAngleZ != 0.0F)
                GlStateManager.rotate(part.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
            if (part.rotateAngleY != 0.0F)
                GlStateManager.rotate(part.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
            if (part.rotateAngleX != 0.0F)
                GlStateManager.rotate(part.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);

            float oldX = part.rotationPointX, oldY = part.rotationPointY, oldZ = part.rotationPointZ;
            float oldRX = part.rotateAngleX, oldRY = part.rotateAngleY, oldRZ = part.rotateAngleZ;

            part.rotationPointX = part.rotationPointY = part.rotationPointZ = 0;
            part.rotateAngleX = part.rotateAngleY = part.rotateAngleZ = 0;

            part.render(scale);

            part.rotationPointX = oldX;
            part.rotationPointY = oldY;
            part.rotationPointZ = oldZ;
            part.rotateAngleX = oldRX;
            part.rotateAngleY = oldRY;
            part.rotateAngleZ = oldRZ;
        }
        GlStateManager.popMatrix();
    }

    private void addPreciseBox(ModelRenderer renderer, int texU, int texV, float x, float y, float z, float w, float h, float d) {
        renderer.cubeList.add(new FloatModelBox(renderer, texU, texV, x, y, z, w, h, d));
    }

    private static class FloatModelBox extends ModelBox {
        private final FloatQuad[] quads;

        public FloatModelBox(ModelRenderer renderer, int texU, int texV, float x, float y, float z, float width, float height, float depth) {
            super(renderer, texU, texV, x, y, z, 0, 0, 0, 0.0F); // 传入 0 阻断原版破损的整数生成逻辑
            this.quads = new FloatQuad[6];

            float endX = x + width;
            float endY = y + height;
            float endZ = z + depth;

            Vec3d p0 = new Vec3d(x, y, z);
            Vec3d p1 = new Vec3d(endX, y, z);
            Vec3d p2 = new Vec3d(endX, endY, z);
            Vec3d p3 = new Vec3d(x, endY, z);
            Vec3d p4 = new Vec3d(x, y, endZ);
            Vec3d p5 = new Vec3d(endX, y, endZ);
            Vec3d p6 = new Vec3d(endX, endY, endZ);
            Vec3d p7 = new Vec3d(x, endY, endZ);

            float tw = renderer.textureWidth;
            float th = renderer.textureHeight;

            this.quads[0] = new FloatQuad(new Vec3d[]{p5, p1, p2, p6}, texU + depth + width, texV + depth, texU + depth + width + depth, texV + depth + height, tw, th); // 右
            this.quads[1] = new FloatQuad(new Vec3d[]{p0, p4, p7, p3}, texU, texV + depth, texU + depth, texV + depth + height, tw, th); // 左
            this.quads[2] = new FloatQuad(new Vec3d[]{p5, p4, p0, p1}, texU + depth, texV, texU + depth + width, texV + depth, tw, th); // 上
            this.quads[3] = new FloatQuad(new Vec3d[]{p2, p3, p7, p6}, texU + depth + width, texV + depth, texU + depth + width + width, texV, tw, th); // 下
            this.quads[4] = new FloatQuad(new Vec3d[]{p1, p0, p3, p2}, texU + depth, texV + depth, texU + depth + width, texV + depth + height, tw, th); // 前
            this.quads[5] = new FloatQuad(new Vec3d[]{p4, p5, p6, p7}, texU + depth + width + depth, texV + depth, texU + depth + width + depth + width, texV + depth + height, tw, th); // 后

            if (renderer.mirror) {
                for (FloatQuad quad : this.quads) {
                    quad.flipFace();
                }
            }
        }

        @Override
        public void render(@Nonnull BufferBuilder renderer, float scale) {
            for (FloatQuad quad : this.quads) {
                quad.draw(renderer, scale);
            }
        }
    }

    private static class FloatQuad {
        public final Vec3d[] vertices = new Vec3d[4];
        public final float[] u = new float[4];
        public final float[] v = new float[4];
        public float normX, normY, normZ;

        public FloatQuad(Vec3d[] verts, float u1, float v1, float u2, float v2, float texW, float texH) {
            System.arraycopy(verts, 0, this.vertices, 0, 4);

            this.u[0] = u2 / texW;
            this.v[0] = v1 / texH;
            this.u[1] = u1 / texW;
            this.v[1] = v1 / texH;
            this.u[2] = u1 / texW;
            this.v[2] = v2 / texH;
            this.u[3] = u2 / texW;
            this.v[3] = v2 / texH;

            calculateNormal();
        }

        public void flipFace() {
            Vec3d[] reversedVerts = new Vec3d[4];
            float[] revU = new float[4];
            float[] revV = new float[4];

            for (int i = 0; i < 4; ++i) {
                reversedVerts[i] = this.vertices[3 - i];
                revU[i] = this.u[3 - i];
                revV[i] = this.v[3 - i];
            }

            System.arraycopy(reversedVerts, 0, this.vertices, 0, 4);
            System.arraycopy(revU, 0, this.u, 0, 4);
            System.arraycopy(revV, 0, this.v, 0, 4);
            calculateNormal();
        }

        private void calculateNormal() {
            Vec3d d1 = this.vertices[1].subtract(this.vertices[0]);
            Vec3d d2 = this.vertices[1].subtract(this.vertices[2]);
            Vec3d cross = d2.crossProduct(d1).normalize();
            this.normX = (float) cross.x;
            this.normY = (float) cross.y;
            this.normZ = (float) cross.z;
        }

        public void draw(BufferBuilder renderer, float scale) {
            renderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int i = 0; i < 4; ++i) {
                Vec3d vec = this.vertices[i];
                renderer.pos(vec.x * scale, vec.y * scale, vec.z * scale)
                        .tex(this.u[i], this.v[i])
                        .normal(this.normX, this.normY, this.normZ)
                        .endVertex();
            }
            Tessellator.getInstance().draw();
        }
    }
}