# Reincarceration

Reincarceration is a Minecraft plugin that introduces a unique reoffender system with challenges and ranks, allowing players to experience new progression on your server.

## Features

- Experience the prison with fun and exciting new challenges!
- Custom modifiers that change gameplay dynamics
- Economy integration with Vault
- Rank progression with configurable requirements
- Easily Setup New Modifiers
- Permission management with LuckPerms
- Configurable settings for easy server customization
- Support for Custom Completion Tags
- Works to restrict access to PlayerVaults and ensure players don't lose their items!
- Custom Currency storage while players complete system
- Restrictions and increasing support for preventing players from cheating!

## Installation

1. Ensure you have the following dependencies installed on your server:
    - [Vault](https://www.spigotmc.org/resources/vault.34315/)
    - [LuckPerms](https://luckperms.net/)
    - [PlayerVaults](https://github.com/drtshock/PlayerVaults)
    - [EconomyShopGUI](https://www.spigotmc.org/resources/economyshopgui.69927/)
    - [CustomTags](https://github.com/KillionRevival/CustomTags)
2. Insert your Completion tags into CustomTags using the following id format: (reincarnation_1, reincarceration_2...)
3. Add 'PublicBukkitValues::reincarceration:flagged' to sold-items-ignored-NBTtags in EconomyShopGUI's config.yml
4. Download the latest Reincarceration.jar from the releases page.
5. Place the jar file in your server's `plugins` folder.
6. Restart your server or load the plugin.

## Configuration

After first run, a `config.yml` file will be generated in the `plugins/Reincarceration` folder. You can customize various aspects of the plugin here.

## Commands

- `/rc` - Open the Reincarceration GUI
- `/flagitem` - Adds a reincarceration flag to the item in your hand (admin)
- `/inspectitem` - Inspects the item in your hand for reincarceration flag and provides item details (admin)
- `/inspectinventory <player>` - Inspects a player's inventory for flagged or unflagged items (admin)
- `/viewplayerdata <player>` - View all database data related to a player (admin)

## Permissions

- `reincarceration.use` - Allows use of basic Reincarceration commands (default: true)
- `reincarceration.gui` - Allows access to the Reincarceration GUI (default: true)
- `reincarceration.startcycle` - Allows entering the reincarceration system (default: false)
- `reincarceration.rankup` - Allows players to rank up (default: false)
- `reincarceration.completecycle` - Allows players to complete cycles (default: false)
- `reincarceration.quitcycle` - Allows players to quit cycles (default: false)
- `reincarceration.listmodifiers` - Allows viewing all active modifiers (default: true)
- `reincarceration.viewonlineplayers` - Allows viewing stats and information of online players via GUI (default: true)
- `reincarceration.admin` - Allows use of Reincarceration admin commands (default: op)
- `reincarceration.admin.flagitem` - Allows adding reincarceration flag to items (default: op)
- `reincarceration.admin.inspectitem` - Allows inspecting items for reincarceration flags (default: op)
- `reincarceration.admin.inspectinventory` - Allows inspecting player inventories for flagged/unflagged items (default: op)
- `reincarceration.admin.viewplayerdata` - Allows viewing all database data for a player (default: op)

## License

This project is licensed under the Apache 2.0 License - see the LICENSE file for details.