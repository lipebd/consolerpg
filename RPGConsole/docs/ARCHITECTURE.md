# RPGConsole — Architecture

## Purpose

This document summarizes the current project structure, the responsibility of each package and the main data and runtime flows.

The architecture is evolving incrementally. The current priority is to keep domain rules, console presentation, external data loading and game-flow coordination separated without adding abstractions before they are useful.

## Main conventions

- Java identifiers and technical comments are written in English.
- Player-facing texts are written in Brazilian Portuguese.
- Stable content IDs use lowercase letters and underscores without accents.
- Definitions contain reusable immutable data.
- Runtime objects contain mutable state for one active game instance.
- External references are validated during initialization.
- Console input belongs to menus and screens rather than domain classes.

## Package overview

| Package | Responsibility | Main types |
|---|---|---|
| `rpg` | Application entry point and dependency construction | `Main` |
| `rpg.characters` | Character state, attributes and ranks | `GameCharacter`, `CharacterStats`, `Player`, `Enemy`, `EnemyRank` |
| `rpg.battle` | Turn-based battle execution and coordination | `Battle`, `BattleResult`, `BattleCoordinator` |
| `rpg.battle.rewards` | Reward calculation and result data | `BattleRewardService`, `BattleRewards` |
| `rpg.skills` | Skill definitions and damage types | `Skill`, `DamageType` |
| `rpg.items` | Items, inventory and individual drop rules | `Item`, `HealingItem`, `ItemCatalog`, `Inventory`, `DropEntry` |
| `rpg.enemies` | Enemy definitions and external enemy/drop loading | `EnemyDefinition`, `EnemyCatalog`, `EnemyLoader`, `EnemyDropCatalog`, `EnemyDropLoader` |
| `rpg.world` | Active world state and high-level coordination | `World` |
| `rpg.world.scenarios` | Scenario hierarchy and scenario registry | `Scenario`, `CombatScenario`, `Field`, `Dungeon`, `City`, `DungeonSettings`, `ScenarioCatalog`, `ScenarioAction` |
| `rpg.world.encounters` | Encounter availability and weighted selection | `EncounterEntry`, `EncounterCatalog`, `EncounterLoader`, `EncounterGenerator`, `EncounterAreaRestriction` |
| `rpg.world.exploration` | Exploration configuration, progress and event selection | `ExplorationSettings`, `ExplorationSession`, `ExplorationEventGenerator`, `ExplorationEventType` |
| `rpg.world.travel` | Directed routes between scenarios | `TravelCatalog` |
| `rpg.ui` | Console menus and screens | `TitleScreen`, `GameOverScreen`, `ConsoleUI`, `ItemMenu`, `TravelMenu`, `ScenarioActionMenu`, `EpicEncounterMenu`, `BattleRewardScreen` |

## Application initialization

`Main` acts as the current composition root. It creates and connects the objects required by the game.

Initialization includes:

1. Creating the shared `Scanner`.
2. Loading enemy definitions.
3. Loading enemy drop definitions.
4. Loading encounter definitions.
5. Creating scenarios and their exploration settings.
6. Registering scenarios and travel routes.
7. Creating the test player and initial skills.
8. Creating `World` with the required catalogs and services.
9. Starting the world loop.

External data is loaded before gameplay. Invalid data prevents the game from starting with partially inconsistent catalogs.

## Character model

```text
GameCharacter
├── Player
└── Enemy
```

`CharacterStats` groups immutable initial values:

- maximum HP, MP and SP;
- attack and defense;
- magic and resistance;
- speed and luck.

`GameCharacter` copies these definitions and stores mutable current HP, MP and SP. It also implements shared behavior such as attacking, defending, spending resources, receiving damage and recovering HP.

`Player` adds player-specific state such as level, EXP, gold, inventory and learned skills.

`Enemy` adds rank, description and its runtime drop table.

## Scenario model

```text
Scenario
├── City
└── CombatScenario
    ├── Field
    └── Dungeon
```

`Scenario` defines the stable ID, display name, description and available actions shared by all scenarios.

`CombatScenario` adds base EXP and gold rewards plus `ExplorationSettings`.

`Field` represents open exploration areas.

`Dungeon` adds `DungeonSettings`, including boss and optional epic enemy IDs.

`City` represents non-combat settlements. Bree currently exists as a basic destination and will later receive inn, shop and tavern functionality.

`ScenarioCatalog` resolves stable scenario IDs to scenario objects.

## Definition and runtime separation

Enemy data is separated into two layers:

| Layer | Example | Purpose |
|---|---|---|
| Definition | `EnemyDefinition` for `lobermo` | Reusable immutable template loaded once |
| Runtime object | One `Enemy` Lobermo in a battle | Mutable HP, MP, SP and battle state |

The same `EnemyDefinition` can create many independent `Enemy` objects without sharing mutable battle state.

The same principle will later apply to external item, scenario and class definitions.

## Enemy data flow

```text
data/enemies.txt
        ↓
EnemyLoader
        ↓
EnemyCatalog
        ↓
EnemyDefinition
        ↓
Enemy runtime instance
```

