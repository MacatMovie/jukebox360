package com.macatmovie.jukebox360.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class ClientConfig {
    private static final double DEFAULT_BLEND = 0.85D;
    public static final List<String> DEFAULT_BLOCK_WHITELIST = List.of(
            "minecraft:jukebox",
            "music_disc_maker:golden_jukebox",
            "etched:album_jukebox",
            "etched:radio"
    );

    private static ModConfigSpec.BooleanValue debugLog;
    private static ModConfigSpec.DoubleValue blend;
    private static ModConfigSpec.ConfigValue<List<? extends String>> blockWhitelist;

    private ClientConfig() {}

    public static void init(ModContainer container) {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("jukebox_360");
        debugLog = builder.define("debug_log", false);
        blend = builder.defineInRange("anchor_blend", DEFAULT_BLEND, 0.0D, 1.0D);
        blockWhitelist = builder.comment(
                "Block registry IDs whose positional sounds should also be managed by Jukebox 360.",
                "This is checked only when a sound starts; the world is never scanned continuously."
        ).defineListAllowEmpty("audio_source_block_whitelist", DEFAULT_BLOCK_WHITELIST, o -> o instanceof String && isValidId((String)o));
        builder.pop();
        container.registerConfig(ModConfig.Type.CLIENT, builder.build(), "jukebox_360-client.toml");
    }

    public static boolean debugLog() {
        try { return debugLog != null && debugLog.get(); } catch (IllegalStateException ignored) { return false; }
    }
    public static double blend() {
        try { return blend != null ? blend.get() : DEFAULT_BLEND; } catch (IllegalStateException ignored) { return DEFAULT_BLEND; }
    }
    public static void setDebugLog(boolean value) { try { if (debugLog != null) debugLog.set(value); } catch (IllegalStateException ignored) {} }
    public static void setBlend(double value) { try { if (blend != null) blend.set(Math.max(0.0D, Math.min(1.0D, value))); } catch (IllegalStateException ignored) {} }
    public static List<String> blockWhitelist() {
        try {
            if (blockWhitelist == null) return new ArrayList<>(DEFAULT_BLOCK_WHITELIST);
            ArrayList<String> out = new ArrayList<>();
            for (Object o : blockWhitelist.get()) if (o instanceof String) out.add((String)o);
            return out;
        } catch (IllegalStateException ignored) { return new ArrayList<>(DEFAULT_BLOCK_WHITELIST); }
    }
    public static void setBlockWhitelist(List<String> values) { try { if (blockWhitelist != null) blockWhitelist.set(normalize(values)); } catch (IllegalStateException ignored) {} }
    public static boolean isBlockWhitelisted(String id) {
        if (id == null) return false;
        for (String entry : blockWhitelist()) if (id.equals(entry)) return true;
        return false;
    }
    public static void save() { /* Forge/NeoForge config values are persisted by the loader. */ }

    public static void resetDefaults() { setDebugLog(false); setBlend(DEFAULT_BLEND); setBlockWhitelist(DEFAULT_BLOCK_WHITELIST); }
    public static boolean isValidId(String id) { return id != null && id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"); }
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
