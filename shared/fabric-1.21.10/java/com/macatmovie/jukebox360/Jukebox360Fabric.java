package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.config.ClientConfig;
import net.fabricmc.api.ClientModInitializer;

public final class Jukebox360Fabric implements ClientModInitializer {
    public static final String MOD_ID = "jukebox_360";

    @Override
    public void onInitializeClient() {
        ClientConfig.load();
    }
}
