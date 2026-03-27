package com.macatmovie.jukebox360;

import net.minecraft.client.sound.SoundInstance;

/**
 * Thread-local bridge used to associate the currently-playing SoundInstance with
 * the Channel/Source being configured.
 */
public final class SoundContext {
    private SoundContext() {}

    public static final ThreadLocal<SoundInstance> CURRENT_SOUND = new ThreadLocal<>();
}
