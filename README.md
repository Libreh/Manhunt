# Manhunt
This is a multiplayer Manhunt (Speedrunner vs Hunter) mod + lobby for the dedicated server.
- When installed on a server, players do not need any mods or resource packs to join.

```txt
⚠️⚠️⚠️ WARNING ⚠️⚠️⚠️
This mod DELETES THE WORLD on startup and shutdown!
```

### Preset Modes:
- **Free Select**: Everyone chooses their own role however they want.
- **Equal Split**: Players are equally split as runners and hunters.
- **Runner Cycle**: One runner versus all hunters, a new runner is selected after the rounds ends.
- **Hunter Infection**: One hunter versus all runners, each time a runner dies they become a hunter.
- **No Selection**: No roles are selected and only a permissioned player can choose their role.

## Configuration
No initial setup is needed to use the mod; it should run out of the box. 
The config file lives in
`./config/manhunt.json`.

Global commands:
- `/preferences` opens the preferences GUI
- `/config` opens the config GUI (needs permission to modify)
- `/coords` prints the current player coordinates in the team chat
- `/listcoords` displays a list of the team's previous "/coords" messages
- `/duration` prints current round duration

Manhunt commands (requires permission):
- `/manhunt reload` reloads the config
- `/runner <player>` sets player to runner
- `/onerunner <player>` sets player to runner and everyone else to hunter
- `/hunter <player>` sets player to hunter
- `/onehunter <player>` sets player to hunter and everyone else to runner
- `/feed <players>` resets specified players' saturation and hunger
- `/start` begins a new round by preloading the terrain and teleporting players
- `/reset` resets the world and teleports everyone back to the lobby
- `/pause` freezese everything, and makes all players blind
- `/unpause` unfreezes everything, and unblinds players

## Credits
Parts of this mod are inspired by and use code by:
- [Ivan-Khar/manhunt-fabricated](https://github.com/Ivan-Khar/manhunt-fabricated)
- [anhgelus/manhunt-mod](https://github.com/anhgelus/manhunt-mod)
- [horrific-tweaks/bingo](https://gitlab.com/horrific-tweaks/bingo) 