`EnemyCatalog` is the central registry used by encounters, bosses, epic encounters and drop validation.

## Encounter data flow

```text
data/encounters.txt
        ↓
EncounterLoader
        ↓
EncounterCatalog
        ↓
EncounterGenerator
        ↓
eligible EnemyDefinition
```

An `EncounterEntry` associates an enemy ID with:

- scenario ID;
- minimum and maximum exploration positions;
- relative selection weight;
- maximum copies in a future enemy group;
- normal, special or unrestricted area availability.

The current generator returns one eligible enemy definition. The next version will generate a list while enforcing scenario group-size settings and `maxCopies`.

## Enemy drop data flow

```text
data/enemy_drops.txt
        ↓
EnemyDropLoader
        ↓
EnemyDropCatalog
        ↓
BattleCoordinator creates Enemy
        ↓
Enemy drop table
        ↓
BattleRewardService
        ↓
Inventory + BattleRewards
```

Each `DropEntry` is rolled independently. Equal items are grouped in a `Map<Item, Integer>` and displayed as one consolidated quantity.

Item equality and hash codes use the stable item ID, allowing logically equal items to share the same map key.

## Exploration flow

```text
ScenarioActionMenu selects EXPLORE
        ↓
World advances ExplorationSession
        ↓
normal or special area is determined
        ↓
ExplorationEventGenerator selects event
        ↓
World dispatches the event consequence
```

`ExplorationSettings` defines:

- exploration length;
- battle, item, merchant and epic chances;
- special-area chance;
- minimum and maximum enemy-group size;
- normal and special-area messages.

`ExplorationSession` stores mutable progress and ensures that merchant and epic events cannot be repeated beyond their configured session rules.

The last position of a dungeon bypasses random event generation and starts its forced boss encounter.

## Battle architecture

The battle system is divided into the following responsibilities:

- `Battle`
  - Coordinates the battle lifecycle and character actions.
- `BattleCoordinator`
  - Creates runtime enemies and grants victory rewards.
- `EnemyBattleGroup`
  - Stores one to three enemy slots.
- `EnemyBattleSlot`
  - Stores one enemy and its unique battle display name.
- `BattleTurnEntry`
  - Represents one participant inside the round queue.
- `BattleTurnOrderGenerator`
  - Calculates action counts and initiative scores.
- `BattleTurnOrder`
  - Tracks the current position and skips defeated entries.
- `BattleTurnOrderScreen`
  - Displays the current and upcoming actions.
- `BattleEscapeService`
  - Calculates the two-stage flee attempt.
- `AllyTargetMenu`
  - Selects living, injured or defeated party members.
- `EnemyTargetMenu`
  - Selects a living enemy target.
- `BattleRewardService`
  - Calculates EXP, gold and consolidated drops.
- `BattleRewardScreen`
  - Displays party progression and collected rewards.
- `CharacterProgressionResult`
  - Stores the individual EXP and level result of one ally.

Current battle flow:

```text
Enemy definitions
        ↓
BattleCoordinator
        ↓
EnemyBattleGroup
        ↓
Battle
        ↓
BattleTurnOrderGenerator
        ↓
BattleTurnOrder        
        ↓
character and enemy actions        
        ↓
BattleResult
        ↓
BattleRewardService
        ↓
BattleRewardScreen
```

## Party ownership

`Party` owns shared resources:

- Inventory
- Gold
- Future global reward buffs
- Future party title

`PlayableCharacter` owns individual state:

- HP
- MP
- SP
- Attributes
- Level
- EXP
- Skills
- Defensive state
- Future equipment
- Future temporary effects

## Skill architecture

`Skill` currently stores:

- Damage data.
- Recovery data.
- Resource costs.
- Recovery type.
- Recovery target type.
- Description.

Recovery effect and target are stored separately.

This prepares the project for a future list of independent skill effects,
where one skill may cause damage, healing, buffs and debuffs using
different targets.

## Travel flow

`TravelCatalog` stores directed source-to-destination relationships. A route must be registered separately for each permitted direction.

`World` decides whether travel is currently allowed. Combat scenarios require a completed exploration session, while cities may allow immediate travel.

`TravelMenu` displays every registered destination and returns the selected `Scenario`. `World` then performs the actual scenario change and creates a new exploration session when required.

## UI boundaries

The UI package contains console-specific behavior:

- reading and validating menu input;
- displaying title, status and result information;
- asking for confirmations;
- waiting for the player to continue.

`World` no longer stores a `Scanner`. It receives one during construction and passes it to the UI and battle objects that actually require console input.

## Current temporary boundaries

The following implementations remain intentionally temporary:

- the exploration item event directly grants Merumã;
- the merchant has no merchandise;
- items are defined as constants in `ItemCatalog`;
- scenarios, exploration settings, dungeon settings and travel routes are constructed in Java;
- Bree is not fully implemented;
- defeat still opens `GameOverScreen` instead of rescuing the party in Bree.

These limitations are tracked in `V2_ROADMAP.md` and should be replaced incrementally rather than through one large rewrite.
