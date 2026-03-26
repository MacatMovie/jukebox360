package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.Jukebox360Runtime;
import com.macatmovie.jukebox360.config.ClientConfig;
import com.macatmovie.jukebox360.mixinaccess.ChannelTagAccessor;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class SoundEngineLambdaWrapMixin {
    private static final Logger LOG = LogManager.getLogger("Jukebox360");

    private static boolean isNoteBlock(SoundInstance sound) {
        if (sound == null || sound.getLocation() == null) return false;
        String path = sound.getLocation().getPath();
        return path.startsWith("block.note_block");
    }

    private void handle(ChannelAccess.ChannelHandle handle, SoundInstance sound) {
        if (sound == null || sound.getLocation() == null) return;

        boolean isRecords = sound.getSource() == SoundSource.RECORDS;
        boolean isNote = isNoteBlock(sound);
        boolean target = isRecords || isNote;

        Vec3 worldPos = new Vec3(sound.getX(), sound.getY(), sound.getZ());

        handle.execute((Channel ch) -> {
            ChannelTagAccessor acc = (ChannelTagAccessor) ch;

            if (target) {
                acc.jukebox360$setAffected(true);
                acc.jukebox360$setRecords(isRecords);
                acc.jukebox360$setWorldPos(worldPos);

                if (isRecords) {
                    // Long-running: keep in active set and project every tick
                    Jukebox360Runtime.add(acc);
                } else {
                    // NOTE_BLOCK is short: project once at play-time, do NOT keep in active set
                    acc.jukebox360$applyProjectionTick();
                    Jukebox360Runtime.remove(acc);
                }

                if (ClientConfig.DEBUG_LOG.get()) {
                    LOG.info("[Jukebox360] Tagged {} for {} pos={}",
                            isRecords ? "RECORDS" : "NOTE_BLOCK",
                            sound.getLocation(), worldPos);
                }
            } else {
                // Channel reuse: if previously tagged, clear immediately so we never hijack other sounds
                if (acc.jukebox360$isAffected() || acc.jukebox360$isRecords()) {
                    acc.jukebox360$setAffected(false);
                    acc.jukebox360$setRecords(false);
                    acc.jukebox360$setWorldPos(null);
                    Jukebox360Runtime.remove(acc);

                    if (ClientConfig.DEBUG_LOG.get()) {
                        LOG.info("[Jukebox360] Cleared tag for {} src={}",
                                sound.getLocation(), sound.getSource());
                    }
                }
            }
        });
    }

    @Inject(method = "lambda$play$7(Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;Lnet/minecraft/client/resources/sounds/SoundInstance;Lcom/mojang/blaze3d/audio/SoundBuffer;)V",
            at = @At("HEAD"), remap = false, require = 0)
    private void jukebox360$lambda7(ChannelAccess.ChannelHandle handle, SoundInstance sound, SoundBuffer buf, CallbackInfo ci) {
        handle(handle, sound);
    }

    @Inject(method = "lambda$play$9(Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;Lnet/minecraft/client/resources/sounds/SoundInstance;Lnet/minecraft/client/sounds/AudioStream;)V",
            at = @At("HEAD"), remap = false, require = 0)
    private void jukebox360$lambda9(ChannelAccess.ChannelHandle handle, SoundInstance sound, AudioStream stream, CallbackInfo ci) {
        handle(handle, sound);
    }
}
