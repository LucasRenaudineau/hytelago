# Hytelago mod

# Table of contents

- Description
- Download links
- In-game commands
- Set-up to play the mod
- Compiling the mod and contributing
- Items, locations, and rules logic

# Description

Hytelago is a Hytale mod developped for playing Archipelago, a multiworld randomizer.

# Download links

- hytelago.jar<br>
[Here on CurseForge](https://www.curseforge.com/hytale/mods/hytelago)

- Apworld<br>
(For the moment, put all files inside apworld inside a Hytale folder, zip it and rename it Hytale.apworld)

- .yaml<br>
(It is game_options.yaml)

- betterMap mod (recommended mod)<br>
[Here on curseforge](https://www.curseforge.com/hytale/mods/bettermap)

- InfiniteTeleporters (recommended mod)
[Here on curseforge](https://www.curseforge.com/hytale/mods/infinite-teleporters)

# In-game commands

/arch-help : lists the commands of Hytelago mod<br>
/arch-connect <ip address> <port> <player_name> : connects yourself to the archipelago server<br>
/arch-items : shows the items table<br>
/arch-locations : shows the locations table<br>
/arch-collect <achievementId> : force-collect an achievement (either from the item table or from the location table)<br>
/arch-set_count <count> <achievementId> : sets the count of an achievement to count<br>
/arch-set_state <achievementId> <NOT_DONE|DONE|COLLECTED> : changes the state of an achievement<br>
/arch-spawn <mobId> : spawns a mob on the player<br>

# Set-up to play the mod

Mod tested on Hytale version : 2026.03.26-89796E57B

Put the .jar of the last version in your Mods folder. Some mods are recommended : betterMap and InfiniteTeleporters.
*Note : Hytelago overrides the assets of almost all workbenches and of the backpack upgrades to change their crafts and tier upgrades, so a lot of mods won't be compatible with it.*
Send the Hytale.apworld to your archipelago server host.
In the .yaml, change the player name, and send it to the archipelago server host.

Once in game, do /op self and then you can list the commands with /arch-help
Once the server is hosted, you can connect using the arch-connect command.
!!! Always connect to the server when you join the world, as it won't send new locations you would have discovered while disconnected. !!!
See your advancement with /arch-locations and collect lost Workbench upgrader items with /arch-items
!!! The only (intentional) non-working location is the kill_frost_dragon location which is also the one to release the player.
When you find the frost dragon, manually type /arch-set_count kill_frost_dragon 1 or more directly /arch-collect kill_frost_dragon

For the moment, don't stack your inventory, since items given to you won't be given if your inventory is full.
You can safely quit the game, reconnect and the items other players sent you during this time will be sent.

# Compiling the mod and contributing

To compile the mod, you'll need the HytaleServer.jar library in the ./libs folder at the root of the project.
In case you would want to contribute, don't bother contact me on Discord as I may not be active on Github, my pseudo is Coblaz

# Items, locations, and rules logic

All items, locations and their IDs are listed in the file rules_apworld.md
In this file, you'll also find the rules soft logic, listed in human-readable english.
The current goal is killing the frost_dragon.
