package me.coblaz.archipelago;

import com.google.gson.*;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Reads and writes each player's last processed AP item index.
 *
 * File location: plugins/AchievementMod/archipelago/<seedId>/<uuid>.json
 * Contents:      {"lastIndex": 42}
 *
 * The {@code seedId} segment scopes the index to one specific game so two AP
 * seeds played by the same player never share an index.
 *
 * On a fresh install (no file) {@link #loadLastIndex} returns {@code -1},
 * which means "process every item starting from index 0". A {@code null} seedId
 * (player not connected) is also treated as "nothing saved".
 */
public final class ArchipelagoProgressSaveManager {

    private static final Path SAVE_DIR =
            Path.of("plugins", "AchievementMod", "archipelago");
    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private ArchipelagoProgressSaveManager() {}

    /**
     * Returns the last successfully processed item index for {@code playerUuid},
     * or {@code -1} if no save file exists yet.
     */
    public static int loadLastIndex(@javax.annotation.Nullable String seedId, @Nonnull String playerUuid) {
        if (seedId == null) return -1;
        Path file = resolveFile(seedId, playerUuid);
        if (!Files.exists(file)) return -1;
        try {
            String raw = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
            return root.get("lastIndex").getAsInt();
        } catch (IOException | JsonParseException | NullPointerException ex) {
            System.err.printf("[ArchipelagoMod] Failed to load progress for %s: %s%n",
                    playerUuid, ex.getMessage());
            return -1;
        }
    }

    /**
     * Persists {@code index} as the last processed item index for {@code playerUuid}.
     */
    public static void saveLastIndex(@javax.annotation.Nullable String seedId, @Nonnull String playerUuid, int index) {
        if (seedId == null) return;   // not connected to a seed -> nothing to scope the save to
        try {
            Path file = resolveFile(seedId, playerUuid);
            Files.createDirectories(file.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("lastIndex", index);
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.printf("[ArchipelagoMod] Failed to save progress for %s: %s%n",
                    playerUuid, ex.getMessage());
        }
    }

    private static Path resolveFile(@Nonnull String seedId, @Nonnull String playerUuid) {
        String safeSeed = seedId.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String safeUuid = playerUuid.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return SAVE_DIR.resolve(safeSeed).resolve(safeUuid + ".json");
    }
}