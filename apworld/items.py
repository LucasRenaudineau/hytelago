from __future__ import annotations

from typing import TYPE_CHECKING

from BaseClasses import Item, ItemClassification

if TYPE_CHECKING:
    from .world import HytaleWorld

ITEM_NAME_TO_ID: dict[str, int] = {
    # Monster spawn traps
    "Golem_Crystal_Earth":       1000,
    "Golem_Firesteel":           1001,
    "Skeleton_Frost_Archer2":    1002,
    "Outlander_Berserker":       1003,
    "Eye_Void":                  1004,
    "Zombie2":                   1005,
    "Scarak_Broodmother_Young":  1006,
    "Scarak_Seeker":             1007,
    "Yeti":                      1008,
    # Tier Upgraders
    "Progressive_Armorer":       2000,
    "Progressive_Backpack":      2001,
    "Progressive_Blacksmith":    2002,
    "Campfire":                  2003,
    "Chef_Stove":                2004,
    "Progressive_Farmer":        2005,
    "Progressive_Furnace":       2006,
    "Salvager":                  2007,
    "Progressive_Tanning":       2008,
    "Progressive_Workbench":     2009,
    # Random loot
    "Ore_Copper10":              3000,
    "Ore_Iron10":                3001,
    "Ore_Cobalt10":              3002,
    "Ore_Gold10":                3003,
    "Ore_Silver10":              3004,
    "Ore_Thorium10":             3005,
    "Ingredient_Hide_Soft10":    3006,
    "Ingredient_Hide_Light10":   3007,
    "Ingredient_Hide_Medium10":  3008,
    "Ingredient_Hide_Heavy10":   3009,
    "Ingredient_Chitin_Sturdy10":3010,
    "Ingredient_Poop":           3011,
    "Flamethrower_Goblin":       3012,
    "Teleporter3":               3013,
    "Armor_Adamantite_Legs":     3014,
    "Armor_Adamantite_Chest":    3015,
    "Armor_Adamantite_Head":     3016,
    "Armor_Adamantite_Hands":    3017,
    "Weapon_Shield_Adamantite":  3018,
    "Weapon_Axe_Cobalt":         3019,
    "Weapon_Longsword_Copper":   3020,
    "Weapon_Shortbow_Combat":    3021,
    "Weapon_Daggers_Bone":       3022,
    "Weapon_Arrow_Clearshot30":  3023,
    "Weapon_Deployable_Healing_Totem": 3024,
    "Repair_Kit2":               3025,
    "Plant_Crop_Carrot_Item5":   3026,
    "Plant_Seeds_Corn":          3027,
    "Plant_Cactus_1":            3028,
    "Ingredient_Charcoal10":     3029,
    "Furniture_Village_Chest_Small2": 3030,
    "Plant_Crop_Wheat_Block3":   3031,
    "Food_Bread5":               3032,
}

DEFAULT_ITEM_CLASSIFICATIONS: dict[str, ItemClassification] = {
    # Monster spawn traps
    "Golem_Crystal_Earth":       ItemClassification.trap,
    "Golem_Firesteel":           ItemClassification.trap,
    "Skeleton_Frost_Archer2":    ItemClassification.trap,
    "Outlander_Berserker":       ItemClassification.trap,
    "Eye_Void":                  ItemClassification.trap,
    "Zombie2":                   ItemClassification.trap,
    "Scarak_Broodmother_Young":  ItemClassification.trap,
    "Scarak_Seeker":             ItemClassification.trap,
    "Yeti":                      ItemClassification.trap,
    # Tier Upgraders – all required by some rule
    "Progressive_Armorer":       ItemClassification.progression,
    "Progressive_Backpack":      ItemClassification.progression,
    "Progressive_Blacksmith":    ItemClassification.progression,
    "Campfire":                  ItemClassification.progression,
    "Chef_Stove":                ItemClassification.progression,
    "Progressive_Farmer":        ItemClassification.progression,
    "Progressive_Furnace":       ItemClassification.progression,
    "Salvager":                  ItemClassification.progression,
    "Progressive_Tanning":       ItemClassification.progression,
    "Progressive_Workbench":     ItemClassification.progression,
    # Ores & materials required by rules
    "Ore_Copper10":              ItemClassification.progression,
    "Ore_Iron10":                ItemClassification.progression,
    "Ore_Cobalt10":              ItemClassification.progression,
    "Ore_Gold10":                ItemClassification.progression,
    "Ore_Silver10":              ItemClassification.progression,
    "Ore_Thorium10":             ItemClassification.progression,
    "Ingredient_Hide_Soft10":    ItemClassification.progression,
    "Ingredient_Hide_Light10":   ItemClassification.progression,
    "Ingredient_Hide_Medium10":  ItemClassification.progression,
    "Ingredient_Hide_Heavy10":   ItemClassification.progression,
    "Ingredient_Chitin_Sturdy10":ItemClassification.progression,
    # Filler
    "Ingredient_Poop":           ItemClassification.filler,
    # Useful loot not directly gating any location
    "Flamethrower_Goblin":       ItemClassification.useful,
    # Teleporters are required by hard-zone rules
    "Teleporter3":               ItemClassification.progression,
    # Adamantite gear – nice but not required by any rule
    "Armor_Adamantite_Legs":     ItemClassification.useful,
    "Armor_Adamantite_Chest":    ItemClassification.useful,
    "Armor_Adamantite_Head":     ItemClassification.useful,
    "Armor_Adamantite_Hands":    ItemClassification.useful,
    "Weapon_Shield_Adamantite":  ItemClassification.useful,
    "Weapon_Axe_Cobalt":         ItemClassification.useful,
    "Weapon_Longsword_Copper":   ItemClassification.useful,
    # Weapons required by kill rules
    "Weapon_Shortbow_Combat":    ItemClassification.progression,
    "Weapon_Daggers_Bone":       ItemClassification.progression,
    "Weapon_Arrow_Clearshot30":  ItemClassification.progression,
    "Weapon_Deployable_Healing_Totem": ItemClassification.useful,
    "Repair_Kit2":               ItemClassification.progression,
    # New loot – helpful but not gating any rule
    "Plant_Crop_Carrot_Item5":   ItemClassification.useful,
    "Plant_Seeds_Corn":          ItemClassification.useful,
    "Plant_Cactus_1":            ItemClassification.useful,
    "Ingredient_Charcoal10":     ItemClassification.useful,
    "Furniture_Village_Chest_Small2": ItemClassification.useful,
    "Plant_Crop_Wheat_Block3":   ItemClassification.useful,
    "Food_Bread5":               ItemClassification.useful,
}

