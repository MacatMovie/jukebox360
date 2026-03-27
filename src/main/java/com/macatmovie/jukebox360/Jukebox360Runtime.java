package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.mixinaccess.SourceTagAccessor;
import org.lwjgl.openal.AL10;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Jukebox360Runtime {
    private Jukebox360Runtime() {}

    private static final Set<SourceTagAccessor> ACTIVE = ConcurrentHashMap.newKeySet();

    public static void add(SourceTagAccessor src) {
        if (src != null) ACTIVE.add(src);
    }

    public static void remove(SourceTagAccessor src) {
        if (src != null) ACTIVE.remove(src);
    }

    public static void tick() {
        Iterator<SourceTagAccessor> it = ACTIVE.iterator();
        while (it.hasNext()) {
            SourceTagAccessor s = it.next();
            try {
                if (s == null || !s.jukebox360$isAffected() || !s.jukebox360$isRecords()) {
                    it.remove();
                    continue;
                }
                int id = s.jukebox360$getSourceId();
                if (id <= 0 || !AL10.alIsSource(id)) {
                    it.remove();
                    continue;
                }
                s.jukebox360$applyProjectionTick();
            } catch (Throwable t) {
                it.remove();
            }
        }
    }
}
