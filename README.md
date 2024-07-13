# Reincarceration

Reincarceration is a Minecraft plugin that introduces a unique prestige system with modifiers, allowing players to experience new challenges and progression on your server.

## Features

- Prestige cycle system with configurable ranks
- Custom modifiers that change gameplay dynamics
- Economy integration with Vault
- Permission management with LuckPerms
- Configurable settings for easy server customization

## Installation

1. Ensure you have [Vault](https://www.spigotmc.org/resources/vault.34315/) and [LuckPerms](https://luckperms.net/) installed on your server.
2. Download the latest Reincarceration.jar from the releases page.
3. Place the jar file in your server's `plugins` folder.
4. Restart your server or load the plugin.

## Configuration

After first run, a `config.yml` file will be generated in the `plugins/Reincarceration` folder. You can customize various aspects of the plugin here, including:

- Economy settings
- Rank configurations
- Modifier settings
- Permission group names

## Commands

- `/reoffender` - View your current prestige status
- `/startcycle <modifier>` - Start a new prestige cycle with a specific modifier
- `/completecycle` - Complete your current prestige cycle
- `/rankup` - Rank up within your current cycle
- `/listmodifiers` - List off available modifiers

## Permissions

- `reincarceration.use` - Allows use of basic plugin commands
- `reincarceration.startcycle` - Allows starting a new cycle
- `reincarceration.completecycle` - Allows completing a cycle
- `reincarceration.rankup` - Allows ranking up
- `reincarceration.listmodifiers` - Allows listing of modifiers
- `reincarceration.admin` - not yet developed out but allows admin controls

## License

This project is licensed under the Apache 2.0 License - see the LICENSE file for details.
