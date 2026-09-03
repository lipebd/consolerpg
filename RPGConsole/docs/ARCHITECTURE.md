# RPGConsole — Architecture

## Purpose

This document summarizes the current project structure, the responsibility of each package and the main data and runtime flows.

The architecture evolves incrementally. The current goal is to keep domain rules, console presentation, external data loading and game-flow coordination separated without creating abstractions before they become useful.

## Main conventions

- Java identifiers and technical comments are written in English.
- Player-facing texts are written in Brazilian Portuguese.
- Stable content IDs use lowercase letters and underscores without accents.
- Definitions contain reusable immutable data.
- Runtime objects contain mutable state for one active game instance.
- External references are validated during initialization.
- Console input belongs to menus and screens rather than domain classes.
- Inventory and gold belong to the party.
- Character resources and progression belong to each playable character.

## Package overview

| Package | Responsibility | Main types |
|---|---|---|
| `rpg` | Application entry point and dependency construction | `Main` |
| `rpg.characters` | Character state, attributes, party ownership and enemy ranks | `GameCharacter`, `CharacterStats`, `PlayableCharacter`, `Party`, `Enemy`, `EnemyRank` |
| `rpg.battle` | Battle execution, enemy groups, initiative and escape | `Battle`, `BattleResult`, `BattleCoordinator`, `EnemyBattleGroup`, `EnemyBattleSlot`, `BattleTurnOrder`, `BattleTurnOrderGenerator`, `BattleEscapeService` |
| `rpg.battle.rewards` | Reward calculation and individual progression results | `BattleRewardService`, `BattleRewards`, `CharacterProgressionResult` |
| `rpg.skills` | Skill definitions, damage, recovery and target types | `Skill`, `DamageType`, `RecoveryType`, `SkillTargetType` |
| `rpg.items` | External item definitions, runtime item behavior, shared inventory and individual drop rules | `ItemDefinition`, `ItemDefinitionCatalog`, `ItemLoader`, `RecoveryItemDefinition`, `RecoveryItemLoader`, `ItemFactory`, `Item`, `BasicItem`, `RecoveryItem`, `ItemCatalog`, `Inventory`, `DropEntry` |
| `rpg.enemies` | Enemy definitions and external enemy/drop loading | `EnemyDefinition`, `EnemyCatalog`, `EnemyLoader`, `EnemyDropCatalog`, `EnemyDropLoader` |
| `rpg.world` | Active world state and high-level coordination | `World` |
| `rpg.world.scenarios` | Scenario hierarchy and scenario registry | `Scenario`, `CombatScenario`, `Field`, `Dungeon`, `City`, `DungeonSettings`, `ScenarioCatalog`, `ScenarioAction` |
| `rpg.world.encounters` | Encounter availability and weighted group generation | `EncounterEntry`, `EncounterCatalog`, `EncounterLoader`, `EncounterGenerator`, `EncounterAreaRestriction` |
| `rpg.world.exploration` | Exploration configuration, progress and event selection | `ExplorationSettings`, `ExplorationSession`, `ExplorationEventGenerator`, `ExplorationEventType` |
| `rpg.world.travel` | Directed routes between scenarios | `TravelCatalog` |
| `rpg.ui` | Console menus, target selection and information screens | `TitleScreen`, `GameOverScreen`, `InventoryMenu`, `BattleItemMenu`, `RecoveryItemUseMenu`, `TravelMenu`, `ScenarioActionMenu`, `EpicEncounterMenu`, `BattleRewardScreen`, `BattleTurnOrderScreen`, `PartyStatusMenu`, `CharacterStatusMenu` |

## Application initialization

`Main` acts as the current composition root. It creates and connects the objects required by the game.

Initialization includes:

1. Creating the shared `Scanner`.
2. Loading enemy definitions.
3. Loading enemy drop definitions.
4. Loading encounter definitions.
5. Creating the temporary playable characters and skills.
6. Creating the party.
7. Creating scenarios and exploration settings.
8. Registering scenarios and travel routes.
9. Creating `World` with the required catalogs and services.
10. Starting the world loop.

