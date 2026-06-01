package me.coblaz.achievements;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Memory-count milestone achievements (1–150).
 *
 * Each entry represents "player has reached at least N memories".
 * Because all 150 share the same underlying counter, call
 * {@link #updateMemoriesCount} with the raw count whenever it
 * changes; the registry will unlock every milestone ≤ that value.
 *
 * Integration points:
 *   • On UpdateMemoriesCount packet  → updateMemoriesCount(reg, playerRef, packet.getCount())
 *   • From WorldSupport directly     → updateMemoriesCount(reg, playerRef,
 *                                          worldSupport.attitudeOverrideMemory.size())
 */
public final class MemoriesAchievements {

    // ---------------------------------------------------------------
    // Definition list (built once at class-load time)
    // ---------------------------------------------------------------

    public static final List<AchievementDefinition> ALL;

    static {
        List<AchievementDefinition> list = new ArrayList<>(150);
        for (int i = 1; i <= 150; i++) {
            list.add(new AchievementDefinition(
                    "memories_" + i,
                    i + (i == 1 ? " Memory" : " Memories"),
                    i          // reachCount == i, so it unlocks exactly when the counter hits i
            ));
        }
        ALL = List.copyOf(list);
    }

    // ---------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------

    /** Register all 150 definitions into the given registry. */
    public static void registerAll(@Nonnull AchievementRegistry reg) {
        for (AchievementDefinition def : ALL) {
            reg.registerAchievement(def);
        }
    }

    // ---------------------------------------------------------------
    // Counter update  (call this whenever the player's memory count changes)
    // ---------------------------------------------------------------

    /**
     * Pushes {@code count} into every milestone so the registry can
     * unlock all thresholds at or below the current value.
     *
     * @param reg        the locations registry (Registries.LOCATIONS)
     * @param playerRef  the player whose memories changed
     * @param count      current total memory count for that player
     */
    public static void updateMemoriesCount(
            @Nonnull AchievementRegistry reg,
            @Nonnull PlayerRef playerRef,
            int count
    ) {
        for (AchievementDefinition def : ALL) {
            reg.setCount(playerRef, def.getId(), count);
        }
    }

    // ---------------------------------------------------------------

    private MemoriesAchievements() {}
}