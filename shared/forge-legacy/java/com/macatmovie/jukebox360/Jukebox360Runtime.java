package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.mixinaccess.ChannelTagAccessor;
import org.lwjgl.openal.AL10;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Jukebox360Runtime {
    private static final Set<ChannelTagAccessor> ACTIVE = ConcurrentHashMap.newKeySet();
    private Jukebox360Runtime() {}

    public static void add(ChannelTagAccessor channel) { if (channel != null) ACTIVE.add(channel); }
    public static void remove(ChannelTagAccessor channel) { if (channel != null) ACTIVE.remove(channel); }

    public static void tick() {
        Iterator<ChannelTagAccessor> it = ACTIVE.iterator();
        while (it.hasNext()) {
            ChannelTagAccessor channel = it.next();
            try {
                if (channel == null || !channel.jukebox360$isAffected() || !channel.jukebox360$isRecords()) { it.remove(); continue; }
                int id = channel.jukebox360$getSourceId();
                if (id <= 0 || !AL10.alIsSource(id)) { it.remove(); continue; }
                channel.jukebox360$applyProjectionTick();
            } catch (Throwable ignored) { it.remove(); }
        }
    }
}
