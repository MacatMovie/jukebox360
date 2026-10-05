package com.macatmovie.jukebox360;

import com.macatmovie.jukebox360.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.util.Mth;

public final class AudioSourceBlockWhitelist {
    private AudioSourceBlockWhitelist() {}

    public static boolean matches(double x, double y, double z) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || ClientConfig.blockWhitelist().isEmpty()) return false;
            BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
            var id = ForgeRegistries.BLOCKS.getKey(mc.level.getBlockState(pos).getBlock());
            return id != null && ClientConfig.isBlockWhitelisted(id.toString());
        } catch (Throwable ignored) {
            return false;
        }
    }
}
