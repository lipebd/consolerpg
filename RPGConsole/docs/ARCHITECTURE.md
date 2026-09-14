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
| `rpg` | Application entry point, dependency construction and shared currency presentation | `Main`, `GameCurrency` |
| `rpg.characters` | Character state, name validation and selection, playable-character level boundaries, natural and equipment-modified attributes, equipped slots, party ownership and enemy ranks | `GameCharacter`, `CharacterStats`, `CharacterEquipment`, `PlayableCharacter`, `PlayableCharacterLevelRules`, `Party`, `Enemy`, `EnemyRank`, `CharacterNameRules`, `CharacterNameCatalog`, `CharacterNameLoader`, `CharacterNameSelector` |
| `rpg.classes` | Immutable playable-class definitions, symbols, external loading, lookup and natural-growth calculation | `PlayableClassDefinition`, `PlayableClassLoader`, `PlayableClassCatalog`, `CharacterGrowthCalculator` |
| `rpg.development` | Isolated configurable party construction for development runs | `DevelopmentPartyFactory` |
| `rpg.battle` | Battle coordination, skill execution and centralized combat calculations | `Battle`, `BattleResult`, `BattleCoordinator`, `SkillExecutor`, `SkillExecution`, `SkillExecutionContext`, `SkillEffectResult`, `DamageType`, `DamageCalculator`, `RecoveryCalculator`, `EffectResistanceCalculator`, `EnemyBattleGroup`, `BattleTurnOrder`, `BattleEscapeService` |
| `rpg.battle.rewards` | Reward calculation, temporary and equipment modifiers, immutable reward snapshots and individual progression results | `BattleRewardService`, `RewardCalculator`, `PartyRewardModifierState`, `BattleRewardModifiers`, `BattleRewards`, `CharacterProgressionResult` |
| `rpg.effects` | Mutable combat-effect state and reusable attribute-stage rules | `CombatEffectState`, `AttributeStageState`, `AttributeStageRules`, `SpecialBattleStateType` |
| `rpg.skills` | Immutable skill and component definitions, external loading and closed skill vocabularies | `SkillDefinition`, `SkillEffectDefinition`, `SkillLoader`, `SkillCatalog`, `SkillTargetType`, `SkillEffectType`, `SkillExecutionPhase`, `SkillConditionType` |
| `rpg.items` | External item definitions, runtime item behavior, shared inventory and individual drop rules | `ItemDefinition`, `ItemDefinitionCatalog`, `ItemLoader`, `RecoveryItemDefinition`, `RecoveryItemLoader`, `ItemFactory`, `Item`, `BasicItem`, `RecoveryItem`, `ItemCatalog`, `Inventory`, `DropEntry` |
| `rpg.items.equipment` | Immutable equipment specialization, loading, lookup, compatibility vocabulary and inventory-to-character transfers | `EquipmentDefinition`, `WeaponDefinition`, `ArmorDefinition`, `TrinketDefinition`, `EquipmentLoader`, `EquipmentCatalog`, `EquipmentService`, `WeaponType`, `ArmorWeight`, `TrinketType` |
| `rpg.enemies` | Enemy definitions and external enemy/drop loading | `EnemyDefinition`, `EnemyCatalog`, `EnemyLoader`, `EnemyDropCatalog`, `EnemyDropLoader` |
| `rpg.world` | Active world state, scenario discovery, rescue coordination and high-level gameplay coordination | `World`, `WorldProgress` |
| `rpg.world.scenarios` | External scenario definitions, runtime scenario hierarchy and scenario creation | `ScenarioDefinition`, `ScenarioDefinitionCatalog`, `ScenarioLoader`, `CombatScenarioDefinition`, `CombatScenarioLoader`, `DungeonDefinition`, `DungeonLoader`, `ScenarioFactory`, `Scenario`, `CombatScenario`, `Field`, `Dungeon`, `City`, `ScenarioCatalog`, `CombatScenarioAction`, `CityAction` |
| `rpg.world.encounters` | Encounter availability and weighted group generation | `EncounterEntry`, `EncounterCatalog`, `EncounterLoader`, `EncounterGenerator` |
| `rpg.world.exploration` | Exploration configuration, progress and event selection | `ExplorationSettings`, `ExplorationSession`, `ExplorationEventGenerator`, `ExplorationEventType`, `ExplorationAreaRestriction` |
| `rpg.world.inns` | Immutable city-INN definitions, complete city coverage validation and lookup by stable or city scenario ID | `InnDefinition`, `InnCatalog`, `InnLoader` |
| `rpg.world.shops` | External shop and offer definitions, runtime stock state, weighted offer generation and atomic commercial transactions | `ShopAccessType`, `ShopDefinition`, `ShopCatalog`, `ShopLoader`, `ShopStockEntry`, `ShopStockCatalog`, `ShopStockLoader`, `ShopOfferState`, `ShopState`, `ShopStateCatalog`, `ShopStateGenerator`, `ShopTransactionService`, `ShopPurchaseResult`, `ShopSaleResult` |
| `rpg.world.rewards` | External exploration reward pools, validation and weighted selection | `ExplorationRewardEntry`, `ExplorationRewardCatalog`, `ExplorationRewardLoader`, `ExplorationRewardGenerator`, `ExplorationReward`, `ExplorationRewardType` |
| `rpg.world.travel` | External directed-route definitions, loading and runtime unlock progress | `TravelRouteDefinition`, `TravelCatalog`, `TravelRouteLoader`, `TravelProgress` |
| `rpg.world.transport` | External paid city-transport definitions, loading and destination lookup | `CityTransportDefinition`, `CityTransportCatalog`, `CityTransportLoader` |
| `rpg.ui` | Console menus, character creation, navigation, target selection, shared formatters and information screens | `TitleScreen`, `CharacterCreationMenu`, `PartyRescueScreen`, `CombatScenarioMenu`, `CityMenu`, `CityLocationMenu`, `InnMenu`, `RouteMenu`, `CityTransportMenu`, `InventoryMenu`, `EquipmentMenu`, `BattleItemMenu`, `RecoveryItemUseMenu`, `EpicEncounterMenu`, `BattleRewardScreen`, `BattleTurnOrderScreen`, `PartyStatusMenu`, `CharacterStatusMenu`, `PlayableClassDetailFormatter`, `EquipmentDetailFormatter`, `SkillDetailFormatter`, `RewardModifierFormatter`, `ShopMenu` |

