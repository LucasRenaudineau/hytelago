from collections.abc import Mapping
from typing import Any

from worlds.AutoWorld import World

from . import items, locations, regions, rules
from . import options as hytale_options
from .web_world import HytaleWebWorld

class HytaleWorld(World):
    """
    Hytale is an adventure/sandbox game.
    Collect crafting stations, raw materials, weapons, and gear to unlock
    increasingly dangerous hunts and crafting achievements.
    """

    game = "Hytale"
    web = HytaleWebWorld()

    options_dataclass = hytale_options.HytaleOptions
    options: hytale_options.HytaleOptions

    location_name_to_id = locations.LOCATION_NAME_TO_ID
    item_name_to_id = items.ITEM_NAME_TO_ID

    # All locations live in the "Hytale" region; the generator starts from "Menu".
    # origin_region_name defaults to "Menu".

    # Generation steps

    def create_regions(self) -> None:
        regions.create_regions(self)
        locations.create_all_locations(self)

    def set_rules(self) -> None:
        rules.set_all_rules(self)

    def create_items(self) -> None:
        items.create_all_items(self)

    # Item helpers

    def create_item(self, name: str) -> items.HytaleItem:
        return items.create_item_with_correct_classification(self, name)

    def get_filler_item_name(self) -> str:
        return items.get_filler_item_name(self)

    # Slot data

    def fill_slot_data(self) -> Mapping[str, Any]:
        # Sent to the Hytale mod on connection so it can build its location and
        # item tables to match this seed's options (and toggle Death Link).
        return {
            "death_link":      self.options.death_link.value,
            "traps":           self.options.traps.value,
            "death_locations": self.options.death_locations.value,
            "memories":        self.options.memories.value,
            "memories_max":    self.options.memories_max.value,
            "memories_every":  self.options.memories_every.value,
            "regions":         self.options.regions.value,
        }
