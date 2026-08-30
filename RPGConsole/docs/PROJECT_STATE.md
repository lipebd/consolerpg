# RPGConsole — Project State

## Current version

V2 under development.

Current Git branch:

`feature/v2-data-driven-enemies`

The V1 MMMVP is preserved by the tag:

`v1.0-mmmvp`

## Project objective

Console RPG developed in Java as an academic project.

The project uses object-oriented programming, including inheritance, abstraction, encapsulation, polymorphism and external text files for game data.

Code identifiers and technical comments are written in English. Texts displayed to the player are written in Brazilian Portuguese.

## Implemented in V1

- Title screen and console clearing.
- Initial scenario: Verdes Ermos.
- Interactive world loop.
- Random exploration encounters.
- One player against one enemy.
- Attack, defend, special skill, analyze, items and flee actions.
- HP, MP and SP.
- Skills with MP and SP costs.
- Enemy ranks and reward multipliers.
- EXP, gold and level progression with excess EXP preservation.
- Battle result screen.
- Item inventory and Merumã healing item.
- Items usable inside and outside battle.
- Game Over screen.
- Executable distribution with bundled Java runtime.

## V2 implemented features

### Data-driven enemies

- Complete immutable `CharacterStats` definitions.
- External enemy definitions in `data/enemies.txt`.
- `EnemyLoader`, `EnemyCatalog` and `EnemyDefinition`.
- Stable enemy and scenario IDs.
- Runtime `Enemy` instances created from reusable definitions.
- Enemy ranks: common, veteran, leader, boss, epic and legendary.

Enemy creation flow:

```text
enemies.txt
    -> EnemyLoader
    -> EnemyCatalog
    -> EnemyDefinition
    -> Enemy
```

### Data-driven encounters

- External encounter entries in `data/encounters.txt`.
- Entries grouped by scenario ID.
- Position ranges.
- Relative selection weights.
- Normal, special or unrestricted area availability.
- Maximum copies stored for future enemy-group generation.
- Random enemy selection through `EncounterGenerator`.

### Linear exploration

- Exploration advances through numbered positions.
- Each combat scenario defines its exploration length and event chances.
- Scenario-specific messages for normal and special areas.
- Supported random events: nothing, battle, item, merchant and optional epic encounter.
- Merchant limited to one appearance per exploration session.
- Epic encounter limited to one offer per dungeon session.
- Exploration progress survives battles and only resets when a new scenario session begins.

### Scenarios and travel

- Scenario inheritance hierarchy with `Scenario`, `CombatScenario`, `Field`, `Dungeon` and `City`.
- `ScenarioCatalog` stores scenarios by stable ID.
- `TravelCatalog` stores directed routes between scenarios.
- `TravelMenu` supports multiple destinations.
- Combat-scenario travel remains locked until exploration is complete.
- Bree, Verdes Ermos and Gruta de Relamparrocha are connected.
- Scenario descriptions are displayed when entering a map and after returning from battle.

### Gruta de Relamparrocha

- Eleven-position dungeon session.
- Random normal and special-area encounters.
- Optional Relampagolem epic encounter.
- Epic encounter may be accepted or avoided.
- Mestre Eletrogoblin forced as the final boss.
- Fleeing from the boss returns the player to the previous position and keeps travel locked.
- Victory unlocks travel from the dungeon.

### External enemy drops

- External drop definitions in `data/enemy_drops.txt`.
- `EnemyDropLoader` and `EnemyDropCatalog`.
- Item lookup through stable IDs in `ItemCatalog`.
- Independent chance and quantity range for each drop.
- Multiple possible drops per enemy.
- Empty drop tables for enemies without registered drops.
- Equal items are consolidated in battle rewards.
- `BattleRewardService` already accepts one enemy or a list of defeated enemies.

Drop flow:

```text
enemy_drops.txt
    -> EnemyDropLoader
    -> EnemyDropCatalog
    -> Enemy
    -> BattleRewardService
    -> BattleRewards and Inventory
```

### World refactoring

Responsibilities previously concentrated in `World` were extracted into:

- `ScenarioActionMenu`
- `TravelMenu`
- `EpicEncounterMenu`
- `BattleCoordinator`
- `BattleRewardService`
- `BattleRewards`
- `BattleRewardScreen`

`World` no longer stores or directly uses `Scanner`. It coordinates scenario state, exploration events and the consequences of battle results.

## Current architecture

### Main packages

- `rpg`: game initialization and dependency construction.
- `rpg.characters`: characters, mutable resources, immutable stats and enemy ranks.
- `rpg.battle`: battle loop, battle results and battle coordination.
- `rpg.battle.rewards`: reward calculation and reward result data.
- `rpg.skills`: skills and damage types.
- `rpg.items`: items, inventory, item catalog and drop entries.
- `rpg.enemies`: enemy definitions, catalogs and external loaders.
- `rpg.world`: world navigation and exploration coordination.
- `rpg.world.scenarios`: scenario hierarchy and scenario catalog.
- `rpg.world.encounters`: encounter entries, loading and generation.
- `rpg.world.exploration`: exploration settings, sessions and event generation.
- `rpg.world.travel`: directed travel routes.
- `rpg.ui`: console menus and screens.

## Character attributes

The complete character attribute set is:

- HP — Health Points
- MP — Mana Points
- SP — Stamina Points
- ATK — Physical Attack
- DEF — Physical Defense
- MAG — Magic
- RES — Magic Resistance
- VEL — Speed
- LCK — Luck

Initial values are grouped in immutable `CharacterStats`. Current HP, MP and SP remain inside `GameCharacter` because they change during gameplay.

Healing power is not a character attribute. Healing values belong to skills or items, while `GameCharacter.recoverHp()` applies recovery.

## Current limitations and temporary implementations

- `Battle` still supports only one player against one enemy.
- `EncounterGenerator` still selects one enemy, despite `minEnemies`, `maxEnemies` and `maxCopies` being available.
- The exploration `ITEM` event still grants `ItemCatalog.MERUMA` directly.
- The merchant event has no merchandise or transaction system yet.
- Items are still defined as constants in `ItemCatalog`.
- Scenario definitions, exploration settings, dungeon settings and travel routes are still created in Java.
- Bree exists as a travel destination but does not yet have its complete inn, shop and tavern functionality.
- Defeat still uses the temporary Game Over screen.

## Current next step

Implement enemy-group generation.

The generator must use:

- minimum and maximum enemies configured by the combat scenario;
- eligible entries for scenario, position and area type;
- relative selection weights;
- `maxCopies` for each encounter entry.

After group generation, adapt `Battle` to support up to three enemies, target selection, multiple enemy turns and consolidated rewards.

## Important conventions

- Stable IDs use lowercase letters and underscores.
- Display names may be translated or changed without changing IDs.
- External data fields are separated by semicolons.
- Lines beginning with `#` are comments.
- Definitions describe reusable immutable data.
- Runtime objects represent individual mutable game instances.
- External references are validated during game initialization.
- Git operations are performed through Git Bash.
- Eclipse is used to edit, compile and run the Java project.
