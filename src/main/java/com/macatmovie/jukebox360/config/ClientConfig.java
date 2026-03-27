package com.macatmovie.jukebox360.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientConfig {
    private ClientConfig() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("jukebox_360-client.json");

    public static boolean DEBUG_LOG = false;
    public static double BLEND = 0.85;

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
        } catch (Throwable ignored) {}
    }

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("debug_log", DEBUG_LOG);
            o.addProperty("anchor_blend", BLEND);
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(o), StandardCharsets.UTF_8);
        } catch (Throwable ignored) {}
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
