package com.underworld.mod.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.underworld.mod.UnderWorldMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import java.util.Locale;

public final class TheTruthHudOverlay {
    private static final ResourceLocation FACE_TEXTURE =
            UnderWorldMod.id("textures/gui/the_truth_face.png");
    private static final String BANNER_TEXT =
            "this server is ^&^#&* please leave server the truth is here";

    private TheTruthHudOverlay() {}

    public static void render(ForgeGui gui, GuiGraphics g, float partialTick, int screenWidth, int screenHeight) {
        if (!ClientTruthHudState.isActive()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        long now = System.currentTimeMillis();
        double remainingSec = ClientTruthHudState.getRemainingSeconds();

        // Subtle crimson screen border vignette
        float pulse = 0.5F + 0.5F * (float) Math.sin(now * 0.008D);
        int edgeAlpha = (int) (26 + 22 * pulse);
        int edgeColor = (edgeAlpha << 24) | 0x7A0000;
        int border = 14;
        g.fill(0, 18, screenWidth, 18 + border, edgeColor);
        g.fill(0, screenHeight - border, screenWidth, screenHeight, edgeColor);
        g.fill(0, 18 + border, border, screenHeight - border, edgeColor);
        g.fill(screenWidth - border, 18 + border, screenWidth, screenHeight - border, edgeColor);

        // Image-7 dark face with glowing red eyes & bloody teeth in all 4 screen corners
        int faceSize = Math.max(52, Math.min(126, (int) (Math.min(screenWidth, screenHeight) * 0.24F)));
        int jitterX = (now % 900L < 70L) ? (int) ((now / 17L) % 5L) - 2 : 0;
        int jitterY = (now % 1100L < 70L) ? (int) ((now / 23L) % 5L) - 2 : 0;
        float faceAlpha = 0.80F + 0.16F * pulse;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.setColor(1.0F, 1.0F, 1.0F, faceAlpha);

        int topY = 20;
        int bottomY = screenHeight - faceSize;
        int rightX = screenWidth - faceSize;

        g.blit(FACE_TEXTURE, jitterX, topY + jitterY, faceSize, faceSize, 0.0F, 0.0F, 256, 256, 256, 256);
        g.blit(FACE_TEXTURE, rightX - jitterX, topY + jitterY, faceSize, faceSize, 0.0F, 0.0F, 256, 256, 256, 256);
        g.blit(FACE_TEXTURE, jitterX, bottomY - jitterY, faceSize, faceSize, 0.0F, 0.0F, 256, 256, 256, 256);
        g.blit(FACE_TEXTURE, rightX - jitterX, bottomY - jitterY, faceSize, faceSize, 0.0F, 0.0F, 256, 256, 256, 256);

        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        // Top red scrolling ticker bar
        Font font = mc.font;
        g.fill(0, 0, screenWidth, 18, 0xCC120000);
        g.fill(0, 17, screenWidth, 18, 0xDDCC1111);

        String segment = BANNER_TEXT + "     ///     ";
        int segWidth = Math.max(1, font.width(segment));
        int scrollPx = (int) ((now / 14L) % segWidth);

        g.enableScissor(0, 0, screenWidth, 18);
        int textColor = (now % 450L < 90L) ? 0xFFFF5555 : 0xFFFF1818;
        for (int x = -scrollPx; x < screenWidth + segWidth; x += segWidth) {
            g.drawString(font, segment, x, 5, textColor, true);
        }
        g.disableScissor();

        // 30-second countdown timer centered near top
        String formattedTime = String.format(Locale.ROOT, "%.1f", remainingSec);
        Component timerText = Component.translatable("hud.under_world.the_truth.timer", formattedTime);
        int timerWidth = font.width(timerText);
        float scale = 1.45F;
        int boxW = Math.round(timerWidth * scale) + 18;
        int boxX = (screenWidth - boxW) / 2;
        int boxY = 23;
        g.fill(boxX, boxY, boxX + boxW, boxY + 20, 0xAA160000);
        g.fill(boxX, boxY, boxX + boxW, boxY + 1, 0xCCFF2222);
        g.fill(boxX, boxY + 19, boxX + boxW, boxY + 20, 0xCCFF2222);

        int timerColor = remainingSec <= 5.0D && (now / 150L) % 2L == 0L ? 0xFFFFFFFF : 0xFFFF2B2B;
        g.pose().pushPose();
        g.pose().translate(screenWidth / 2.0F, boxY + 4.0F, 0.0F);
        g.pose().scale(scale, scale, 1.0F);
        g.drawCenteredString(font, timerText, 0, 0, timerColor);
        g.pose().popPose();
    }
}
