package com.underworld.mod.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.underworld.mod.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.joml.Matrix4f;

public final class GeometricNightmareOverlay {
    private GeometricNightmareOverlay() {}

    public static void render(ForgeGui gui, GuiGraphics g, float partialTick, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !mc.player.hasEffect(ModEffects.GEOMETRIC_NIGHTMARE.get())) {
            return;
        }

        float t = (System.currentTimeMillis() % 600000L) * 0.001F;
        Matrix4f pose = g.pose().last().pose();

        // Dark dizzy peripheral pulsing tint
        int edgeAlpha = (int) (35 + 25 * Math.sin(t * 4.5F));
        int vignetteColor = (edgeAlpha << 24) | 0x05020A;
        g.fill(0, 0, w, 16, vignetteColor);
        g.fill(0, h - 16, w, h, vignetteColor);
        g.fill(0, 16, 16, h - 16, vignetteColor);
        g.fill(w - 16, 16, w, h - 16, vignetteColor);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buf = tesselator.getBuilder();

        // 1. Filled dark & crimson-tinted geometric shards / rotating triangles
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        float cx = w * 0.5F;
        float cy = h * 0.5F;
        float minDim = Math.min(w, h);

        // Peripheral geometric triangle prisms
        for (int i = 0; i < 6; i++) {
            float angle = t * (i % 2 == 0 ? 0.85F : -0.65F) + i * 1.0472F;
            float orbit = minDim * (0.37F + 0.06F * (float) Math.sin(t * 2.2F + i));
            float px = cx + (float) Math.cos(angle) * orbit;
            float py = cy + (float) Math.sin(angle) * orbit;
            float size = minDim * (0.075F + 0.03F * (float) Math.cos(t * 3.1F + i * 1.7F));
            float rot = t * 1.9F + i * 0.9F;
            int cr = (i % 3 == 0) ? 185 : 8;
            int cg = (i % 3 == 0) ? 28 : 8;
            int cb = (i % 3 == 0) ? 60 : 16;
            int ca = (i % 3 == 0) ? 92 : 128;
            addEquilateralTriangle(buf, pose, px, py, size, rot, cr, cg, cb, ca);
        }

        // Sharp geometric corner wedges closing inward rhythmically
        float wedge = minDim * (0.11F + 0.04F * (float) Math.sin(t * 3.6F));
        addTri(buf, pose, 0, 0, wedge, 0, 0, wedge, 4, 4, 8, 155);
        addTri(buf, pose, w, 0, w, wedge, w - wedge, 0, 4, 4, 8, 155);
        addTri(buf, pose, 0, h, 0, h - wedge, wedge, h, 4, 4, 8, 155);
        addTri(buf, pose, w, h, w - wedge, h, w, h - wedge, 4, 4, 8, 155);
        BufferUploader.drawWithShader(buf.end());

        // 2. Crisp wireframe geometric rings, squares, and intersecting fractal lines
        buf.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        // Rotating inner fractured triangles & diamonds around reticle (light translucent white/pink)
        for (int ring = 0; ring < 3; ring++) {
            int sides = (ring == 0) ? 3 : (ring == 1 ? 4 : 6);
            float rad = minDim * (0.16F + ring * 0.115F) * (1.0F + 0.12F * (float) Math.sin(t * 2.8F + ring));
            float rot = t * (ring % 2 == 0 ? 1.15F : -0.92F) + ring * 0.5F;
            int alpha = ring == 0 ? 135 : 165;
            int r = ring == 1 ? 245 : 230;
            int gr = ring == 1 ? 75 : 230;
            int b = ring == 1 ? 110 : 245;
            addPolygonOutline(buf, pose, cx, cy, rad, sides, rot, r, gr, b, alpha);
        }

        // Skewed geometric grid / tangent lines slicing across screen corners
        for (int k = 0; k < 4; k++) {
            float sweep = (float) Math.sin(t * 1.4F + k * 1.57F);
            float x0 = (k < 2) ? 0.0F : w;
            float y0 = cy + sweep * (h * 0.35F);
            float x1 = cx + (float) Math.cos(t * 1.1F + k) * (minDim * 0.42F);
            float y1 = (k % 2 == 0) ? 0.0F : h;
            addLine(buf, pose, x0, y0, x1, y1, 255, 255, 255, 95);
        }
        BufferUploader.drawWithShader(buf.end());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void addEquilateralTriangle(BufferBuilder buf, Matrix4f pose,
                                               float cx, float cy, float r, float rot,
                                               int red, int green, int blue, int alpha) {
        float x0 = cx + (float) Math.cos(rot) * r;
        float y0 = cy + (float) Math.sin(rot) * r;
        float x1 = cx + (float) Math.cos(rot + 2.094395F) * r;
        float y1 = cy + (float) Math.sin(rot + 2.094395F) * r;
        float x2 = cx + (float) Math.cos(rot + 4.18879F) * r;
        float y2 = cy + (float) Math.sin(rot + 4.18879F) * r;
        addTri(buf, pose, x0, y0, x1, y1, x2, y2, red, green, blue, alpha);
    }

    private static void addTri(BufferBuilder buf, Matrix4f pose,
                               float x0, float y0, float x1, float y1, float x2, float y2,
                               int r, int g, int b, int a) {
        buf.vertex(pose, x0, y0, 0.0F).color(r, g, b, a).endVertex();
        buf.vertex(pose, x1, y1, 0.0F).color(r, g, b, a).endVertex();
        buf.vertex(pose, x2, y2, 0.0F).color(r, g, b, a).endVertex();
    }

    private static void addPolygonOutline(BufferBuilder buf, Matrix4f pose,
                                          float cx, float cy, float radius, int sides, float rot,
                                          int r, int g, int b, int a) {
        float step = (float) (Math.PI * 2.0D / sides);
        for (int i = 0; i < sides; i++) {
            float a0 = rot + i * step;
            float a1 = rot + (i + 1) * step;
            addLine(buf, pose,
                    cx + (float) Math.cos(a0) * radius, cy + (float) Math.sin(a0) * radius,
                    cx + (float) Math.cos(a1) * radius, cy + (float) Math.sin(a1) * radius,
                    r, g, b, a);
        }
    }

    private static void addLine(BufferBuilder buf, Matrix4f pose,
                                float x0, float y0, float x1, float y1,
                                int r, int g, int b, int a) {
        buf.vertex(pose, x0, y0, 0.0F).color(r, g, b, a).endVertex();
        buf.vertex(pose, x1, y1, 0.0F).color(r, g, b, a).endVertex();
    }
}
