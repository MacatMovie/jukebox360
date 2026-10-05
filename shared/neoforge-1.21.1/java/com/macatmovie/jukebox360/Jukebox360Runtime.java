package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.mixinaccess.ChannelTagAccessor;
import org.lwjgl.openal.AL10;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds currently-active tagged channels so we can re-apply projection every client tick.
 * IMPORTANT: Minecraft reuses OpenAL channels; we must remove stale channels promptly.
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
        Iterator<ChannelTagAccessor> it = ACTIVE.iterator();
        while (it.hasNext()) {
            ChannelTagAccessor ch = it.next();
            try {
                if (ch == null || !ch.jukebox360$isAffected() || !ch.jukebox360$isRecords()) {
                    it.remove();
                    continue;
                }
                int srcId = ch.jukebox360$getSourceId();
                // If the source is invalid (freed/reused), stop tracking it immediately.
                if (srcId <= 0 || !AL10.alIsSource(srcId)) {
                    it.remove();
                    continue;
                }
                ch.jukebox360$applyProjectionTick();
            } catch (Throwable t) {
                // Any OpenAL invalid-name or channel reuse issues -> drop it.
                it.remove();
            }
        }
    }
}
