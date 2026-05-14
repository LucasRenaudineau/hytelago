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
            new Entry("Workbench_Tier_Upgrader1", "workbench_tier_upgrader1", "Workbench Upgrader", 1, true,
                    List.of(new ItemReward("Workbench_Tier_Upgrader1", 1))),
            new Entry("Poop_Ingredient", "poop_ingredient", "Poop Collector", 1, false,
                    List.of(new ItemReward("Poop_Ingredient", 1)))
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