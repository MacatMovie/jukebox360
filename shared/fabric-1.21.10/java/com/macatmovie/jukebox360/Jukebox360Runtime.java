package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.mixinaccess.SourceTagAccessor;
import org.lwjgl.openal.AL10;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Jukebox360Runtime {
    private static final Set<SourceTagAccessor> ACTIVE = ConcurrentHashMap.newKeySet();

    private Jukebox360Runtime() {}

    public static void add(SourceTagAccessor source) {
        if (source != null) ACTIVE.add(source);
    }

    public static void remove(SourceTagAccessor source) {
        if (source != null) ACTIVE.remove(source);
    }

    public static void tick() {
        Iterator<SourceTagAccessor> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            SourceTagAccessor source = iterator.next();
            try {
                if (source == null || !source.jukebox360$isAffected() || !source.jukebox360$isRecords()) {
                    iterator.remove();
                    continue;
                }

                int sourceId = source.jukebox360$getSourceId();
                if (sourceId <= 0 || !AL10.alIsSource(sourceId)) {
                    iterator.remove();
                    continue;
                }

                source.jukebox360$applyProjectionTick();
            } catch (Throwable ignored) {
                iterator.remove();
            }
        }
    }
}
