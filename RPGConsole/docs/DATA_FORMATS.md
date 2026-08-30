# RPGConsole — Data Formats

This document describes the external text files used to load game data.

## General rules

- Files use UTF-8 encoding.
- Fields are separated by semicolons (`;`).
- Empty lines are ignored.
- Lines beginning with `#` are comments.
- Stable IDs use lowercase letters and underscores.
- Enum values are written using their Java constant names.
- Display texts may use Brazilian Portuguese characters and accents.
- Current language convention: Java identifiers in English. Technical comments in English. Game texts in Brazilian Portuguese. Content ID in Brazilian Portuguese.

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
| 10 | Speed | `int` | `0` |
| 11 | Luck | `int` | `0` |
| 12 | Enemy rank | `EnemyRank` | `COMMON` |
| 13 | Description | `String` | `Uma criatura lupina...` |

Header:


# id;name;maxHp;maxMp;maxSp;attack;defense;magic;resistance;speed;luck;rank;description

Example:

lobermo;Lobermo;50;0;0;8;5;0;0;0;0;COMMON;Uma criatura lupina que percorre os Verdes Ermos em busca de presas.
Loading process

Enemy data follows this flow:

`enemies.txt -> EnemyLoader -> EnemyCatalog -> EnemyDefinition -> Enemy`

EnemyLoader performs the following operations:

Reads all lines from the file.
Ignores blank lines and comments.
Splits each data line using ;.
Requires exactly 13 fields.
Converts numeric fields to int.
Converts the rank text to EnemyRank.
Creates a CharacterStats.
Creates and registers an EnemyDefinition.

Invalid data causes the game initialization to stop with an error
message instead of creating a partially invalid enemy.

Enemy drops

Enemy drops are not currently loaded from enemies.txt.

The Lobermo's Merumã drop remains temporarily configured inside
World.

Drops will receive a separate external format so that enemy combat data
and reward data remain independent.

Planned data files

The following formats are planned but have not been implemented:

`data/encounters.txt`
`data/enemy_drops.txt`
`data/items.txt`
`Scenario and exploration event data`

Their final formats must be documented here when implemented.

## Encounter definitions

File:

`data/encounters.txt`

Each encounter line contains exactly three fields:

| Position | Field | Java type | Example |
|---:|---|---|---|
| 1 | Scenario ID | `String` | `verdes_ermos` |
| 2 | Enemy ID | `String` | `lobermo` |
| 3 | Selection weight | `int` | `100` |

Header:

`# scenarioId;enemyId;minPosition;maxPosition;weight;maxCopies;areaRestrictiont`

Example:

`verdes_ermos;lobermo;1;5;50;2;ANY`

The weight is relative and does not represent the chance that a battle
will occur. It determines which enemy is selected after an encounter
has already been triggered.
minPosition is the smallest position the monster can appear. maxPosition defines the latest it can appear.
maxCopies restricts the amount of the same monster in a single battle, while areaRestriction defines if a monster can appear in NORMAL_ONLY areas, SPECIAL_ONLY areas or ANY