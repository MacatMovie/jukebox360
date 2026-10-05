package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.Jukebox360Runtime;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftTickMixin {
    @Inject(method = "tick", at = @At("TAIL"), remap = false)
    private void jukebox360$tick(CallbackInfo ci) {
        Jukebox360Runtime.tick();
    }
}
