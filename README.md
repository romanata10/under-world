# Under World (`under_world`) — v2.1.0

A Minecraft **1.20.1 Forge** psychological & Dreamcore horror mod inspired by *The Broken Script*, built with **GeckoLib 4**, **Fzzy Config**, **Configured**, and full multiplayer server/client synchronization.

## Requirements

- **Minecraft**: `1.20.1`
- **Forge**: `47.1.47+` (`47.4.0` target)
- **GeckoLib**: `4.8.4+` (`geckolib-forge-1.20.1`)
- **Fzzy Config**: `0.7.5+1.20.1+forge` (plus **Kotlin for Forge** `4.11.0+`)
- **Configured** *(optional client GUI integration)*: `2.2.3+`

## Entities (v1 + v2)

- **`friend`** (`under_world:friend`):
  - Appears once per world on Day 1 evening (`dayTime >= 12500`), broadcasts a yellow server join message with a vanilla cave sound, spawns in the world, and stays permanently listed in the multiplayer player tab list.
  - **v2 Stalker AI**: physically stalks players from afar, cracks open nearby wooden doors, leaves cryptic signs (`"hello?"`, `"the flowers watch at night"`, `"red bricks"`), builds rare Void Shrines behind players, and when spotted up close either vanishes in dark smoke or pulls victims into **The Fun World**.
  - Emits ambient chat messages and replies to English/Russian chat triggers (`hello`/`hi`/`привет`, `who are you?`/`кто ты?`, `friend`/`друг`, `void`, `hands`, `tree`, `the sky`, `fun world`, `maze`, `smile`).
  - Insulting `friend` in English or Russian replies `"why.."` and triggers a punishment (lightning strike, explosion, or high-altitude falling anvil).
- **`hands`** (`under_world:hands`):
  - Wire/scribble multi-eyed humanoid cave encounter: extends clawed arms, chases the target for 1 minute while breaking surrounding blocks, disconnects killed players (configurable), or burrows underground.
- **`tree?`** (`under_world:tree_mimic`):
  - Disguised oak tree ambush entity: awakens glowing eyes and dark tentacles on approach, requires an axe to deal lethal blows.
- **`the sky`** (`under_world:the_sky`):
  - Colossal ~100-block-tall distant silhouette apparition that roars, watches, and either stomps or vanishes when approached.
- **`eyes of fun`** (`under_world:eyes_of_fun`) *(v2)*:
  - Tall dual-blossomed flower-stalk horror haunting **The Fun World** at night (one staring red/orange eye blossom, one withered white/pink toothy blossom, and hanging dark tendrils).
  - **Look-Rule**: freezes dead still whenever directly observed in unobstructed line of sight, and sprints lethally toward the player the moment they look away.
- **`smile`** (`under_world:smile`) *(v2)*:
  - Tall four-legged pitch-black arachnid stalker with a floating glowing pale-yellow smiling mask haunting **The Maze** corridors.
  - Unleashes a distorted digital roar on sight and relentlessly sprints through brick hallways with rapid spindly footsteps.
- **`the truth`** (`under_world:the_truth`) *(v2.1)*:
  - Super-mega-rare **50-block tall × 30-block wide** pitch-black stick/root colossus with a cracked triangular staring-eye mask and hanging claw tendrils.
  - Erupts out of the ground ahead of the player with an earth-shaking roar and chases relentlessly for 30 seconds while shattering any blocks in its path.
  - Triggers a full horror HUD takeover (4-corner watching face, scrolling crimson warning banner, and 30s survival countdown). If it catches you before the timer ends, it kills and disconnects you — and upon rejoining, a 100-block cobblestone pillar spawns beneath you.

## Mechanics & Dimensions (v2 + v2.1)

- **Anti-Spawn-Kill** *(v2.1)*: When a player dies to any `under_world` entity, that entity (and nearby aggro `under_world` entities) vanish cleanly so they cannot spawn-kill the player upon respawning.
- **The Fun World** (`under_world:the_fun_world`):
  - Accessible by stepping into a **Void Shrine** ritual core in the Overworld or being warped by `friend` at close range.
  - **Accelerated Clock** *(v2.1)*: Day and night cycle **4× faster** (configurable) inside The Fun World independently of the Overworld.
  - Dreamcore Windows-98 plains of saturated lime grass, glossy daisies, flat blossoms, blue/red spotted mushrooms, and a cobalt-to-white sky under a **Giant Yellow Sun with One Big Eye**.
  - When night falls, the sky turns pitch black, every flower opens a glowing watching eye that tracks the player, and **`Eyes of Fun`** begins hunting.
  - Look for **Maze Red-Brick Ruins** on the plains containing a glowing **`Maze Rift Portal`** (`under_world:maze_portal`) to enter **The Maze**.
- **The Maze** (`under_world:maze`):
  - Classic Windows 95/98 3D Maze screensaver labyrinth of red clay bricks, warm tan parquet floors, speckled grey ceiling tiles, dense dark corridor fog, and **`Smile`** hunting inside.
  - Navigate the connected brick corridors to locate a glowing **`Maze Escape Terminal`** (`under_world:maze_teleporter`) and return safely to the Overworld.

## Commands (`/underworld`, op level 2)

- `/underworld spawn <friend|hands|tree|tree_mimic|the_sky|eyes_of_fun|smile|the_truth> [<player>]`
- `/underworld trigger <friend_join|friend_chat|hands_encounter|tree_encounter|the_sky_event|eyes_of_fun|smile|the_truth> [<player>]`
- `/underworld tp <overworld|fun_world|maze> [<player>]`
- `/underworld structure <void_shrine|broken_shaft> [<player>]`
- `/underworld events <enable|disable|status>`
- `/underworld reset friend`

## Configuration

Settings are exposed in both **Fzzy Config** (`config/under_world/settings.toml`, synced in multiplayer) and **Configured** (`config/under_world-common.toml` / `config/under_world-client.toml`), kept synchronized at runtime.

## Building

```bash
./gradlew build
```
