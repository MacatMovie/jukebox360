package com.macatmovie.jukebox360.client.gui;

import com.macatmovie.jukebox360.config.ClientConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public final class Jukebox360ConfigScreen extends Screen {
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Config screens are standalone screens; never leave the previous UI visible underneath.
        context.fill(0, 0, this.width, this.height, 0xF0101010);
        super.render(context, mouseX, mouseY, delta);
    }


    private final Screen parent;
    private BlendSlider blendSlider;
    private ButtonWidget debugButton;
    private ButtonWidget whitelistButton;

    public Jukebox360ConfigScreen(Screen parent) {
        super(Text.literal("Jukebox 360 Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 100;
        int y = Math.max(35, this.height / 2 - 72);

        blendSlider = this.addDrawableChild(new BlendSlider(x, y, 200, 20));

        debugButton = this.addDrawableChild(ButtonWidget.builder(debugText(), b -> {
            ClientConfig.setDebugLog(!ClientConfig.debugLog());
            b.setMessage(debugText());
        }).dimensions(x, y + 26, 200, 20).build());

        whitelistButton = this.addDrawableChild(ButtonWidget.builder(whitelistText(), b ->
                this.client.setScreen(new BlockWhitelistScreen(this)))
                .dimensions(x, y + 52, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset to Defaults"), b -> {
            ClientConfig.resetDefaults();
            ClientConfig.save();
            blendSlider.syncFromConfig();
            debugButton.setMessage(debugText());
            whitelistButton.setMessage(whitelistText());
        }).dimensions(x, y + 84, 98, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> closeAndSave())
                .dimensions(x + 102, y + 84, 98, 20).build());
    }

    private Text debugText() { return Text.literal("Debug Logging: " + (ClientConfig.debugLog() ? "ON" : "OFF")); }
    private Text whitelistText() { return Text.literal("Audio Source Block Whitelist (" + ClientConfig.blockWhitelist().size() + ")..."); }

    private void closeAndSave() {
        ClientConfig.save();
        this.client.setScreen(parent);
    }

    @Override public void close() { closeAndSave(); }

    private static final class BlendSlider extends SliderWidget {
        private BlendSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Text.literal(""), ClientConfig.blend());
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal("Anchor Blend: " + Math.round(this.value * 100.0D) + "%"));
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
