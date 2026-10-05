package me.coblaz.achievements;

import me.coblaz.items.ItemReward;

import javax.annotation.Nonnull;
import java.util.List;

public final class AchievementDefinition {

    private final String          id;
    private final String          title;
    private final int             neededCount;
    private final boolean         multipleCollects;
    private final List<ItemReward> rewardItems;
    private final String          category;

    // Full constructor
    public AchievementDefinition(
            @Nonnull String           id,
            @Nonnull String           title,
            int                       neededCount,
            boolean                   multipleCollects,
            @Nonnull List<ItemReward> rewardItems,
            @Nonnull String           category
    ) {
        this.id               = id;
        this.title            = title;
        this.neededCount      = neededCount;
        this.multipleCollects = multipleCollects;
        this.rewardItems      = List.copyOf(rewardItems);
        this.category         = category;
    }

    // Short constructor: no reward items
    public AchievementDefinition(
            @Nonnull String id,
            @Nonnull String title,
            int neededCount,
            @Nonnull String category
    ) {
        this(id, title, neededCount, false, List.of(), category);
    }

    // Getters
    @Nonnull public String           getId()              { return id; }
    @Nonnull public String           getTitle()           { return title; }
    public   int                     getNeededCount()     { return neededCount; }
    public   boolean                 isMultipleCollects() { return multipleCollects; }
    @Nonnull public List<ItemReward> getRewardItems()     { return rewardItems; }
    @Nonnull public String           getCategory()        { return category; }
}