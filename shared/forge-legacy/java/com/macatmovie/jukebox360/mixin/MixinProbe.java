package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.config.ClientConfig;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinProbe {
    private static final Logger LOG = LogManager.getLogger("Jukebox360");

    @Inject(method = "<init>", at = @At("RETURN"), require = 0)
    private void jukebox360$probe(CallbackInfo ci) {
        if (ClientConfig.DEBUG_LOG.get()) {
            LOG.info("[Jukebox360] Mixins active. debug_log=true");
        }
    }
}
