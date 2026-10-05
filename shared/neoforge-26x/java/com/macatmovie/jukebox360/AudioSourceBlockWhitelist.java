package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;

public final class AudioSourceBlockWhitelist {
    private AudioSourceBlockWhitelist() {}

    public static boolean matches(double x, double y, double z) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || ClientConfig.blockWhitelist().isEmpty()) return false;
            BlockPos pos = BlockPos.containing(x, y, z);
            var id = BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(pos).getBlock());
            return id != null && ClientConfig.isBlockWhitelisted(id.toString());
        } catch (Throwable ignored) {
            return false;
        }
    }
}
