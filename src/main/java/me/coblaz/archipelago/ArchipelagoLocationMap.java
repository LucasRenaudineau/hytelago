package me.coblaz.archipelago;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps every achievement ID (as registered in Registries) to its numeric
 * Archipelago location ID.
 *
 * The numbers come from the YAML world definition:
 *   1xxx = monster kills
 *   2xxx = general kill milestones
 *   3xxx = death achievements
 *   4xxx = item-possession achievements
 */
public final class ArchipelagoLocationMap {

    private static final Map<String, Long> ACH_TO_LOCATION_ID = new HashMap<>();

    static {
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
        ACH_TO_LOCATION_ID.put("kill_scarac_broodmother_young", 1018L);
        ACH_TO_LOCATION_ID.put("kill_toad_rhino_magma",         1019L);
        ACH_TO_LOCATION_ID.put("kill_snake_marsh",              1020L);
        ACH_TO_LOCATION_ID.put("kill_yeti",                     1021L);
        ACH_TO_LOCATION_ID.put("kill_frost_dragon",             1022L);

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
        ACH_TO_LOCATION_ID.put("death_fire",        3004L);
        ACH_TO_LOCATION_ID.put("death_projectile",  3006L);

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
        ACH_TO_LOCATION_ID.put("collect_carrot_20",              4013L);
        ACH_TO_LOCATION_ID.put("collect_corn_5",                 4014L);
        ACH_TO_LOCATION_ID.put("collect_cactus_5",               4015L);
        ACH_TO_LOCATION_ID.put("collect_food_kebab_vegetable_20",4016L);
        ACH_TO_LOCATION_ID.put("collect_furniture_crude_torch_10",4017L);
        ACH_TO_LOCATION_ID.put("smelt_iron",                     4018L);
        ACH_TO_LOCATION_ID.put("smelt_copper",                   4019L);
        ACH_TO_LOCATION_ID.put("smelt_silver",                   4020L);
        ACH_TO_LOCATION_ID.put("smelt_gold",                     4021L);
        ACH_TO_LOCATION_ID.put("smelt_thorium",                  4022L);
        ACH_TO_LOCATION_ID.put("smelt_cobalt",                   4023L);
        ACH_TO_LOCATION_ID.put("collect_armor_copper_head",      4024L);
        ACH_TO_LOCATION_ID.put("collect_armor_copper_hands",     4025L);
        ACH_TO_LOCATION_ID.put("collect_armor_copper_chest",     4026L);
        ACH_TO_LOCATION_ID.put("collect_armor_copper_legs",      4027L);
        ACH_TO_LOCATION_ID.put("collect_weapon_mace_copper",     4028L);
        ACH_TO_LOCATION_ID.put("collect_weapon_longsword_copper",4029L);
        ACH_TO_LOCATION_ID.put("collect_armor_iron_head",        4030L);
        ACH_TO_LOCATION_ID.put("collect_armor_iron_hands",       4031L);
        ACH_TO_LOCATION_ID.put("collect_armor_iron_chest",       4032L);
        ACH_TO_LOCATION_ID.put("collect_armor_iron_legs",        4033L);
        ACH_TO_LOCATION_ID.put("collect_weapon_mace_iron",       4034L);
        ACH_TO_LOCATION_ID.put("collect_weapon_longsword_iron",  4035L);

        // Memories achievements
        for (int i = 1; i <= 150; i++) {
            ACH_TO_LOCATION_ID.put("memories_" + i, 5000L + i);
        }
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

    private ArchipelagoLocationMap() {}
}