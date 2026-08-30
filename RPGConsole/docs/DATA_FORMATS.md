# RPGConsole — Data Formats

This document describes the external text files used to load game data.

## General rules

- Files use UTF-8 encoding.
- Fields are separated by semicolons (`;`).
- Empty lines are ignored.
- Lines beginning with `#` are comments.
- Stable IDs use lowercase letters and underscores.
- Enum values use their Java constant names.
- Display texts may use Brazilian Portuguese characters and accents.
- Java identifiers and technical comments are written in English.
- Texts displayed to the player are written in Brazilian Portuguese.
- Content IDs are written in Brazilian Portuguese without accents.
- Invalid references or malformed lines stop game initialization instead of creating partially invalid data.

## Enemy definitions

File:

`data/enemies.txt`

Each valid enemy line contains exactly 13 fields:

| Position | Field | Java type | Example |
|---:|---|---|---|
| 1 | ID | `String` | `lobermo` |
| 2 | Name | `String` | `Lobermo` |
| 3 | Maximum HP | `int` | `50` |
| 4 | Maximum MP | `int` | `0` |
| 5 | Maximum SP | `int` | `0` |
| 6 | Attack | `int` | `8` |
| 7 | Defense | `int` | `5` |
| 8 | Magic | `int` | `0` |
| 9 | Resistance | `int` | `0` |
| 10 | Speed | `int` | `5` |
| 11 | Luck | `int` | `0` |
| 12 | Enemy rank | `EnemyRank` | `COMMON` |
| 13 | Description | `String` | `Uma criatura lupina...` |

Header:

```text
# id;name;maxHp;maxMp;maxSp;attack;defense;magic;resistance;speed;luck;rank;description
```

Example:

```text
lobermo;Lobermo;50;0;0;8;5;0;0;5;0;COMMON;Uma criatura lupina que percorre os Verdes Ermos em busca de presas.
```

Supported enemy ranks:

- `COMMON`
- `VETERAN`
- `LEADER`
- `BOSS`
- `EPIC`
- `LEGENDARY`

Loading flow:

```text
enemies.txt
    -> EnemyLoader
    -> EnemyCatalog
    -> EnemyDefinition
    -> Enemy
```

`EnemyLoader` ignores blank lines and comments, requires exactly 13 fields, converts numeric values and `EnemyRank`, creates `CharacterStats`, and registers an `EnemyDefinition`.

Enemy definitions contain reusable immutable data. Each battle creates a separate mutable `Enemy` instance from its definition.

## Encounter definitions

File:

`data/encounters.txt`

Each valid encounter line contains exactly 7 fields:

| Position | Field | Java type | Example |
|---:|---|---|---|
| 1 | Scenario ID | `String` | `verdes_ermos` |
| 2 | Enemy ID | `String` | `lobermo` |
| 3 | Minimum position | `int` | `1` |
| 4 | Maximum position | `int` | `5` |
| 5 | Selection weight | `int` | `50` |
| 6 | Maximum copies | `int` | `2` |
| 7 | Area restriction | `EncounterAreaRestriction` | `ANY` |

Header:

```text
# scenarioId;enemyId;minPosition;maxPosition;weight;maxCopies;areaRestriction
```

Example:

```text
verdes_ermos;lobermo;1;5;50;2;ANY
```

Area restriction values:

- `NORMAL_ONLY`: available only in normal exploration areas.
- `SPECIAL_ONLY`: available only in special exploration areas.
- `ANY`: available in both area types.

Rules:

- `minPosition` is the first exploration position where the enemy may appear.
- `maxPosition` is the last valid position.
- `weight` is relative and does not represent the chance that a battle occurs.
- Weights are compared only among entries eligible for the current scenario, position and area type.
- Eligible weights do not need to total 100.
- `maxCopies` limits how many copies of the enemy may join one generated enemy group.
- The current single-enemy battle does not use `maxCopies` yet; it is stored for the upcoming group generator.
- The referenced enemy ID must exist in `EnemyCatalog`.

Loading flow:

```text
encounters.txt
    -> EncounterLoader
    -> EncounterCatalog
    -> EncounterGenerator
    -> EnemyDefinition
```

## Enemy drop definitions

File:

`data/enemy_drops.txt`

Each valid enemy drop line contains exactly 5 fields:

| Position | Field | Java type | Example |
|---:|---|---|---|
| 1 | Enemy ID | `String` | `lobermo` |
| 2 | Item ID | `String` | `meruma` |
| 3 | Drop chance | `int` | `50` |
| 4 | Minimum quantity | `int` | `1` |
| 5 | Maximum quantity | `int` | `1` |

Header:

```text
# enemyId;itemId;dropChance;minQuantity;maxQuantity
```

Examples:

```text
lobermo;meruma;50;1;1
passavento;meruma;40;1;2
```

Rules:

- `dropChance` must be between 0 and 100.
- Minimum quantity must be greater than zero.
- Maximum quantity cannot be lower than minimum quantity.
- Every row is rolled independently.
- One enemy may therefore drop several different items in the same battle.
- The same item cannot be registered twice for the same enemy.
- Enemies without registered rows have an empty drop table.
- The referenced enemy ID must exist in `EnemyCatalog`.
- The referenced item ID must exist in `ItemCatalog`.
- Drops of the same item from multiple defeated enemies are added together before the result screen is displayed.

Example of independent rolls:

```text
lobermo;meruma;50;1;1
lobermo;pelo_lupino;30;1;2
```

The Lobermo may drop nothing, only Merumã, only Pelo Lupino, or both items.

Loading and runtime flow:

```text
enemy_drops.txt
    -> EnemyDropLoader
    -> EnemyDropCatalog
    -> BattleCoordinator
    -> Enemy drop table
    -> BattleRewardService
    -> Inventory and BattleRewards
```

## Current item IDs

Items currently remain defined as constants in `ItemCatalog`.

Implemented IDs:

| ID | Name | Type | Effect |
|---|---|---|---|
| `meruma` | Merumã | `HealingItem` | Recovers 25 HP |

`ItemCatalog.getById()` resolves the IDs used by external drop data.

## Planned data files

The following external formats are planned but have not been implemented:

- `data/items.txt`
- scenario definitions
- exploration event item tables
- travel routes
- dungeon settings

Their final formats must be documented here when implemented.