## Application initialization

`Main` acts as the current composition root. It loads external definitions, constructs runtime catalogs and connects the objects required by the game.

Initialization includes:

1. Creating the shared `Scanner`.
2. Loading and validating external random character names.
3. Loading playable-class definitions.
4. Loading skill headers and ordered skill-effect components and creating `SkillCatalog`.
5. Loading enemy and base item definitions.
6. Loading equipment, recovery-item and skill-scroll specialized definitions.
7. Creating the runtime `ItemCatalog` through `ItemFactory`.
8. Creating `EquipmentService` from the item and equipment catalogs.
9. Loading enemy drops and encounter definitions.
10. Loading common scenario definitions.
11. Loading city-transport definitions, INN definitions with complete city coverage and combat-scenario definitions whose rescue destinations are validated against scenario and INN catalogs.
12. Loading shop definitions and their possible stock entries.
13. Generating one runtime state for every shop through `ShopStateGenerator`.
14. Loading exploration-reward, dungeon and scenario-branch definitions.
15. Creating and registering runtime scenarios through `ScenarioFactory`.
16. Resolving the initial scenario and creating `WorldProgress`.
17. Loading adjacent travel routes and creating `TravelProgress`.
18. Creating one shared `CharacterGrowthCalculator`.
19. Creating either the confirmed new-game protagonist or an isolated development party according to the application arguments.
20. Creating `World` with the required catalogs, services, shop states, progress state and shared dependencies.
21. Starting the world loop.

External data is loaded before gameplay. Invalid definitions or unresolved references prevent the game from starting with partially inconsistent catalogs.

INN definitions are loaded before combat-scenario definitions. This makes `InnCatalog` available to `CombatScenarioLoader`, which can reject rescue destinations that are not registered cities with a valid INN.

During a normal run, `Main` repeatedly opens `TitleScreen` and `CharacterCreationMenu` until the player confirms a protagonist. The confirmed character is created as a level-1 Flagelo and becomes the only initial member and leader of the new `Party`. Cancelling character creation returns to the title screen without creating partial character state.

When the `--dev-party` argument is present, `Main` bypasses the normal title and creation flow and delegates party construction to `DevelopmentPartyFactory`. This development-only path supports one to four configured members with distinct classes, arbitrary positive levels, configurable initial gold and optional temporary reward modifiers while remaining isolated from normal new-game behavior.

`WorldProgress`, `TravelProgress` and `ShopStateCatalog` represent separate runtime concerns. `WorldProgress` records discovered scenarios, `TravelProgress` records unlocked adjacent routes, and `ShopStateCatalog` preserves every shop's selected offers and remaining stock for the current program execution.

## Character model

```text
GameCharacter
├── PlayableCharacter
└── Enemy

Party
└── one to four PlayableCharacter objects
```

`CharacterStats` groups and validates immutable initial values:

- Maximum HP, MP and SP.
- Attack and defense.
- Magic and resistance.
- Speed and luck.

Maximum HP and all six regular attributes must be positive. Maximum MP and SP may be zero. Loaders add file, line and content-ID context when invalid stats prevent a definition from being created.

`GameCharacter` stores mutable current HP, MP and SP and implements behavior shared by allies and enemies, including:

- Defending.
- Spending MP and SP.
- Receiving damage.
- Recovering HP, MP and SP and reviving.
- Applying, reducing and clearing temporary combat effects.
- Returning effective attributes after stage modifiers and special structural states.
- Returning an immutable snapshot of its natural attributes, without temporary combat effects.
- Checking whether the character is alive.
- Restoring all resources for a living character.

`PlayableCharacter` adds individual progression and player-controlled behavior:

- Level.
- EXP.
- Its playable-class definition.
- Its three equipment slots.
- Immutable class skill definitions.
- Learned skill IDs stored in acquisition order.
- Automatic `STARTER` and `LEVEL` skill acquisition.
- Explicit `SCROLL` skill learning and replacement.
- Natural-stat recalculation through `CharacterGrowthCalculator` after gaining one or more levels.

`PlayableCharacterLevelRules` owns the supported level range and next-level experience formula. The current range is level 1 through level 20. `PlayableCharacter` delegates boundary checks and experience requirements to that rule, uses `long` intermediate arithmetic when receiving EXP and keeps current EXP at zero after reaching the maximum level.

`SkillDefinition` and `EquipmentDefinition` reuse the same range validation, preventing external content from requiring an unreachable character level.

Natural growth has one formula owner:

```text
PlayableClassDefinition + target level
                    ↓
        CharacterGrowthCalculator
                    ↓
             CharacterStats
                    ↓
          PlayableCharacter
```

The calculator derives the complete natural-stat snapshot directly from the class definition and target level. It does not increment the character's previous values, which prevents accumulated drift and allows a multi-level gain to replace natural stats once after the final level is known.

