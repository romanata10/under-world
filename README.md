# Under World (`under_world`)

A Minecraft **1.20.1 Forge** horror mod inspired by *The Broken Script*, built with **GeckoLib 4**, **Fzzy Config**, **Configured**, and full multiplayer server/client synchronization.

## Requirements

- **Minecraft**: `1.20.1`
- **Forge**: `47.1.47+` (`47.4.0` target)
- **GeckoLib**: `4.8.4+` (`geckolib-forge-1.20.1`)
- **Fzzy Config**: `0.7.5+1.20.1+forge` (plus **Kotlin for Forge** `4.11.0+`)
- **Configured** *(optional client GUI integration)*: `2.2.3+`

## Entities (v1)

- **`friend`** (`under_world:friend`):
  - Appears once per world on Day 1 evening (`dayTime >= 12500`), broadcasts a yellow server join message with a vanilla cave sound, spawns in the world, and stays permanently listed in the multiplayer player tab list.
  - Emits ambient chat messages (`"hello?"`, `"im your friend"`, `"the void"`) and responds to English/Russian chat triggers (`hello`/`hi`/`привет`, `who are you?`/`кто ты?`, `friend`/`друг`, `void`, `hands`, `tree`, `the sky`).
  - Insulting or swearing at `friend` in English or Russian replies `"why.."` and triggers a punishment (lightning strike, explosion, or high-altitude falling anvil).
- **`hands`** (`under_world:hands`):
  - Wire/scribble multi-eyed humanoid inspired by *The Broken Script*.
  - Rare deep-cave encounter: extends clawed arms, chases the target for 1 minute (`1200` ticks) while breaking blocks around/behind itself with loud glitch and scream sounds, either killing and disconnecting the player or burrowing underground and vanishing after 1 minute.
- **`tree?`** (`under_world:tree_mimic`):
  - Disguised as a normal oak tree. On approach (or attack), sprouts dark ground tentacles, opens glowing white eyes across its trunk and leaves, and chases the player with wooden footsteps and creaking awaken audio.
  - Requires an axe to deal lethal blows; drops charcoal and oak logs.
- **`the sky`** (`under_world:the_sky`):
  - Colossal ~100-block-tall, ~50-block-wide black scribble silhouette.
  - Super-rare distant apparition that roars on arrival, stands still watching, and vanishes with a vanilla cave sound when approached — with a configurable chance (`25%` default) to unleash a ground-shaking stomp attack first.

## Commands (`/underworld`, op level 2)

- `/underworld spawn <friend|hands|tree|tree_mimic|the_sky> [<player>]` — Spawns the entity `15–20` blocks away from the player.
- `/underworld trigger <friend_join|friend_chat|hands_encounter|tree_encounter|the_sky_event> [<player>]` — Manually triggers an event.
- `/underworld structure <void_shrine|broken_shaft> [<player>]` — Places a mod structure near the player.
- `/underworld events <enable|disable|status>` — Enables, disables, or inspects automatic world horror events.
- `/underworld reset friend` — Resets the per-world one-time `friend` join flag and tab-list entry for testing.

## Configuration

Settings are exposed in both **Fzzy Config** (`config/under_world/settings.toml`, synced in multiplayer) and **Configured** (`config/under_world-common.toml` / `config/under_world-client.toml`), kept synchronized at runtime.

## Building

```bash
./gradlew build
```

> Note: Building requires Java 17 and outbound access to Mojang, MinecraftForge, Cloudsmith (GeckoLib), FzzyMaven, KotlinForForge, SpongePowered, and CurseMaven repositories.
