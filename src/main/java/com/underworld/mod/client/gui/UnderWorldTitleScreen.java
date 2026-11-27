package com.underworld.mod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.underworld.mod.UnderWorldMod;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Random;

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
            "it watches from the wires",
            "freeze when the clock ticks",
            "don't look away from the petals",
            "something followed you out of the maze",
            "bring an axe for the wrong tree",
            "why is the sky breathing?",
            "C:\\WINDOWS\\system32\\under_world.exe",
            "404: escape route not found — RUN LEFT",
            "your boat is drifting toward the rig",
            "never mine behind Kira_99's house",
            "do not open the basement trapdoor",
            "150,000 volts and hungry",
            "the ball changed its shape again",
            "steve? no. look closer.",
            "something splashed under the boat",
            "do not dig deeper than y = 16",
            "error buffer: 01:30 remaining",
            "the desktop icons are falling",
            "http://the-MinECRaT-fREe-downladn.com/",
            "no one else is on this server",
            "it wears Misha's skin now",
            "check the void shrine before dusk"
    };

    private static final String[] SCENE_LABELS = {
            "THE PLAINS // LIVING PYLON",
            "THE WIND OCEAN // TURBINE STALKER",
            "THE XP & BROWSER // THE ___.COM",
            "THE FUN WORLD & MAZE // SMILE",
            "THE TIME // ENDLESS CLOCKS",
            "THE ERROR // THE TRUTH & BALL",
            "OVERWORLD NIGHT // HANDS & FRIEND"
    };

    private static final Random SCENE_RNG = new Random();
    private static int lastSceneIndex = -1;
    private static int lastSplashIndex = -1;

    private int sceneIndex;
    private int splashIndex;

    public UnderWorldTitleScreen() {
        super(false);
        this.sceneIndex = pickNextDifferent(SCENE_LABELS.length, lastSceneIndex);
        lastSceneIndex = this.sceneIndex;
        this.splashIndex = pickNextDifferent(HORROR_SPLASHES.length, lastSplashIndex);
        lastSplashIndex = this.splashIndex;
    }

    private static int pickNextDifferent(int count, int last) {
        if (count <= 1) return 0;
        int pick = SCENE_RNG.nextInt(count);
        if (pick == last) {
            pick = (pick + 1 + SCENE_RNG.nextInt(count - 1)) % count;
        }
        return pick;
    }

    @Override
    protected void init() {
        super.init();
        // Corner Wiki button (top-right corner) + small BG cycle button right next to it
        int wikiW = 58;
        int bgW = 34;
        int topY = 8;
        int rightX = this.width - wikiW - 8;

        this.addRenderableWidget(Button.builder(Component.translatable("wiki.under_world.button"),
                btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new UnderWorldWikiScreen(this));
                    }
                }).bounds(rightX, topY, wikiW, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("BG ↻"),
                btn -> {
                    this.sceneIndex = (this.sceneIndex + 1) % SCENE_LABELS.length;
                    lastSceneIndex = this.sceneIndex;
                    this.splashIndex = pickNextDifferent(HORROR_SPLASHES.length, this.splashIndex);
                    lastSplashIndex = this.splashIndex;
                }).bounds(rightX - bgW - 4, topY, bgW, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long ms = Util.getMillis();
        float timeSec = ms / 1000.0F;

        renderAnimatedModScene(g, ms, timeSec, this.sceneIndex);

        // Subtle CRT horizontal scanlines + occasional analog glitch band
        for (int y = 0; y < this.height; y += 3) {
            g.fill(0, y, this.width, y + 1, 0x26000000);
        }
        if ((ms % 4200L) < 130L) {
            int glitchY = (int) ((ms * 17L) % Math.max(1, this.height - 20));
            g.fill(0, glitchY, this.width, glitchY + 3, 0x40FF2640);
            g.fill(0, glitchY + 4, this.width, glitchY + 5, 0x3036CCFF);
        }

        // Custom analog UNDER WORLD logo banner with faint horizontal glitch jitter
        int logoW = Math.min(320, Math.max(180, this.width - 40));
        int logoH = (logoW * 64) / 424;
        int jitterX = ((ms % 3100L) < 90L) ? ((ms % 2 == 0) ? 2 : -2) : 0;
        int logoX = (this.width - logoW) / 2 + jitterX;
        int logoY = 24;

        RenderSystem.enableBlend();
        g.blit(UNDER_WORLD_LOGO, logoX, logoY, logoW, logoH, 0.0F, 0.0F, 424, 64, 424, 64);
        RenderSystem.disableBlend();

        // Cryptic splash line centered below UNDER WORLD logo
        String splash = HORROR_SPLASHES[this.splashIndex];
        g.drawCenteredString(this.font, splash, this.width / 2, logoY + logoH + 6, 0xFFE63946);

        // Dark CRT framing behind active widgets + render every vanilla/Forge/mod button cleanly!
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

        // Clean analog footer strings showing active scene
        g.drawString(this.font, "Under World v2.4.1 // " + SCENE_LABELS[this.sceneIndex], 8, this.height - 14, 0xFF98A6B8);
        String copy = "Minecraft 1.20.1 (Do not look away)";
        int cw = this.font.width(copy);
        g.drawString(this.font, copy, this.width - cw - 8, this.height - 14, 0xFF7A8494);
    }

    private void renderAnimatedModScene(GuiGraphics g, long ms, float timeSec, int scene) {
        int horizonY = (int) (this.height * 0.72F);
        switch (scene) {
            case 0 -> renderPlainsPylonScene(g, ms, timeSec, horizonY);
            case 1 -> renderWindOceanScene(g, ms, timeSec, horizonY);
            case 2 -> renderXpBrowserScene(g, ms, timeSec, horizonY);
            case 3 -> renderFunMazeScene(g, ms, timeSec, horizonY);
            case 4 -> renderTimeClocksScene(g, ms, timeSec, horizonY);
            case 5 -> renderErrorTruthScene(g, ms, timeSec, horizonY);
            default -> renderNightCaveScene(g, ms, timeSec, horizonY);
        }
    }

    private void renderPlainsPylonScene(GuiGraphics g, long ms, float timeSec, int horizonY) {
        g.fillGradient(0, 0, this.width, this.height, 0xFF06090D, 0xFF141B22);
        g.fillGradient(0, horizonY - 36, this.width, horizonY + 12, 0x00141B22, 0x552B151A);
        for (int x = 0; x < this.width; x += 4) {
            float ridge = Mth.sin(x * 0.015F) * 9.0F + Mth.cos(x * 0.032F) * 5.0F;
            g.fill(x, (int) (horizonY + ridge), x + 4, this.height, 0xFF070A0D);
        }
        // Winding plains road vanishing toward center
        for (int y = horizonY + 4; y < this.height; y += 3) {
            float t = (float) (y - horizonY) / Math.max(1, this.height - horizonY);
            int halfW = Math.max(2, Math.round(4 + t * 55));
            int cx = this.width / 2 + Math.round(Mth.sin(t * 4.0F) * 18.0F);
            g.fill(cx - halfW, y, cx + halfW, y + 3, 0xFF1A1610);
        }
        // Giant Living Pylon walking with arcing sparks
        int stride = Math.round(Mth.sin(timeSec * 1.6F) * 6.0F);
        int pylonX = Math.max(54, this.width / 7);
        drawPylonSilhouette(g, pylonX, horizonY + 14, (ms / 450L) % 2L == 0L, stride);
        if ((ms / 180L) % 5L == 0L) {
            g.fill(pylonX - 24, horizonY - 46, pylonX + 24, horizonY - 45, 0xAA66E0FF);
        }
        // Glowing green/dark portal turf patch
        int px = this.width / 2 - 22;
        g.fill(px, horizonY + 18, px + 44, horizonY + 24, 0x8815422B);
        drawTurbineSilhouette(g, Math.min(this.width - 52, (this.width * 7) / 8), horizonY + 10, timeSec, false);
    }

    private void renderWindOceanScene(GuiGraphics g, long ms, float timeSec, int horizonY) {
        g.fillGradient(0, 0, this.width, horizonY, 0xFF0B1117, 0xFF283744);
        g.fillGradient(0, horizonY, this.width, this.height, 0xFF0E1D29, 0xFF04090F);
        // Moving ocean waves
        for (int y = horizonY; y < this.height; y += 8) {
            int shift = Math.round(Mth.sin(timeSec * 1.4F + y * 0.1F) * 18.0F);
            for (int x = -20; x < this.width; x += 48) {
                g.fill(x + shift, y, x + shift + 28, y + 2, 0x336C96B0);
            }
        }
        // Offshore Oil Rig silhouette on left with flaming flare stack
        int rigX = Math.max(66, this.width / 6);
        int rigBase = horizonY + 18;
        g.fill(rigX - 30, rigBase - 24, rigX + 30, rigBase - 18, 0xFF14181F);
        g.fill(rigX - 24, rigBase - 18, rigX - 18, rigBase + 28, 0xFF101318);
        g.fill(rigX + 18, rigBase - 18, rigX + 24, rigBase + 28, 0xFF101318);
        g.fill(rigX - 22, rigBase - 58, rigX - 18, rigBase - 24, 0xFF181D26);
        int flameH = 4 + (int) ((ms / 110L) % 5L);
        g.fill(rigX - 23, rigBase - 58 - flameH, rigX - 17, rigBase - 58, 0xFFFF6E24);
        // Oil Rig Portal glow on deck
        g.fill(rigX - 6, rigBase - 26, rigX + 6, rigBase - 24, 0xCC33E0FF);
        // Wind Turbine Stalker awakening on right
        drawTurbineSilhouette(g, Math.min(this.width - 66, (this.width * 5) / 6), horizonY + 20, timeSec * 1.35F, true);
        // Drifting Oak Boat silhouette in middle-bottom
        int bx = this.width / 2 + Math.round(Mth.sin(timeSec * 0.8F) * 20.0F);
        int by = horizonY + 32 + Math.round(Mth.cos(timeSec * 2.1F) * 2.0F);
        g.fill(bx - 16, by, bx + 16, by + 5, 0xFF2B1D12);
        g.fill(bx - 11, by + 5, bx + 11, by + 7, 0xFF1C120B);
    }

    private void renderXpBrowserScene(GuiGraphics g, long ms, float timeSec, int horizonY) {
        // Windows XP Bliss sky & green hill
        g.fillGradient(0, 0, this.width, horizonY, 0xFF1A57A8, 0xFF66A8F0);
        // Drifting clouds
        for (int i = 0; i < 4; i++) {
            int cx = Math.floorMod((int) (timeSec * 12 + i * 130), this.width + 80) - 40;
            int cy = 44 + i * 18;
            g.fill(cx - 24, cy, cx + 24, cy + 10, 0x55FFFFFF);
            g.fill(cx - 14, cy - 5, cx + 14, cy + 5, 0x66FFFFFF);
        }
        // Green XP bliss hill
        for (int x = 0; x < this.width; x += 4) {
            float curve = Mth.sin(x * 0.008F) * 24.0F;
            int hy = (int) (horizonY - 12 + curve);
            g.fill(x, hy, x + 4, this.height - 18, 0xFF256E22);
        }
        // Windows XP Blue Taskbar & Green Start
        g.fill(0, this.height - 18, this.width, this.height, 0xFF1942A8);
        g.fill(0, this.height - 18, 52, this.height, 0xFF2FA134);
        // Falling/corrupted black desktop icons on left
        for (int i = 0; i < 3; i++) {
            int ix = 22 + i * 32;
            int iy = (int) ((58 + i * 36 + (ms / 35L) % 60));
            g.fill(ix, iy, ix + 14, iy + 14, 0xAA0C0D12);
            g.fill(ix + 2, iy + 2, ix + 12, iy + 6, 0xAAFF3344);
        }
        // Corrupted Browser window + The ___.com claw silhouette on right
        int wx = Math.max(this.width / 2 + 24, this.width - 170);
        int wy = Math.max(46, horizonY - 96);
        g.fill(wx, wy, wx + 140, wy + 92, 0xCC12141A);
        g.fill(wx, wy, wx + 140, wy + 14, 0xFF2C3444);
        g.fill(wx + 6, wy + 4, wx + 115, wy + 10, 0xFF0A0D12); // address bar
        // Green DOWNLOAD button + crimson eyes inside browser frame
        g.fill(wx + 24, wy + 58, wx + 116, wy + 76, ((ms / 350L) % 2L == 0L) ? 0xFF33B844 : 0xFFCC2233);
        g.fill(wx + 48, wy + 30, wx + 56, wy + 34, 0xFFFF2233);
        g.fill(wx + 84, wy + 30, wx + 92, wy + 34, 0xFFFF2233);
        g.fill(wx + 50, wy + 40, wx + 90, wy + 47, 0xFFFFEEEE);
    }

    private void renderFunMazeScene(GuiGraphics g, long ms, float timeSec, int horizonY) {
        // Bright neon-green meadow darkening toward crimson Maze doorway
        g.fillGradient(0, 0, this.width, horizonY, 0xFF143D72, 0xFF2A1822);
        for (int x = 0; x < this.width; x += 4) {
            float h = Mth.sin(x * 0.02F) * 14.0F;
            g.fill(x, (int) (horizonY + h), x + 4, this.height, 0xFF2F8C1F);
        }
        // Eyes of Fun tall daisies on left
        for (int i = 0; i < 3; i++) {
            int fx = 40 + i * 38;
            int fy = horizonY + 12;
            g.fill(fx - 2, fy - 48, fx + 2, fy, 0xFF256E1A);
            g.fill(fx - 10, fy - 64, fx + 10, fy - 46, 0xFFE6C83A);
            g.fill(fx - 5, fy - 58, fx - 2, fy - 54, 0xFF09090B);
            g.fill(fx + 2, fy - 58, fx + 5, fy - 54, 0xFF09090B);
        }
        // Red brick Maze wall & Smile glowing face on right
        int mx = this.width - 130;
        g.fill(mx, 44, this.width - 14, this.height - 18, 0xAA5A1818);
        g.fill(mx + 26, 68, mx + 92, this.height - 18, 0xFF040406);
        float bob = Mth.sin(timeSec * 3.0F) * 3.0F;
        int sx = mx + 59;
        int sy = (int) (106 + bob);
        g.fill(sx - 14, sy - 10, sx - 6, sy - 2, 0xFFFFEEEE);
        g.fill(sx + 6, sy - 10, sx + 14, sy - 2, 0xFFFFEEEE);
        g.fill(sx - 18, sy + 8, sx + 18, sy + 14, 0xFFFF3344);
    }

    private void renderTimeClocksScene(GuiGraphics g, long ms, float timeSec, int horizonY) {
        g.fillGradient(0, 0, this.width, this.height, 0xFF1A1426, 0xFF3A2918);
        // Floating clock platforms in the sky
        int[] dx = {this.width / 5, this.width / 2, (this.width * 4) / 5};
        int[] dy = {horizonY - 30, horizonY + 14, horizonY - 48};
        for (int i = 0; i < 3; i++) {
            float floatY = Mth.sin(timeSec * 1.1F + i * 2.0F) * 5.0F;
            int cy = Math.round(dy[i] + floatY);
            g.fill(dx[i] - 34, cy - 6, dx[i] + 34, cy + 6, 0xFF6B5326);
            g.fill(dx[i] - 28, cy - 4, dx[i] + 28, cy + 4, 0xFFD9C69C);
            // Rotating clock hands
            float ang = timeSec * (i % 2 == 0 ? 1.5F : -2.2F);
            int hx = dx[i] + Math.round(Mth.cos(ang) * 16);
            int hy = cy - 16 + Math.round(Mth.sin(ang) * 16);
            g.fill(dx[i] - 10, cy - 26, dx[i] + 10, cy - 6, 0xAAE8D8B0);
            g.fill(hx - 2, hy - 2, hx + 2, hy + 2, 0xFF14100C);
        }
        // Clock entity tall silhouette on center disc
        int cx = this.width / 2;
        int base = horizonY + 10;
        g.fill(cx - 3, base - 76, cx + 3, base, 0xFF0A090C);
        g.fill(cx - 14, base - 98, cx + 14, base - 70, 0xFFC8B070);
        g.fill(cx - 10, base - 94, cx + 10, base - 74, 0xFF121014);
        if ((ms / 500L) % 2L == 0L) {
            g.fill(cx - 1, base - 90, cx + 1, base - 78, 0xFFFF3344);
        }
    }

    private void renderErrorTruthScene(GuiGraphics g, long ms, float timeSec, int horizonY) {
        // Error Wastes neon glitch sky + dark terrain
        g.fillGradient(0, 0, this.width, horizonY, 0xFF12081E, 0xFF241630);
        g.fillGradient(0, horizonY, this.width, this.height, 0xFF09040E, 0xFF050208);
        // Scattered Error glitch blocks
        for (int i = 0; i < 12; i++) {
            int gx = (i * 73 + (int) (ms / 90L)) % Math.max(1, this.width);
            int gy = 40 + ((i * 53) % Math.max(1, horizonY - 40));
            int col = (i % 3 == 0) ? 0x66FF00FF : (i % 3 == 1 ? 0x66FFFF00 : 0x6600FFFF);
            g.fill(gx, gy, gx + 14, gy + 14, col);
        }
        // Colossal 'the truth' towering silhouette in background
        int tx = this.width / 2;
        g.fill(tx - 68, 22, tx + 68, this.height - 20, 0xAA030305);
        g.fill(tx - 38, 66, tx - 14, 80, 0xFFFFFFFF);
        g.fill(tx + 14, 66, tx + 38, 80, 0xFFFFFFFF);
        // Morphing The Ball floating on left
        int bx = Math.max(60, this.width / 6);
        int by = horizonY - 38 + Math.round(Mth.sin(timeSec * 2.0F) * 8.0F);
        int morph = 14 + Math.round(Mth.cos(timeSec * 3.1F) * 6.0F);
        g.fill(bx - morph, by - 16, bx + morph, by + 16, 0xFF040407);
        g.fill(bx - morph - 1, by - 17, bx + morph + 1, by - 16, 0xAA9B4DFF);
    }

    private void renderNightCaveScene(GuiGraphics g, long ms, float timeSec, int horizonY) {
        g.fillGradient(0, 0, this.width, horizonY, 0xFF030408, 0xFF0B0E18);
        g.fillGradient(0, horizonY, this.width, this.height, 0xFF050608, 0xFF020204);
        // Giant The Sky towering in misty backdrop
        int sx = this.width - 96;
        g.fill(sx - 28, 12, sx + 28, horizonY + 12, 0x660B0D14);
        // Tree? crooked silhouette on left
        int tx = 58;
        g.fill(tx - 4, horizonY - 64, tx + 4, horizonY + 16, 0xFF12100C);
        g.fill(tx - 24, horizonY - 82, tx + 24, horizonY - 56, 0xFF0A140B);
        if ((ms / 500L) % 3L == 0L) {
            g.fill(tx - 3, horizonY - 68, tx - 1, horizonY - 66, 0xFFFF2233);
            g.fill(tx + 1, horizonY - 68, tx + 3, horizonY - 66, 0xFFFF2233);
        }
        // friend peeking from window frame on right
        int fx = this.width - 70;
        g.fill(fx - 12, horizonY - 32, fx + 12, horizonY - 6, 0xFF1B2430);
        g.fill(fx - 4, horizonY - 26, fx + 4, horizonY - 18, 0xFF2B6F99);
        // Multi-jointed Hands reaching up from bottom cave
        int reach = Math.round(Mth.sin(timeSec * 2.5F) * 8.0F);
        int hx = this.width / 2 - 160;
        if (hx > 90) {
            g.fill(hx, this.height - 54 - reach, hx + 5, this.height, 0xFF08080A);
            g.fill(hx + 16, this.height - 62 + reach, hx + 21, this.height, 0xFF08080A);
        }
    }

    private static void drawPylonSilhouette(GuiGraphics g, int cx, int baseY, boolean blinkRed, int stride) {
        int col = 0xFF101319;
        g.fill(cx - 10 - stride, baseY - 24, cx - 5 - stride, baseY, col);
        g.fill(cx + 5 + stride, baseY - 24, cx + 10 + stride, baseY, col);
        g.fill(cx - 3, baseY - 90, cx + 3, baseY - 20, col);
        g.fill(cx - 24, baseY - 62, cx + 24, baseY - 58, col);
        g.fill(cx - 18, baseY - 78, cx + 18, baseY - 74, col);
        g.fill(cx - 23, baseY - 58, cx - 21, baseY - 40, 0xAA0C0D12);
        g.fill(cx + 21, baseY - 58, cx + 23, baseY - 43, 0xAA0C0D12);
        if (blinkRed) {
            g.fill(cx - 1, baseY - 95, cx + 2, baseY - 91, 0xFFFF2438);
            g.fill(cx - 19, baseY - 81, cx - 16, baseY - 78, 0xFFFF2438);
            g.fill(cx + 16, baseY - 81, cx + 19, baseY - 78, 0xFFFF2438);
        }
    }

    private static void drawTurbineSilhouette(GuiGraphics g, int cx, int baseY, float timeSec, boolean crimsonEye) {
        int col = 0xFF141922;
        int hubY = baseY - 82;
        g.fill(cx - 2, hubY, cx + 2, baseY, col);
        g.fill(cx - 4, hubY - 4, cx + 4, hubY + 4, col);
        for (int b = 0; b < 3; b++) {
            float angle = timeSec * 1.2F + b * (Mth.TWO_PI / 3.0F);
            for (int r = 4; r <= 36; r += 2) {
                int bx = cx + Math.round(Mth.cos(angle) * r);
                int by = hubY + Math.round(Mth.sin(angle) * r);
                g.fill(bx - 1, by - 1, bx + 1, by + 1, col);
            }
        }
        if (crimsonEye) {
            g.fill(cx - 2, hubY - 2, cx + 2, hubY + 2, 0xFFFF2438);
        } else if (((int) (timeSec * 2.0F)) % 2 == 0) {
            g.fill(cx - 1, hubY - 6, cx + 1, hubY - 4, 0xFFFF2438);
        }
    }
}
