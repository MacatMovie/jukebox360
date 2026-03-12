package com.macatmovie.jukebox360.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static ModConfigSpec SPEC;

    public static ModConfigSpec.BooleanValue DEBUG_LOG;
    public static ModConfigSpec.DoubleValue BLEND;

    public static void init(ModContainer container) {
        B.push("jukebox_360");
        DEBUG_LOG = B.define("debug_log", false);
        BLEND = B.defineInRange("anchor_blend", 0.85, 0.0, 1.0);
        B.pop();

        SPEC = B.build();
        container.registerConfig(ModConfig.Type.CLIENT, SPEC, "jukebox_360-client.toml");
    }
}
