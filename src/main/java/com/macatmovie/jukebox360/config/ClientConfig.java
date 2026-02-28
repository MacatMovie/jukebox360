package com.macatmovie.jukebox360.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ClientConfig {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.BooleanValue DEBUG_LOG;
    public static ForgeConfigSpec.DoubleValue BLEND;

    public static void init() {
        B.push("jukebox_360");
        DEBUG_LOG = B.define("debug_log", false);
        BLEND = B.defineInRange("anchor_blend", 0.85, 0.0, 1.0);
        B.pop();
        SPEC = B.build();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC, "jukebox_360-client.toml");
    }
}
