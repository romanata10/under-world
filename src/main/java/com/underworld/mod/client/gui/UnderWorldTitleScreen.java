package com.underworld.mod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.underworld.mod.UnderWorldMod;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class UnderWorldTitleScreen extends TitleScreen {
    private static final ResourceLocation UNDER_WORLD_LOGO =
            UnderWorldMod.id("textures/gui/under_world_logo.png");

    private static final String[] HORROR_SPLASHES = {
            "this server is ^&^#&*",
            "friend joined the game",
            "don't click DOWNLOAD",
            "the plains never end",
            "welcome to the hell",
            "do not wake the turbine",
            "it watches from the wires"
    };

    private final int splashIndex;

    public UnderWorldTitleScreen() {
        super(false);
        this.splashIndex = (int) (Math.abs(Util.getMillis()) % HORROR_SPLASHES.length);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long ms = Util.getMillis();
        float timeSec = ms / 1000.0F;

        // 1. Deep CRT analog horror dusk sky gradient
        g.fillGradient(0, 0, this.width, this.height, 0xFF040508, 0xFF10151F);
        int horizonY = (int) (this.height * 0.72F);
        g.fillGradient(0, horizonY - 36, this.width, horizonY + 12, 0x0010151F, 0x55221018);

        // 2. Distant rolling plains silhouette
        for (int x = 0; x < this.width; x += 4) {
            float ridge = Mth.sin(x * 0.015F) * 9.0F + Mth.cos(x * 0.032F) * 5.0F;
            int ry = (int) (horizonY + ridge);
            g.fill(x, ry, x + 4, this.height, 0xFF06070A);
        }

        // 3. Distant Power Pylon (ЛЭП) silhouette on left side with blinking crimson beacon
        int pylonX = Math.max(46, this.width / 8);
        int pylonBaseY = horizonY + 10;
        drawPylonSilhouette(g, pylonX, pylonBaseY, (ms / 650L) % 2L == 0L);

        // 4. Distant rotating Wind Turbine silhouette on right side
        int turbX = Math.min(this.width - 52, (this.width * 7) / 8);
        int turbBaseY = horizonY + 12;
        drawTurbineSilhouette(g, turbX, turbBaseY, timeSec);

        // 5. Eerie distant blinking eyes silhouette in center-top darkness (flickers subtly)
        float eyePulse = Mth.sin(timeSec * 0.9F);
        if (eyePulse > 0.35F) {
            int eyeAlpha = (int) (Mth.clamp((eyePulse - 0.35F) * 1.4F, 0.0F, 1.0F) * 150.0F);
            int eyeColor = (eyeAlpha << 24) | 0xFF283A;
            int ex = this.width / 2;
            int ey = horizonY - 12;
            g.fill(ex - 9, ey, ex - 6, ey + 2, eyeColor);
            g.fill(ex + 6, ey, ex + 9, ey + 2, eyeColor);
        }

        // 6. Subtle CRT horizontal scanlines + occasional analog glitch band
        for (int y = 0; y < this.height; y += 3) {
            g.fill(0, y, this.width, y + 1, 0x26000000);
        }
        if ((ms % 4200L) < 130L) {
            int glitchY = (int) ((ms * 17L) % Math.max(1, this.height - 20));
            g.fill(0, glitchY, this.width, glitchY + 3, 0x40FF2640);
            g.fill(0, glitchY + 4, this.width, glitchY + 5, 0x3036CCFF);
        }

        // 7. Custom analog UNDER WORLD logo banner with faint horizontal glitch jitter
        int logoW = Math.min(320, Math.max(180, this.width - 40));
        int logoH = (logoW * 64) / 424;
        int jitterX = ((ms % 3100L) < 90L) ? ((ms % 2 == 0) ? 2 : -2) : 0;
        int logoX = (this.width - logoW) / 2 + jitterX;
        int logoY = 24;

        RenderSystem.enableBlend();
        g.blit(UNDER_WORLD_LOGO, logoX, logoY, logoW, logoH, 0.0F, 0.0F, 424, 64, 424, 64);
        RenderSystem.disableBlend();

        // 8. Cryptic splash line centered below UNDER WORLD logo
        String splash = HORROR_SPLASHES[this.splashIndex];
        g.drawCenteredString(this.font, splash, this.width / 2, logoY + logoH + 6, 0xFFE63946);

        // 9. Dark CRT framing behind active widgets + render every vanilla/Forge button cleanly!
        for (GuiEventListener child : this.children()) {
            if (child instanceof AbstractWidget w && w.visible) {
                int borderColor = w.isHoveredOrFocused() ? 0xFFE63946 : 0x883A4458;
                g.fill(w.getX() - 2, w.getY() - 2, w.getX() + w.getWidth() + 2, w.getY() + w.getHeight() + 2, 0x9906080C);
                g.fill(w.getX() - 2, w.getY() - 2, w.getX() - 1, w.getY() + w.getHeight() + 2, borderColor);
                g.fill(w.getX() + w.getWidth() + 1, w.getY() - 2, w.getX() + w.getWidth() + 2, w.getY() + w.getHeight() + 2, borderColor);
            }
        }
        for (GuiEventListener child : this.children()) {
            if (child instanceof Renderable renderable) {
                renderable.render(g, mouseX, mouseY, partialTick);
            }
        }

        // 10. Clean analog footer strings
        g.drawString(this.font, "Under World v2.4.0 // SIGNAL ACTIVE", 8, this.height - 14, 0xFF98A6B8);
        String copy = "Minecraft 1.20.1 (Do not look away)";
        int cw = this.font.width(copy);
        g.drawString(this.font, copy, this.width - cw - 8, this.height - 14, 0xFF7A8494);
    }

    private static void drawPylonSilhouette(GuiGraphics g, int cx, int baseY, boolean blinkRed) {
        int col = 0xFF101319;
        // Legs + central lattice spire
        g.fill(cx - 8, baseY - 24, cx - 5, baseY, col);
        g.fill(cx + 5, baseY - 24, cx + 8, baseY, col);
        g.fill(cx - 3, baseY - 90, cx + 3, baseY - 20, col);
        // Cross-arms & torn wires
        g.fill(cx - 22, baseY - 62, cx + 22, baseY - 58, col);
        g.fill(cx - 16, baseY - 78, cx + 16, baseY - 74, col);
        g.fill(cx - 21, baseY - 58, cx - 20, baseY - 40, 0xAA0C0D12);
        g.fill(cx + 20, baseY - 58, cx + 21, baseY - 43, 0xAA0C0D12);
        if (blinkRed) {
            g.fill(cx - 1, baseY - 94, cx + 2, baseY - 91, 0xFFFF2438);
        }
    }

    private static void drawTurbineSilhouette(GuiGraphics g, int cx, int baseY, float timeSec) {
        int col = 0xFF141922;
        int hubY = baseY - 82;
        // Shaft
        g.fill(cx - 2, hubY, cx + 2, baseY, col);
        g.fill(cx - 3, hubY - 3, cx + 3, hubY + 3, col);
        // 3 sweeping pixel blades
        for (int b = 0; b < 3; b++) {
            float angle = timeSec * 1.15F + b * (Mth.TWO_PI / 3.0F);
            for (int r = 4; r <= 34; r += 2) {
                int bx = cx + Math.round(Mth.cos(angle) * r);
                int by = hubY + Math.round(Mth.sin(angle) * r);
                g.fill(bx - 1, by - 1, bx + 1, by + 1, col);
            }
        }
        if (((int) (timeSec * 2.0F)) % 2 == 0) {
            g.fill(cx - 1, hubY - 5, cx + 1, hubY - 3, 0xFFFF2438);
        }
    }
}
