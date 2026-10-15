package me.coblaz.achievements;

import com.hypixel.hytale.builtin.gameflags.GameFlagsResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Goblin lab repair achievements (8000–8004 on the Archipelago side).
 *
 * Hytale fires no event when a goblin lab device is repaired. Instead, each
 * repair raises a game flag (AugmentUpgrades.tryUpgrade -> GameFlagsResource),
 * persisted in universe/resources/GameFlags.json. Every time the player opens
 * /arch-locations, the flags are read and every achievement whose flag is set
 * is marked DONE.
 *
 * Game flags are universe-wide, not per player: in multiplayer, one player's
 * repair counts for everyone. They are only raised inside a played instance
 * copy (the Forgotten Temple), see AugmentProgression.participates().
 */
public final class RepairAchievements {

    public record Entry(
            String flagKey,        // GameFlagsResource key (ProgressionKey of the block)
            String achievementId,  // AP location name (8000–8004)
            String title           // display name shown in the table
    ) {}

    public static final List<Entry> ALL = List.of(
            // Set the first time the player uses the broken generator, before repairing it
            new Entry("goblin_lab_generator_found", "repair_goblin_lab_generator_found", "Find the Goblin Power Generator"),
            new Entry("goblin_lab_power",           "repair_goblin_lab_power",           "Repair the Goblin Power Generator"),
            new Entry("goblin_lab_recipe_a",        "repair_goblin_lab_recipe_a",        "Repair the Boiler"),
            new Entry("goblin_lab_recipe_b",        "repair_goblin_lab_recipe_b",        "Repair the Engine"),
            new Entry("goblin_lab_recipe_c",        "repair_goblin_lab_recipe_c",        "Repair the Compressor")
    );

    // ---------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------

    /** Register all repair definitions into the given registry. */
    public static void registerAll(@Nonnull AchievementRegistry reg) {
        for (Entry entry : ALL) {
            reg.registerAchievement(new AchievementDefinition(
                    entry.achievementId(),
                    entry.title(),
                    1,
                    Category.REPAIR
            ));
        }
    }

    // ---------------------------------------------------------------
    // Flag sync  (call when /arch-locations is opened)
    // ---------------------------------------------------------------

    /**
     * For every repair whose game flag is on, sets the achievement's count to 1
     * and its state to DONE, if it is still NOT_DONE. Achievements already DONE
     * or COLLECTED are left untouched.
     *
     * @param reg       the locations registry (Registries.LOCATIONS)
     * @param playerRef the player opening the achievements page
     */
    public static void syncRepairFlags(
            @Nonnull AchievementRegistry reg,
            @Nonnull PlayerRef playerRef
    ) {
        GameFlagsResource flags = GameFlagsResource.get();

        for (Entry entry : ALL) {
            if (flags.getLevel(entry.flagKey()) < 1) continue;
            if (reg.getData(playerRef, entry.achievementId()).getStatus() != AchievementStatus.NOT_DONE) continue;

            reg.setCount(playerRef, entry.achievementId(), 1);
            reg.setStatus(playerRef, entry.achievementId(), AchievementStatus.DONE);
        }
    }

    private RepairAchievements() {}
}
