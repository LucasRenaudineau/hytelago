from __future__ import annotations

from typing import TYPE_CHECKING

from BaseClasses import Location

if TYPE_CHECKING:
    from .world import HytaleWorld

# If some numbers are missing, it is because some locations were removed because they did not work or did not please me.
# For the moment and for any contributer, I would advise to continue get increasing numbers and let the holes.
LOCATION_NAME_TO_ID: dict[str, int] = {
    # Monster kill
    "kill_zombie":                   1000,
    "kill_horse_skeleton_armored":   1001,
    "kill_zombie_burnt":             1002,
    "kill_golem_crystal_flame":      1003,
    "kill_golem_crystal_earth":      1004,
    "kill_golem_firesteel":          1005,
    "kill_golem_crystal_frost":      1006,
    "kill_skeleton_frost_archer":    1007,
    "kill_zombie_frost":             1008,
    "kill_goblin_hermit":            1009,
    "kill_outlander_berserker":      1010,
    "kill_outlander_brute":          1011,
    "kill_outlander_marauder":       1012,
    "kill_outlander_priest":         1013,
    "kill_outlander_stalker":        1014,
    "kill_golem_crystal_sand":       1015,
    "kill_zombie_sand":              1016,
    "kill_eye_void":                 1017,
    "kill_scarac_broodmother_young": 1018,
    "kill_toad_rhino_magma":         1019,
    "kill_snake_marsh":              1020,
    "kill_yeti":                     1021,
    "kill_frost_dragon":             1022,
    "kill_bear_grizzly": 1023,
    "kill_skeleton_fighter": 1024,
    "kill_wolf_black": 1025,
    "kill_horse_skeleton": 1026,
    # General kills
    "first_kill":    2000,
    "ten_kills":     2001,
    "thirty_kills":  2002,
    "fifty_kills":   2003,
    "seventy_kills": 2004,
    "hundred_kills": 2005,
    # Death achievements
    "death_fall":        3000,
    "death_drowning":    3001,
    "death_fire":        3004,
    "death_projectile":  3006,
    # Item possessed
    "collect_watering_can":           4000,
    "collect_voidheart":              4001,
    "collect_fire_essence_5":         4002,
    "collect_ice_essence_50":         4003,
    "collect_life_essence_100":       4004,
    "collect_life_essence_500":       4005,
    "collect_void_essence_20":        4006,
    "collect_wheat_100":              4007,
    "collect_bucket":                 4008,
    "collect_fishing_trap":           4009,
    "collect_kweebec_plush":          4010,
    "collect_tankard":                4011,
    "collect_spike_trap_10":          4012,
    "collect_carrot_20":              4013,
    "collect_corn_5":                 4014,
    "collect_cactus_5":               4015,
    "collect_food_kebab_vegetable_20":4016,
    "collect_furniture_crude_torch_10":4017,
    "smelt_iron":                     4018,
    "smelt_copper":                   4019,
    "smelt_silver":                   4020,
    "smelt_gold":                     4021,
    "smelt_thorium":                  4022,
    "smelt_cobalt":                   4023,
    "collect_armor_copper_head":      4024,
    "collect_armor_copper_hands":     4025,
    "collect_armor_copper_chest":     4026,
    "collect_armor_copper_legs":      4027,
    "collect_weapon_mace_copper":     4028,
    "collect_weapon_longsword_copper":4029,
    "collect_armor_iron_head":        4030,
    "collect_armor_iron_hands":       4031,
    "collect_armor_iron_chest":       4032,
    "collect_armor_iron_legs":        4033,
    "collect_weapon_mace_iron":       4034,
    "collect_weapon_longsword_iron":  4035,
}

class HytaleLocation(Location):
    game = "Hytale"

# Creation

def create_all_locations(world: HytaleWorld) -> None:
    hytale_region = world.get_region("Hytale")
    hytale_region.add_locations(LOCATION_NAME_TO_ID, HytaleLocation)
