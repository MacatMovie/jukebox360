package com.macatmovie.jukebox360.client;

import com.macatmovie.jukebox360.Jukebox360Runtime;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Client tick hook that re-applies projection for active tagged channels. */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientTickHandler {
    private ClientTickHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Jukebox360Runtime.tick();
    }
}