`PlayableClassDefinition` also stores one visible Unicode code point as the class symbol. Symbols provide compact class identification in interfaces without replacing the complete class name in detailed views. Primary and secondary attributes remain domain data, while `PlayableClassDetailFormatter` translates their growth priority into the `↑↑` and `↑` presentation used during character creation.

`Party` stores:

- One to four playable characters.
- Shared inventory.
- Shared gold.
- Temporary global reward-modifier state.
- The combined party-wide gold modifier supplied by equipped trinkets.
- Future party title.

`Enemy` adds:

- Enemy rank.
- Description.
- Runtime drop table.

## Party ownership

Shared state belongs to `Party`:

- Inventory.
- Gold.
- Temporary global EXP and gold modifiers together with their remaining rewarded victories.
- Future party title.

`Party.MAXIMUM_GOLD` defines the liquid-GOLD ceiling of `999999999`. `Inventory.MAXIMUM_QUANTITY_PER_ITEM` defines the limit of `999` units for each stable item ID while leaving the number of different owned item IDs unlimited. Both mutation owners use `long` intermediate arithmetic and reject additions that do not fit completely.

Individual state belongs to `PlayableCharacter`:

- HP.
- MP.
- SP.
- Attributes.
- Level.
- EXP.
- Playable-class definition, class skill definitions and learned skill IDs.
- Defensive state.
- One equipped weapon, armor and trinket.
- The individual EXP modifier supplied by its equipped trinket.
- Temporary combat effects.

This separation allows future recruitment to grow the party without duplicating shared resources. The first member remains the permanent protagonist and party leader.

## Initial character creation

Playable-character naming is centralized in `CharacterNameRules`. It normalizes names to Unicode NFC, removes outer spaces, collapses repeated ordinary spaces and limits the result to 16 Unicode code points. Valid names contain Unicode letters separated only by single spaces, hyphens or apostrophes. Blank names, controls, malformed separators, numbers, unrelated punctuation and overlong values are rejected.

Every `PlayableCharacter` validates and stores its normalized name during construction. `Party` additionally rejects a new member whose normalized name matches an existing member without regard to letter case. Accents remain significant.

External random names follow this flow:

```text
character_names.txt
        ↓
CharacterNameLoader
        ↓
CharacterNameCatalog
        ↓
CharacterNameSelector
        ↓
CharacterCreationMenu
```

`CharacterNameLoader` applies the same domain rules, rejects case-insensitive duplicates and requires at least four valid names so that a complete party can eventually receive distinct names. `CharacterNameSelector` chooses uniformly from the names remaining after caller-provided exclusions.

`CharacterCreationMenu` allows the player to enter the protagonist's name or request a random one. Random selection may be repeated before confirmation, and the current proposal is excluded from the next roll. The confirmation view displays the proposed name, the Flagelo's complete class name, its primary and secondary specializations and its external description.

The menu returns only the confirmed name. `Main` creates the `PlayableCharacter` and initial `Party` afterward, so cancellation returns to `TitleScreen` without leaving partially created domain state.

Development construction remains a separate path:

```text
--dev-party
      ↓
DevelopmentPartyFactory
      ↓
Party with one to four configured members
```

`DevelopmentPartyFactory` validates party size, levels within the supported playable-character range and distinct class IDs. It creates each character through the normal domain constructor, advances configured levels through normal progression and restores resources to the resulting maximums. The first configured member becomes the leader.

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
- Optional preferred rescue scenario ID.
- Required fallback rescue scenario ID.
- Rescue description.

`ScenarioFactory` propagates these rescue values from `CombatScenarioDefinition` when creating each runtime `Field` or `Dungeon`.

`Field` represents open exploration areas.

`Dungeon` adds `DungeonSettings`, including:

- A required group of one to three boss enemy IDs.
- An optional group of one to three epic enemy IDs.

`City` represents non-combat settlements. Cities use their own action flow, separate functional locations from adjacent exits and do not create exploration sessions. Every registered city has exactly one externally configured INN. Bree currently also provides a fixed shop, while taverns remain a future system. Paid city transport is available through the shared world flow.

`ScenarioCatalog` resolves stable scenario IDs to runtime scenario objects.

## Definition and runtime separation

Enemy data is separated into two layers:

| Layer | Example | Purpose |
|---|---|---|
| Definition | `EnemyDefinition` for `lobermo` | Reusable immutable template loaded once |
| Runtime object | One Lobermo inside a battle | Mutable HP, resources and battle state |

The same `EnemyDefinition` can create many independent `Enemy` objects without sharing mutable battle state.

This distinction also permits repeated enemies in one battle. Two Lobermos created from the same definition have independent HP and defensive state.

The same separation applies to items, scenarios, playable classes and skills. Scenario definitions are transformed into runtime `City`, `Field` and `Dungeon` objects by `ScenarioFactory`. A `PlayableCharacter` references immutable class and skill definitions, while one mutable `SkillExecution` represents the state of a single skill use. A separate mutable `Skill` object is unnecessary because persistent per-use state does not belong to the definition.

Shop data follows the same separation:

| Layer | Purpose |
|---|---|
| `ShopDefinition` | Immutable identity, scenario association, access type, offer-slot count, name and description |
| `ShopStockEntry` | Immutable possible offer, including item, price percentage, initial stock and selection weight |
| `ShopOfferState` | Mutable remaining quantity of one selected offer |
| `ShopState` | Active offers preserved for one shop during the current execution |
| `ShopStateCatalog` | Stable lookup of every generated shop state |

`ShopStateGenerator` selects each shop's active offers once during application initialization. Immutable possible entries remain separate from mutable remaining stock, and exhausted slots do not activate entries that were not initially selected.

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
CombatScenarioMenu selects EXPLORE
        ↓
World advances ExplorationSession
        ↓
normal or special area is determined
        ↓