External data is loaded before gameplay. Invalid data prevents the game from starting with partially inconsistent catalogs.

Playable characters, skills, scenarios and travel routes are still created in Java. Moving these definitions to external files is part of the V3 roadmap.

## Character model

```text
GameCharacter
├── PlayableCharacter
└── Enemy

Party
└── one to four PlayableCharacter objects
```

`CharacterStats` groups immutable initial values:

- Maximum HP, MP and SP.
- Attack and defense.
- Magic and resistance.
- Speed and luck.

`GameCharacter` stores mutable current HP, MP and SP and implements behavior shared by allies and enemies, including:

- Attacking.
- Defending.
- Spending MP and SP.
- Receiving damage.
- Recovering HP.
- Checking whether the character is alive.
- Restoring all resources for a living character.

`PlayableCharacter` adds individual progression and player-controlled behavior:

- Level.
- EXP.
- Learned skills.
- Physical skill execution.

`Party` stores:

- One to four playable characters.
- Shared inventory.
- Shared gold.
- Future global reward buffs.
- Future party title.

`Enemy` adds:

- Enemy rank.
- Description.
- Runtime drop table.

## Party ownership

Shared state belongs to `Party`:

- Inventory.
- Gold.
- Future global EXP multiplier.
- Future global gold multiplier.
- Future party title.

Individual state belongs to `PlayableCharacter`:

- HP.
- MP.
- SP.
- Attributes.
- Level.
- EXP.
- Skills.
- Defensive state.
- Future equipment.
- Future individual EXP multiplier.
- Future temporary buffs and debuffs.

This separation allows party members to enter and leave the group without duplicating shared resources.

## Scenario model

```text
Scenario
├── City
└── CombatScenario
    ├── Field
    └── Dungeon
```

`Scenario` defines:

- Stable ID.
- Display name.
- Description.
- Available actions.

`CombatScenario` adds:

- Base EXP reward.
- Base gold reward.
- `ExplorationSettings`.

`Field` represents open exploration areas.

`Dungeon` adds `DungeonSettings`, including:

- Boss enemy ID.
- Optional epic enemy ID.

`City` represents non-combat settlements. Bree currently exists as a basic destination and will later receive INN, shop and tavern functionality.

`ScenarioCatalog` resolves stable scenario IDs to runtime scenario objects.

## Definition and runtime separation

Enemy data is separated into two layers:

| Layer | Example | Purpose |
|---|---|---|
| Definition | `EnemyDefinition` for `lobermo` | Reusable immutable template loaded once |
| Runtime object | One Lobermo inside a battle | Mutable HP, resources and battle state |

The same `EnemyDefinition` can create many independent `Enemy` objects without sharing mutable battle state.

This distinction also permits repeated enemies in one battle. Two Lobermos created from the same definition have independent HP and defensive state.

The same principle will later apply to external item, skill, scenario and playable-class definitions.

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

`EnemyCatalog` is the central enemy-definition registry used by:

- Random encounters.
- Dungeon bosses.
- Optional epic encounters.
- Drop-reference validation.
- Runtime enemy creation.

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
List<EnemyDefinition>
        ↓
BattleCoordinator
        ↓
EnemyBattleGroup
```

An `EncounterEntry` associates an enemy ID with:

- Scenario ID.
- Minimum exploration position.
- Maximum exploration position.
- Relative selection weight.
- Maximum copies in one battle.
- Normal, special or unrestricted area availability.

Encounter generation also uses the minimum and maximum enemy counts configured by the current combat scenario.

Selection weights are relative and do not need to total 100.

The generator creates a list of eligible enemy definitions while respecting:

- Scenario ID.
- Exploration position.
- Area type.
- Relative weights.
- Per-entry `maxCopies`.
- Scenario minimum group size.
- Scenario maximum group size.
- Global enemy-group limit.

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
Enemy runtime drop table
        ↓
BattleRewardService
        ↓
BattleRewards
        ↓
Party Inventory
```