# Item pool quantities (Ingredient_Poop is not included because it is the filler item)
# Ingredient_Poop is excluded here; it is added dynamically as filler to fill
# any remaining location slots.

ITEM_POOL_QUANTITIES: dict[str, int] = {
    # Monster spawn traps
    "Golem_Crystal_Earth":        2,
    "Golem_Firesteel":            2,
    "Skeleton_Frost_Archer2":     2,
    "Outlander_Berserker":        2,
    "Eye_Void":                   2,
    "Zombie2":                    6,
    "Scarak_Broodmother_Young":   2,
    "Scarak_Seeker":              2,
    "Yeti":                       2,
    # Tier Upgraders
    "Progressive_Armorer":        2,
    "Progressive_Backpack":       3,
    "Progressive_Blacksmith":     2,
    "Campfire":                   1,
    "Chef_Stove":                 1,
    "Progressive_Farmer":         3,
    "Progressive_Furnace":        2,
    "Salvager":                   1,
    "Progressive_Tanning":        2,
    "Progressive_Workbench":      3,
    # Random loot
    "Ore_Copper10":               3,
    "Ore_Iron10":                 3,
    "Ore_Cobalt10":               3,
    "Ore_Gold10":                 3,
    "Ore_Silver10":               3,
    "Ore_Thorium10":              3,
    "Ingredient_Hide_Soft10":     4,
    "Ingredient_Hide_Light10":    4,
    "Ingredient_Hide_Medium10":   4,
    "Ingredient_Hide_Heavy10":    4,
    "Ingredient_Chitin_Sturdy10": 4,
    "Flamethrower_Goblin":        1,
    "Teleporter3":                10,
    "Armor_Adamantite_Legs":      1,
    "Armor_Adamantite_Chest":     1,
    "Armor_Adamantite_Head":      1,
    "Armor_Adamantite_Hands":     1,
    "Weapon_Shield_Adamantite":   1,
    "Weapon_Axe_Cobalt":          1,
    "Weapon_Longsword_Copper":    1,
    "Weapon_Shortbow_Combat":     1,
    "Weapon_Daggers_Bone":        1,
    "Weapon_Arrow_Clearshot30":   10,
    "Weapon_Deployable_Healing_Totem": 2,
    "Repair_Kit2":                10,
    "Plant_Crop_Carrot_Item5":    2,
    "Plant_Seeds_Corn":           2,
    "Plant_Cactus_1":             2,
    "Ingredient_Charcoal10":      10,
    "Furniture_Village_Chest_Small2": 5,
    "Plant_Crop_Wheat_Block3":    10,
    "Food_Bread5":                10,
}

class HytaleItem(Item):
    game = "Hytale"

# Helpers

def get_filler_item_name(world: HytaleWorld) -> str:  # noqa: ARG001
    return "Ingredient_Poop"

def create_item_with_correct_classification(world: HytaleWorld, name: str) -> HytaleItem:
    return HytaleItem(
        name,
        DEFAULT_ITEM_CLASSIFICATIONS[name],
        ITEM_NAME_TO_ID[name],
        world.player,
    )

def create_all_items(world: HytaleWorld) -> None:
    itempool: list[HytaleItem] = []

    for item_name, quantity in ITEM_POOL_QUANTITIES.items():
        for _ in range(quantity):
            itempool.append(world.create_item(item_name))

    # Fill remaining slots with Ingredient_Poop
    needed_filler = (
        len(world.multiworld.get_unfilled_locations(world.player)) - len(itempool)
    )
    itempool += [world.create_filler() for _ in range(needed_filler)]

    world.multiworld.itempool += itempool
