package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.client.gui.Jukebox360ConfigScreen;
import com.macatmovie.jukebox360.config.ClientConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.config.ModConfig;

@Mod(value = "jukebox_360", dist = Dist.CLIENT)
public class Jukebox360 {
    public Jukebox360(ModContainer container) {
        ClientConfig.init(container);
        container.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> new Jukebox360ConfigScreen(parent));
        // (Config is client-only; nothing else to init.)
    }
}
