from __future__ import annotations

from typing import TYPE_CHECKING

from BaseClasses import Region

if TYPE_CHECKING:
    from .world import HytaleWorld


def create_regions(world: HytaleWorld) -> None:
    # All locations live in a single "Hytale" region.
    # Access logic is handled entirely by per-location rules (rules.py), not by region topology.
    menu = Region("Menu", world.player, world.multiworld)
    hytale = Region("Hytale", world.player, world.multiworld)

    world.multiworld.regions += [menu, hytale]

    # Unconditional entrance: the player can always reach the game world.
    menu.connect(hytale, "Start Game")
