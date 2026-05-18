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
            new Entry("Workbench_Tier_Upgrader1", "workbench_tier_upgrader1", "Workbench Upgrader 1", 1, true,
                    List.of(new ItemReward("Workbench_Tier_Upgrader1", 1))),
            new Entry("Workbench_Tier_Upgrader2", "workbench_tier_upgrader2", "Workbench Upgrader 2", 2, true,
                    List.of(new ItemReward("Workbench_Tier_Upgrader2", 1))),
            new Entry("Workbench_Tier_Upgrader3", "workbench_tier_upgrader3", "Workbench Upgrader 3", 3, true,
                    List.of(new ItemReward("Workbench_Tier_Upgrader3", 1))),
            new Entry("Campfire_Tier_Upgrader1", "campfire_tier_upgrader1", "Campfire Upgrader 1", 1, true,
                    List.of(new ItemReward("Campfire_Tier_Upgrader1", 1))),

            new Entry("Progressive_Armorer1", "progressive_armorer1", "Armorer Upgrader 1", 1, true,
                    List.of(new ItemReward("Progressive_Armorer1", 1))),
            new Entry("Progressive_Armorer2", "progressive_armorer2", "Armorer Upgrader 2", 2, true,
                    List.of(new ItemReward("Progressive_Armorer2", 1))),

            new Entry("Progressive_Backpack1", "progressive_backpack1", "Backpack Upgrader 1", 1, true,
                    List.of(new ItemReward("Progressive_Backpack1", 1))),
            new Entry("Progressive_Backpack2", "progressive_backpack2", "Backpack Upgrader 2", 2, true,
                    List.of(new ItemReward("Progressive_Backpack2", 1))),
            new Entry("Progressive_Backpack3", "progressive_backpack3", "Backpack Upgrader 3", 3, true,
                    List.of(new ItemReward("Progressive_Backpack3", 1))),

            new Entry("Progressive_Blacksmith1", "progressive_blacksmith1", "Blacksmith Upgrader 1", 1, true,
                    List.of(new ItemReward("Progressive_Blacksmith1", 1))),
            new Entry("Progressive_Blacksmith2", "progressive_blacksmith2", "Blacksmith Upgrader 2", 2, true,
                    List.of(new ItemReward("Progressive_Blacksmith2", 1))),

            new Entry("Campfire", "campfire", "Campfire", 1, true,
                    List.of(new ItemReward("Campfire", 1))),

            new Entry("Chef_Stove", "chef_stove", "Chef Stove", 1, true,
                    List.of(new ItemReward("Chef_Stove", 1))),

            new Entry("Progressive_Farmer1", "progressive_farmer1", "Farmer Upgrader 1", 1, true,
                    List.of(new ItemReward("Progressive_Farmer1", 1))),
            new Entry("Progressive_Farmer2", "progressive_farmer2", "Farmer Upgrader 2", 2, true,
                    List.of(new ItemReward("Progressive_Farmer2", 1))),

            new Entry("Progressive_Furnace1", "progressive_furnace1", "Furnace Upgrader 1", 1, true,
                    List.of(new ItemReward("Progressive_Furnace1", 1))),
            new Entry("Progressive_Furnace2", "progressive_furnace2", "Furnace Upgrader 2", 2, true,
                    List.of(new ItemReward("Progressive_Furnace2", 1))),

            new Entry("Salvager", "salvager", "Salvager", 1, true,
                    List.of(new ItemReward("Salvager", 1))),

            new Entry("Progressive_Tanning1", "progressive_tanning1", "Tanning Upgrader 1", 1, true,
                    List.of(new ItemReward("Progressive_Tanning1", 1))),
            new Entry("Progressive_Tanning2", "progressive_tanning2", "Tanning Upgrader 2", 2, true,
                    List.of(new ItemReward("Progressive_Tanning2", 1))),

            new Entry("Progressive_Workbench1", "progressive_workbench1", "Workbench Upgrader 1", 1, true,
                    List.of(new ItemReward("Progressive_Workbench1", 1))),
            new Entry("Progressive_Workbench2", "progressive_workbench2", "Workbench Upgrader 2", 2, true,
                    List.of(new ItemReward("Progressive_Workbench2", 1))),
            new Entry("Progressive_Workbench3", "progressive_workbench3", "Workbench Upgrader 3", 3, true,
                    List.of(new ItemReward("Progressive_Workbench3", 1)))
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