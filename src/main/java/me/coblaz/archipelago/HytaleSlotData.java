package me.coblaz.archipelago;

/**
 * Slot data sent by the Hytale apworld in {@code fill_slot_data()}.
 *
 * The Archipelago Toggle/Range options serialise to integers, so booleans
 * arrive as 0/1. The mod uses these values, received once on connection, to
 * build its location and item tables to match the seed the player generated.
 *
 * @see ArchipelagoLocationMap#buildActiveLocationIds(HytaleSlotData)
 */
public final class HytaleSlotData {

    /** Bounce deaths between worlds (and seed one of each trap in the pool). */
    public int death_link;

    /** Monster-spawn trap items are present in the pool. */
    public int traps;

    /** The "die in a specific way" locations are part of the seed. */
    public int death_locations;

    /** The memories_N locations are part of the seed. */
    public int memories = 1;

    /** Highest memory milestone (memories_N) that is a location. */
    public int memories_max = 150;

    /** Step between included memory milestones (2 -> memories_1, 3, 5, ...). */
    public int memories_every = 1;

    /** The region-exploration locations are part of the seed. */
    public int regions = 1;

    public boolean hasDeathLink()      { return death_link != 0; }
    public boolean hasTraps()          { return traps != 0; }
    public boolean hasDeathLocations() { return death_locations != 0; }
    public boolean hasMemories()       { return memories != 0; }
    public boolean hasRegions()        { return regions != 0; }
}
