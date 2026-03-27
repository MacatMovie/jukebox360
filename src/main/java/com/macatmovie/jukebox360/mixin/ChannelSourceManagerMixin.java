package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.Jukebox360Runtime;
import com.macatmovie.jukebox360.config.ClientConfig;
import com.macatmovie.jukebox360.mixinaccess.SourceTagAccessor;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.Source;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Channel.SourceManager.class)
public class ChannelSourceManagerMixin {
    private static final Logger LOG = LogManager.getLogger("Jukebox360");

    @Shadow Source source;

    @Unique
    private static boolean isNoteBlock(SoundInstance sound) {
        Identifier id = sound.getId();
        if (id == null) return false;
        return id.getPath().startsWith("block.note_block");
    }

    @Inject(method = "run(Ljava/util/function/Consumer;)V", at = @At("HEAD"))
    private void jukebox360$runHead(Consumer<Source> action, CallbackInfo ci) {
        SoundInstance sound = com.macatmovie.jukebox360.SoundContext.CURRENT_SOUND.get();
        if (sound == null || this.source == null) {
            if (com.macatmovie.jukebox360.config.ClientConfig.DEBUG_LOG) {
                org.apache.logging.log4j.LogManager.getLogger("Jukebox360").info("[Jukebox360] SourceManager.run: no CURRENT_SOUND or no source");
            }
            return;
        }

        SourceTagAccessor acc = (SourceTagAccessor) (Object) this.source;

        boolean isRecords = sound.getCategory() == SoundCategory.RECORDS;
        boolean isNote = isNoteBlock(sound);
        boolean target = isRecords || isNote;

        if (target) {
            acc.jukebox360$setAffected(true);
            acc.jukebox360$setRecords(isRecords);

            Vec3d pos = new Vec3d(sound.getX(), sound.getY(), sound.getZ());
            acc.jukebox360$setWorldPos(pos);

            if (isRecords) {
                Jukebox360Runtime.add(acc);
            } else {
                acc.jukebox360$applyProjectionTick();
                Jukebox360Runtime.remove(acc);
            }

            if (ClientConfig.DEBUG_LOG) {
                LOG.info("[Jukebox360] Tagged {} for {} pos={}",
                        isRecords ? "RECORDS" : "NOTE_BLOCK",
                        sound.getId(),
                        pos);
            }
        } else {
            if (acc.jukebox360$isAffected() || acc.jukebox360$isRecords()) {
                acc.jukebox360$setAffected(false);
                acc.jukebox360$setRecords(false);
                acc.jukebox360$setWorldPos(null);
                Jukebox360Runtime.remove(acc);

                if (ClientConfig.DEBUG_LOG) {
                    LOG.info("[Jukebox360] Cleared tag for {} cat={}", sound.getId(), sound.getCategory());
                }
            }
        }
    }
}
