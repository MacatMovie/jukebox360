package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.mixinaccess.ChannelTagAccessor;
import org.lwjgl.openal.AL10;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Jukebox360Runtime {
    private static final Set<ChannelTagAccessor> ACTIVE = ConcurrentHashMap.newKeySet();

    private Jukebox360Runtime() {}

    public static void add(ChannelTagAccessor channel) {
        if (channel != null) ACTIVE.add(channel);
    }

    public static void remove(ChannelTagAccessor channel) {
        if (channel != null) ACTIVE.remove(channel);
    }

    public static void tick() {
        Iterator<ChannelTagAccessor> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            ChannelTagAccessor channel = iterator.next();
            try {
                if (channel == null || !channel.jukebox360$isAffected() || !channel.jukebox360$isRecords()) {
                    iterator.remove();
                    continue;
                }

                int sourceId = channel.jukebox360$getSourceId();
                if (sourceId <= 0 || !AL10.alIsSource(sourceId)) {
                    iterator.remove();
                    continue;
                }

                channel.jukebox360$applyProjectionTick();
            } catch (Throwable ignored) {
                iterator.remove();
            }
        }
    }
}