ExplorationEventGenerator selects event
        ↓
World dispatches the event consequence
```

A merchant roll becomes `NOTHING` after the session has already encountered its merchant.

When a merchant event occurs, `World` selects uniformly among the `EXPLORATION` shops associated with the current scenario. It then resolves the selected merchant through `ShopStateCatalog` and opens the shared `ShopMenu`. The same merchant definition always resolves to the same runtime state during the current program execution.

`ExplorationSettings` defines:

- Exploration length.
- Battle chance.
- Reward chance.
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

## Exploration reward architecture

Exploration rewards use a separate data flow from battle rewards:

```text
data/exploration_rewards.txt
        ↓
ExplorationRewardLoader
        ↓
ExplorationRewardCatalog
        ↓
ExplorationRewardGenerator
        ↓
ExplorationReward
        ↓
World grants an Item or gold to Party
```

`ExplorationRewardEntry` stores one immutable pool entry containing:

- Reward type.
- Optional item ID.
- Minimum and maximum quantity.
- Relative selection weight.
- Area restriction.

`ExplorationRewardLoader` validates scenario references, item references, entry fields, exact duplicates and the availability of every required normal or special-area pool.

`ExplorationRewardCatalog` groups entries by scenario ID and returns those eligible for the current area type. `ANY` entries remain eligible in both normal and special areas.

`ExplorationRewardGenerator` selects exactly one eligible entry according to relative weights and generates an inclusive random quantity within its configured range.

`ExplorationReward` represents the selected runtime result. `World` resolves item IDs through `ItemCatalog`, adds item quantities to the shared `Inventory`, and adds gold directly to `Party`.

Exploration rewards are repeatable and remain independent from the individual drop rolls performed after battles.

## Battle architecture

The battle system is divided into the following responsibilities:

- `Battle`
  - Coordinates the battle lifecycle, target selection, presentation and character actions.
- `BattleCoordinator`
  - Creates runtime enemies, starts battles and grants victory rewards.
- `SkillExecutor`
  - Evaluates component conditions and applicability, performs trigger and resistance tests and delegates each applied operation to its single domain owner.
- `SkillExecution`
  - Preserves the mutable state and accumulated results of one skill use, including deferred `POST_ACTION` components.
- `SkillExecutionContext`
  - Stores the user, the targets selected once for the complete skill use and a snapshot of equipped weapon base damage.
- `SkillEffectResult` and `SkillEffectOutcome`
  - Report `APPLIED`, `RESISTED` or `NOT_TRIGGERED` without requiring the interface to infer what happened.
- `DamageCalculator`
  - Calculates physical and magical damage for skills and basic attacks.
- `RecoveryCalculator`
  - Calculates attribute-scaled HP recovery and revival power.
- `EffectResistanceCalculator`
  - Centralizes the LCK-against-RES resistance formula and its balance factor.
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
  - Selects living or defeated party members according to context.
- `EnemyTargetMenu`
  - Selects a living enemy target.
- `BattleRewardService`
  - Calculates EXP, gold, progression and consolidated drops; captures natural-stat snapshots around level gains and identifies newly unlocked skills.
- `BattleRewards`
  - Stores the complete reward result.
- `CharacterProgressionResult`
  - Stores one ally's EXP, levels, natural-stat snapshots, newly unlocked skills and resource-restoration result.
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
SkillExecutor and combat calculators when required
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

Skill content is split between two external files and two immutable definition types:

- `skills.txt` defines the skill identity, class, acquisition type, level requirement, resource costs, name and description.
- `skill_effects.txt` defines ordered components, execution phase, effect type, target type and effect-specific data.
- `SkillDefinition` combines one header with its validated ordered components.
- `SkillEffectDefinition` validates the fields permitted and required by each component type.

`SkillLoader` reads both files, validates cross-references and component ordering and registers complete definitions in `SkillCatalog`.

`SkillAcquisitionType` distinguishes:

- `STARTER` skills, which temporarily support the Flagelo before the level-7 class choice.
- `LEVEL` skills, which are learned automatically when their configured level requirement is reached.
- `SCROLL` skills, whose configured level is a minimum requirement and which must be learned explicitly from an item.

`PlayableCharacter` stores learned skills through stable IDs while continuing to reference the unique definitions owned by `SkillCatalog`. Automatically available skills are learned during character construction and after level gains. Available skills are displayed with non-scroll skills first, followed by learned scroll skills in learning order.

A character may learn at most two `SCROLL` skills. Once both slots are occupied, a new scroll skill can replace only another learned `SCROLL` skill; `STARTER` and `LEVEL` skills cannot be replaced through this flow.

Skill-scroll specialization uses a separate external link:

```text
items.txt
skills.txt
playable_classes.txt
        ↓
skill_scrolls.txt
        ↓
SkillScrollLoader
        ↓
SkillScrollDefinitionCatalog
        ↓
ItemFactory
        ↓
SkillScroll
```

`skill_scrolls.txt` links one `SKILL_SCROLL` item ID to one `SCROLL` skill ID and its compatible class ID. `SkillScrollLoader` validates all three catalogs, rejects class mismatches and requires complete one-to-one specialization for every scroll item and scroll skill.

`SkillScrollService` coordinates character learning with party-inventory consumption. It removes exactly one scroll only after `PlayableCharacter` reports successful learning or replacement. Rejected and cancelled attempts preserve both the inventory and the character's learned skills.

One use follows this lifecycle:

```text
select targets once
        ↓
create SkillExecutionContext
        ↓
execute PRE_ACTION components
        ↓
execute PRIMARY components
        ↓
finishConsumedTurn
        ↓
