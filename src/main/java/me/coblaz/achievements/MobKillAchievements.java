package me.coblaz.achievements;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MobKillAchievements {

    public record Entry(
            String roleId,
            String achievementId,
            String title,
            int needed
    ) {}

    // ── Master list ───────────────────────────────────────────────────────────
    // roleId        → exact string returned by npc.getRoleName() (case-insensitive match)
    // needed        → kills required; adjust freely

    public static final List<Entry> ALL = List.of(
            new Entry("zombie",                   "kill_zombie",                   "Zombie Slayer",          2),
            new Entry("horse_skeleton_armored",   "kill_horse_skeleton_armored",   "Armored Bones",          3),
            new Entry("skeleton_burnt_soldier",   "kill_skeleton_burnt_soldier",   "Burnt to the Bone",      5),
            new Entry("zombie_burnt",             "kill_zombie_burnt",             "Crispy Undead",          5),
            new Entry("golem_crystal_flame",      "kill_golem_crystal_flame",      "Flame Crystal Crusher",  1),
            new Entry("golem_crystal_earth",      "kill_golem_crystal_earth",      "Earth Crystal Crusher",  1),
            new Entry("golem_firesteel",          "kill_golem_firesteel",          "Firesteel Breaker",      1),
            new Entry("golem_crystal_frost",      "kill_golem_crystal_frost",      "Frost Crystal Crusher",  1),
            new Entry("skeleton_frost_archer",    "kill_skeleton_frost_archer",    "Cold Shot",              5),
            new Entry("zombie_frost",             "kill_zombie_frost",             "Frozen Undead",          5),
            new Entry("goblin_hermit",            "kill_goblin_hermit",            "Hermit Hunter",          5),
            new Entry("outlander_berserker",      "kill_outlander_berserker",      "Berserker Slayer",       5),
            new Entry("outlander_brute",          "kill_outlander_brute",          "Brute Force",            5),
            new Entry("outlander_hunter",         "kill_outlander_hunter",         "Hunter Hunted",          5),
            new Entry("outlander_marauder",       "kill_outlander_marauder",       "Marauder's End",         5),
            new Entry("outlander_priest",         "kill_outlander_priest",         "Unholy Defeat",          5),
            new Entry("outlander_stalker",        "kill_outlander_stalker",        "Stalker Stopped",        5),
            new Entry("golem_crystal_sand",       "kill_golem_crystal_sand",       "Sand Crystal Crusher",   3),
            new Entry("zombie_sand",              "kill_zombie_sand",              "Desert Undead",          5),
            new Entry("eye_void",                 "kill_eye_void",                 "Void Sight",             1),
            new Entry("scarak_broodmother_young", "kill_scarak_broodmother_young", "Brood Breaker",          1),
            new Entry("toad_rhino_magma", "kill_toad_rhino_magma", "Did not like my kiss ?", 1),
            new Entry("snake_marsh", "kill_snake_marsh", "Sliding in the desert", 5),
            new Entry("yeti", "kill_yeti", "It really exists", 1),
            new Entry("frost_dragon", "kill_frost_dragn", "Already finished (/", 1),
            new Entry("bear_grizzly", "kill_bear_grizzly", "Not affraid of a big bear", 1),
            new Entry("skeleton_fighter", "kill_skeleton_fighter", "A simple fight", 1)
    );

    // ── Lookup map: roleName (lowercase) → achievementId ─────────────────────

    private static final Map<String, String> ROLE_TO_ACH_ID =
            ALL.stream().collect(Collectors.toMap(
                    e -> e.roleId().toLowerCase(),
                    Entry::achievementId
            ));

    // ── API ───────────────────────────────────────────────────────────────────

    /** Registers every entry into the given registry. Call once at startup. */
    public static void registerAll(@Nonnull AchievementRegistry reg) {
        for (Entry e : ALL) {
            reg.registerAchievement(new AchievementDefinition(
                    e.achievementId(), e.title(), e.needed()
            ));
        }
    }

    /**
     * Returns the achievement ID for the given NPC role name,
     * or null if that mob has no associated achievement.
     */
    @Nullable
    public static String achievementIdForRole(@Nonnull String roleName) {
        return ROLE_TO_ACH_ID.get(roleName.toLowerCase());
    }

    private MobKillAchievements() {}
}