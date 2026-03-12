package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.mixinaccess.ChannelTagAccessor;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds currently-active tagged channels so we can re-apply projection every client tick.
 * This avoids relying on vanilla channel update call paths, which differ between sounds.
 */
public final class Jukebox360Runtime {
    private Jukebox360Runtime() {}

    private static final Set<ChannelTagAccessor> ACTIVE = ConcurrentHashMap.newKeySet();

    public static void add(ChannelTagAccessor ch) {
        if (ch != null) ACTIVE.add(ch);
    }

    public static void remove(ChannelTagAccessor ch) {
        if (ch != null) ACTIVE.remove(ch);
    }

    public static void tick() {
        for (ChannelTagAccessor ch : ACTIVE) {
            try {
                ch.jukebox360$applyProjectionTick();
            } catch (Throwable ignored) {
                // If something goes wrong (channel freed/reused), just skip this tick.
            }
        }
    }
}