execute deferred POST_ACTION components
```

For each component, `SkillExecutor` evaluates:

```text
condition
    → applicability
    → one component trigger roll
    → one resistance roll per eligible target, when resistible
    → application
```

A blank external trigger chance means 100%. A component trigger is rolled once and shared across its targets; resistance remains individual because it uses each target's effective RES. Negative attribute stages and dissipation are resistible. Distorção Ancestral is reversible but not resistible and remains outside purification and dissipation.

Damage, recovery, attribute-stage mutation and resource changes remain owned by their existing calculators or character operations. This keeps `SkillExecutor` responsible for orchestration instead of duplicating domain formulas.

`DAMAGE_POWER_MODIFIER` affects every later compatible damage component against the same target during the same `SkillExecution`. Multiple modifiers accumulate with overflow protection and never leak into another target or skill use.

Player basic attacks read the equipped weapon's damage type, normal-attack name and base weapon damage. Without a weapon, they fall back to physical damage and zero weapon power. Offensive skill components always add 100% of the equipped weapon's base damage, while the component's own `DamageType` independently selects ATK against DEF or MAG against RES. `DamageType` therefore belongs to `rpg.battle`, where both attacks and skills consume it.

## Equipment architecture

Every equipment item has two linked definitions:

- `items.txt` supplies its stable identity, category, commercial data, name and description.
- `weapons.txt`, `armors.txt` or `trinkets.txt` supplies its equipment-specific fields.

`EquipmentLoader` validates the specialized files against `ItemDefinitionCatalog` and registers all definitions in one `EquipmentCatalog`, keyed by the same stable item ID. Runtime equipment remains represented by the corresponding `BasicItem` from `ItemCatalog`; equipping transfers one owned unit out of `Inventory` and stores its immutable definition in `CharacterEquipment`.

`CharacterEquipment` owns exactly three optional slots: weapon, armor and trinket. Slot mutation is package-private and is exposed through validated operations on `PlayableCharacter`. `EquipmentService` coordinates transfers between party inventory and character: a replacement returns the previous item to inventory, while unequipping returns the removed item.

Compatibility rules are centralized through `PlayableCharacter.canEquip()`:

- Every class may equip every weapon when its level requirement is met.
- Armor requires an exact match between the armor's `ArmorWeight` and the class's armor weight.
- Every class may equip every trinket when its level requirement is met.

The natural values stored by `GameCharacter` remain separate from structural equipment bonuses. Public maximum-resource and attribute getters combine natural values with equipment before applying temporary combat states. Removing or replacing equipment clamps current HP, MP and SP to their new maximums; it does not heal resources when maximums increase.

The same `EquipmentMenu` instance is created by `World` and shared by `InventoryMenu` and `CharacterStatusMenu`. This supports both directions without duplicating rules:

```text
Inventory -> equipment item -> character -> confirmation
Status -> character -> equipment slot -> inventory item -> confirmation
```

`EquipmentDetailFormatter` produces shared details and change-only confirmation comparisons. Player-facing text calls weapon base damage `Poder`; `DBA` remains internal vocabulary. Equipped units are absent from inventory rather than marked with an `(E)` flag.

`EquipmentService` checks inventory capacity before returning an unequipped or replaced item. A transfer that cannot return every participating item is rejected before changing equipment, while defensive rollback checks protect items already removed from inventory.

## Battle reward architecture

`BattleRewardService` calculates:

- Base EXP and gold after enemy-rank multiplication and aggregation.
- Temporarily modified global EXP and gold.
- Final EXP for each character after temporary and equipped individual modifiers are combined additively.
- Calculated party gold after temporary and all equipped trinket gold modifiers are combined additively.
- Individual character progression.
- Independent drop rolls.
- Consolidated item quantities.

`RewardCalculator` is the single owner of additive percentage combination, half-up rounding and reward-result overflow checks.

`PartyRewardModifierState` stores at most one temporary EXP modifier and one temporary gold modifier. Both may coexist, but a second modifier of the same type cannot be activated while the first remains active. Their durations decrease only after a victory grants rewards.

`BattleRewardModifiers` is an immutable snapshot of the modifiers used for one victory. It preserves the applied percentages and post-reward remaining durations for presentation even after the mutable party state advances.

EXP is granted individually to every party member.

Gold and items are granted once to `Party`.

Calculated gold and item drops are granted only up to their domain limits. `BattleRewards` stores the granted and discarded portions separately so the normal reward summary remains accurate and an additional limit notice appears only when something could not be received.

`CharacterProgressionResult` stores:

- Character reference.
- EXP received.
- Previous level.
- Current level.
- Previous natural-stat snapshot.
- Current natural-stat snapshot.
- Skill definitions unlocked while crossing the level interval.
- Whether HP, MP and SP were restored.

A surviving character who gains at least one level restores all resources after the new natural maximums are installed.

A defeated character still receives EXP and may level up, but remains defeated and receives no resource restoration.

An equipped trinket's EXP modifier applies only to its wearer. Gold modifiers from every equipped trinket are summed for the party. All applicable percentages are combined before `RewardCalculator` rounds once. Reward-modifier potions remain deferred; they will activate the existing party state rather than introduce another reward formula.

## Adjacent travel flow

`TravelCatalog` stores immutable directed `TravelRouteDefinition` entries loaded from `data/travel_routes.txt`.

A route must be registered separately for each permitted direction. `TravelProgress` tracks which route IDs are currently unlocked and exposes only unlocked routes from a requested source scenario.

`World` decides whether adjacent-route selection is currently allowed:

- Completed combat scenarios permit following an unlocked route.
- Incomplete combat scenarios keep adjacent routes unavailable.
- Cities expose unlocked adjacent routes as exits.

`RouteMenu` receives the currently available route definitions and returns the selected `TravelRouteDefinition`. It does not resolve destinations or change world state.

`World` resolves the destination through `ScenarioCatalog`. When the destination is a combat scenario, the route also determines the initial exploration position. City destinations do not use exploration positions.

City exits reuse this route infrastructure. Their display names can represent gates, roads, portals or other exit types, while route descriptions provide their flavor and destination text.

## Scenario discovery

`WorldProgress` stores the scenario IDs discovered during the active game. It validates scenario references through `ScenarioCatalog` and exposes discovered IDs without exposing mutable internal state.

The initial scenario is marked as discovered when `World` is created. Every successful scenario change records its destination, regardless of whether entry occurs through an adjacent route, an exploration branch, paid city transport or full-party rescue.

Rescue may discover a configured fallback city that was not previously known. This discovery does not unlock unrelated adjacent routes.

Scenario discovery remains independent from `TravelProgress`. Discovering a scenario does not unlock an adjacent route, and unlocking a route does not automatically discover its destination.

## Paid city-transport flow

`CityTransportCatalog` stores immutable `CityTransportDefinition` entries loaded from `data/city_transport.txt`. Each definition identifies one destination city, its fixed price and its player-facing travel description.

The loader validates that every destination references an existing `CITY`, that prices are positive and that no destination city is registered more than once.

`World` builds the available destination list by selecting configured cities already recorded in `WorldProgress`. When transport begins from a city, the current city is excluded.

`CityTransportMenu`:

- displays discovered destinations before undiscovered `--` slots;
- derives the total slot count from registered `CITY` scenarios;
- displays prices and available party currency through `GameCurrency`;
- keeps unaffordable destinations visible;
- validates the selected destination and available gold;
- requests confirmation before payment;
- preserves gold when travel is cancelled or unaffordable;
- deducts gold only after all validations and confirmation succeed;
- returns the selected `CityTransportDefinition` without changing the scenario.

`World` resolves the selected destination through `ScenarioCatalog`, changes directly to that city and displays the definition's travel description.

Paid city transport is available from cities, fields and dungeons, including incomplete combat-scenario exploration, but never during battle. Leaving an incomplete exploration discards its current `ExplorationSession`. Permanent party and world state already obtained remains unchanged. Entering that combat scenario again creates a fresh exploration session from the position defined by the route used to return.

## Full-party rescue flow

`Battle` remains responsible only for battle resolution. After displaying its final defeat message, it returns `BattleResult.DEFEAT` to `World`.

```text
Battle
    -> BattleResult.DEFEAT
    -> World resolves rescue city and INN
    -> World calculates and deducts rescue cost
    -> Party.fullyRestoreMembers()
    -> PartyRescueScreen
    -> World changes to the rescue city
