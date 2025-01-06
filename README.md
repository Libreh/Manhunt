# Manhunt
A server-side Manhunt mod for Fabric.

```txt
⚠️⚠️⚠️ WARNING ⚠️⚠️⚠️
This mod DELETES THE WORLD on startup and shutdown!
```

## Commands (and permissions):
- `/preferences` - Opens the Preferences GUI (`manhunt.preferences`, available by default)
- `/config` - Opens the Config GUI (`manhunt.config.show`, available by default but requires `manhunt.config.modify` to modify)
- `/duration` - Displays the duration (`manhunt.duration`, only works once a round has started)
- `/coords` - Sends coordinates to team chat (`manhunt.coords`, available by default)
- `/listcoords` - List all send team coordinates (`manhunt.coords.list`, available by default)
- `/manhunt relaod` - Reloads configuration (requires `manhunt.reload`)
- `/runner <player>` - Assign runner to player (requires `manhunt.runner`)
- `/onerunner <player>` - Player is only runner everyone else is hunter (requires `manhunt.onerunner`)
- `/hunter <player>` - Assign hunter to player (requires `manhunt.hunter`)
- `/onerunner <player>` - Player is only hunter everyone else is runner (requires `manhunt.onehunter`)
- `/feed <players>` - Resets specified players' saturation and hunger (requires `manhunt.feed`)
- `/start` - Starts round by preloading the terrain (requires `manhunt.start`)
- `/reset` - Resets the world and teleports everyone back to lobby (requires `manhunt.reset`)
- `/pause` - Freezes everything and makes all players blind (requires `manhunt.pause`)
- `/unpause` - Unfreezes everything and unblinds all players (requires `manhunt.unpause`)

## Configuration
```json5
{
  "game_options": {
    // Preloads the terrain before start
    "preload_distance": 5,
    // Distributes players based on the option
    "preset_mode": "free_select",
    "team_color": {
      // Shows specified team color or white
      "enabled": true,
      "hunters_color": "RED",
      "runners_color": "GREEN"
    },
    // In seconds until the hunters aren't frozen from start
    "head_start": 0,
    // In minutes until the hunters win
    "time_limit": 180
  },
  "global_preferences": {
    // Plays when a round starts and ends
    "custom_sounds": "per_player",
    // Is displayed when a round starts and ends
    "custom_titles": "per_player",
    // Can damage teammates
    "friendly_fire": "per_player",
    // Disabled bed placement if enemy is within 9 blocks
    "bed_explosions_pvp": "runners_preference",
    // Disabled emptying lava bucket in nether if enemy is within 9 blocks
    "nether_lava_pvp": "runners_preference",
    // Displays the world seed at the end of the round
    "announce_seed": "per_player",
    // Displays the duration at the end of the round
    "announce_duration": "per_player"
  },
  "mod_integrations": {
    "vanilla_integration": {
      // Vanilla settings and gamerules
      "enabled": true,
      "difficulty": "NORMAL",
      "border_size": 59999968,
      "spawn_radius": 10,
      "spectators_generate_chunks": false
    }
  },
  // Is applied on start and shutdown
  "files_to_reset": [
    "advancements",
    "data/idcounts.dat",
    "data/map_*.dat",
    "data/raids.dat",
    "data/random_sequences.dat",
    "data/scoreboard.dat",
    "DIM*",
    "dimension",
    "entities",
    "playerdat",
    "poi",
    "region",
    "stats",
    "level.dat",
    "level.dat_old"
  ]
}
```

## Credits
Parts of this mod are inspired by and uses code by:
- [Ivan-Khar/manhunt-fabricated](https://github.com/Ivan-Khar/manhunt-fabricated)
- [anhgelus/manhunt-mod](https://github.com/anhgelus/manhunt-mod)
- [horrific-tweaks/bingo](https://gitlab.com/horrific-tweaks/bingo) 