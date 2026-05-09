package me.coblaz.achievements;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SmeltingAchievements {

    public record Entry(
            String itemId,
            String achievementId,
            String title,
            int needed
    ) {}

    // ── Master list ───────────────────────────────────────────────────────────
    // itemId   → exact string returned by itemStack.getItemId()
    // needed   → bars required; adjust freely

    public static final List<Entry> ALL = List.of(
            new Entry("Ingredient_Bar_Iron",    "smelt_iron",    "Iron Smith",    5),
            new Entry("Ingredient_Bar_Copper",  "smelt_copper",  "Copper Smith",  5),
            new Entry("Ingredient_Bar_Silver",  "smelt_silver",  "Silver Smith",  5),
            new Entry("Ingredient_Bar_Gold",    "smelt_gold",    "Gold Smith",    5),
            new Entry("Ingredient_Bar_Thorium", "smelt_thorium", "Thorium Smith", 5),
            new Entry("Ingredient_Bar_Cobalt",  "smelt_cobalt",  "Cobalt Smith",  5)
    );

    // ── Lookup map: itemId (lowercase) → achievementId ────────────────────────

    private static final Map<String, String> ITEM_TO_ACH_ID =
            ALL.stream().collect(Collectors.toMap(
                    e -> e.itemId().toLowerCase(),
                    Entry::achievementId
            ));

    // ── API ───────────────────────────────────────────────────────────────────

    /** Registers every entry into the given registry. Call once at startup. */
    public static void registerAll(@Nonnull AchievementRegistry reg) {
        for (Entry e : ALL) {
            reg.registerAchievement(new AchievementDefinition(
                    e.achievementId(), e.title(), e.needed()
            ));
        }
    }

    /**
     * Returns the achievement ID for the given item ID,
     * or null if that item has no associated achievement.
     */
    @Nullable
    public static String achievementIdForItem(@Nonnull String itemId) {
        return ITEM_TO_ACH_ID.get(itemId.toLowerCase());
    }

    private SmeltingAchievements() {}
}