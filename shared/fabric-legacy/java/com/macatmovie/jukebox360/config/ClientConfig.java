package com.macatmovie.jukebox360.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class ClientConfig {
    private ClientConfig() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("jukebox_360-client.json");
    public static final List<String> DEFAULT_BLOCK_WHITELIST = List.of(
            "minecraft:jukebox",
            "music_disc_maker:golden_jukebox",
            "etched:album_jukebox",
            "etched:radio"
    );

    public static boolean DEBUG_LOG = false;
    public static double BLEND = 0.85;
    private static List<String> BLOCK_WHITELIST = new ArrayList<>(DEFAULT_BLOCK_WHITELIST);

    public static void load() {
        try {
            if (!Files.exists(FILE)) {
                save();
                return;
            }
            String raw = Files.readString(FILE, StandardCharsets.UTF_8);
            JsonObject o = GSON.fromJson(raw, JsonObject.class);
            if (o == null) return;

            if (o.has("debug_log")) DEBUG_LOG = o.get("debug_log").getAsBoolean();
            if (o.has("anchor_blend")) BLEND = clamp(o.get("anchor_blend").getAsDouble(), 0.0, 1.0);
            if (o.has("audio_source_block_whitelist") && o.get("audio_source_block_whitelist").isJsonArray()) {
                ArrayList<String> values = new ArrayList<>();
                for (JsonElement e : o.getAsJsonArray("audio_source_block_whitelist")) {
                    if (e.isJsonPrimitive()) values.add(e.getAsString());
                }
                BLOCK_WHITELIST = normalize(values);
            }
        } catch (Throwable ignored) {}
    }

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("debug_log", DEBUG_LOG);
            o.addProperty("anchor_blend", BLEND);
            JsonArray list = new JsonArray();
            for (String id : BLOCK_WHITELIST) list.add(id);
            o.add("audio_source_block_whitelist", list);
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(o), StandardCharsets.UTF_8);
        } catch (Throwable ignored) {}
    }

    public static boolean debugLog() { return DEBUG_LOG; }
    public static double blend() { return BLEND; }
    public static void setDebugLog(boolean value) { DEBUG_LOG = value; }
    public static void setBlend(double value) { BLEND = clamp(value, 0.0, 1.0); }
    public static List<String> blockWhitelist() { return List.copyOf(BLOCK_WHITELIST); }
    public static void setBlockWhitelist(List<String> values) { BLOCK_WHITELIST = normalize(values); }
    public static boolean isBlockWhitelisted(String id) { return id != null && BLOCK_WHITELIST.contains(id); }
    public static void resetDefaults() {
        DEBUG_LOG = false;
        BLEND = 0.85;
        BLOCK_WHITELIST = new ArrayList<>(DEFAULT_BLOCK_WHITELIST);
    }

    public static boolean isValidId(String id) {
        return id != null && id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }

    private static List<String> normalize(List<String> values) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (values != null) {
            for (String raw : values) {
                if (raw == null) continue;
                String id = raw.trim().toLowerCase();
                if (isValidId(id)) out.add(id);
            }
        }
        return new ArrayList<>(out);
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
