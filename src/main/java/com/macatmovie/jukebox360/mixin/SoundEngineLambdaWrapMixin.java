package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.config.ClientConfig;
import com.macatmovie.jukebox360.Jukebox360Runtime;
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

    private void tagHead(ChannelAccess.ChannelHandle handle, SoundInstance sound) {
        if (sound == null || sound.getLocation() == null) return;

        boolean isRecords = sound.getSource() == SoundSource.RECORDS;
        boolean isNote = isNoteBlock(sound);
        if (!isRecords && !isNote) return;

        Vec3 worldPos = new Vec3(sound.getX(), sound.getY(), sound.getZ());

        handle.execute((Channel ch) -> {
            ChannelTagAccessor acc = (ChannelTagAccessor) ch;
            acc.jukebox360$setAffected(true);
            acc.jukebox360$setRecords(isRecords);
            acc.jukebox360$setWorldPos(worldPos);
            Jukebox360Runtime.add(acc);

            if (ClientConfig.DEBUG_LOG.get()) {
                LOG.info("[Jukebox360] Tagged {} for {} pos={}",
                        isRecords ? "RECORDS" : "NOTE_BLOCK",
                        sound.getLocation(), worldPos);
            }
        });
    }

    @Inject(method = "lambda$play$7(Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;Lnet/minecraft/client/resources/sounds/SoundInstance;Lcom/mojang/blaze3d/audio/SoundBuffer;)V",
            at = @At("HEAD"), remap = false, require = 0)
    private void jukebox360$lambda7(ChannelAccess.ChannelHandle handle, SoundInstance sound, SoundBuffer buf, CallbackInfo ci) { tagHead(handle, sound); }

    @Inject(method = "lambda$play$9(Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;Lnet/minecraft/client/resources/sounds/SoundInstance;Lnet/minecraft/client/sounds/AudioStream;)V",
            at = @At("HEAD"), remap = false, require = 0)
    private void jukebox360$lambda9(ChannelAccess.ChannelHandle handle, SoundInstance sound, AudioStream stream, CallbackInfo ci) { tagHead(handle, sound); }
}
