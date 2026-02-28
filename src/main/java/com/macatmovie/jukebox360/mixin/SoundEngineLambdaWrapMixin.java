package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.config.ClientConfig;
import com.macatmovie.jukebox360.mixinaccess.ChannelTagAccessor;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL10;
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

    private static Vec3 computeBlendedPos(Vec3 worldPos) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return worldPos;

        Vec3 ear = player.getEyePosition();
        Vec3 toWorld = worldPos.subtract(ear);
        double dist = toWorld.length();
        if (dist < 1e-6) return worldPos;

        Vec3 dirWorld = toWorld.normalize();

        Vec3 look = player.getLookAngle();
        if (look.lengthSqr() < 1e-6) look = new Vec3(0, 0, 1);
        Vec3 dirLook = look.normalize();

        double blend = ClientConfig.BLEND.get();

        Vec3 blendedDir = dirWorld.scale(1.0 - blend).add(dirLook.scale(blend));
        if (blendedDir.lengthSqr() < 1e-10) blendedDir = dirWorld;
        blendedDir = blendedDir.normalize();

        return ear.add(blendedDir.scale(dist));
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

            // NOTE BLOCKS are short: apply the projection immediately at play time so you can hear it
            // even if no further Channel updates happen before the sound ends.
            if (isNote) {
                Vec3 out = computeBlendedPos(worldPos);
                int srcId = acc.jukebox360$getSourceId();
                AL10.alSourcei(srcId, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
                AL10.alSource3f(srcId, AL10.AL_POSITION, (float) out.x, (float) out.y, (float) out.z);
            }

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
