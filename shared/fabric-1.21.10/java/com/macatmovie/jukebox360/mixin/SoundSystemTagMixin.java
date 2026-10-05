package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.AudioSourceBlockWhitelist;
import com.macatmovie.jukebox360.Jukebox360Runtime;
import com.macatmovie.jukebox360.config.ClientConfig;
import com.macatmovie.jukebox360.mixinaccess.SourceTagAccessor;
import com.mojang.logging.LogUtils;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SoundSystem.class)
public abstract class SoundSystemTagMixin {
    private static final Logger LOG = LogUtils.getLogger();

    @Shadow @Final
    private Map<SoundInstance, Channel.SourceManager> sources;

    @Inject(method = "play", at = @At("RETURN"))
    private void jukebox360$afterPlay(SoundInstance sound, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        Channel.SourceManager manager = this.sources.get(sound);
        if (manager == null) return;

        boolean records = sound.getCategory() == SoundCategory.RECORDS;
        Identifier id = sound.getId();
        boolean noteBlock = id != null && id.getPath().startsWith("block.note_block");
        boolean whitelistBlock = !records && !noteBlock
                && AudioSourceBlockWhitelist.matches(sound.getX(), sound.getY(), sound.getZ());
        boolean affected = records || noteBlock || whitelistBlock;
        boolean sustained = records || whitelistBlock;
        Vec3d worldPos = new Vec3d(sound.getX(), sound.getY(), sound.getZ());

        manager.run(source -> {
            SourceTagAccessor accessor = (SourceTagAccessor) (Object) source;

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
                LOG.info("[Jukebox360] Tagged {} sound {} at {}", records ? "RECORDS" : (noteBlock ? "NOTE_BLOCK" : "WHITELIST_BLOCK"), id, worldPos);
            }
        });
    }

    private static void clear(SourceTagAccessor accessor) {
        if (!accessor.jukebox360$isAffected() && !accessor.jukebox360$isRecords()) return;
        accessor.jukebox360$setAffected(false);
        accessor.jukebox360$setRecords(false);
        accessor.jukebox360$setWorldPos(null);
        Jukebox360Runtime.remove(accessor);
    }
}
