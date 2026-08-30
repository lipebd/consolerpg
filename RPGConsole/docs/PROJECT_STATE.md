# RPGConsole — Project State

## Current version

V2 under development.

Current Git branch:

`feature/v2-data-driven-enemies`

The V1 MMMVP is preserved by the tag:

`v1.0-mmmvp`

## Project objective

Console RPG developed in Java as an academic project.

The project uses object-oriented programming, including inheritance,
abstraction, encapsulation, polymorphism and external text files for
game data.

Code identifiers and comments are written in English.
Texts displayed to the player are written in Brazilian Portuguese.

## Implemented in V1

- Title screen.
- Initial scenario: Verdes Ermos.
- Interactive world loop.
- Random exploration encounters.
- One player against one enemy.
- Attack, defend, special skill, analyze, items and flee actions.
- HP, MP and SP.
- Skills with MP and SP costs.
- Enemy ranks and reward multipliers.
- EXP, gold and level progression.
- Excess EXP is preserved after leveling up.
- Battle result screen.
- Item inventory.
- Merumã healing item.
- Enemy item drops.
- Items usable inside and outside battle.
- Game Over screen.
- Executable distribution with bundled Java runtime.

## Current architecture

### Main packages

- `rpg`
  - Game initialization.
- `rpg.characters`
  - Characters, attributes, player, enemies and enemy ranks.
- `rpg.battle`
  - Battle loop and battle results.
- `rpg.skills`
  - Skills and damage types.
- `rpg.items`
  - Items, inventory, item catalog and drops.
- `rpg.world`
  - World navigation and exploration.
- `rpg.world.scenarios`
  - Base scenario classes and scenario types.
- `rpg.enemies`
  - Enemy definitions, catalog and external file loader.
- `rpg.ui`
  - Console menus and screens.

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

These initial values are grouped in `CharacterStats`.

Current HP, MP and SP remain inside `GameCharacter` because they change
during gameplay.

Healing power is not a character attribute. Healing values belong to
skills or items, while `GameCharacter.recoverHp()` applies the recovery.

## V2 progress

Completed:

- Created `CharacterStats`.
- Removed the obsolete `healingPower` character attribute.
- Created `EnemyDefinition`.
- Created `EnemyCatalog`.
- Created `EnemyLoader`.
- Created external `data/enemies.txt`.
- Loaded the Lobermo definition from the text file.
- Passed `EnemyCatalog` from `Main` to `World`.
- Replaced the hardcoded Lobermo attributes with catalog data.
- Preserved the existing Merumã drop.

Current enemy creation flow:

`enemies.txt -> EnemyLoader -> EnemyCatalog -> EnemyDefinition -> Enemy`

## Temporary implementation

The Lobermo's attributes, rank and description come from `enemies.txt`.

Its Merumã drop table is still temporarily defined inside `World`.

The encounter method is still specifically named
`startLobermoEncounter()`.

These temporary parts will be replaced by the encounter generation
system.

## Current next step

Add a stable ID to every `Scenario`.

Example:

- Internal ID: `verdes_ermos`
- Display name: `Verdes Ermos`

The ID will be used by encounter tables, travel and the future save
system.

After scenario IDs, implement an external list of enemies available in
each area and randomly generate encounters from that list.

## Important conventions

- Stable IDs use lowercase letters and underscores.
- Display names may be translated or changed without changing IDs.
- External data fields are separated by semicolons.
- Lines beginning with `#` are comments.
- Definitions describe reusable data.
- Runtime objects represent individual mutable game instances.
- The Git repository is controlled through Git Bash.
- Eclipse is used only to edit and run the Java project.