Each `DropEntry` is rolled independently.

This allows one enemy to:

- Drop nothing.
- Drop one registered item.
- Drop several registered items.
- Drop a random quantity within each entry's range.

Drops from every defeated enemy are combined in a `Map<Item, Integer>`.

Item equality and hash codes use the stable item ID, allowing logically equal items to share the same map key and appear as one consolidated quantity.

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

- Exploration length.
- Battle chance.
- Item chance.
- Merchant chance.
- Epic encounter chance.
- Special-area chance.
- Minimum enemy-group size.
- Maximum enemy-group size.
- Normal-area message.
- Special-area message.

`ExplorationSession` stores mutable progress and session-specific state.

It also ensures that:

- A merchant cannot appear more than once per session.
- An epic encounter cannot be offered more than once per dungeon session.
- Exploration progress survives regular battles.
- A failed final-boss attempt can return the session to the previous position.

The final dungeon position bypasses random event generation and starts the configured boss encounter.

## Battle architecture

The battle system is divided into the following responsibilities:

- `Battle`
  - Coordinates the battle lifecycle and character actions.
- `BattleCoordinator`
  - Creates runtime enemies, starts battles and grants victory rewards.
- `EnemyBattleGroup`
  - Stores one to three enemy slots.
- `EnemyBattleSlot`
  - Associates one runtime enemy with a unique battle display name.
- `BattleSide`
  - Identifies whether a turn participant belongs to the party or enemy group.
- `BattleTurnEntry`
  - Represents one participant action inside the round queue.
- `BattleTurnOrderGenerator`
  - Calculates action counts, initiative scores and tie-breakers.
- `BattleTurnOrder`
  - Tracks the current queue position and skips defeated entries.
- `BattleTurnOrderScreen`
  - Displays the current and upcoming actions.
- `BattleEscapeService`
  - Calculates the two-stage flee attempt.
- `EscapeAttemptResult`
  - Stores the result and relevant flee chances.
- `AllyTargetMenu`
  - Selects living, injured or defeated party members according to context.
- `EnemyTargetMenu`
  - Selects a living enemy target.
- `BattleRewardService`
  - Calculates EXP, gold, progression and consolidated drops.
- `BattleRewards`
  - Stores the complete reward result.
- `CharacterProgressionResult`
  - Stores one ally's EXP, levels and resource-restoration result.
- `BattleRewardScreen`
  - Displays party progression and collected rewards.

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
ally and enemy actions
        ↓
BattleResult
        ↓
BattleRewardService
        ↓
BattleRewards
        ↓
