package me.coblaz.achievements;

import javax.annotation.Nonnull;

public final class AchievementDefinition {

    private final String id;
    private final String title;
    private final int neededCount;

    public AchievementDefinition(@Nonnull String id, @Nonnull String title, int neededCount) {
        this.id = id;
        this.title = title;
        this.neededCount = neededCount;
    }

    @Nonnull public String getId()       { return id; }
    @Nonnull public String getTitle()    { return title; }
    public   int    getNeededCount()     { return neededCount; }
}
