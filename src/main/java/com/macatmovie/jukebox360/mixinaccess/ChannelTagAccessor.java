package com.macatmovie.jukebox360.mixinaccess;

import net.minecraft.world.phys.Vec3;

public interface ChannelTagAccessor {
    void jukebox360$setRecords(boolean records);
    boolean jukebox360$isRecords();

    void jukebox360$setWorldPos(Vec3 pos);
    Vec3 jukebox360$getWorldPos();

    void jukebox360$setAffected(boolean affected);
    boolean jukebox360$isAffected();

    int jukebox360$getSourceId();
}
