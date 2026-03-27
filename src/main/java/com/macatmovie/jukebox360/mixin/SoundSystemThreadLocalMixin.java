package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.SoundContext;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Thread-local bridge: associate the currently-playing SoundInstance with the Channel/Source being configured.
 *
 * In this Yarn build, SoundSystem.play(SoundInstance) is void, so we only inject with CallbackInfo.
 */
@Mixin(SoundSystem.class)
public class SoundSystemThreadLocalMixin {

    @Inject(method = "play", at = @At("HEAD"))
    private void jukebox360$playHead(SoundInstance sound, CallbackInfo ci) {
        SoundContext.CURRENT_SOUND.set(sound);
    }

    @Inject(method = "play", at = @At("RETURN"))
    private void jukebox360$playReturn(SoundInstance sound, CallbackInfo ci) {
        SoundContext.CURRENT_SOUND.remove();
    }
}
