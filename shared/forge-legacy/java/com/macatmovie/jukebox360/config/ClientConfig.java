package com.macatmovie.jukebox360.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ClientConfig {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec SPEC;
    public static final List<String> DEFAULT_BLOCK_WHITELIST = List.of(
            "minecraft:jukebox",
            "music_disc_maker:golden_jukebox",
            "etched:album_jukebox",
            "etched:radio"
    );

    public static ForgeConfigSpec.BooleanValue DEBUG_LOG;
    public static ForgeConfigSpec.DoubleValue BLEND;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCK_WHITELIST;

    public static void init() {
        B.push("jukebox_360");
        DEBUG_LOG = B.define("debug_log", false);
        BLEND = B.defineInRange("anchor_blend", 0.85, 0.0, 1.0);
        BLOCK_WHITELIST = B.comment(
                "Block registry IDs whose positional sounds should also be managed by Jukebox 360.",
                "This is checked only when a sound starts; the world is never scanned continuously."
        ).defineListAllowEmpty(java.util.List.of("audio_source_block_whitelist"), () -> DEFAULT_BLOCK_WHITELIST, o -> o instanceof String && isValidId((String)o));
        B.pop();
        SPEC = B.build();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC, "jukebox_360-client.toml");
    }

    public static boolean debugLog() { return DEBUG_LOG != null && DEBUG_LOG.get(); }
    public static double blend() { return BLEND != null ? BLEND.get() : 0.85D; }
    public static void setDebugLog(boolean value) { if (DEBUG_LOG != null) DEBUG_LOG.set(value); }
    public static void setBlend(double value) { if (BLEND != null) BLEND.set(Math.max(0.0D, Math.min(1.0D, value))); }

    public static List<String> blockWhitelist() {
        if (BLOCK_WHITELIST == null) return new ArrayList<>(DEFAULT_BLOCK_WHITELIST);
        ArrayList<String> out = new ArrayList<>();
        for (Object o : BLOCK_WHITELIST.get()) if (o instanceof String) out.add((String)o);
        return out;
    }

    public static void setBlockWhitelist(List<String> values) {
        if (BLOCK_WHITELIST != null) BLOCK_WHITELIST.set(normalize(values));
    }

    public static boolean isBlockWhitelisted(String id) {
        if (id == null) return false;
        for (String entry : blockWhitelist()) if (id.equals(entry)) return true;
        return false;
    }

    public static void save() { /* Forge/NeoForge config values are persisted by the loader. */ }

    public static void resetDefaults() {
        setDebugLog(false);
        setBlend(0.85D);
        setBlockWhitelist(DEFAULT_BLOCK_WHITELIST);
    }

    public static boolean isValidId(String id) {
        return id != null && id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }

    private static List<String> normalize(List<String> values) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (values != null) for (String raw : values) {
            if (raw == null) continue;
            String id = raw.trim().toLowerCase();
            if (isValidId(id)) out.add(id);
        }
        return new ArrayList<>(out);
    }
}
