package com.customcrystals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CrystalConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("customcrystals.json");
    private static CrystalConfig current = CrystalConfig.defaults();

    private CrystalConfigManager() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            CrystalConfig config = GSON.fromJson(reader, CrystalConfig.class);
            if (config != null) {
                current = sanitize(config);
            }
        } catch (IOException | JsonParseException e) {
            current = CrystalConfig.defaults();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(current, writer);
            }
        } catch (IOException e) {
            // At worst we skip saving; keep running.
        }
    }

    public static CrystalConfig get() {
        return current;
    }

    public static void update(CrystalConfig next) {
        current = sanitize(next);
        save();
    }

    private static CrystalConfig sanitize(CrystalConfig config) {
        CrystalConfig cleaned = CrystalConfig.defaults();

        cleaned.coreTintEnabled = config.coreTintEnabled;
        cleaned.coreColor = sanitizeColor(config.coreColor);
        cleaned.framesTintEnabled = config.framesTintEnabled;
        cleaned.framesColor = sanitizeColor(config.framesColor);
        cleaned.scale = clamp(config.scale, 0.25f, 4.0f);
        cleaned.verticalOffset = clamp(config.verticalOffset, -1.5f, 1.5f);
        cleaned.beamEnabled = config.beamEnabled;
        cleaned.spinMultiplier = clamp(config.spinMultiplier, 0.1f, 4.0f);
        return cleaned;
    }

    private static int sanitizeColor(int color) {
        return color & 0xFFFFFF;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
