package com.underworld.mod.client.gui;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class UnderWorldWikiScreen extends Screen {
    private static final int TAB_ENTITIES = 0;
    private static final int TAB_DIMENSIONS = 1;
    private static final int TAB_BLOCKS = 2;
    private static final int TAB_STRUCTURES = 3;
    private static final int TAB_EVENTS = 4;
    private static final int TAB_COMMANDS = 5;

    private record WikiEntry(String id, int accentColor) {}

    private static final WikiEntry[][] CATEGORY_ENTRIES = {
            // 0: All 14 Entities
            {
                    new WikiEntry("entity.friend", 0xFF55FF99),
                    new WikiEntry("entity.hands", 0xFFE63946),
                    new WikiEntry("entity.tree_mimic", 0xFF70A838),
                    new WikiEntry("entity.the_sky", 0xFF8C9BB8),
                    new WikiEntry("entity.eyes_of_fun", 0xFFFFFF55),
                    new WikiEntry("entity.smile", 0xFFFF3344),
                    new WikiEntry("entity.the_truth", 0xFFFFFFFF),
                    new WikiEntry("entity.clock", 0xFFE8BA48),
                    new WikiEntry("entity.the_mimic", 0xFF42C5F5),
                    new WikiEntry("entity.demon_fish", 0xFFD93B6B),
                    new WikiEntry("entity.the_ball", 0xFFB066FF),
                    new WikiEntry("entity.living_pylon", 0xFFFF6B35),
                    new WikiEntry("entity.wind_turbine_stalker", 0xFFB8D4E8),
                    new WikiEntry("entity.the_dot_com", 0xFFFF2233)
            },
            // 1: All 8 Dimensions
            {
                    new WikiEntry("dim.fun_world", 0xFF6CE63E),
                    new WikiEntry("dim.maze", 0xFFB83030),
                    new WikiEntry("dim.error", 0xFFFF55FF),
                    new WikiEntry("dim.time", 0xFFFFCC44),
                    new WikiEntry("dim.plains", 0xFFA5B8A0),
                    new WikiEntry("dim.wind_ocean", 0xFF7BA2B8),
                    new WikiEntry("dim.xp", 0xFF3A8EFF),
                    new WikiEntry("dim.browser", 0xFF4CD6FF)
            },
            // 2: Blocks, Items & Effects
            {
                    new WikiEntry("blocks.computer", 0xFF70D8FF),
                    new WikiEntry("blocks.portals", 0xFFE63946),
                    new WikiEntry("blocks.fun_maze", 0xFF7CE850),
                    new WikiEntry("blocks.error_time", 0xFFE8B848),
                    new WikiEntry("blocks.plains_ocean", 0xFF9EB4C8),
                    new WikiEntry("blocks.xp_browser", 0xFF4CE070),
                    new WikiEntry("blocks.effects", 0xFFC070FF)
            },
            // 3: Structures & Lore Builds
            {
                    new WikiEntry("struct.worldgen", 0xFF88D488),
                    new WikiEntry("struct.power_pylon", 0xFFFF7040),
                    new WikiEntry("struct.void_shrine", 0xFF9050FF),
                    new WikiEntry("struct.lore_builds", 0xFFF2C14E),
                    new WikiEntry("struct.books", 0xFF66CCFF)
            },
            // 4: Events, Mechanics & Audio
            {
                    new WikiEntry("events.friend_day1", 0xFF55FF99),
                    new WikiEntry("events.hell", 0xFFFF2244),
                    new WikiEntry("events.rules", 0xFF98B0CC),
                    new WikiEntry("events.audio", 0xFFE699FF)
            },
            // 5: Commands & Config
            {
                    new WikiEntry("cmd.spawn_trigger", 0xFF66E0FF),
                    new WikiEntry("cmd.dim_struct", 0xFFFFAA44),
                    new WikiEntry("cmd.events_reset", 0xFF70EE88),
                    new WikiEntry("cmd.config", 0xFFC4CCE0)
            }
    };

    private static final String[] TAB_KEYS = {
            "wiki.under_world.tab.entities",
            "wiki.under_world.tab.dimensions",
            "wiki.under_world.tab.blocks",
            "wiki.under_world.tab.structures",
            "wiki.under_world.tab.events",
            "wiki.under_world.tab.commands"
    };

    private final Screen parent;
    private int currentTab = TAB_ENTITIES;
    private int selectedIndex = 0;
    private int listScroll = 0;
    private int detailScroll = 0;

    private final List<Button> tabButtons = new ArrayList<>();
    private final List<Button> entryButtons = new ArrayList<>();

    public UnderWorldWikiScreen(Screen parent) {
        super(Component.translatable("wiki.under_world.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.tabButtons.clear();
        this.entryButtons.clear();

        int topY = 26;
        int margin = 12;
        int usableW = this.width - margin * 2 - 64;
        int tabCount = TAB_KEYS.length;
        int tabW = Math.max(54, (usableW - (tabCount - 1) * 4) / tabCount);

        for (int i = 0; i < tabCount; i++) {
            final int tabIdx = i;
            Button btn = Button.builder(Component.translatable(TAB_KEYS[i]), b -> {
                this.currentTab = tabIdx;
                this.selectedIndex = 0;
                this.listScroll = 0;
                this.detailScroll = 0;
                this.rebuildEntryButtons();
            }).bounds(margin + i * (tabW + 4), topY, tabW, 20).build();
            this.addRenderableWidget(btn);
            this.tabButtons.add(btn);
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> onClose())
                .bounds(this.width - margin - 58, topY, 58, 20).build());

        this.rebuildEntryButtons();
    }

    private void rebuildEntryButtons() {
        for (Button old : this.entryButtons) {
            this.removeWidget(old);
        }
        this.entryButtons.clear();

        int leftX = 12;
        int leftW = Math.min(185, Math.max(125, this.width / 3 - 10));
        int panelTop = 54;
        int panelH = this.height - panelTop - 16;
        int visibleRows = Math.max(1, (panelH - 12) / 22);

        WikiEntry[] entries = CATEGORY_ENTRIES[this.currentTab];
        int maxListScroll = Math.max(0, entries.length - visibleRows);
        this.listScroll = Mth.clamp(this.listScroll, 0, maxListScroll);

        for (int row = 0; row < visibleRows; row++) {
            int idx = this.listScroll + row;
            if (idx >= entries.length) break;
            final int entryIdx = idx;
            String title = I18n.get("wiki.under_world." + entries[idx].id + ".name");
            Button b = Button.builder(Component.literal(title), btn -> {
                this.selectedIndex = entryIdx;
                this.detailScroll = 0;
            }).bounds(leftX + 4, panelTop + 6 + row * 22, leftW - 8, 20).build();
            this.addRenderableWidget(b);
            this.entryButtons.add(b);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int leftW = Math.min(185, Math.max(125, this.width / 3 - 10));
        int step = delta > 0 ? -1 : 1;
        if (mouseX <= 12 + leftW) {
            WikiEntry[] entries = CATEGORY_ENTRIES[this.currentTab];
            int panelH = this.height - 54 - 16;
            int visibleRows = Math.max(1, (panelH - 12) / 22);
            int maxScroll = Math.max(0, entries.length - visibleRows);
            int next = Mth.clamp(this.listScroll + step, 0, maxScroll);
            if (next != this.listScroll) {
                this.listScroll = next;
                this.rebuildEntryButtons();
            }
            return true;
        } else {
            this.detailScroll = Math.max(0, this.detailScroll + step * 18);
            return true;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 265) { // UP
            this.detailScroll = Math.max(0, this.detailScroll - 22);
            return true;
        }
        if (keyCode == 264) { // DOWN
            this.detailScroll += 22;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long ms = Util.getMillis();

        // Dark CRT terminal backdrop
        g.fillGradient(0, 0, this.width, this.height, 0xFF04060A, 0xFF0C121B);
        for (int y = 0; y < this.height; y += 3) {
            g.fill(0, y, this.width, y + 1, 0x22000000);
        }

        // Header bar
        g.fill(0, 0, this.width, 22, 0xCC090D14);
        g.fill(0, 21, this.width, 22, 0xFFE63946);
        g.drawString(this.font, I18n.get("wiki.under_world.header"), 12, 7, 0xFFFF5566);
        String hint = I18n.get("wiki.under_world.scroll_hint");
        g.drawString(this.font, hint, this.width - this.font.width(hint) - 12, 7, 0xFF8C9BB0);

        int margin = 12;
        int leftW = Math.min(185, Math.max(125, this.width / 3 - 10));
        int panelTop = 54;
        int panelH = this.height - panelTop - 14;
        int rightX = margin + leftW + 8;
        int rightW = this.width - rightX - margin;

        // Left navigation frame
        g.fill(margin, panelTop, margin + leftW, panelTop + panelH, 0xAA070A0F);
        drawBoxOutline(g, margin, panelTop, leftW, panelH, 0x883A4A60);

        // Right detail frame
        g.fill(rightX, panelTop, rightX + rightW, panelTop + panelH, 0xB8080C12);
        WikiEntry[] entries = CATEGORY_ENTRIES[this.currentTab];
        this.selectedIndex = Mth.clamp(this.selectedIndex, 0, Math.max(0, entries.length - 1));
        WikiEntry current = entries[this.selectedIndex];
        drawBoxOutline(g, rightX, panelTop, rightW, panelH, current.accentColor());

        // Active tab indicator
        for (int i = 0; i < this.tabButtons.size(); i++) {
            Button tb = this.tabButtons.get(i);
            if (i == this.currentTab) {
                g.fill(tb.getX(), tb.getY() + tb.getHeight() - 2, tb.getX() + tb.getWidth(), tb.getY() + tb.getHeight(), 0xFFE63946);
            }
        }

        super.render(g, mouseX, mouseY, partialTick);

        // Highlight active left entry
        for (int i = 0; i < this.entryButtons.size(); i++) {
            int entryIdx = this.listScroll + i;
            if (entryIdx == this.selectedIndex) {
                Button eb = this.entryButtons.get(i);
                g.fill(eb.getX() - 2, eb.getY(), eb.getX(), eb.getY() + eb.getHeight(), current.accentColor());
            }
        }

        // Render right-hand encyclopedia article inside clipped scissor region
        int pad = 12;
        int contentX = rightX + pad;
        int contentY = panelTop + pad;
        int contentW = Math.max(80, rightW - pad * 2 - 8);
        int contentH = Math.max(40, panelH - pad * 2);

        String title = I18n.get("wiki.under_world." + current.id + ".name");
        String subtitle = I18n.get("wiki.under_world." + current.id + ".tag");
        String body = I18n.get("wiki.under_world." + current.id + ".desc");

        List<FormattedCharSequence> lines = new ArrayList<>();
        lines.addAll(this.font.split(Component.literal(body), contentW));

        int totalTextH = 34 + lines.size() * 12;
        int maxDetailScroll = Math.max(0, totalTextH - contentH);
        this.detailScroll = Mth.clamp(this.detailScroll, 0, maxDetailScroll);

        g.enableScissor(rightX + 2, panelTop + 2, rightX + rightW - 2, panelTop + panelH - 2);
        int yCursor = contentY - this.detailScroll;

        // Animated tiny radar dot + Entry Title
        if (((ms / 450L) & 1L) == 0L) {
            g.fill(contentX, yCursor + 2, contentX + 5, yCursor + 7, current.accentColor());
        }
        g.drawString(this.font, Component.literal(title).withStyle(s -> s.withBold(true)), contentX + 10, yCursor, current.accentColor());
        yCursor += 14;
        g.drawString(this.font, subtitle, contentX + 10, yCursor, 0xFF9DB2CC);
        yCursor += 13;
        g.fill(contentX, yCursor, contentX + contentW, yCursor + 1, 0x664A5A70);
        yCursor += 8;

        for (FormattedCharSequence seq : lines) {
            if (yCursor + 12 >= panelTop && yCursor <= panelTop + panelH) {
                g.drawString(this.font, seq, contentX, yCursor, 0xFFE4ECF5);
            }
            yCursor += 12;
        }
        g.disableScissor();

        // Scrollbar indicator if needed
        if (maxDetailScroll > 0) {
            int barH = Math.max(18, (contentH * contentH) / Math.max(1, totalTextH));
            int barY = panelTop + 6 + Math.round(((float) this.detailScroll / maxDetailScroll) * (panelH - 12 - barH));
            g.fill(rightX + rightW - 6, panelTop + 6, rightX + rightW - 3, panelTop + panelH - 6, 0x44222B38);
            g.fill(rightX + rightW - 6, barY, rightX + rightW - 3, barY + barH, current.accentColor());
        }
    }

    private static void drawBoxOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }
}
