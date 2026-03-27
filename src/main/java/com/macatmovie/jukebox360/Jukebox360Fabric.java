package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.config.ClientConfig;
import net.fabricmc.api.ClientModInitializer;

public class Jukebox360Fabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientConfig.load();
    }
}
