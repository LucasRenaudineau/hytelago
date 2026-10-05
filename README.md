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

- Apworld (To generate it by yourself, zip the ./apworld/hytale folder and rename the output hytale.apworld) <br>
[Here in the github releases](https://github.com/LucasRenaudineau/hytelago/releases)

- .yaml (it is ./game_options.yaml)
[Here in the github releases](https://github.com/LucasRenaudineau/hytelago/releases)

- betterMap mod (recommended mod)<br>
[Here on curseforge](https://www.curseforge.com/hytale/mods/bettermap)

- InfiniteTeleporters (recommended mod)
[Here on curseforge](https://www.curseforge.com/hytale/mods/infinite-teleporters)

# In-game commands

/arch-help : lists the commands of Hytelago mod<br>
/arch-connect <ip address> <port> <player_name> [--password=<password>]: connects yourself to the archipelago server<br>
/arch-items : shows the items table<br>
/arch-locations : shows the locations table<br>
/arch-collect <achievementId> : force-collect an achievement (either from the item table or from the location table)<br>
/arch-set_count <count> <achievementId> : sets the count of an achievement to count<br>
/arch-set_state <achievementId> <NOT_DONE|DONE|COLLECTED> : changes the state of an achievement<br>
/arch-collect_first <n:int> : sets the first n locations of the table (goal excluded) to DONE, for testing<br>
/arch-spawn <mobId> : spawns a mob on the player<br>
/arch-death_link <on|off> : activate or deactivate death link<br>

# Set-up to play the mod

Mod tested on Hytale version : 0.6.8

Put the .jar of the last version in your Mods folder. Some mods are recommended : betterMap and InfiniteTeleporters.<br>
*Note : Hytelago overrides the assets of almost all workbenches and of the backpack upgrades to change their crafts and tier upgrades, so a lot of mods won't be compatible with it.*
Send the hytale.apworld to your archipelago server host.
In the .yaml, change the player name, and send it to the archipelago server host.
*Note : If you don't know how to self host a Archipelago server, read [this guide](https://archipelago.gg/tutorial/Archipelago/setup_en).*<br>

Once in game, do /op self and then you can list the commands with /arch-help<br>
Once the server is hosted, you can connect using the arch-connect command.<br>
!!! Always connect to the server when you join the world, as it won't send new locations you would have discovered while disconnected. !!!<br>
See your advancement with /arch-locations and collect lost Workbench upgrader items with /arch-items<br>
The goal location complete_x_percent (top of /arch-locations) counts your done locations. Once percent_goal % of the other locations (set in the .yaml) are done, collect it to complete your goal and release.

You can safely quit the game, reconnect and the items other players sent you during this time will be sent. You only need to reconnect to archipelago every time you reload your world.

# Compiling the mod and contributing/contact

To compile the mod, you'll need the HytaleServer.jar library in the ./libs folder at the root of the project.
In case you would want to contribute, don't bother contact me on Discord as I may not be active on Github, my pseudo is Coblaz.
The Archipelago discord server has a hytale post in future-game-design.

# Items, locations, and rules logic

All items, locations and their IDs are listed in the file rules_apworld.md
In this file, you'll also find the rules soft logic, listed in human-readable english.
The goal is the complete_x_percent location: complete percent_goal % of the other locations (rounded down), then collect it.
