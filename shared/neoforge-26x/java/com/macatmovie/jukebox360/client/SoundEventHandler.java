package com.macatmovie.jukebox360.client;

import com.macatmovie.jukebox360.AudioSourceBlockWhitelist;
import com.macatmovie.jukebox360.Jukebox360;
import com.macatmovie.jukebox360.Jukebox360Runtime;
import com.macatmovie.jukebox360.config.ClientConfig;
import com.macatmovie.jukebox360.mixinaccess.ChannelTagAccessor;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.logging.LogUtils;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;
import net.neoforged.neoforge.client.event.sound.PlayStreamingSourceEvent;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = Jukebox360.MOD_ID, value = Dist.CLIENT)
public final class SoundEventHandler {
    private static final Logger LOG = LogUtils.getLogger();
    private static final AtomicBoolean MIXIN_WARNING_LOGGED = new AtomicBoolean();

    private SoundEventHandler() {}

    @SubscribeEvent
    public static void onSoundSource(PlaySoundSourceEvent event) {
        tag(event.getSound(), event.getChannel());
    }

    @SubscribeEvent
    public static void onStreamingSource(PlayStreamingSourceEvent event) {
        tag(event.getSound(), event.getChannel());
    }

    private static void tag(SoundInstance sound, Channel channel) {
        if (sound == null || channel == null) return;

        if (!(channel instanceof ChannelTagAccessor accessor)) {
            if (MIXIN_WARNING_LOGGED.compareAndSet(false, true)) {
                LOG.error("[Jukebox360] Channel mixin was not applied; jukebox audio cannot be adjusted. "
                        + "Check that jukebox_360.mixins.json is registered in neoforge.mods.toml.");
            }
            return;
        }

        boolean records = sound.getSource() == SoundSource.RECORDS;
        boolean noteBlock = sound.getIdentifier() != null
                && sound.getIdentifier().getPath().startsWith("block.note_block");
        boolean whitelistBlock = !records && !noteBlock
                && AudioSourceBlockWhitelist.matches(sound.getX(), sound.getY(), sound.getZ());
        boolean sustained = records || whitelistBlock;

        if (!records && !noteBlock && !whitelistBlock) {
            clear(accessor);
            return;
        }

        Vec3 worldPos = new Vec3(sound.getX(), sound.getY(), sound.getZ());
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
    }

    private static void clear(ChannelTagAccessor accessor) {
        if (!accessor.jukebox360$isAffected() && !accessor.jukebox360$isRecords()) return;
        accessor.jukebox360$setAffected(false);
        accessor.jukebox360$setRecords(false);
        accessor.jukebox360$setWorldPos(null);
        Jukebox360Runtime.remove(accessor);
    }
}
