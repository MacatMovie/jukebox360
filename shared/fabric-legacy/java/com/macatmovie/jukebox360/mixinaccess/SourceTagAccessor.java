package com.macatmovie.jukebox360.mixinaccess;

import net.minecraft.util.math.Vec3d;

public interface SourceTagAccessor {
    void jukebox360$setRecords(boolean records);
    boolean jukebox360$isRecords();

    void jukebox360$setAffected(boolean affected);
    boolean jukebox360$isAffected();

    void jukebox360$setWorldPos(Vec3d pos);
    Vec3d jukebox360$getWorldPos();

    int jukebox360$getSourceId();

    void jukebox360$applyProjectionTick();
}