```

`World` selects the optional preferred rescue city only when it is already present in `WorldProgress`. Otherwise, it selects the required fallback city. Runtime type validation ensures that the selected scenario is a `City`, while startup validation guarantees that it has an INN.

The rescue cost uses the greater value between the destination INN price and 10% of current party gold rounded upward, then limits that value to the gold currently available. A zero-gold party is rescued without calling `Party.spendGold(0)`.

Complete restoration belongs to `Party.fullyRestoreMembers()`. The method revives defeated members and then fully restores every member's HP, MP and SP. Both `World` and `InnMenu` reuse this operation.

`PartyRescueScreen` is presentation-only. It displays the external narrative, selected city, restoration message and formatted cost, then waits for ENTER.

Random and optional epic encounter methods propagate whether the current exploration should continue through boolean return values. A defeat returns `false` to `explore()`, preventing the failed position from advancing or completing without terminating the world loop. Boss encounters already inspect `BattleResult` directly.

After confirmation, `World.changeScenario()` moves to the city, records its discovery and discards the failed `ExplorationSession`. It does not modify `TravelProgress`, shop state or other persistent runtime progress.

## City, shop and INN flow

`World` dispatches the current scenario to a type-specific menu:
- `CombatScenarioMenu` is exclusive to fields and dungeons.
- `CityMenu` is exclusive to cities.

- Unsupported runtime scenario types cause an internal-state error.

`CityMenu` receives an incrementally built list of available `CityAction` values. It omits actions that currently have no functional destination, keeps continuous numeric selection and separates navigation actions from inventory and party status through whitespace.

The paid `Viajar` action is included only when at least one configured and discovered destination city is available. It opens `CityTransportMenu` without mixing paid transport with city exits or adjacent-route unlocking.

The city flow separates three navigation concepts:

- Visiting a functional city location.
- Leaving through an unlocked adjacent route.
- Traveling directly to another discovered city through paid transport.

`CityLocationMenu` displays the names of currently functional locations. Location descriptions and actions are shown only after the player enters the selected location. The list may combine the city's optional INN, every associated `CITY` shop and every associated crafter NPC, while unfinished services such as taverns remain absent instead of appearing as placeholders.

Current INN data flow:

```text
data/inns.txt
    -> InnLoader
    -> InnDefinition
    -> InnCatalog
    -> World
    -> CityLocationMenu
    -> InnMenu
```

`InnCatalog` resolves INNs by stable ID and by city scenario ID and rejects multiple definitions for the same city. After loading, `InnLoader` verifies that every registered city has an INN; together, these rules guarantee exactly one INN per city.

`InnMenu` owns the iterative resting interaction and uses the shared `Party` instance. It validates whether rest is necessary and affordable, requests confirmation, charges shared gold and delegates complete restoration to `Party.fullyRestoreMembers()`.

`City`, location and INN navigation remain iterative. Returning from the INN leads back to location selection, and returning again leads back to the city menu without recursive menu calls.

Current shop data and runtime flow:

```text
data/shops.txt
    -> ShopLoader
    -> ShopCatalog
              \
               -> ShopStateGenerator
              /
