from dataclasses import dataclass

from Options import Choice, DeathLink, PerGameCommonOptions, Range, Toggle


class Goal(Choice):
    """
    reach_x_percent: collect the complete_x_percent location, done once
    percent_goal % of the other locations are collected.
    kill_gobliterator: kill the third phase of the Gobliterator. The
    complete_x_percent location is removed from the seed.

    Any other value falls back to reach_x_percent.
    """
    display_name = "Goal"
    option_reach_x_percent = 0
    option_kill_gobliterator = 1
    default = 0

    @classmethod
    def from_text(cls, text: str) -> "Goal":
        try:
            return super().from_text(text)
        except KeyError:
            return cls(cls.default)


class PercentGoal(Range):
    """
    Percentage of the other locations that must be done to complete the goal
    location (complete_x_percent), which releases the player.
    Only used when the goal is reach_x_percent.
    """
    display_name = "Percent Goal"
    range_start = 1
    range_end = 100
    default = 80


class Traps(Toggle):
    """
    Add monster-spawn trap items to the item pool.

    When enabled, traps are filled up to their maximum quantity (in addition to
    the single copy seeded by Death Link, if that is also enabled).
    """
    display_name = "Traps"
    default = False


class DeathLocations(Toggle):
    """
    Add the death-achievement locations (dying in specific ways: falling,
    drowning, fire, projectiles) to the pool.
    """
    display_name = "Death Locations"
    default = False


class Memories(Toggle):
    """
    Add the memory-collection locations (memories_1 ... memories_150) to the
    pool. Use "Maximum Memories" and "Memories Every" to control how many.
    """
    display_name = "Memories"
    default = True


class MemoriesMax(Range):
    """
    Highest memory milestone (memories_N) that can become a location.
    Only takes effect when Memories is enabled.
    """
    display_name = "Maximum Memories"
    range_start = 1
    range_end = 150
    default = 150


class MemoriesEvery(Range):
    """
    Step between included memory milestones. For example a value of 2 includes
    memories_1, memories_3, memories_5, ... Only takes effect when Memories is
    enabled.
    """
    display_name = "Memories Every"
    range_start = 1
    range_end = 150
    default = 1


class Regions(Toggle):
    """
    Add the region-exploration locations (visiting each of the 12 regions) to
    the pool.
    """
    display_name = "Regions"
    default = True


@dataclass
class HytaleOptions(PerGameCommonOptions):
    goal: Goal
    percent_goal: PercentGoal
    death_link: DeathLink
    traps: Traps
    death_locations: DeathLocations
    memories: Memories
    memories_max: MemoriesMax
    memories_every: MemoriesEvery
    regions: Regions