BattleRewardScreen
```

## Enemy battle groups

`EnemyBattleGroup` contains one to three `EnemyBattleSlot` objects.

Each slot:

- Owns one runtime enemy.
- Provides its battle display name.
- Reports whether its enemy remains alive.

When repeated enemies exist, their slot names are numbered.

Example:

```text
Passavento 1
Passavento 2
Lobermo
```

Defeated slots remain visible but cannot act or be selected as valid targets.

Changing the global maximum requires reviewing only the centralized group limit and interfaces designed around visible group size, rather than rewriting the battle hierarchy.

## Battle initiative

A battle round contains one unified queue for allies and enemies.

`BattleTurnOrderGenerator`:

1. Collects every living participant.
2. Calculates the average effective Speed of the opposing side.
3. Calculates how many actions each participant receives.
4. Creates one entry for each action.
5. Calculates each entry's initiative score.
6. Sorts entries by initiative, Luck and random final tie-breaker.

`BattleTurnOrder` keeps the queue snapshot for the current round and advances through living entries.

A defeated participant's future entries remain in the snapshot but are skipped.

A revived ally does not exist in the current queue snapshot and therefore acts only in the following round.

## Skill architecture

`Skill` currently stores:

- Required level.
- Damage type.
- Base damage.
- Base healing.
- MP cost.
- SP cost.
- Recovery type.
- Target type.
- Description.

Recovery effect and target are stored separately.

Examples:

- Healing with `SELF`.
- Healing with `SINGLE_ALLY`.
- Healing with `ALL_ALLIES`.
- Healing with `ALL_BUT_SELF`.
- Revival with `SINGLE_ALLY`.
- Revival with `ALL_ALLIES`.

This prepares the project for a future collection of independent skill effects, where one skill may apply damage, healing, buffs and debuffs using different targets.

## Reward architecture

`BattleRewardService` calculates:

- Total EXP.
- Total gold.
- Individual character progression.
- Independent drop rolls.
- Consolidated item quantities.

EXP is granted individually to every party member.

Gold and items are granted once to `Party`.

`CharacterProgressionResult` stores:

- Character reference.
- EXP received.
- Previous level.
- Current level.
- Whether HP, MP and SP were restored.

A surviving character who gains at least one level restores all resources.

A defeated character still receives EXP and may level up, but remains defeated and receives no resource restoration.

## Travel flow

`TravelCatalog` stores directed source-to-destination relationships.

A route must be registered separately for each permitted direction.

`World` decides whether travel is currently allowed:

- Completed combat scenarios permit travel.
- Incomplete combat scenarios keep travel locked.
- Cities may allow immediate travel.

`TravelMenu` displays every registered destination and returns the selected `Scenario`.

`World` then performs the scenario change and creates a new exploration session when required.

## UI boundaries

The UI package contains console-specific behavior:

- Reading and validating menu input.
- Displaying title, status and result information.
- Selecting allies and enemies.
- Asking for confirmations.
- Waiting for the player to continue.

Important UI components include:

- `ScenarioActionMenu`
- `TravelMenu`
- `InventoryMenu`
- `BattleItemMenu`
- `RecoveryItemUseMenu`
- `EpicEncounterMenu`
- `EnemyTargetMenu`
- `AllyTargetMenu`
- `BattleTurnOrderScreen`
- `BattleRewardScreen`
- `PartyStatusMenu`
- `CharacterStatusMenu`
- `GameOverScreen`

`InventoryMenu` controls the general inventory flow outside battle. It displays non-empty item categories, paginates their contents and opens item details.

`BattleItemMenu` is a separate battle-specific flow. It lists only items that can currently be used in battle and reports success only when an item produces an effect.

`RecoveryItemUseMenu` contains the shared recovery-item execution flow, including target selection, effect application and conditional consumption. This prevents the general and battle menus from duplicating item-use rules.

`ItemMenuFormatter` contains shared presentation rules such as deterministic column widths, name truncation and recovery-effect summaries. It does not decide item behavior.

`PartyStatusMenu` displays a compact overview of one to four allies and shared gold.

`CharacterStatusMenu` displays one character's detailed resources, attributes, progression and learned skills.

`World` does not store a `Scanner`. Console input is handled by the components that require it.

## Current temporary boundaries

The following implementations remain intentionally temporary:

- The exploration `REWARD` event still resolves the fixed `meruma` ID from the runtime `ItemCatalog`. Scenario-specific item and gold selection will move to external exploration reward pools.
- Only base item data and recovery-item behavior are currently externalized. Equipment and skill-scroll specialization will be defined when their corresponding systems exist.
- Recovery-item purification is represented in external data and runtime definitions, but it cannot remove effects until the status-effect system is implemented.
- The merchant has no merchandise.
- Playable characters and test skills are constructed in `Main`.
- Temporary recovery and revive skills exist for testing.
- Playable classes are not represented as data.
- Equipment, buffs and debuffs are not implemented.
- Bree is not fully implemented.
- Defeat opens `GameOverScreen` instead of rescuing the party in Bree.
- Save and load are not implemented.
- `Battle` remains large and may later be divided into more focused action services.

These limitations are tracked in `V3_ROADMAP.md` and should be replaced incrementally instead of through one large rewrite.