data/shop_stock.txt
    -> ShopStockLoader
    -> ShopStockCatalog
    -> ShopStateCatalog
    -> World
    -> ShopMenu
    -> ShopTransactionService
```

`CITY` shops are exposed through `CityLocationMenu`. `EXPLORATION` shops are exposed through the existing merchant event. Both access types resolve the same `ShopState` objects and open the same `ShopMenu`.

`ShopMenu` controls player-facing navigation, deterministic sorting, pagination, quantity selection, transaction confirmation and feedback. It displays every monetary value through `GameCurrency`.

`ShopTransactionService` owns the shared transaction rules:

- Purchases validate the selected active offer, quantity, remaining stock, total and available party gold before changing state.
- Successful purchases deduct party gold, add items to the shared inventory and reduce only the selected offer's remaining stock.
- Sales validate the selected item, commercial availability, owned quantity, total and resulting party gold before changing state.
- Successful sales remove items from the shared inventory and credit party gold.
- Items sold by the player never enter shop stock.
- Cancelled, rejected or capacity-exceeding transactions preserve gold, inventory and shop stock.
- Purchase totals use overflow-checked arithmetic and receiving capacities are checked against the explicit GOLD and per-item limits.
- Purchases and sales validate every receiving limit before spending GOLD or removing items.
- Defensive rollback operations verify that every removed resource is restored completely.
- Every transaction is atomic from the player's perspective.

An item is sellable to every shop when it defines `shopPurchasePrice`. `KEY_ITEM` definitions cannot provide that price and the category is excluded from the sale interface.

## Crafting architecture

Crafting extends the existing data-driven city-location flow without introducing a separate scenario type or mutable crafter runtime object.

Recipe data flow:

```text
data/crafting_recipes.txt
    -> CraftingRecipeLoader
    -> CraftingRecipeDefinition
        -> ordered CraftingMaterial requirements
    -> CraftingRecipeCatalog
```

Crafter data and interaction flow:

```text
data/crafters.txt
    -> CrafterLoader
    -> CrafterDefinition
    -> CrafterCatalog
    -> World
    -> CityLocationMenu
    -> CraftingMenu
    -> CraftingService
    -> Party inventory
