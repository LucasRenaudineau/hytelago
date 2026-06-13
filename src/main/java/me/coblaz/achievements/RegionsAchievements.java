package me.coblaz.achievements;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Region-exploration achievements (6000–6011 on the Archipelago side).
 *
 * Each entry represents "player has visited the region at least once"
 * (neededCount == 1). Every time the player opens /arch-locations, the
 * achievement matching the region they are currently standing in gets
 * its count incremented by 1.
 *
 * WorldMapTracker.ZoneDiscoveryInfo.regionName() returns the raw
 * translation-key suffix (e.g. "Zone1_Tier1" for "Drifting Plains",
 * see map.region.* in server.lang), so each entry carries the in-game
 * region key it matches against.
 */
public final class RegionsAchievements {

    public record Entry(
            String regionKey,      // ZoneDiscoveryInfo.regionName()
            String achievementId,  // AP location name (6000–6011)
            String title           // display name shown in the table
    ) {}

    public static final List<Entry> ALL = List.of(
            new Entry("Zone1_Tier1", "region_drifting_plains",    "Drifting Plains"),
            new Entry("Zone1_Tier2", "region_seedlings_woods",    "Seedling Woods"),
            new Entry("Zone1_Tier3", "region_the_fens",           "The Fens"),
            new Entry("Zone2_Tier1", "region_golden_steppes",     "Golden Steppes"),
            new Entry("Zone2_Tier2", "region_badlands",           "Badlands"),
            new Entry("Zone2_Tier3", "region_desolate_bassin",    "Desolate Basin"),
            new Entry("Oceans",      "region_crystalline_depths", "Crystalline Depths"),
            new Entry("Zone3_Tier1", "region_frostmarch_tundra",  "Frostmarch Tundra"),
            new Entry("Zone3_Tier2", "region_boreal_reach",       "Boreal Reach"),
            new Entry("Zone3_Tier3", "region_the_everfrost",      "The Everfrost"),
            new Entry("Zone4_Tier4", "region_cinder_wastes",      "Cinder Wastes"),
            new Entry("Zone4_Tier5", "region_charred_woodlands",  "Charred Woodlands")
    );

    // Lookup map: regionKey (lowercase) -> achievementId

    private static final Map<String, String> REGION_KEY_TO_ACH_ID =
            ALL.stream().collect(Collectors.toMap(
                    e -> e.regionKey().toLowerCase(),
                    Entry::achievementId
            ));

    // ---------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------

    /** Register all region definitions into the given registry. */
    public static void registerAll(@Nonnull AchievementRegistry reg) {
        for (Entry entry : ALL) {
            reg.registerAchievement(new AchievementDefinition(
                    entry.achievementId(),
                    "Explore " + entry.title(),
                    1
            ));
        }
    }

    // ---------------------------------------------------------------
    // Counter update  (call when /arch-locations is opened)
    // ---------------------------------------------------------------

    /**
     * Adds 1 to the count of the achievement matching the region the
     * player is currently in. Does nothing if the region is not in the
     * table (e.g. Zone1_Spawn, temples or other unmapped areas).
     *
     * @param reg        the locations registry (Registries.LOCATIONS)
     * @param playerRef  the player opening the achievements page
     * @param regionName the raw region key reported by the
     *                   WorldMapTracker (e.g. "Zone1_Tier1")
     */
    public static void incrementCurrentRegion(
            @Nonnull AchievementRegistry reg,
            @Nonnull PlayerRef playerRef,
            @Nullable String regionName
    ) {
        if (regionName == null) return;

        String achId = REGION_KEY_TO_ACH_ID.get(regionName.trim().toLowerCase());
        if (achId != null) {
            reg.incrementCount(playerRef, achId, 1);
        }
    }

    private RegionsAchievements() {}
}
