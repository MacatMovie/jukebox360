package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.client.gui.Jukebox360ConfigScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin extends Screen {
    protected SoundOptionsScreenMixin(Component title) { super(title); }
    @Inject(method="init", at=@At("TAIL"))
    private void jukebox360$button(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(Component.literal("Jukebox 360..."), b ->
                this.minecraft.gui.setScreen(new Jukebox360ConfigScreen(this)))
                .bounds(Math.max(6, this.width - 156), 6, 150, 20).build());
    }
}
