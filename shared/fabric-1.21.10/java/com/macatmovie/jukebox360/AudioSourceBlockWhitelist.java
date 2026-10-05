package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.config.ClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

public final class AudioSourceBlockWhitelist {
    private AudioSourceBlockWhitelist() {}

    public static boolean matches(double x, double y, double z) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null || ClientConfig.blockWhitelist().isEmpty()) return false;
            BlockPos pos = BlockPos.ofFloored(x, y, z);
            var id = Registries.BLOCK.getId(mc.world.getBlockState(pos).getBlock());
            return id != null && ClientConfig.isBlockWhitelisted(id.toString());
        } catch (Throwable ignored) {
            return false;
        }
    }
}
