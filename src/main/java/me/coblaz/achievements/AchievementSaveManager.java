package me.coblaz.achievements;

import com.google.gson.*;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public final class AchievementSaveManager {

    private static final Path SAVE_DIR = Path.of("plugins", "AchievementMod", "players");
    private static final Gson GSON     = new GsonBuilder().setPrettyPrinting().create();

    private AchievementSaveManager() {}

    /** Write every achievement for one player to their JSON file. */
    public static void save(@Nonnull String playerKey,
                            @Nonnull Map<String, PlayerAchievementData> data) {
        try {
            Files.createDirectories(SAVE_DIR);

            JsonObject root = new JsonObject();
            for (Map.Entry<String, PlayerAchievementData> e : data.entrySet()) {
                JsonObject ach = new JsonObject();
                ach.addProperty("count",  e.getValue().getCount());
                ach.addProperty("status", e.getValue().getStatus().name());
                root.add(e.getKey(), ach);
            }

            Files.writeString(
                    SAVE_DIR.resolve(sanitize(playerKey) + ".json"),
                    GSON.toJson(root),
                    StandardCharsets.UTF_8
            );

        } catch (IOException ex) {
            System.err.println("[AchievementMod] Save failed for " + playerKey + ": " + ex.getMessage());
        }
    }

    /** Read a player's JSON file back into a live map. Returns empty map if no file yet. */
    @Nonnull
    public static Map<String, PlayerAchievementData> load(@Nonnull String playerKey) {
        Path file = SAVE_DIR.resolve(sanitize(playerKey) + ".json");
        Map<String, PlayerAchievementData> result = new HashMap<>();
        if (!Files.exists(file)) return result;

        try {
            JsonObject root = JsonParser.parseString(
                    Files.readString(file, StandardCharsets.UTF_8)
            ).getAsJsonObject();

            for (Map.Entry<String, JsonElement> e : root.entrySet()) {
                JsonObject ach = e.getValue().getAsJsonObject();

                PlayerAchievementData d = new PlayerAchievementData();
                d.setCount(ach.get("count").getAsInt());
                d.setStatus(AchievementStatus.valueOf(ach.get("status").getAsString()));
                result.put(e.getKey(), d);
            }

        } catch (IOException | IllegalArgumentException | JsonParseException ex) {
            System.err.println("[AchievementMod] Load failed for " + playerKey + ": " + ex.getMessage());
        }

        return result;
    }

    /** Keeps file names safe regardless of what playerRef.toString() returns. */
    @Nonnull
    private static String sanitize(@Nonnull String key) {
        return key.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}