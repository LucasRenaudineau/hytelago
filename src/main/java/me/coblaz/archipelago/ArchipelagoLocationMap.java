package me.coblaz.archipelago;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps every achievement ID (as registered in Registries) to its numeric
 * Archipelago location ID.
 *
 * The numbers come from the YAML world definition:
 *   1xxx = monster kills
 *   2xxx = general kill milestones
 *   3xxx = death achievements
 *   4xxx = item-possession achievements
 *   5xxx = memories collected
 *   6xxx = regions explored
 *   7000 = goal (complete_x_percent)
 *   8xxx = goblin lab repairs
 */
public final class ArchipelagoLocationMap {

    private static final Map<String, Long> ACH_TO_LOCATION_ID = new HashMap<>();

    static {
        // Goal
        ACH_TO_LOCATION_ID.put("complete_x_percent",            7000L);

        // Monster kills
        ACH_TO_LOCATION_ID.put("kill_zombie",                   1000L);
        ACH_TO_LOCATION_ID.put("kill_horse_skeleton_armored",   1001L);
        ACH_TO_LOCATION_ID.put("kill_zombie_burnt",             1002L);
        ACH_TO_LOCATION_ID.put("kill_golem_crystal_flame",      1003L);
        ACH_TO_LOCATION_ID.put("kill_golem_crystal_earth",      1004L);
        ACH_TO_LOCATION_ID.put("kill_golem_firesteel",          1005L);
        ACH_TO_LOCATION_ID.put("kill_golem_crystal_frost",      1006L);
        ACH_TO_LOCATION_ID.put("kill_skeleton_frost_archer",    1007L);
        ACH_TO_LOCATION_ID.put("kill_zombie_frost",             1008L);
        ACH_TO_LOCATION_ID.put("kill_goblin_hermit",            1009L);
        ACH_TO_LOCATION_ID.put("kill_outlander_berserker",      1010L);
        ACH_TO_LOCATION_ID.put("kill_outlander_brute",          1011L);
        ACH_TO_LOCATION_ID.put("kill_outlander_marauder",       1012L);
        ACH_TO_LOCATION_ID.put("kill_outlander_priest",         1013L);
        ACH_TO_LOCATION_ID.put("kill_outlander_stalker",        1014L);
        ACH_TO_LOCATION_ID.put("kill_golem_crystal_sand",       1015L);
        ACH_TO_LOCATION_ID.put("kill_zombie_sand",              1016L);
        ACH_TO_LOCATION_ID.put("kill_eye_void",                 1017L);
        ACH_TO_LOCATION_ID.put("kill_scarak_broodmother", 1018L);
        ACH_TO_LOCATION_ID.put("kill_toad_rhino_magma",         1019L);
        ACH_TO_LOCATION_ID.put("kill_snake_marsh",              1020L);
        ACH_TO_LOCATION_ID.put("kill_yeti",                     1021L);
        ACH_TO_LOCATION_ID.put("kill_bear_grizzly",             1023L);
        ACH_TO_LOCATION_ID.put("kill_skeleton_fighter",         1024L);
        ACH_TO_LOCATION_ID.put("kill_wolf_black_2",             1025L);
        ACH_TO_LOCATION_ID.put("kill_horse_skeleton",           1026L);
        ACH_TO_LOCATION_ID.put("kill_spawn_void",               1027L);
        ACH_TO_LOCATION_ID.put("kill_larva_void_5",             1028L);
        ACH_TO_LOCATION_ID.put("kill_spectre_void",             1029L);
        ACH_TO_LOCATION_ID.put("kill_rat_20",                   1030L);
        ACH_TO_LOCATION_ID.put("kill_dungeon_goblin_scrapper_10", 1031L);
        ACH_TO_LOCATION_ID.put("kill_dungeon_goblin_mace_2",    1032L);
        ACH_TO_LOCATION_ID.put("kill_dungeon_goblin_miner_4",   1033L);
        ACH_TO_LOCATION_ID.put("kill_dungeon_goblin_burner",    1034L);
        ACH_TO_LOCATION_ID.put("kill_dungeon_goblin_guardian",  1035L);
        ACH_TO_LOCATION_ID.put("kill_dungeon_goblin_lobber",    1036L);
        ACH_TO_LOCATION_ID.put("kill_dungeon_goblin_turret",    1037L);
        ACH_TO_LOCATION_ID.put("kill_dungeon_feastmaster_boss", 1038L);
        ACH_TO_LOCATION_ID.put("kill_dungeon_goblin_lobber_patrol", 1039L);
        ACH_TO_LOCATION_ID.put("kill_goblin_gobliterator_phase1", 1040L);
        ACH_TO_LOCATION_ID.put("kill_goblin_gobliterator_phase2", 1041L);
        ACH_TO_LOCATION_ID.put("kill_goblin_gobliterator_phase3", 1042L);

        // General kill milestones
        ACH_TO_LOCATION_ID.put("first_kill",    2000L);
        ACH_TO_LOCATION_ID.put("ten_kills",     2001L);
        ACH_TO_LOCATION_ID.put("thirty_kills",  2002L);
        ACH_TO_LOCATION_ID.put("fifty_kills",   2003L);
        ACH_TO_LOCATION_ID.put("seventy_kills", 2004L);
        ACH_TO_LOCATION_ID.put("hundred_kills", 2005L);

        // Death achievements
        ACH_TO_LOCATION_ID.put("death_fall",        3000L);
        ACH_TO_LOCATION_ID.put("death_drowning",    3001L);
        ACH_TO_LOCATION_ID.put("death_fire",        3002L);
        ACH_TO_LOCATION_ID.put("death_projectile",  3003L);

        // Item-possession achievements
        ACH_TO_LOCATION_ID.put("collect_watering_can",           4000L);
        ACH_TO_LOCATION_ID.put("collect_voidheart",              4001L);
        ACH_TO_LOCATION_ID.put("collect_fire_essence_5",         4002L);
        ACH_TO_LOCATION_ID.put("collect_ice_essence_50",         4003L);
        ACH_TO_LOCATION_ID.put("collect_life_essence_100",       4004L);
        ACH_TO_LOCATION_ID.put("collect_life_essence_500",       4005L);
        ACH_TO_LOCATION_ID.put("collect_void_essence_20",        4006L);
        ACH_TO_LOCATION_ID.put("collect_wheat_100",              4007L);
        ACH_TO_LOCATION_ID.put("collect_bucket",                 4008L);
        ACH_TO_LOCATION_ID.put("collect_fishing_trap",           4009L);
        ACH_TO_LOCATION_ID.put("collect_kweebec_plush",          4010L);
        ACH_TO_LOCATION_ID.put("collect_tankard",                4011L);
        ACH_TO_LOCATION_ID.put("collect_spike_trap_10",          4012L);
        ACH_TO_LOCATION_ID.put("collect_corn_5",                 4013L);
        ACH_TO_LOCATION_ID.put("collect_cactus_5",               4014L);
        ACH_TO_LOCATION_ID.put("collect_food_kebab_vegetable_20",4015L);
        ACH_TO_LOCATION_ID.put("collect_furniture_crude_torch_10",4016L);
        ACH_TO_LOCATION_ID.put("smelt_iron",                     4017L);
        ACH_TO_LOCATION_ID.put("smelt_copper",                   4018L);
        ACH_TO_LOCATION_ID.put("smelt_silver",                   4019L);
        ACH_TO_LOCATION_ID.put("smelt_gold",                     4020L);
        ACH_TO_LOCATION_ID.put("smelt_thorium",                  4021L);
        ACH_TO_LOCATION_ID.put("smelt_cobalt",                   4022L);
        ACH_TO_LOCATION_ID.put("collect_armor_copper_head",      4023L);
        ACH_TO_LOCATION_ID.put("collect_armor_copper_hands",     4024L);
        ACH_TO_LOCATION_ID.put("collect_armor_copper_chest",     4025L);
        ACH_TO_LOCATION_ID.put("collect_armor_copper_legs",      4026L);
        ACH_TO_LOCATION_ID.put("collect_weapon_mace_copper",     4027L);
        ACH_TO_LOCATION_ID.put("collect_weapon_sword_copper",4028L);
        ACH_TO_LOCATION_ID.put("collect_armor_iron_head",        4029L);
        ACH_TO_LOCATION_ID.put("collect_armor_iron_hands",       4030L);
        ACH_TO_LOCATION_ID.put("collect_armor_iron_chest",       4031L);
        ACH_TO_LOCATION_ID.put("collect_armor_iron_legs",        4032L);
        ACH_TO_LOCATION_ID.put("collect_weapon_mace_iron",       4033L);
        ACH_TO_LOCATION_ID.put("collect_weapon_sword_iron",  4034L);

        // Memories achievements
        for (int i = 1; i <= 150; i++) {
            ACH_TO_LOCATION_ID.put("memories_" + i, 5000L + i);
        }

        // Regions explored
        ACH_TO_LOCATION_ID.put("region_drifting_plains",    6000L);
        ACH_TO_LOCATION_ID.put("region_seedlings_woods",    6001L);
        ACH_TO_LOCATION_ID.put("region_the_fens",           6002L);
        ACH_TO_LOCATION_ID.put("region_golden_steppes",     6003L);
        ACH_TO_LOCATION_ID.put("region_badlands",           6004L);
        ACH_TO_LOCATION_ID.put("region_desolate_bassin",    6005L);
        ACH_TO_LOCATION_ID.put("region_crystalline_depths", 6006L);
        ACH_TO_LOCATION_ID.put("region_frostmarch_tundra",  6007L);
        ACH_TO_LOCATION_ID.put("region_boreal_reach",       6008L);
        ACH_TO_LOCATION_ID.put("region_the_everfrost",      6009L);
        ACH_TO_LOCATION_ID.put("region_cinder_wastes",      6010L);
        ACH_TO_LOCATION_ID.put("region_charred_woodlands",  6011L);

        // Goblin lab repairs
        ACH_TO_LOCATION_ID.put("repair_goblin_lab_generator_found", 8000L);
        ACH_TO_LOCATION_ID.put("repair_goblin_lab_power",           8001L);
        ACH_TO_LOCATION_ID.put("repair_goblin_lab_recipe_a",        8002L);
        ACH_TO_LOCATION_ID.put("repair_goblin_lab_recipe_b",        8003L);
        ACH_TO_LOCATION_ID.put("repair_goblin_lab_recipe_c",        8004L);
    }

