package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.Jukebox360Runtime;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientTickMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void jukebox360$tick(CallbackInfo ci) {
        Jukebox360Runtime.tick();
    }
}
