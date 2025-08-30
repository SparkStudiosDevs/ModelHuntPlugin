# ModelHunt Plugin

An advanced Minecraft Paper plugin that creates an interactive model hunting experience using ModelEngine and MythicMobs integration.

## Features

- **Model Hunting System**: Players can click on custom 3D models to receive rewards
- **ModelEngine Integration**: Seamless integration with ModelEngine for custom 3D models
- **MythicMobs Integration**: Uses MythicMobs for entity management and advanced behaviors
- **Configurable Locations**: Admins can set up hunt locations anywhere in the world
- **Flexible Reward System**: Support for items, experience, money, and custom commands
- **Permission System**: Granular permissions for players and administrators
- **Cooldown Management**: Prevents spam clicking with configurable cooldowns
- **Statistics Tracking**: Track player hunting progress and statistics

## Dependencies

- **Paper 1.21.4** (or compatible version)
- **ModelEngine 4.0+** (Required for 3D models)
- **MythicMobs 5.6+** (Required for entity management)
- **PlaceholderAPI** (Optional - for dynamic placeholders)
- **Vault** (Optional - for economy integration)

## Installation

1. Download and install the required dependencies (ModelEngine and MythicMobs)
2. Place the ModelHunt plugin JAR file in your `plugins` folder
3. Restart your server
4. Configure hunt locations in `config.yml`
5. Set up your ModelEngine models and MythicMobs configurations

## PlaceholderAPI Integration

The plugin provides extensive PlaceholderAPI support for dynamic content:

### Available Placeholders
- `%modelhunt_total_hunts%` - Total hunts completed by player
- `%modelhunt_total_rewards%` - Total rewards received by player
- `%modelhunt_session_clicks%` - Clicks in current session
- `%modelhunt_first_hunt_time%` - Time since first hunt
- `%modelhunt_last_hunt_time%` - Time since last hunt
- `%modelhunt_active_locations%` - Number of active hunt locations
- `%modelhunt_total_locations%` - Total configured hunt locations
- `%modelhunt_spawned_models%` - Currently spawned models
- `%modelhunt_hunt_<location_id>%` - Completions for specific hunt
- `%modelhunt_cooldown_<location_id>%` - Cooldown time for specific hunt
- `%modelhunt_last_<location_id>%` - Time since last completion of specific hunt

### Usage Examples
```yaml
click-message: "§a{player} found a model! (Hunt #%modelhunt_total_hunts%)"
completion-message: "§6Congratulations! You've completed %modelhunt_hunt_easter_egg% Easter hunts!"
commands:
  - "tellraw @a {\"text\":\"{player} completed a hunt! Total: %modelhunt_total_hunts%\",\"color\":\"green\"}"
```

## Permissions

### Player Permissions (Default: Enabled)
- `hunt.click` - Allows clicking on hunt models
- `hunt.view` - Allows viewing hunt information
- `hunt.stats` - Allows viewing personal hunt statistics

### Admin Permissions (Default: OP only)
- `hunt.admin.use` - Access to admin commands
- `hunt.admin.create` - Create new hunt locations
- `hunt.admin.delete` - Delete hunt locations
- `hunt.admin.edit` - Edit hunt locations
- `hunt.admin.reload` - Reload plugin configuration
- `hunt.admin.list` - List all hunt locations
- `hunt.admin.teleport` - Teleport to hunt locations

## Commands

### Player Commands
- `/hunt stats [player]` - View hunt statistics

### Admin Commands
- `/hunt create <id> <model-id> <mythic-mob-type> [rewards...]` - Create a new hunt location
- `/hunt delete <id>` - Delete a hunt location
- `/hunt list` - List all hunt locations
- `/hunt reload` - Reload plugin configuration
- `/hunt tp <id>` - Teleport to a hunt location
- `/hunt toggle <id>` - Toggle a hunt location's active state

## Configuration

### Basic Settings
```yaml
cooldown-seconds: 300  # Cooldown between clicks (5 minutes)
debug: false  # Enable debug logging
```

### Hunt Locations
Each hunt location supports:
- **Location**: World coordinates and rotation
- **Model ID**: ModelEngine model identifier
- **MythicMob Type**: MythicMobs entity type
- **Click/Completion Messages**: Custom messages for interactions
- **Rewards**: Items, experience, money
- **Commands**: Custom commands to execute
- **Respawn Time**: How long before the model respawns
- **Click Radius**: Maximum distance for successful clicks

### Reward Types
- `item:MATERIAL*AMOUNT` - Give items (e.g., `item:DIAMOND*5`)
- `exp:AMOUNT` - Give experience points (e.g., `exp:100`)
- `money:AMOUNT` - Give money via economy plugin (e.g., `money:50.0`)
- `command:COMMAND` - Execute custom command (e.g., `command:give {player} special_item`)

## Usage Example

1. Create ModelEngine models for your hunt objects
2. Set up MythicMobs configurations for the entities
3. Use `/hunt create easter_egg my_egg_model EasterBunny item:DIAMOND*3 exp:100` to create a hunt
4. Players can find and click the model to receive rewards
5. The model will respawn after the configured time

## Integration Notes

- Models are spawned using MythicMobs for entity management
- ModelEngine handles the 3D model rendering
- The plugin detects clicks on ModelEngine entities
- Supports all standard MythicMobs features (AI, skills, etc.)

## Support

For support, feature requests, or bug reports, please visit our GitHub repository or contact the development team.