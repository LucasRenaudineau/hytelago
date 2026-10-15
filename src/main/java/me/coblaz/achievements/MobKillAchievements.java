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

    public static final List<Entry> ALL = List.of(
            new Entry("zombie",                   "kill_zombie",                   "Zombie Slayer",          2),
            new Entry("horse_skeleton_armored",   "kill_horse_skeleton_armored",   "Armored Bones",          3),
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
            new Entry("outlander_marauder",       "kill_outlander_marauder",       "Marauder's End",         5),
            new Entry("outlander_priest",         "kill_outlander_priest",         "Unholy Defeat",          5),
            new Entry("outlander_stalker",        "kill_outlander_stalker",        "Stalker Stopped",        5),
            new Entry("golem_crystal_sand",       "kill_golem_crystal_sand",       "Sand Crystal Crusher",   3),
            new Entry("zombie_sand",              "kill_zombie_sand",              "Desert Undead",          5),
            new Entry("eye_void",                 "kill_eye_void",                 "Void Sight",             1),
            new Entry("scarak_broodmother", "kill_scarak_broodmother", "Brood Breaker",          1),
            new Entry("toad_rhino_magma", "kill_toad_rhino_magma", "Did not like my kiss ?", 1),
            new Entry("snake_marsh", "kill_snake_marsh", "Sliding in the desert", 5),
            new Entry("yeti", "kill_yeti", "It really exists", 1),
            new Entry("bear_grizzly", "kill_bear_grizzly", "Not afraid of a big bear", 1),
            new Entry("skeleton_fighter", "kill_skeleton_fighter", "A simple fight", 1),
            new Entry("wolf_black", "kill_wolf_black_2", "WooooooOOO !", 2),
            new Entry("horse_skeleton", "kill_horse_skeleton", "Bone destroyer", 1),
            new Entry("spawn_void", "kill_spawn_void", "Spawn Camper", 1),
            new Entry("larva_void", "kill_larva_void_5", "Pest Control", 5),
            new Entry("spectre_void", "kill_spectre_void", "Ghostbuster", 1),
            new Entry("rat", "kill_rat_20", "Rat Catcher", 20),
            new Entry("dungeon_goblin_scrapper", "kill_dungeon_goblin_scrapper_10", "Scrap Collector", 10),
            new Entry("dungeon_goblin_mace", "kill_dungeon_goblin_mace_2", "Mace Off", 2),
            new Entry("dungeon_goblin_miner", "kill_dungeon_goblin_miner_4", "Strike the Miners", 4),
            new Entry("dungeon_goblin_burner", "kill_dungeon_goblin_burner", "Fire Extinguisher", 1),
            new Entry("dungeon_goblin_guardian", "kill_dungeon_goblin_guardian", "Guard Down", 1),
            new Entry("dungeon_goblin_lobber", "kill_dungeon_goblin_lobber", "Lob Stopper", 1),
            new Entry("dungeon_goblin_turret", "kill_dungeon_goblin_turret", "Turret Disarmed", 1),
            new Entry("dungeon_feastmaster_boss", "kill_dungeon_feastmaster_boss", "The Feast is Over", 1),
            new Entry("dungeon_goblin_lobber_patrol", "kill_dungeon_goblin_lobber_patrol", "Patrol Ambushed", 1),
            new Entry("goblin_gobliterator_phase1", "kill_goblin_gobliterator_phase1", "Gobliterator: Phase 1", 1),
            new Entry("goblin_gobliterator_phase2", "kill_goblin_gobliterator_phase2", "Gobliterator: Phase 2", 1),
            new Entry("goblin_gobliterator_phase3", "kill_goblin_gobliterator_phase3", "Gobliterated", 1)
    );

    // Lookup map: roleName (lowercase) -> achievementId

    private static final Map<String, String> ROLE_TO_ACH_ID =
            ALL.stream().collect(Collectors.toMap(
                    e -> e.roleId().toLowerCase(),
                    Entry::achievementId
            ));

    /**
     * Extra NPC roles that are the same creature as one of the entries above but
     * ship under a different role asset, so killing them counts all the same.
     * Key and value are both lowercase role names.
     */
    private static final Map<String, String> ROLE_ALIASES = Map.of(
            // The Tier-3 Scarak hive spawns the dungeon variant of the boss
            // (Server/NPC/Spawn/Markers/Dungeon_Scarak_Broodmother.json), while
            // the other hives use the Scarak_Boss marker -> Scarak_Broodmother.
            "dungeon_scarak_broodmother", "scarak_broodmother"
    );

    // API

    /** Registers every entry into the given registry. Call once at startup. */
    public static void registerAll(@Nonnull AchievementRegistry reg) {
        for (Entry e : ALL) {
            reg.registerAchievement(new AchievementDefinition(
                    e.achievementId(), e.title(), e.needed(), Category.MONSTER_KILL
            ));
        }
    }

    /**
     * Returns the achievement ID for the given NPC role name,
     * or null if that mob has no associated achievement.
     */
    @Nullable
    public static String achievementIdForRole(@Nonnull String roleName) {
        String key = roleName.toLowerCase();

        String achId = ROLE_TO_ACH_ID.get(key);
        if (achId != null) return achId;

        String alias = ROLE_ALIASES.get(key);
        if (alias != null) return ROLE_TO_ACH_ID.get(alias);

        return null;
    }

    private MobKillAchievements() {}
}