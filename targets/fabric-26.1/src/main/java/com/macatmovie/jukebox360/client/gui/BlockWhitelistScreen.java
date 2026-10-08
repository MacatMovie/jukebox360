package com.macatmovie.jukebox360.client.gui;

import com.macatmovie.jukebox360.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class BlockWhitelistScreen extends Screen {
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // 26.x uses the render-state extraction pipeline instead of Screen#render.
        // Ask vanilla to provide the normal dimmed/blurred screen background.
        this.extractTransparentBackground(graphics);
    }

    private static final int PAGE_SIZE = 5;
    private final Screen parent;
    private final List<String> entries;
    private final int page;
    private EditBox input;

    public BlockWhitelistScreen(Screen parent) { this(parent, ClientConfig.blockWhitelist(), 0); }
    private BlockWhitelistScreen(Screen parent, List<String> entries, int page) {
        super(Component.literal("Jukebox 360 Audio Source Blocks"));
        this.parent = parent; this.entries = new ArrayList<>(entries); this.page = Math.max(0, page);
    }

    @Override protected void init() {
        super.init();
        int x = this.width / 2 - 150; int y = 28;
        input = new EditBox(this.font, x, y, 220, 20, Component.literal("modid:block"));
        input.setMaxLength(160); this.addRenderableWidget(input);
        this.addRenderableWidget(Button.builder(Component.literal("Add"), b -> addEntry()).bounds(x + 226, y, 74, 20).build());
        int start = page * PAGE_SIZE; int end = Math.min(entries.size(), start + PAGE_SIZE);
        for (int i = start; i < end; i++) {
            final int index = i; String id = entries.get(i);
            this.addRenderableWidget(Button.builder(Component.literal("Remove: " + id), b -> removeEntry(index))
                    .bounds(x, y + 30 + (i - start) * 24, 300, 20).build());
        }
        int navY = y + 30 + PAGE_SIZE * 24 + 4;
        Button prev = this.addRenderableWidget(Button.builder(Component.literal("< Previous"), b -> openPage(Math.max(0, page - 1))).bounds(x, navY, 96, 20).build());
        prev.active = page > 0;
        int maxPage = entries.isEmpty() ? 0 : (entries.size() - 1) / PAGE_SIZE;
        Button next = this.addRenderableWidget(Button.builder(Component.literal("Next >"), b -> openPage(Math.min(maxPage, page + 1))).bounds(x + 102, navY, 96, 20).build());
        next.active = page < maxPage;
        this.addRenderableWidget(Button.builder(Component.literal("Reset List"), b -> this.minecraft.setScreen(new BlockWhitelistScreen(parent, ClientConfig.DEFAULT_BLOCK_WHITELIST, 0))).bounds(x + 204, navY, 96, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> saveAndClose()).bounds(this.width / 2 - 75, navY + 28, 150, 20).build());
    }
    private void addEntry() { String id=input.getValue().trim().toLowerCase(); if(!ClientConfig.isValidId(id)||entries.contains(id))return; entries.add(id); this.minecraft.setScreen(new BlockWhitelistScreen(parent, entries, (entries.size()-1)/PAGE_SIZE)); }
    private void removeEntry(int index) { if(index>=0&&index<entries.size())entries.remove(index); int max=entries.isEmpty()?0:(entries.size()-1)/PAGE_SIZE; this.minecraft.setScreen(new BlockWhitelistScreen(parent, entries, Math.min(page,max))); }
    private void openPage(int next) { this.minecraft.setScreen(new BlockWhitelistScreen(parent, entries, next)); }
    private void saveAndClose() { ClientConfig.setBlockWhitelist(entries); ClientConfig.save(); this.minecraft.setScreen(parent); }
    @Override public void onClose() { saveAndClose(); }
}
