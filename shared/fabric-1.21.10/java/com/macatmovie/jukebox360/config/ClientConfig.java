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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("jukebox_360-client.json");
    public static final List<String> DEFAULT_BLOCK_WHITELIST = List.of(
            "minecraft:jukebox",
            "music_disc_maker:golden_jukebox",
            "etched:album_jukebox",
            "etched:radio"
    );

    private static boolean debugLog = false;
    private static double blend = 0.85D;
    private static List<String> blockWhitelist = new ArrayList<>(DEFAULT_BLOCK_WHITELIST);

    private ClientConfig() {}

    public static boolean debugLog() { return debugLog; }
    public static double blend() { return blend; }
    public static void setDebugLog(boolean value) { debugLog = value; }
    public static void setBlend(double value) { blend = clamp(value, 0.0D, 1.0D); }
    public static List<String> blockWhitelist() { return List.copyOf(blockWhitelist); }
    public static void setBlockWhitelist(List<String> values) { blockWhitelist = normalize(values); }
    public static boolean isBlockWhitelisted(String id) { return id != null && blockWhitelist.contains(id); }
    public static boolean isValidId(String id) { return id != null && id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"); }

    public static void resetDefaults() {
        debugLog = false;
        blend = 0.85D;
        blockWhitelist = new ArrayList<>(DEFAULT_BLOCK_WHITELIST);
    }

    public static void load() {
        try {
            if (!Files.exists(FILE)) { save(); return; }
            JsonObject object = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), JsonObject.class);
            if (object == null) return;
            if (object.has("debug_log")) debugLog = object.get("debug_log").getAsBoolean();
            if (object.has("anchor_blend")) blend = clamp(object.get("anchor_blend").getAsDouble(), 0.0D, 1.0D);
            if (object.has("audio_source_block_whitelist") && object.get("audio_source_block_whitelist").isJsonArray()) {
                ArrayList<String> values = new ArrayList<>();
                for (JsonElement e : object.getAsJsonArray("audio_source_block_whitelist")) {
                    if (e.isJsonPrimitive()) values.add(e.getAsString());
                }
                blockWhitelist = normalize(values);
            }
        } catch (Throwable ignored) {}
    }

    public static void save() {
        try {
            JsonObject object = new JsonObject();
            object.addProperty("debug_log", debugLog);
            object.addProperty("anchor_blend", blend);
            JsonArray list = new JsonArray();
            for (String id : blockWhitelist) list.add(id);
            object.add("audio_source_block_whitelist", list);
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(object), StandardCharsets.UTF_8);
        } catch (Throwable ignored) {}
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

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
