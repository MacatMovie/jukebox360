package com.macatmovie.jukebox360.client.gui;

import com.macatmovie.jukebox360.config.ClientConfig;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class Jukebox360ConfigScreen extends Screen {
    private final Screen parent;
    private BlendSlider blendSlider;
    private Button debugButton;
    private Button whitelistButton;

    public Jukebox360ConfigScreen(Screen parent) {
        super(Component.literal("Jukebox 360 Configuration"));
        this.parent = parent;
    }

    @Override protected void init() {
        int x = width / 2 - 100;
        int y = Math.max(35, height / 2 - 72);
        blendSlider = addRenderableWidget(new BlendSlider(x, y, 200, 20));
        debugButton = addRenderableWidget(new Button(x, y + 26, 200, 20, debugText(), b -> { ClientConfig.setDebugLog(!ClientConfig.debugLog()); b.setMessage(debugText()); }));
        whitelistButton = addRenderableWidget(new Button(x, y + 52, 200, 20, whitelistText(), b -> minecraft.setScreen(new BlockWhitelistScreen(this))));
        addRenderableWidget(new Button(x, y + 84, 98, 20, Component.literal("Reset to Defaults"), b -> { ClientConfig.resetDefaults(); ClientConfig.save(); blendSlider.syncFromConfig(); debugButton.setMessage(debugText()); whitelistButton.setMessage(whitelistText()); }));
        addRenderableWidget(new Button(x + 102, y + 84, 98, 20, Component.literal("Done"), b -> closeAndSave()));
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