```

`CraftingRecipeDefinition` stores a stable recipe ID, one output item ID, a player-facing name and narrative description, and an ordered immutable list of required items. Output quantity is fixed at one and therefore is not duplicated as mutable or external state.

`CraftingMaterial` represents one item requirement and positive quantity. Its name is retained from the original contribution, but its item reference is category-neutral: `MATERIAL` is not required for crafting participation.

`CraftingRecipeLoader` validates output and ingredient references against `ItemDefinitionCatalog`. Recipe definitions do not copy item properties, and runtime presentation later resolves complete items through `ItemCatalog`.

`CrafterDefinition` stores immutable NPC content and an ordered list of recipe IDs. It does not require a parallel mutable crafter class because the NPC currently owns no runtime state.

`CrafterLoader` accepts only city scenario references in the initial integration block and validates every recipe reference. `CrafterCatalog` supports lookup by stable ID and ordered grouping by scenario ID. Unlike `InnCatalog`, it deliberately permits multiple definitions in one city.

`Main` loads common item definitions before crafting recipes and loads scenarios before crafters. This makes every external reference available before its dependent definition is constructed.

`World` adds every crafter associated with the current city to the existing location-selection flow. INNs, city shops and crafters preserve separate definitions and menus while sharing continuous location numbering.

`CraftingMenu` owns recipe selection, item-name resolution, requirement presentation, owned-quantity presentation, confirmation and player-facing feedback. It does not mutate inventory directly.

`CraftingService` owns the transaction:

* It resolves all required and output items through `ItemCatalog`.
* It identifies every insufficient requirement before mutation.
* It protects the resulting output quantity against integer overflow.
* It removes ingredients only after all requirements pass.
* It produces exactly one output item.
* It restores already removed ingredients if an unexpected output failure occurs.
* Rejected attempts preserve the complete inventory.

Crafting uses the shared inventory owned by `Party`. It does not own a second inventory, use shop prices, modify shop state or restrict ingredients and outputs by `ItemCategory`.

Before consuming materials, `CraftingService` calculates the resulting output quantity against the per-item limit. When the output is also one of the ingredients, the validation accounts for the quantity consumed before adding the produced unit. A capacity failure preserves every ingredient.

## UI boundaries

The UI package contains console-specific behavior:

- Reading and validating menu input.
- Displaying title, status and result information.
- Selecting allies and enemies.
- Asking for confirmations.
- Waiting for the player to continue.

Important UI components include:

- `CombatScenarioMenu`
- `CityMenu`
- `CityLocationMenu`
- `CraftingMenu`
- `InnMenu`
- `RouteMenu`
- `CityTransportMenu`
- `InventoryMenu`
- `EquipmentMenu`
- `BattleItemMenu`
- `RecoveryItemUseMenu`
- `SkillScrollUseMenu`
- `EpicEncounterMenu`
- `EnemyTargetMenu`
- `AllyTargetMenu`
- `BattleTurnOrderScreen`
- `BattleRewardScreen`
- `PartyStatusMenu`
- `CharacterStatusMenu`
- `CharacterCreationMenu`
- `PlayableClassDetailFormatter`
- `SkillDetailFormatter`
- `EquipmentDetailFormatter`
- `RewardModifierFormatter`
- `PartyRescueScreen`
- `ShopMenu`

`CharacterCreationMenu` controls manual or random protagonist naming, name changes, cancellation and confirmation. It does not create partial character state: the confirmed name returns to `Main`, which constructs the protagonist and `Party`.

`PlayableClassDetailFormatter` presents a class's primary and secondary attribute specializations using shared arrow notation. It derives the displayed attributes from `PlayableClassDefinition` and does not own growth rules.

`PartyRescueScreen` receives already resolved rescue information from `World`. It does not calculate costs, spend gold, restore characters, discover scenarios or change the current scenario.

`World` selects the appropriate menu according to the runtime scenario type. Combat actions are built incrementally from exploration progress, available branches, unlocked routes and paid-transport availability, then displayed by `CombatScenarioMenu`. City actions are built from functional locations, unlocked exits and paid-transport availability, then displayed by `CityMenu`. Both menus preserve continuous numbering, omit unavailable actions and separate action groups through whitespace.

`RouteMenu` selects among unlocked adjacent routes. `CityTransportMenu` separately displays configured discovered cities, validates affordability, requests confirmation and controls payment before returning the selected paid destination.

`InventoryMenu` controls the general inventory flow outside battle. It displays every item category in deterministic enum order, keeps empty categories selectable, reports an empty category only after it is selected, paginates non-empty contents and opens contextual item details. Recovery items may be used, equipment items may open the shared equipment flow and skill scrolls may teach their linked skill to the unique compatible party member. Empty inventory screens remain open until the player explicitly returns or closes the inventory.

`ShopMenu` is shared by fixed city shops and exploration merchants. It displays the configured shop identity and party gold, lists active purchase offers with deterministic pagination, organizes owned sellable items by category and keeps the shop open until the player explicitly leaves.

`CraftingMenu` displays the selected crafter's ordered recipes, recipe narrative, output item and required-versus-owned ingredient quantities. It requests confirmation and translates `CraftingResult` into player-facing feedback while delegating every inventory mutation to `CraftingService`.

`BattleItemMenu` is a separate battle-specific flow. It lists only items that can currently be used in battle and reports success only when an item produces an effect.

`RecoveryItemUseMenu` contains the shared recovery-item execution flow, including target selection, effect application and conditional consumption. This prevents the general and battle menus from duplicating item-use rules.

`SkillScrollUseMenu` resolves the unique party member whose class matches a scroll, reports learning restrictions and controls cancelled or confirmed scroll-skill replacement. Domain mutation and conditional consumption remain delegated to `PlayableCharacter` and `SkillScrollService`.

`ItemMenuFormatter` contains shared presentation rules such as deterministic column widths, name truncation and recovery-effect summaries. It does not decide item behavior.

`PartyStatusMenu` displays a compact two-line overview of one to four allies, including each complete character name, class symbol, level and current resources. It also displays the shared party currency, active temporary EXP modifier and total effective party gold modifier.

`CharacterStatusMenu` displays one character's complete name, class symbol and class name, detailed resources, equipment-modified attributes, progression, total effective EXP modifier, currently learned natural and scroll skills and access to equipment slots.

`EquipmentMenu` supports equipping from an inventory item and managing a selected character's three slots. It filters incompatible choices, confirms change-only stat and attack differences and delegates mutations to `EquipmentService`.

`EquipmentDetailFormatter` formats equipment descriptions, requirements, bonuses and comparison previews for both entry routes.

`SkillDetailFormatter` derives one shared player-facing skill sheet from `SkillDefinition` and its ordered components. It uses the first displayable `PRIMARY` component as the main type, power and target, while the external narrative description explains secondary effects. `CharacterStatusMenu` and battle confirmation reuse the same formatted details instead of maintaining separate translations.

`RewardModifierFormatter` produces shared multiplier text. Party status and the battle-reward screen show temporary global EXP and total party GOLD; the individual character screen separately shows that character's total EXP multiplier. The battle-reward screen intentionally does not list every ally's individual EXP modifier.

`World` does not store a `Scanner`. Console input is handled by the components that require it.

## Currency presentation

`GameCurrency` centralizes the player-facing currency name and numeric formatting. The current name is `GOLD`, and numeric values use the `G` suffix without a separating space, such as `100G`.

It does not own party gold, perform transactions or define prices. Gold remains mutable party state, while prices remain part of the systems that charge or reward currency.

Menus and screens call `GameCurrency.getName()` when the currency is referenced directly and `GameCurrency.format(int)` whenever a numeric amount is displayed. Changing the visible currency convention therefore requires changing only `GameCurrency`.

## Current temporary boundaries

The following implementations remain intentionally temporary:

- Reward-modifier potions are deferred. Their future item behavior will activate the existing `PartyRewardModifierState` rather than duplicate reward calculation or duration state.
- New games currently begin with only the confirmed protagonist. Story-based ally recruitment, random ally classes and party-title acquisition remain future work.
- `character_names.txt` currently contains only temporary entries pending creative-team curation.
- Playable-class definitions, natural class growth and the complete approved 30-skill class-progression roster are integrated; skill powers and resource costs remain provisional pending the general balance pass.
- Enemy actions currently use basic attacks; enemy skill selection can reuse the skill executor later but is outside the current scope.
- Every registered city currently provides an INN, while Bree also provides a fixed shop; tavern functionality remains unavailable.
- Shop state is regenerated when the application starts; persistence and restocking remain postponed until save and load are implemented.
- Save and load are not implemented.
- `Battle` remains large and may later be divided into more focused action services.
- Permanent skill-scroll items, skills and curated enemy drops remain deferred until their draft effects receive the required combat mechanics and final content review.
- The Flagelo's temporary `STARTER` skills remain learned until their retirement is implemented as part of the future level-7 class-choice flow.

These limitations are tracked in `V4_ROADMAP.md` and should be replaced incrementally instead of through one large rewrite.
