package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.client.gui.Jukebox360ConfigScreen;
import com.macatmovie.jukebox360.config.ClientConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Jukebox360.MOD_ID, dist = Dist.CLIENT)
public final class Jukebox360 {
    public static final String MOD_ID = "jukebox_360";

    public Jukebox360(ModContainer container) {
        ClientConfig.init(container);
        container.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> new Jukebox360ConfigScreen(parent));
    }
}