    /**
     * Returns the numeric Archipelago location ID for the given achievement ID,
     * or {@code null} if this achievement has no corresponding AP location
     * (i.e. it is a purely local achievement not tracked by the multiworld).
     */
    @Nullable
    public static Long getLocationId(@Nonnull String achievementId) {
        return ACH_TO_LOCATION_ID.get(achievementId);
    }

    /**
     * Builds the set of location IDs that actually exist in the connected seed,
     * filtering the full catalogue above by the player's slot data. This mirrors
     * the apworld's {@code get_active_location_names} so the mod only treats the
     * locations the generator created as real checks.
     *
     * Location-id ranges:
     *   1000-1999  monster kills            (always)
     *   2000-2999  general kill milestones  (always)
     *   3000-3999  death achievements       (death_locations)
     *   4000-4999  item-possession          (always)
     *   5000-5999  memories                 (memories / memories_max / memories_every)
     *   6000-6999  regions explored         (regions)
     *   7000       goal                     (always)
     *   8000-8999  goblin lab repairs       (always)
     */
    @Nonnull
    public static Set<Long> buildActiveLocationIds(@Nonnull HytaleSlotData slot) {
        int every = Math.max(1, slot.memories_every);
        Set<Long> active = new HashSet<>();

        for (long id : ACH_TO_LOCATION_ID.values()) {
            if (id >= 3000 && id <= 3999) {
                if (!slot.hasDeathLocations()) continue;
            } else if (id > 5000 && id <= 5999) {
                if (!slot.hasMemories()) continue;
                int index = (int) (id - 5000);
                if (index > slot.memories_max) continue;
                // memories_every == 2 keeps memories_1, memories_3, memories_5, ...
                if ((index - 1) % every != 0) continue;
            } else if (id >= 6000 && id <= 6999) {
                if (!slot.hasRegions()) continue;
            }
            active.add(id);
        }
        return active;
    }

    private ArchipelagoLocationMap() {}
}
