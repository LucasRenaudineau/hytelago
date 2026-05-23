package me.coblaz.achievements;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DeathAchievements {

    public record Entry(
            String causeId,       // matches DamageCause.getId() or EnvironmentSource.getType()
            String achievementId,
            String title,
            int    needed
    ) {}

    public static final List<Entry> ALL = List.of(
            new Entry("fall",         "death_fall",         "Gravity Check",           1),
            new Entry("drowning",     "death_drowning",     "Taking a Deep Breath",    1),
            new Entry("fire",         "death_fire",         "Crispy",                  1),
            new Entry("projectile",   "death_projectile",   "Porcupine",               5)
    );

    private static final Map<String, String> CAUSE_TO_ACH_ID =
            ALL.stream().collect(Collectors.toMap(
                    e -> e.causeId().toLowerCase(),
                    Entry::achievementId
            ));

    public static void registerAll(@Nonnull AchievementRegistry reg) {
        for (Entry e : ALL) {
            reg.registerAchievement(new AchievementDefinition(
                    e.achievementId(), e.title(), e.needed()
            ));
        }
    }

    @Nullable
    public static String achievementIdForCause(@Nonnull String causeId) {
        return CAUSE_TO_ACH_ID.get(causeId.toLowerCase());
    }

    private DeathAchievements() {}
}