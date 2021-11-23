# Nations And Villages

This work-in-progress Minecraft Spigot plugin adds in functionality for player-driven nations with added features to villagers

## Nations

* Create or join nations
* Claim land
* Declare other nations as enemies
* Manage ranks and permissions for citizens
  * Ranks: Leader, legate, member, non-member
  * Each rank can have customizable permissions
  * Permissions: modify blocks, open containers, attack entities, claim land, manage members, declare enemies, access config

## Villagers

### Merchants
* Used for player markers
* Hold up to a double chest's worth of items
* Player puts an item on sale by holding an item, using the sell command, and then clicking on a merchant
* Other players can buy the items through the merchant

### Lumberjack
* Chops down all nearby trees and replants them
* Supports all tree types
* Picks up all logs, sticks, saplings, and apples
* Items can be accessed by the player

### Guard
* Attacks hostile mobs as well as enemy players, villagers, and iron golems
* Can be equipped with any armor and sword, including enchantments
* Slowly heals over time
* Guard modes
  * Stationary (default)
    * Stays at a position unless fighting an enemy
    * If enemy goes outside the guard's range, it will return to its post
  * Wander
    * Walks around like a normal villager does
    * Attacks enemies it comes across
  * Bodyguard
    * Follows the player
    * Attacks any enemies near the player
    * If the enemy gets too far from the player, it will continue following
  * Follow
    * Follows the player
    * Does not attack enemies

##Commands

Format: command ( alias ) < required > [ optional ]
* money (balance, bal) - shows the player their balance
* sell \<price | cancel> - prepares an item to be sold through a merchant
* Nation commands - /nation \<subcommand>
  * autoclaim (ac) - toggles autoclaim for the player and automatically claims land for their nation when entering new chunks
  * autounclaim (auc) - toggles autounclaim for the player and automatically unclaims land from their nation when entering new chunks
  * claim (c) - claims the current chunk for the player's nation
  * config (settings) - opens a menu to manage settings for the player's nation
  * create \<name> - creates a new nation
  * demote \<username> - demotes a player in the nation
  * enemy \<nation> - declares or un-declares a nation as an enemy of the player's nation
  * exile \<username> - exiles a player from the nation
  * info [nation] - gives information about a nation
  * invite \<username> - invites a player to the nation if they are online
  * join \<nation> - joins a nation if the player is invited
  * leave \<nation> - leaves the nation the player is currently in
  * promote \<username> - promotes a player in the nation
  * rename \<name> - renames the nation
  * unclaim (uc) - unclaims the current chunk from the player's nation

## Videos
Note: These videos were made during development and may not reflect the current state of the plugin.

## Land Claiming

[![Land Claiming](http://img.youtube.com/vi/RQeRAsu24uM/0.jpg)](http://www.youtube.com/watch?v=RQeRAsu24uM "Land Claiming")

## Creating and Joining Nations

[![Creating and Joining Nations](http://img.youtube.com/vi/uNxb-nWiP5M/0.jpg)](http://www.youtube.com/watch?v=uNxb-nWiP5M "Creating and Joining Nations")

## Villager Jobs

### Merchant

[![Nations and Villages Plugin - Merchants](http://img.youtube.com/vi/cBXJ9Sj9XLo/0.jpg)](http://www.youtube.com/watch?v=cBXJ9Sj9XLo "Nations and Villages Plugin - Merchants")

### Lumberjack

[![Nations and Villages Plugin - Lumberjacks](http://img.youtube.com/vi/BknNogFoOiU/0.jpg)](http://www.youtube.com/watch?v=BknNogFoOiU "Nations and Villages Plugin - Lumberjacks")

### Guard

[![Nations and Villages Plugin - Guards](http://img.youtube.com/vi/ToemhkZ7dOU/0.jpg)](http://www.youtube.com/watch?v=ToemhkZ7dOU "Nations and Villages Plugin - Guards")

##Planned Features

* Farmer villager enhancement
* Miner villager
* Nation flags/banners
* GUI for nation and player info
* Nation bank