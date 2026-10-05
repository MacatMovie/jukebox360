package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.client.gui.Jukebox360ConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "jukebox_360", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Jukebox360Client {
    private Jukebox360Client() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new Jukebox360ConfigScreen(parent))
        ));
    }
}
