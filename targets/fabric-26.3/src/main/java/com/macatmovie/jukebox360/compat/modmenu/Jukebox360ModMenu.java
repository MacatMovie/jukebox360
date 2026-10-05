package com.macatmovie.jukebox360.compat.modmenu;

import com.macatmovie.jukebox360.client.gui.Jukebox360ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Optional Mod Menu integration. Mod Menu itself is not required at runtime. */
public final class Jukebox360ModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return Jukebox360ConfigScreen::new;
    }
}
