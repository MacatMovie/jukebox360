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
 * In 1.20.1 Yarn, SoundSystem has play(SoundInstance) and play(SoundInstance, int).
 * We hook both with require=0 so it stays resilient across minor mapping diffs.
 */
@Mixin(SoundSystem.class)
public class SoundSystemThreadLocalMixin {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), require = 0)
    private void jukebox360$playHead(SoundInstance sound, CallbackInfo ci) {
        SoundContext.CURRENT_SOUND.set(sound);
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("RETURN"), require = 0)
    private void jukebox360$playReturn(SoundInstance sound, CallbackInfo ci) {
        SoundContext.CURRENT_SOUND.remove();
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;I)V", at = @At("HEAD"), require = 0)
    private void jukebox360$playHeadDelay(SoundInstance sound, int delay, CallbackInfo ci) {
        SoundContext.CURRENT_SOUND.set(sound);
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;I)V", at = @At("RETURN"), require = 0)
    private void jukebox360$playReturnDelay(SoundInstance sound, int delay, CallbackInfo ci) {
        SoundContext.CURRENT_SOUND.remove();
    }
}
