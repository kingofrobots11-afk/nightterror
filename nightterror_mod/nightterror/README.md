# 🌑 Night Terror Mod — Forge 1.20.1

A Minecraft Forge mod featuring a terrifying night-only boss encounter.

---

## 🧟 The Night Stalker

A powerful two-phase boss that hunts players from dusk until dawn.

### Stats
| Attribute       | Value       |
|-----------------|-------------|
| Health          | 300 HP      |
| Attack Damage   | 12 (Phase 1), 18 (Phase 2) |
| Speed           | 0.32 (Phase 1), 0.45 (Phase 2) |
| XP Reward       | 150         |
| Spawn Time      | Night only (tick 13000+) |

### Mechanics
- **Phase 1** — Hunts players, periodic terror scream applies Weakness + Slowness in a 20-block radius.
- **Phase 2 (≤50% HP)** — Grows larger (1.15× scale), gains speed & damage, teleports behind its target, applies Darkness + Blindness to nearby players, boss bar turns red.
- **Daylight Weakness** — Burns in direct sunlight. Surviving the night defeats the encounter.
- **Boss Bar** — A purple (then red) boss health bar is shown to all players who can see the stalker.

### Drops
- 2–5 **Soul Shards** (custom item, loot-enchant compatible)
- 1–3 **Bottles o' Enchanting** (common)
- 5–15 **Emeralds** (uncommon)
- **Enchanted Golden Apple** (rare)

### Chat Announcements
- **Dusk (tick 13000):** "The darkness stirs… something hunts tonight."
- **Pre-dawn (tick 22500):** "Dawn approaches… survive just a little longer."
- **Boss death:** "The Night Stalker has been vanquished! Dawn breaks safely."

---

## 📦 Installation

### Requirements
- Minecraft **1.20.1**
- Forge **47.2.0+**
- **GeckoLib** 4.4.4+ for Forge 1.20.1

### Building from Source
```bash
./gradlew build
```
The compiled `.jar` will be in `build/libs/`.

Place the jar (and GeckoLib jar) into your `mods/` folder.

---

## 🎨 Customizing the Texture / Model

The placeholder texture is at:
```
src/main/resources/assets/nightterror/textures/entity/night_stalker.png
```
The geo model is at:
```
src/main/resources/assets/nightterror/geo/night_stalker.geo.json
```
Open the `.geo.json` in **Blockbench** (using the GeckoLib plugin) to edit the model and re-export animations.

---

## 🗂 Project Structure

```
nightterror/
├── build.gradle
├── settings.gradle
├── gradle.properties
└── src/main/
    ├── java/com/nightterror/
    │   ├── NightTerrorMod.java          ← Mod entry point
    │   ├── init/
    │   │   ├── ModEntities.java         ← Entity registry
    │   │   └── ModItems.java            ← Item registry
    │   ├── entity/
    │   │   ├── NightStalkerEntity.java  ← Boss AI, phases, boss bar
    │   │   └── client/
    │   │       ├── NightStalkerModel.java
    │   │       └── NightStalkerRenderer.java
    │   └── event/
    │       ├── ClientSetup.java         ← Renderer registration
    │       ├── CommonSetup.java         ← Attributes + spawn placement
    │       └── ForgeEvents.java         ← Night messages, death drops
    └── resources/
        ├── META-INF/mods.toml
        ├── pack.mcmeta
        ├── assets/nightterror/
        │   ├── lang/en_us.json
        │   ├── textures/entity/night_stalker.png
        │   ├── geo/night_stalker.geo.json
        │   └── animations/night_stalker.animation.json
        └── data/nightterror/
            └── loot_tables/entities/night_stalker.json
```
