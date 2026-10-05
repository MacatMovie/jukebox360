package com.macatmovie.jukebox360.client.gui;

import com.macatmovie.jukebox360.config.ClientConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class BlockWhitelistScreen extends Screen {
    private static final int PAGE_SIZE = 5;
    private final Screen parent;
    private final List<String> entries;
    private final int page;
    private TextFieldWidget input;

    public BlockWhitelistScreen(Screen parent) { this(parent, ClientConfig.blockWhitelist(), 0); }
    private BlockWhitelistScreen(Screen parent, List<String> entries, int page) {
        super(Text.literal("Jukebox 360 Audio Source Blocks"));
        this.parent = parent;
        this.entries = new ArrayList<>(entries);
        this.page = Math.max(0, page);
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 150;
        int y = 28;
        input = new TextFieldWidget(this.textRenderer, x, y, 220, 20, Text.literal("modid:block"));
        input.setMaxLength(160);
        this.addDrawableChild(input);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Add"), b -> addEntry())
                .dimensions(x + 226, y, 74, 20).build());

        int start = page * PAGE_SIZE;
        int end = Math.min(entries.size(), start + PAGE_SIZE);
        for (int i = start; i < end; i++) {
            final int index = i;
            String id = entries.get(i);
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Remove: " + id), b -> removeEntry(index))
                    .dimensions(x, y + 30 + (i - start) * 24, 300, 20).build());
        }

        int navY = y + 30 + PAGE_SIZE * 24 + 4;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("< Previous"), b -> openPage(Math.max(0, page - 1)))
                .dimensions(x, navY, 96, 20).build()).active = page > 0;
        int maxPage = entries.isEmpty() ? 0 : (entries.size() - 1) / PAGE_SIZE;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Next >"), b -> openPage(Math.min(maxPage, page + 1)))
                .dimensions(x + 102, navY, 96, 20).build()).active = page < maxPage;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset List"), b ->
                this.client.setScreen(new BlockWhitelistScreen(parent, ClientConfig.DEFAULT_BLOCK_WHITELIST, 0)))
                .dimensions(x + 204, navY, 96, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> saveAndClose())
                .dimensions(this.width / 2 - 75, navY + 28, 150, 20).build());
    }

    private void addEntry() {
        String id = input.getText().trim().toLowerCase();
        if (!ClientConfig.isValidId(id) || entries.contains(id)) return;
        entries.add(id);
        this.client.setScreen(new BlockWhitelistScreen(parent, entries, (entries.size() - 1) / PAGE_SIZE));
    }
    private void removeEntry(int index) {
        if (index >= 0 && index < entries.size()) entries.remove(index);
        int maxPage = entries.isEmpty() ? 0 : (entries.size() - 1) / PAGE_SIZE;
        this.client.setScreen(new BlockWhitelistScreen(parent, entries, Math.min(page, maxPage)));
    }
    private void openPage(int next) { this.client.setScreen(new BlockWhitelistScreen(parent, entries, next)); }
    private void saveAndClose() { ClientConfig.setBlockWhitelist(entries); ClientConfig.save(); this.client.setScreen(parent); }
    @Override public void close() { saveAndClose(); }
}
