package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.config.ClientConfig;
import net.minecraftforge.fml.common.Mod;

@Mod("jukebox_360")
public class Jukebox360 {
    public Jukebox360() {
        ClientConfig.init();
    }
}
