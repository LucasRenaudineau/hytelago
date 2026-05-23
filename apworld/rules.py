from __future__ import annotations

from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from .world import HytaleWorld


# ─── Helpers ─────────────────────────────────────────────────────────────────

def _make_rule(player: int, *requirements: tuple[str, int] | str):
    """Return an access-rule function.

    Each requirement is either a bare item name (count=1) or a (name, count) tuple.
    """
    parsed: list[tuple[str, int]] = []
    for req in requirements:
        if isinstance(req, tuple):
            parsed.append(req)
        else:
            parsed.append((req, 1))

    def rule(state) -> bool:
        return all(state.has(item, player, count) for item, count in parsed)

    return rule


# ─── Main entry point ─────────────────────────────────────────────────────────

def set_all_rules(world: HytaleWorld) -> None:
    player = world.player
    multiworld = world.multiworld

    def set_rule(location_name: str, *requirements: tuple[str, int] | str) -> None:
        multiworld.get_location(location_name, player).access_rule = _make_rule(
            player, *requirements
        )

    # ── General kill milestones ───────────────────────────────────────────────

    # first_kill, ten_kills – no requirements (bare hands suffice)

    set_rule(
        "thirty_kills",
        ("Progressive_Workbench", 2),
        "Campfire",
        ("Progressive_Blacksmith", 2),
        "Progressive_Furnace",
        "Progressive_Tanning",
        "Weapon_Daggers_Bone",
        "Weapon_Shortbow_Combat",
        "Weapon_Arrow_Clearshot30",
        "Ingredient_Hide_Soft10",
        "Ore_Iron10",
    )

    set_rule(
        "fifty_kills",
        ("Progressive_Workbench", 2),
        ("Progressive_Blacksmith", 2),
        "Progressive_Furnace",
        "Progressive_Tanning",
        "Ore_Cobalt10",
        "Ingredient_Hide_Light10",
        "Weapon_Daggers_Bone",
        "Ingredient_Hide_Medium10",
        "Ingredient_Chitin_Sturdy10",
    )

    _hard_kills = _make_rule(
        player,
        ("Progressive_Workbench", 3),
        ("Progressive_Blacksmith", 2),
        "Progressive_Furnace",
        "Progressive_Tanning",
        "Ore_Thorium10",
        "Ingredient_Hide_Medium10",
        "Ingredient_Hide_Heavy10",
        "Ingredient_Chitin_Sturdy10",
    )
    for loc in ("seventy_kills", "hundred_kills"):
        multiworld.get_location(loc, player).access_rule = _hard_kills

    # ── Monster Kill – frost / cold biome ─────────────────────────────────────

    _frost_kills = _make_rule(
        player,
        ("Progressive_Armorer", 2),
        ("Progressive_Backpack", 2),
        ("Progressive_Blacksmith", 2),
        ("Progressive_Furnace", 2),
        ("Progressive_Tanning", 2),
        ("Progressive_Workbench", 2),
        "Ore_Cobalt10",
        "Ingredient_Hide_Medium10",
        "Ingredient_Hide_Light10",
        "Teleporter3",
    )
    for loc in ("kill_zombie_frost", "kill_skeleton_frost_archer", "kill_yeti"):
        multiworld.get_location(loc, player).access_rule = _frost_kills

    # ── Monster Kill – burnt / sand / marsh / magma ───────────────────────────

    _grizzly_bear_kill = _make_rule(
        player,
        "Progressive_Workbench",
        "Progressive_Blacksmith",
        "Progressive_Furnace"
    )
    for loc in ("kill_grizzly_bear"):
        multiworld.get_location(loc,player).access_rule = _grizzly_bear_kill

    _mid_kills = _make_rule(
        player,
        "Progressive_Armorer",
        "Progressive_Backpack",
        "Progressive_Blacksmith",
        "Progressive_Furnace",
        "Progressive_Tanning",
        ("Progressive_Workbench", 2),
        "Ore_Iron10",
        "Ingredient_Hide_Light10",
        "Ingredient_Chitin_Sturdy10",
        "Teleporter3",
    )
    for loc in (
        "kill_zombie_burnt",
        "kill_zombie_sand",
        "kill_scarac_broodmother_young",
        "kill_toad_rhino_magma",
        "kill_snake_marsh",
    ):
        multiworld.get_location(loc, player).access_rule = _mid_kills

    # ── Monster Kill – high-tier golems + frost dragon ────────────────────────

    _elite_kills = _make_rule(
        player,
        ("Progressive_Armorer", 2),
        ("Progressive_Backpack", 2),
        ("Progressive_Blacksmith", 2),
        ("Progressive_Furnace", 2),
        ("Progressive_Tanning", 2),
        ("Progressive_Workbench", 3),
        "Ore_Thorium10",
        "Ore_Cobalt10",
        "Ingredient_Hide_Soft10",
        "Ingredient_Hide_Light10",
        "Ingredient_Hide_Medium10",
        "Ingredient_Hide_Heavy10",
        "Ingredient_Chitin_Sturdy10",
        ("Teleporter3", 2),
        "Repair_Kit2",
    )
    for loc in (
        "kill_golem_crystal_flame",
        "kill_golem_firesteel",
        "kill_golem_crystal_frost",
        "kill_golem_crystal_sand",
        "kill_frost_dragon",
    ):
        multiworld.get_location(loc, player).access_rule = _elite_kills

    # ── Monster Kill – outlanders ─────────────────────────────────────────────

    _outlander_kills = _make_rule(
        player,
        ("Progressive_Armorer", 2),
        ("Progressive_Backpack", 2),
        ("Progressive_Blacksmith", 2),
        ("Progressive_Furnace", 2),
        "Progressive_Tanning",
        ("Progressive_Workbench", 2),
        "Ore_Thorium10",
        "Ore_Cobalt10",
        "Ingredient_Hide_Soft10",
        "Ingredient_Hide_Light10",
        "Ingredient_Hide_Medium10",
        "Ingredient_Hide_Heavy10",
        "Ingredient_Chitin_Sturdy10",
        "Teleporter3",
    )
    for loc in (
        "kill_outlander_berserker",
        "kill_outlander_brute",
        "kill_outlander_marauder",
        "kill_outlander_priest",
        "kill_outlander_stalker",
    ):
        multiworld.get_location(loc, player).access_rule = _outlander_kills

    # ── Monster Kill – earth golem (easiest golem) ────────────────────────────

    set_rule(
        "kill_golem_crystal_earth",
        "Ore_Copper10",
        "Progressive_Workbench",
        "Progressive_Armorer",
        "Progressive_Blacksmith",
        "Progressive_Furnace",
    )

    # kill_zombie, kill_horse_skeleton_armored, kill_goblin_hermit, kill_eye_void
    # have no stated requirements – always accessible.

    # ── Death achievements ────────────────────────────────────────────────────

    _teleport_death = _make_rule(
        player,
        "Progressive_Workbench",
        "Progressive_Furnace",
        "Progressive_Armorer",
        "Teleporter3",
    )
    for loc in ("death_fire"):
        multiworld.get_location(loc, player).access_rule = _teleport_death

    # death_fall, death_drowning, death_suffocation, death_physical, death_projectile
    # have no stated requirements.

    # ── Item possessed ────────────────────────────────────────────────────────

    set_rule(
        "collect_watering_can",
        "Progressive_Farmer",
        "Progressive_Furnace",
        "Ore_Iron10",
        "Progressive_Workbench",
    )

    # collect_fire_essence_5 and collect_ice_essence_50 share the same rule
    _fire_ice_essence = _make_rule(
        player,
        ("Progressive_Armorer", 2),
        ("Progressive_Blacksmith", 2),
        ("Progressive_Furnace", 2),
        ("Progressive_Workbench", 2),
        "Ore_Copper10",
        "Ore_Iron10",
        "Ore_Cobalt10",
        "Ingredient_Hide_Soft10",
        "Ingredient_Hide_Medium10",
        ("Teleporter3", 2),
    )
    for loc in ("collect_fire_essence_5", "collect_ice_essence_50"):
        multiworld.get_location(loc, player).access_rule = _fire_ice_essence

    set_rule(
        "collect_life_essence_100",
        "Progressive_Armorer",
        "Progressive_Blacksmith",
        "Progressive_Furnace",
        ("Progressive_Workbench", 2),
        "Ore_Copper10",
        "Ingredient_Hide_Soft10",
        "Progressive_Farmer",
    )

    set_rule(
        "collect_life_essence_500",
        "Progressive_Armorer",
        "Progressive_Blacksmith",
        "Progressive_Furnace",
        "Progressive_Workbench",
        "Ore_Copper10",
        "Ingredient_Hide_Soft10",
        ("Progressive_Farmer", 2),
        "Teleporter3",
    )

    # collect_voidheart and collect_void_essence_20 share the same rule
    _void_rule = _make_rule(
        player,
        ("Progressive_Armorer", 2),
        ("Progressive_Blacksmith", 2),
        ("Progressive_Furnace", 2),
        ("Progressive_Workbench", 2),
        "Ore_Copper10",
        "Ore_Iron10",
        "Ore_Cobalt10",
        "Ingredient_Hide_Soft10",
        "Ingredient_Hide_Medium10",
        ("Teleporter3", 2),
        "Repair_Kit2",
    )
    for loc in ("collect_voidheart", "collect_void_essence_20"):
        multiworld.get_location(loc, player).access_rule = _void_rule

    set_rule(
        "collect_wheat_100",
        "Progressive_Armorer",
        "Progressive_Blacksmith",
        "Progressive_Furnace",
        "Progressive_Workbench",
        "Ore_Copper10",
        "Ore_Iron10",
        "Ingredient_Hide_Soft10",
        "Teleporter3",
        ("Progressive_Farmer", 2),
    )

    set_rule(
        "collect_bucket",
        "Progressive_Workbench",
        "Ore_Iron10",
        "Progressive_Furnace",
        "Progressive_Farmer",
    )

    set_rule(
        "collect_fishing_trap",
        ("Progressive_Workbench", 2),
        "Ore_Iron10",
        ("Progressive_Farmer", 2),
        ("Progressive_Armorer", 2),
        "Progressive_Furnace",
    )

    set_rule(
        "collect_kweebec_plush",
        "Progressive_Workbench",
        "Progressive_Farmer",
    )

    set_rule(
        "collect_tankard",
        "Progressive_Workbench",
        "Progressive_Farmer",
        "Ore_Iron10",
        "Progressive_Furnace",
    )

    set_rule("collect_spike_trap_10", "Progressive_Workbench")

    _farm_cooking = _make_rule(
        player,
        "Progressive_Workbench",
        ("Progressive_Farmer", 2),
        "Chef_Stove",
        "Campfire",
    )
    for loc in ("collect_carrot_20", "collect_corn_5"):
        multiworld.get_location(loc, player).access_rule = _farm_cooking

    set_rule("collect_cactus_5", "Teleporter3")

    set_rule(
        "collect_food_kebab_vegetable_20",
        "Progressive_Farmer",
        "Progressive_Workbench",
        "Chef_Stove",
    )

    set_rule("collect_furniture_crude_torch_10", "Progressive_Workbench")

    # ── Smelting ──────────────────────────────────────────────────────────────

    set_rule("smelt_iron",   "Ore_Iron10",   "Progressive_Furnace")
    set_rule("smelt_copper", "Ore_Copper10", "Progressive_Furnace")
    set_rule("smelt_silver", "Ore_Silver10", "Progressive_Furnace")
    set_rule("smelt_gold",   "Ore_Gold10",   "Progressive_Furnace")
    set_rule("smelt_thorium","Ore_Thorium10","Progressive_Furnace")
    set_rule("smelt_cobalt", "Ore_Cobalt10", "Progressive_Furnace")

    # ── Copper gear ───────────────────────────────────────────────────────────

    _copper_gear = _make_rule(
        player,
        "Progressive_Workbench",
        "Progressive_Armorer",
        "Progressive_Blacksmith",
        "Progressive_Furnace",
        "Ore_Copper10",
        "Salvager",
    )
    for loc in (
        "collect_armor_copper_head",
        "collect_armor_copper_hands",
        "collect_armor_copper_chest",
        "collect_armor_copper_legs",
        "collect_weapon_mace_copper",
        "collect_weapon_longsword_copper",
    ):
        multiworld.get_location(loc, player).access_rule = _copper_gear

    # ── Iron gear ─────────────────────────────────────────────────────────────

    _iron_gear = _make_rule(
        player,
        "Progressive_Workbench",
        "Progressive_Armorer",
        "Progressive_Blacksmith",
        "Progressive_Furnace",
        "Ore_Iron10",
        "Salvager",
    )
    for loc in (
        "collect_armor_iron_head",
        "collect_armor_iron_hands",
        "collect_armor_iron_chest",
        "collect_armor_iron_legs",
        "collect_weapon_mace_iron",
        "collect_weapon_longsword_iron",
    ):
        multiworld.get_location(loc, player).access_rule = _iron_gear

    # ── Completion condition ──────────────────────────────────────────────────
    # Goal: defeat the frost dragon, the hardest achievement in the game.
    world.multiworld.completion_condition[player] = _make_rule(
        player,
        ("Progressive_Armorer", 2),
        ("Progressive_Backpack", 2),
        ("Progressive_Blacksmith", 2),
        ("Progressive_Furnace", 2),
        ("Progressive_Tanning", 2),
        ("Progressive_Workbench", 3),
        "Ore_Thorium10",
        "Ore_Cobalt10",
        "Ingredient_Hide_Soft10",
        "Ingredient_Hide_Light10",
        "Ingredient_Hide_Medium10",
        "Ingredient_Hide_Heavy10",
        "Ingredient_Chitin_Sturdy10",
        ("Teleporter3", 2),
        "Repair_Kit2",
    )
