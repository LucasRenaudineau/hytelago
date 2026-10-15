package me.coblaz.achievements;

import java.util.List;

/** Categories of the achievements, used as tabs in the locations table. */
public final class Category {

    public static final String ALL              = "All";
    public static final String GOAL             = "Goal";
    public static final String MONSTER_KILL     = "Monster kill";
    public static final String GENERAL_KILLS    = "General kills";
    public static final String DEATH            = "Death";
    public static final String AGRICULTURE      = "Agriculture";
    public static final String MATERIALS        = "Materials";
    public static final String COMBAT_EQUIPMENT = "Combat equipment";
    public static final String MEMORIES         = "Memories";
    public static final String REGIONS          = "Regions";
    public static final String REPAIR           = "Repair";
    public static final String ITEMS            = "Items";

    /** Tabs of the locations table, in display order. Must match the #Tab buttons of AchievementListPage.ui. */
    public static final List<String> LOCATION_TABS = List.of(
            ALL, GOAL, MONSTER_KILL, GENERAL_KILLS, DEATH,
            AGRICULTURE, MATERIALS, COMBAT_EQUIPMENT, MEMORIES, REGIONS, REPAIR
    );

    private Category() {}
}
