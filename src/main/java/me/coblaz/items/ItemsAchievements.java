package me.coblaz.items;

import me.coblaz.achievements.AchievementDefinition;
import me.coblaz.achievements.AchievementRegistry;
import me.coblaz.items.ItemReward;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ItemsAchievements {

    public record Entry(
            String           itemId,
            String           achievementId,
            String           title,
            int              needed,
            boolean          multipleCollects,
            List<ItemReward> rewardItems
    ) {}

    public static final List<Entry> ALL = List.of(
            new Entry("Campfire", "campfire_tier_upgrader1", "Campfire Upgrader 1", 1, true,
                    List.of(new ItemReward("Campfire_Tier_Upgrader1", 1))),

            new Entry("Progressive_Armorer", "progressive_armorer1", "Armorer Upgrader 1", 1, true,
                    List.of(new ItemReward("Armorer_Workbench_Tier_Upgrader1", 1))),
            new Entry("Progressive_Armorer", "progressive_armorer2", "Armorer Upgrader 2", 2, true,
                    List.of(new ItemReward("Armorer_Workbench_Tier_Upgrader2", 1))),

            new Entry("Progressive_Backpack1", "progressive_backpack1", "Backpack Upgrader 1", 1, true,
                    List.of(new ItemReward("Backpack_Tier_Upgrader1", 1))),
            new Entry("Progressive_Backpack", "progressive_backpack2", "Backpack Upgrader 2", 2, true,
                    List.of(new ItemReward("Backpack_Tier_Upgrader2", 1))),
            new Entry("Progressive_Backpack", "progressive_backpack3", "Backpack Upgrader 3", 3, true,
                    List.of(new ItemReward("Backpack_Tier_Upgrader3", 1))),

            new Entry("Progressive_Blacksmith", "progressive_blacksmith1", "Blacksmith Upgrader 1", 1, true,
                    List.of(new ItemReward("Blacksmith_Anvil_Tier_Upgrader1", 1))),
            new Entry("Progressive_Blacksmith", "progressive_blacksmith2", "Blacksmith Upgrader 2", 2, true,
                    List.of(new ItemReward("Blacksmith_Anvil_Tier_Upgrader2", 1))),

            new Entry("Campfire", "campfire", "Campfire", 1, true,
                    List.of(new ItemReward("Campfire_Tier_Upgrader1", 1))),

            new Entry("Chef_Stove", "chef_stove", "Chef Stove", 1, true,
                    List.of(new ItemReward("Chef_Stove_Tier_Upgrader1", 1))),

            new Entry("Progressive_Farmer", "progressive_farmer1", "Farmer Upgrader 1", 1, true,
                    List.of(new ItemReward("Farmer_Workbench_Tier_Upgrader1", 1))),
            new Entry("Progressive_Farmer", "progressive_farmer2", "Farmer Upgrader 2", 2, true,
                    List.of(new ItemReward("Farmer_Workbench_Tier_Upgrader2", 1))),
            new Entry("Progressive_Farmer", "progressive_farmer3", "Farmer Upgrader 3", 3, true,
                    List.of(new ItemReward("Farmer_Workbench_Tier_Upgrader3", 1))),

            new Entry("Progressive_Furnace", "progressive_furnace1", "Furnace Upgrader 1", 1, true,
                    List.of(new ItemReward("Furnace_Tier_Upgrader1", 1))),
            new Entry("Progressive_Furnace", "progressive_furnace2", "Furnace Upgrader 2", 2, true,
                    List.of(new ItemReward("Furnace_Tier_Upgrader2", 1))),

            new Entry("Salvager", "salvager", "Salvager", 1, true,
                    List.of(new ItemReward("Salvager_Workbench_Tier_Upgrader1", 1))),

            new Entry("Progressive_Tanning", "progressive_tanning1", "Tanning Upgrader 1", 1, true,
                    List.of(new ItemReward("Tanning_Rack_Tier_Upgrader1", 1))),
            new Entry("Progressive_Tanning", "progressive_tanning2", "Tanning Upgrader 2", 2, true,
                    List.of(new ItemReward("Tanning_Rack_Tier_Upgrader2", 1))),

            new Entry("Progressive_Workbench", "progressive_workbench1", "Workbench Upgrader 1", 1, true,
                    List.of(new ItemReward("Workbench_Tier_Upgrader1", 1))),
            new Entry("Progressive_Workbench", "progressive_workbench2", "Workbench Upgrader 2", 2, true,
                    List.of(new ItemReward("Workbench_Tier_Upgrader2", 1))),
            new Entry("Progressive_Workbench", "progressive_workbench3", "Workbench Upgrader 3", 3, true,
                    List.of(new ItemReward("Workbench_Tier_Upgrader3", 1)))
    );

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
                    e.multipleCollects(),
                    e.rewardItems(),
                    "items"
            ));
        }
    }

    @Nonnull
    public static List<String> achievementIdsForItem(@Nonnull String itemId) {
        return ITEM_TO_ACH_IDS.getOrDefault(itemId.toLowerCase(), List.of());
    }

    private ItemsAchievements() {}
}