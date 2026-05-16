package me.coblaz.achievements;

import me.coblaz.items.ItemReward;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class LocationsPropertyAchievements {

    public record Entry(
            String          itemId,
            String          achievementId,
            String          title,
            int             needed,
            boolean          multipleCollects,
            List<ItemReward> rewardItems      // ← new; use List.of() for all current entries
    ) {}

    public static final List<Entry> ALL = List.of(
            new Entry("Tool_Watering_Can",        "collect_watering_can",       "Green Thumb",          1,  false, List.of()),
            new Entry("Ingredient_Voidheart",     "collect_voidheart",          "Heart of the Void",    1,  false, List.of()),
            new Entry("Ingredient_Fire_Essence",  "collect_fire_essence_5",     "Fire Starter",         5,  false, List.of()),
            new Entry("Ingredient_Ice_Essence",   "collect_ice_essence_50",     "Ice Collector",        50, false, List.of()),
            new Entry("Ingredient_Life_Essence",  "collect_life_essence_100",   "Life Gatherer",        100,false, List.of()),
            new Entry("Ingredient_Life_Essence",  "collect_life_essence_500",   "Life Hoarder",         500,false, List.of()),
            new Entry("Ingredient_Void_Essence",  "collect_void_essence_20",    "Void Touched",         20, false, List.of()),
            new Entry("Plant_Crop_Wheat_Item",    "collect_wheat_100",          "Wheat Farmer",         100,false, List.of()),
            new Entry("Container_Bucket",         "collect_bucket",             "Bucketeer",            1,  false, List.of()),
            new Entry("Tool_Fishing_Trap",        "collect_fishing_trap",       "Trapper",              1,  false, List.of()),
            new Entry("Deco_Kweebec_Plush",       "collect_kweebec_plush",      "Plush Collector",      1,  false, List.of()),
            new Entry("Deco_Tankard",             "collect_tankard",            "Cheers!",              1,  false, List.of()),
            new Entry("Survival_Trap_Spike_Wood", "collect_spike_trap_10",      "Spike Layer",          10, false, List.of()),
            new Entry("Plant_Crop_Corn_Item",     "collect_corn_5",             "Corn Harvest",         5,  false, List.of()),
            new Entry("Plant_Cactus_Flower",      "collect_cactus_5",           "Desert Bloom",         5,  false, List.of()),
            new Entry("Plant_Crop_Rice_Item",     "collect_rice_5",             "Rice Farmer",          5,  false, List.of()),
            new Entry("Food_Kebab_Vegetable",     "collect_food_kebab_vegetable_20", "Vegetable Lover", 20, false, List.of()),
            new Entry("Furniture_Crude_Torch",    "collect_furniture_crude_torch_10","Need light !",    10, false, List.of()),
            new Entry("Ingredient_Bar_Iron",      "smelt_iron",                 "Iron Smith",           5,  false, List.of()),
            new Entry("Ingredient_Bar_Copper",    "smelt_copper",               "Copper Smith",         5,  false, List.of()),
            new Entry("Ingredient_Bar_Silver",    "smelt_silver",               "Silver Smith",         5,  false, List.of()),
            new Entry("Ingredient_Bar_Gold",      "smelt_gold",                 "Gold Smith",           5,  false, List.of()),
            new Entry("Ingredient_Bar_Thorium",   "smelt_thorium",              "Thorium Smith",        5,  false, List.of()),
            new Entry("Ingredient_Bar_Cobalt",    "smelt_cobalt",               "Cobalt Smith",         5,  false, List.of()),
            new Entry("Armor_Copper_Head", "collect_armor_copper_head", "Copper Head", 1, false, List.of()),
            new Entry("Armor_Copper_Hands", "collect_armor_copper_hands", "Copper Hands", 1, false, List.of()),
            new Entry("Armor_Copper_Chest", "collect_armor_copper_chest", "Copper Chest", 1, false, List.of()),
            new Entry("Armor_Copper_Legs", "collect_armor_copper_legs", "Copper Legs", 1, false, List.of()),
            new Entry("Weapon_Mace_Copper", "collect_weapon_mace_copper", "Copper Mace", 1, false, List.of()),
            new Entry("Weapon_Longsword_Copper", "collect_weapon_longsword_copper", "Copper Longsword", 1, false, List.of()),
            new Entry("Armor_Iron_Head", "collect_armor_iron_head", "Iron Head", 1, false, List.of()),
            new Entry("Armor_Iron_Hands", "collect_armor_iron_hands", "Iron Hands", 1, false, List.of()),
            new Entry("Armor_Iron_Chest", "collect_armor_iron_chest", "Iron Chest", 1, false, List.of()),
            new Entry("Armor_Iron_Legs", "collect_armor_iron_legs", "Iron Legs", 1, false, List.of()),
            new Entry("Weapon_Mace_Iron", "collect_weapon_mace_iron", "Iron Mace", 1, false, List.of()),
            new Entry("Weapon_Longsword_Iron", "collect_weapon_longsword_iron", "Iron Longsword", 1, false, List.of())
    );

    // ── Lookup: itemId (lowercase) → list of achievementIds ──────────────────
    // Uses a list because one item can map to multiple achievements
    // (e.g. Life Essence at 100 AND 500)

    // ── Lookup: itemId (lowercase) → list of achievementIds ──────────────────
    private static final Map<String, List<String>> ITEM_TO_ACH_IDS =
            ALL.stream().collect(Collectors.groupingBy(
                    e -> e.itemId().toLowerCase(),
                    Collectors.mapping(Entry::achievementId, Collectors.toList())
            ));

    public static void registerAll(@Nonnull AchievementRegistry reg) {
        for (Entry e : ALL) {
            reg.registerAchievement(new AchievementDefinition(
                    e.achievementId(),
                    e.title(),
                    e.needed(),
                    e.multipleCollects(),            // multipleCollects — false for all current entries
                    e.rewardItems(),
                    "locations"           // ← group, drives which command shows them
            ));
        }
    }

    @Nonnull
    public static List<String> achievementIdsForItem(@Nonnull String itemId) {
        return ITEM_TO_ACH_IDS.getOrDefault(itemId.toLowerCase(), List.of());
    }

    private LocationsPropertyAchievements() {}
}