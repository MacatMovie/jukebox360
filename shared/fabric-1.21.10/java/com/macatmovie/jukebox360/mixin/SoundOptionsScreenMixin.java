package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.client.gui.Jukebox360ConfigScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin extends Screen {
    protected SoundOptionsScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void jukebox360$addConfigButton(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Jukebox 360..."), b ->
                this.client.setScreen(new Jukebox360ConfigScreen(this)))
                .dimensions(Math.max(6, this.width - 156), 6, 150, 20).build());
    }
}
