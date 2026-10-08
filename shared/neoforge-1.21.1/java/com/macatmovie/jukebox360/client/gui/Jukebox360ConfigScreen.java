package com.macatmovie.jukebox360.client.gui;

import com.macatmovie.jukebox360.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class Jukebox360ConfigScreen extends Screen {
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xF0101010);
        super.render(graphics, mouseX, mouseY, partialTick);
    }


    private final Screen parent;
    private BlendSlider blendSlider;
    private Button debugButton;
    private Button whitelistButton;

    public Jukebox360ConfigScreen(Screen parent) {
        super(Component.literal("Jukebox 360 Configuration"));
        this.parent = parent;
    }

    @Override protected void init() {
        super.init();
        int x = width / 2 - 100;
        int y = Math.max(35, height / 2 - 72);
        blendSlider = addRenderableWidget(new BlendSlider(x, y, 200, 20));
        debugButton = addRenderableWidget(Button.builder(debugText(), b -> { ClientConfig.setDebugLog(!ClientConfig.debugLog()); b.setMessage(debugText()); }).bounds(x, y + 26, 200, 20).build());
        whitelistButton = addRenderableWidget(Button.builder(whitelistText(), b -> minecraft.setScreen(new BlockWhitelistScreen(this))).bounds(x, y + 52, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset to Defaults"), b -> { ClientConfig.resetDefaults(); ClientConfig.save(); blendSlider.syncFromConfig(); debugButton.setMessage(debugText()); whitelistButton.setMessage(whitelistText()); }).bounds(x, y + 84, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> closeAndSave()).bounds(x + 102, y + 84, 98, 20).build());
    }

    private Component debugText() { return Component.literal("Debug Logging: " + (ClientConfig.debugLog() ? "ON" : "OFF")); }
    private Component whitelistText() { return Component.literal("Audio Source Block Whitelist (" + ClientConfig.blockWhitelist().size() + ")..."); }
    private void closeAndSave() { ClientConfig.save(); minecraft.setScreen(parent); }
    @Override public void onClose() { closeAndSave(); }

    private static final class BlendSlider extends AbstractSliderButton {
        private BlendSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.literal(""), ClientConfig.blend());
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("Anchor Blend: " + Math.round(this.value * 100.0D) + "%"));
        }

        @Override
        protected void applyValue() {
            double snapped = Math.round(this.value * 100.0D) / 100.0D;
            this.value = snapped;
            ClientConfig.setBlend(snapped);
            updateMessage();
        }

        private void syncFromConfig() {
            this.value = ClientConfig.blend();
            updateMessage();
        }
    }
}
