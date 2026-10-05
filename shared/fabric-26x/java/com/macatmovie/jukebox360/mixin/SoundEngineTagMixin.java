package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.AudioSourceBlockWhitelist;
import com.macatmovie.jukebox360.Jukebox360Runtime;
import com.macatmovie.jukebox360.config.ClientConfig;
import com.macatmovie.jukebox360.mixinaccess.ChannelTagAccessor;
import com.mojang.logging.LogUtils;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SoundEngine.class)
public abstract class SoundEngineTagMixin {
    private static final Logger LOG = LogUtils.getLogger();

    @Shadow @Final
    private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Inject(method = "play", at = @At("RETURN"))
    private void jukebox360$afterPlay(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        ChannelAccess.ChannelHandle handle = this.instanceToChannel.get(sound);
        if (handle == null) return;

        boolean records = sound.getSource() == SoundSource.RECORDS;
        boolean noteBlock = sound.getIdentifier() != null
                && sound.getIdentifier().getPath().startsWith("block.note_block");
        boolean whitelistBlock = !records && !noteBlock
                && AudioSourceBlockWhitelist.matches(sound.getX(), sound.getY(), sound.getZ());
        boolean affected = records || noteBlock || whitelistBlock;
        boolean sustained = records || whitelistBlock;
        Vec3 worldPos = new Vec3(sound.getX(), sound.getY(), sound.getZ());

        handle.execute(channel -> {
            ChannelTagAccessor accessor = (ChannelTagAccessor) (Object) channel;

            if (!affected) {
                clear(accessor);
                return;
            }

            accessor.jukebox360$setAffected(true);
            accessor.jukebox360$setRecords(sustained);
            accessor.jukebox360$setWorldPos(worldPos);

            if (sustained) {
                Jukebox360Runtime.add(accessor);
            } else {
                accessor.jukebox360$applyProjectionTick();
                Jukebox360Runtime.remove(accessor);
            }

            if (ClientConfig.debugLog()) {
                LOG.info("[Jukebox360] Tagged {} sound {} at {}",
                        records ? "RECORDS" : (noteBlock ? "NOTE_BLOCK" : "WHITELIST_BLOCK"),
                        sound.getIdentifier(), worldPos);
            }
        });
    }

    private static void clear(ChannelTagAccessor accessor) {
        if (!accessor.jukebox360$isAffected() && !accessor.jukebox360$isRecords()) return;
        accessor.jukebox360$setAffected(false);
        accessor.jukebox360$setRecords(false);
        accessor.jukebox360$setWorldPos(null);
        Jukebox360Runtime.remove(accessor);
    }
}
