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
| `rpg.flags` | Immutable permanent-flag definitions, external loading and mutable set-only campaign flag state | `FlagDefinition`, `FlagCatalog`, `FlagLoader`, `CampaignFlags` |
| `rpg.requirements` | Immutable reusable requirement sets, external loading, pure runtime evaluation and quest-availability dependency validation | `RequirementDefinition`, `RequirementSetDefinition`, `RequirementCatalog`, `RequirementLoader`, `RequirementEvaluator`, `QuestAvailabilityValidator` |
| `rpg.quests` | Immutable quest, step and objective definitions, mutable campaign progress, synchronous gameplay-event processing and NPC-interaction resolution | `QuestDefinition`, `QuestStepDefinition`, `QuestObjectiveDefinition`, `QuestCatalog`, `QuestLoader`, `QuestProgress`, `QuestProgressService`, `QuestNpcInteractionResolver` |
| `rpg.quests.effects` | Immutable permanent quest effects, trigger vocabulary, external loading and ordered lookup | `QuestEffectDefinition`, `QuestEffectCatalog`, `QuestEffectLoader`, `QuestEffectTrigger`, `QuestEffectType` |
| `rpg.quests.rewards` | Immutable material quest rewards, external loading, ordered lookup and reward-type vocabulary | `QuestRewardDefinition`, `QuestRewardCatalog`, `QuestRewardLoader`, `QuestRewardType` |
| `rpg.narrative` | Narrative locations, NPC identity and placement, linear dialogues, quest-dialogue associations, text processing and cross-catalog narrative validation | `NarrativeLocationDefinition`, `NarrativeLocationCatalog`, `NpcDefinition`, `NpcCatalog`, `NpcPlacementDefinition`, `NpcPlacementCatalog`, `DialogueDefinition`, `DialogueCatalog`, `QuestDialogueDefinition`, `QuestDialogueCatalog`, `NarrativeTextProcessor`, `NarrativeReferenceValidator` |
| `rpg.persistence` | Immutable campaign snapshots, strict versioned property encoding, authenticated encrypted containers and atomic save-file access | `CampaignSnapshot`, `CampaignSnapshotFactory`, `CampaignPropertiesSerializer`, `CampaignPropertiesDeserializer`, `CampaignSaveCodec`, `CampaignSaveService`, `CampaignLoadService` |
| `rpg.recruitment` | Recruitment definitions, persistent candidate identity, ally construction, class selection and atomic recruitment composition | `QuestRecruitmentDefinition`, `QuestRecruitmentCatalog`, `RecruitmentCandidateState`, `RecruitmentCandidateStateCatalog`, `AllyRecruitmentService`, `ClassPathRecruitmentService` |
| `rpg.world` | Active world state, fictional world time, scenario discovery, rescue coordination, shop-restocking coordination, narrative interaction and high-level gameplay flow | `World`, `WorldClock`, `WorldProgress` |
| `rpg.world.scenarios` | External scenario and dungeon definitions, runtime scenario hierarchy, permanent dungeon progression and scenario creation | `ScenarioDefinition`, `ScenarioDefinitionCatalog`, `ScenarioLoader`, `CombatScenarioDefinition`, `CombatScenarioLoader`, `DungeonDefinition`, `DungeonLoader`, `ScenarioFactory`, `Scenario`, `CombatScenario`, `Field`, `Dungeon`, `DungeonSettings`, `DungeonProgress`, `City`, `ScenarioCatalog`, `CombatScenarioAction`, `CityAction` |
| `rpg.world.encounters` | Encounter availability and weighted group generation | `EncounterEntry`, `EncounterCatalog`, `EncounterLoader`, `EncounterGenerator` |
| `rpg.world.exploration` | Exploration configuration, progress and event selection | `ExplorationSettings`, `ExplorationSession`, `ExplorationEventGenerator`, `ExplorationEventType`, `ExplorationAreaRestriction` |
| `rpg.world.inns` | Immutable city-INN definitions, complete city coverage validation and lookup by stable or city scenario ID | `InnDefinition`, `InnCatalog`, `InnLoader` |
| `rpg.world.shops` | External shop and offer definitions, runtime stock state, weighted offer generation and atomic commercial transactions | `ShopAccessType`, `ShopDefinition`, `ShopCatalog`, `ShopLoader`, `ShopStockEntry`, `ShopStockCatalog`, `ShopStockLoader`, `ShopOfferState`, `ShopState`, `ShopStateCatalog`, `ShopStateGenerator`, `ShopTransactionService`, `ShopPurchaseResult`, `ShopSaleResult` |
| `rpg.world.rewards` | External exploration reward pools, validation and weighted selection | `ExplorationRewardEntry`, `ExplorationRewardCatalog`, `ExplorationRewardLoader`, `ExplorationRewardGenerator`, `ExplorationReward`, `ExplorationRewardType` |
| `rpg.world.travel` | External directed-route definitions, loading and runtime unlock progress | `TravelRouteDefinition`, `TravelCatalog`, `TravelRouteLoader`, `TravelProgress` |
| `rpg.world.transport` | External paid city-transport definitions, loading and destination lookup | `CityTransportDefinition`, `CityTransportCatalog`, `CityTransportLoader` |
| `rpg.ui` | Console menus, character creation, navigation, dialogue presentation, quest interaction, target selection, shared formatters and information screens | `TitleScreen`, `CharacterCreationMenu`, `PartyRescueScreen`, `CombatScenarioMenu`, `CityMenu`, `CityLocationMenu`, `NarrativeLocationMenu`, `NpcInteractionMenu`, `DialoguePresenter`, `QuestOfferMenu`, `QuestProgressPresenter`, `QuestRewardSummaryFormatter`, `InventoryMenu`, `EquipmentMenu`, `ShopMenu`, `AllyRecruitmentMenu`, `ClassPathTransitionFailurePresenter` |

## Application initialization

`Main` acts as the current composition root. It loads external definitions, constructs runtime catalogs and connects the objects required by the game.

Initialization includes:

Initialization follows dependency order:

1. Create the shared `ConsoleInput`.
2. Load foundational character-name, playable-class, skill, enemy and item definitions.
3. Load equipment, recovery-item, skill-scroll, enemy-drop and encounter definitions against their required foundational catalogs.
4. Load common scenario, transport, INN, shop, crafting, exploration-reward, dungeon, branch and adjacent-route definitions.
5. Load complete quest definitions from quest headers, sequential steps and ordered objectives.
6. Load permanent-flag definitions and reusable requirement sets.
7. Validate requirement references and quest-availability dependencies after quest, item and flag catalogs are available.
8. Load ordered quest rewards and permanent quest effects after their item and route references are available.
9. Load narrative locations, NPC identities, fixed NPC placements and linear dialogues.
10. Load quest-dialogue associations and perform cross-catalog narrative validation, including speakers, starting NPCs, interaction objectives and dialogue contexts.
11. Create runtime item, scenario, shop and other catalog-backed objects.
12. Resolve or reconstruct the active party and existing persisted world state.
13. Create the current `QuestProgress`, `CampaignFlags` and `RecruitmentCandidateStateCatalog` campaign state. Their coordinated persistence remains deferred until the V6 save-format integration.
14. Construct the shared quest-progress, class-path, candidate-introduction and recruitment services.
15. Construct `World` with the required immutable catalogs, mutable campaign state, generators, services, presenters and shared dependencies.

Each loader builds a complete candidate result before returning its catalog. Cross-catalog validators run only after all definitions required by that validation are available.

External data is loaded before gameplay. Invalid definitions or unresolved references prevent the game from starting with partially inconsistent catalogs.

INN definitions are loaded before combat-scenario definitions. This makes `InnCatalog` available to `CombatScenarioLoader`, which can reject rescue destinations that are not registered cities with a valid INN.

During a normal run, `Main` repeatedly opens `TitleScreen` and `CharacterCreationMenu` until the player confirms a protagonist. The confirmed character is created as a level-1 Flagelo and becomes the only initial member and leader of the new `Party`. Cancelling character creation returns to the title screen without creating partial character state.

When the `--dev-party` argument is present, `Main` bypasses the normal title and creation flow and delegates party construction to `DevelopmentPartyFactory`. This development-only path supports one to four configured members with distinct classes, arbitrary positive levels, configurable initial gold and optional temporary reward modifiers while remaining isolated from normal new-game behavior.

`WorldProgress`, `TravelProgress`, `WorldClock` and `ShopStateCatalog` represent separate runtime concerns. `WorldProgress` records discovered scenarios, `TravelProgress` records unlocked adjacent routes, `WorldClock` stores authoritative elapsed campaign hours, and `ShopStateCatalog` preserves every shop's selected offers and remaining stock for the current restocking cycle.

`QuestProgress` and `CampaignFlags` add two independent campaign-level concerns. `QuestProgress` stores quest lifecycle, current sequential steps and objective quantities. `CampaignFlags` stores only permanent set-only world consequences declared by `FlagCatalog`. Neither object duplicates immutable content from its definition catalog.

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
- Automatic `LEVEL` skill acquisition.
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

`Dungeon` adds immutable `DungeonSettings`, including:

- A required narrative boss group.
- A required recurring boss group.
- The recurring-boss cooldown in fictional world hours.
- A required epic enemy group.
- The epic minimum exploration position.
- The epic normal, special or unrestricted area requirement.
- The epic cooldown in fictional world hours.

Narrative and recurring boss groups may reuse the same canonical enemy IDs. Epic IDs remain disjoint from both boss groups.

`City` represents non-combat settlements. Cities use their own action flow, separate visitable locations from adjacent exits and do not create exploration sessions. Every registered city has exactly one externally configured INN and may also expose shops, crafters and requirement-controlled narrative locations. Narrative spaces such as taverns use the narrative-location system instead of becoming new scenario types or being mixed into shop, INN or crafting definitions. Paid city transport remains available through the shared world flow.

`DungeonProgress` owns mutable campaign-level progression separately from immutable `DungeonSettings` and transient `ExplorationSession` state. It registers every runtime dungeon and stores narrative completion together with the absolute elapsed-hour availability of recurring bosses and epic encounters.

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
World resolves the current ExplorationSession position
        ↓
normal or special area is determined
        ↓
World evaluates current merchant and epic eligibility
        ↓
ExplorationEventGenerator selects or redirects the event
        ↓
World dispatches the event consequence
        ↓
ExplorationSession advances when the result permits
```

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

`ExplorationSession` stores only transient progress and session-specific state:

- Current exploration position.
- Whether the current area is special.
- Whether a merchant has already been encountered.
- Whether an epic encounter has already been offered.

A merchant cannot appear more than once per session. A repeated merchant result becomes `NOTHING` in a normal area and `REWARD` in a special area.

Special areas never resolve without content. A direct `NOTHING` result becomes `REWARD`.

For dungeon events, `World` combines transient session state with permanent `DungeonProgress` and immutable `DungeonSettings`. Epic eligibility is evaluated at the reached position using:

- Minimum position.
- Area restriction.
- Session offer state.
- Absolute cooldown availability.

An ineligible epic result becomes a regular battle using the current area's encounter pool.

The final dungeon position bypasses random event generation. `World` selects the narrative boss before first completion, the recurring boss after its cooldown, or an empty final resolution while the recurring boss remains unavailable.

Boss and epic availability is not snapshotted when an exploration session begins. World-time advancement from battles earlier in the same session can make either encounter available later in that session.

When a merchant event occurs, `World` selects uniformly among the `EXPLORATION` shops associated with the current scenario. It resolves the selected merchant through the active `ShopStateCatalog` and opens the shared `ShopMenu`.

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

- Beginner `LEVEL` skills, which belong exclusively to the internal `flagelo_iniciante` class before the level-7 transition.
- `LEVEL` skills, which are learned automatically when their configured level requirement is reached.
- `SCROLL` skills, whose configured level is a minimum requirement and which must be learned explicitly from an item.

`PlayableCharacter` stores learned skills through stable IDs while continuing to reference the unique definitions owned by `SkillCatalog`. Automatically available skills are learned during character construction and after level gains. Available skills are displayed with non-scroll skills first, followed by learned scroll skills in learning order.

A character may learn at most two `SCROLL` skills. Once both slots are occupied, a new scroll skill can replace only another learned `SCROLL` skill; `LEVEL` skills cannot be replaced through this flow.

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

## World time architecture

`WorldClock` owns the fictional campaign time as one mutable elapsed-hour value. A new campaign starts with eight elapsed hours, representing day 1 at 08:00. Displayed days and hours are derived from this value using 24-hour days.

The clock exposes a general positive-hour advancement operation and can determine whether a proposed advancement crosses midnight without mutating its state. Non-positive advancement and `long` overflow are rejected before the stored value changes.

`World` owns the gameplay actions that consume time. The current finished-battle duration is centralized as:

```java
private static final int HOURS_PER_FINISHED_BATTLE = 1;
```

Every random, epic and boss battle returns through `World.startEncounter()`. A `VICTORY` or `DEFEAT` result passes this configured duration to the general world-time operation. `FLED` does not advance time, and avoiding an optional encounter never reaches the battle integration point.

After a boss or epic victory returns from `startEncounter()`, `World` records the next availability through `DungeonProgress` using the already-advanced elapsed hour. Refusal, escape and defeat do not invoke the cooldown-recording operations.

When an advancement does not cross midnight, `World` advances only the clock.

When an advancement crosses midnight, `World`:

1. Requests a complete candidate `ShopStateCatalog` from the existing `ShopStateGenerator`.
2. Leaves the current clock and shop states unchanged while generation is in progress.
3. Advances the clock only after complete generation succeeds.
4. Replaces the active catalog reference with the complete candidate.

`ShopStateGenerator.generate()` always creates new `ShopState` and `ShopOfferState` instances for every registered shop. New weighted selections begin with canonical initial stock and never merge with the previous cycle.

The generated catalog is committed as a whole. A generation failure therefore preserves both the previous elapsed time and every previous shop state.

`WorldTimeFormatter` derives the Brazilian Portuguese console presentation from `WorldClock`. It displays `☼` from 06:00 through 17:59 and `☾` from 18:00 through 05:59. These symbols and all formatted clock values are presentation-only and are not stored as campaign state.

## City, shop and INN flow

`World` dispatches the current scenario to a type-specific menu:
- `CombatScenarioMenu` is exclusive to fields and dungeons.
- `CityMenu` is exclusive to cities.

- Unsupported runtime scenario types cause an internal-state error.

`CityMenu` receives an incrementally built list of available `CityAction` values. It omits actions that currently have no functional destination and keeps continuous numeric selection. Scenario actions appear horizontally on the first row, while inventory and party status appear on the second row. The current formatted world time is displayed immediately above the action prompt.

The paid `Viajar` action is included only when at least one configured and discovered destination city is available. It opens `CityTransportMenu` without mixing paid transport with city exits or adjacent-route unlocking.

The city flow separates three navigation concepts:

- Visiting a functional city location.
- Leaving through an unlocked adjacent route.
- Traveling directly to another discovered city through paid transport.

`CityLocationMenu` displays every currently accessible destination associated with the city. The list may combine the city's INN, `CITY` shops, crafters and visible narrative locations while preserving continuous numbering. Descriptions and destination-specific actions are shown only after the player enters the selected location.

System locations preserve their existing definitions and dedicated menus. Narrative locations are resolved separately through `NarrativeLocationCatalog` and filtered through their current requirement sets. A visible narrative location may contain no visible NPCs, in which case its menu displays `Não há ninguém aqui.` without treating the data as invalid.

Current narrative-location flow:

```text
data/narrative_locations.txt
    -> NarrativeLocationLoader
    -> NarrativeLocationCatalog
                         \
data/npcs.txt             \
    -> NpcLoader           -> World
    -> NpcCatalog         /     -> CityLocationMenu
                         /      -> NarrativeLocationMenu
data/npc_placements.txt /       -> NpcInteractionMenu
    -> NpcPlacementLoader        -> DialoguePresenter
    -> NpcPlacementCatalog
```

Location and placement requirement sets are evaluated when their menus are built and again before protected interaction. Hidden locations and NPCs do not occupy menu positions.

`World` derives whether any visible quest interaction exists beneath the current city action, narrative location or NPC. `CityMenu`, `CityLocationMenu` and `NarrativeLocationMenu` display `[!]` from that derived result. The marker does not distinguish offer, progress or completion and disappears immediately when no currently available quest interaction remains.

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

`CombatScenarioMenu` uses the same two-row action presentation and displays the same shared `WorldClock` state. `ConsoleUI.displayActionRows()` centralizes the numbered two-row layout without moving action availability or selection rules out of their owning menus.

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

`Main` creates one reusable `ShopStateGenerator` and passes it to `World` together with the initial generated catalog. `World` is the only owner of the active catalog reference. City and exploration shop access always resolve a `ShopState` from the currently active catalog, so a successful midnight replacement affects every later shop access without leaving menus attached to an obsolete catalog.

`CITY` shops are exposed through `CityLocationMenu`. `EXPLORATION` shops are exposed through the existing merchant event. Both access types resolve the same `ShopState` objects and open the same `ShopMenu`.

`ShopMenu` controls player-facing navigation, deterministic sorting, pagination, quantity selection, transaction confirmation and feedback. It displays every monetary value through `GameCurrency`, the current time through `WorldTimeFormatter` and the exact next midnight restocking boundary on its main screen.

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

## Quest, requirement and narrative architecture

The V6 quest foundation separates external immutable content from mutable state belonging to one campaign.

```text
QuestDefinition
└── ordered QuestStepDefinition
    └── ordered QuestObjectiveDefinition

QuestProgress
└── mutable state only for active and completed quests
```

`QuestLoader` assembles complete definitions from `quests.txt`, `quest_steps.txt` and `quest_objectives.txt`. The loader uses private drafts while all three files are being read and exposes only fully validated `QuestDefinition` objects through `QuestCatalog`.

Quest steps are linear and numbered consecutively from one. Objectives are ordered inside their step and use one of the closed initial types:

- `TALK_TO_NPC`
- `DEFEAT_ENEMY`
- `ANALYZE_ENEMY`
- `OBTAIN_ITEM`
- `DELIVER_ITEM`
- `REACH_SCENARIO`
- `COMPLETE_DUNGEON`
- `WIN_BATTLE`

`QuestProgress` stores only mutable lifecycle and objective progress. A quest is `NOT_STARTED`, `ACTIVE` or `COMPLETED`. Only the current step of an active quest has mutable objective quantities. Advancing a completed step creates the state of the next step; completing the final step moves the quest permanently to the completed collection.

`QuestProgressService` is the synchronous integration boundary between confirmed gameplay operations and quest state. Callers explicitly submit typed `QuestProgressEvent` values after the corresponding operation succeeds. One event may update multiple active quests, but it is evaluated only against each quest's current step and is not replayed against a step unlocked by that same event.

The service returns immutable `QuestProgressResult` and `QuestProgressUpdate` values. Domain services do not print quest notifications. `QuestProgressPresenter` translates the returned updates, completion failures, effects and rewards into grouped player-facing output.

`QuestProgressCoordinator` is the console-application boundary between confirmed gameplay operations, `QuestProgressService` and `QuestProgressPresenter`. It owns neither quest state nor progression rules. Callers submit confirmed events through it, and it selects standard or battle-context presentation for the returned result. The project retains explicit synchronous calls rather than introducing a global event bus.

NPC interaction is resolved separately through `QuestNpcInteractionResolver`. It derives currently available quest offers and active current-step interactions from quest state, requirements and the selected canonical NPC. It does not store marker state or mutate quest progress.

### Permanent flags and reusable requirements

Permanent flags use the same definition-and-state separation:

```text
data/flags.txt
    -> FlagLoader
    -> FlagDefinition
    -> FlagCatalog

FlagCatalog + active IDs
    -> CampaignFlags
```

Every defined flag begins absent. `CampaignFlags` accepts only IDs registered in `FlagCatalog`, sets each flag at most once and treats repeated assignment as an idempotent no-op. V6 flags are never removed.

Requirements are reusable immutable AND groups:

```text
data/requirements.txt
    -> RequirementLoader
    -> ordered RequirementDefinition objects
    -> RequirementSetDefinition
    -> RequirementCatalog
```

The initial requirement vocabulary is:

- `LEADER_LEVEL_AT_LEAST`
- `ITEM_QUANTITY_AT_LEAST`
- `QUEST_NOT_STARTED`
- `QUEST_ACTIVE`
- `QUEST_COMPLETED`
- `FLAG_SET`
- `FLAG_UNSET`

`RequirementEvaluator` reads the current party, inventory, quest progress and campaign flags without mutating or caching them. A blank requirement-set reference means unrestricted access. Every protected interaction is evaluated again immediately before it occurs.

`QuestAvailabilityValidator` runs after quest and requirement catalogs are available. It rejects missing requirement sets, direct self-dependencies and indirect cycles among quest-availability requirements before gameplay begins.

Requirement results are derived state. They are not stored in campaign snapshots or copied into quest, location or NPC definitions.

### Quest rewards, effects and completion transactions

Material rewards and permanent effects remain separate from quest identity:

```text
data/quest_rewards.txt
    -> QuestRewardLoader
    -> QuestRewardCatalog

data/quest_effects.txt
    -> QuestEffectLoader
    -> QuestEffectCatalog
```

The initial material reward types are `GOLD`, `PARTY_EXP` and `ITEM`. Rewards preserve their declared order for each quest. Player-facing reward summaries are derived through `QuestRewardSummaryFormatter` instead of duplicated as free narrative text.

The initial permanent effect types are `SET_FLAG` and `UNLOCK_ROUTE`. Effects are associated with the `QUEST_START`, `STEP_COMPLETE` or `QUEST_COMPLETE` trigger and preserve their declared order at each trigger location.

Quest completion uses one coordinated plan. Before changing campaign state, `QuestProgressService` validates delivery quantities, reward receiving limits, effect applicability and the expected current quest state. EXP is not granted before capacity-sensitive GOLD and item rewards are known to be valid.

Only after complete validation does the service consume delivery items, grant rewards, apply effects and advance the quest state. A rejected completion returns an explicit `QuestCompletionFailure` and preserves inventory, GOLD, character progression, flags, routes and quest progress.

Items granted by a completed quest may produce later acquisition events for other active quests after the transaction succeeds. The completed quest does not consume its own reward event retroactively.

### Narrative locations, NPCs and dialogues

Narrative content separates place, character identity, placement and speech:

```text
NarrativeLocationDefinition
NpcDefinition
NpcPlacementDefinition
DialogueDefinition
└── ordered DialogueLineDefinition
QuestDialogueDefinition
```

A `NarrativeLocationDefinition` associates one narrative space with a city and an optional visibility requirement set. It does not replace INNs, shops or crafters.

`NpcDefinition` owns stable identity, display name, description and default dialogue. `NpcPlacementDefinition` independently associates that NPC with at most one narrative location and an optional visibility requirement set. This separation permits an NPC to exist as a dialogue speaker without being placed in a city.

`DialogueDefinition` contains an ordered immutable sequence of utterances. Speaker types are `NPC`, `ENEMY`, `LEADER`, `NARRATOR` and `RECRUIT`. Ordinary NPC and enemy speakers resolve canonical catalog identities. Leader and narrator lines do not define speaker IDs. Recruit speakers reference canonical candidate IDs declared by `QuestRecruitmentCatalog`. `World` introduces missing candidate state before presentation, while `DialoguePresenter` only resolves the already established persistent generated name. Repeated presentation never rerolls or recreates the candidate.

`NarrativeTextProcessor` centralizes the supported literal `\n` decoding, placeholder validation and runtime replacement. The current placeholders are `{leaderName}`, `{groupMember2}`, `{groupMember3}` and `{groupMember4}`. Resolution fails before mutation when a referenced party position does not exist.

`QuestDialogueCatalog` associates quest state with external dialogues through the `OFFER`, `ACCEPT`, `DECLINE`, `PENDING` and `COMPLETE` contexts. Accept and decline choices are interface behavior rather than a generic branching-dialogue language.

Startup validation is shared between the individual narrative loaders and `NarrativeReferenceValidator`. Together, they verify narrative locations, NPC placements, dialogue speakers, default NPC dialogues, quest starting NPCs, interaction-objective NPCs and quest-dialogue coverage without exposing a partially populated catalog.

`World` derives visible narrative locations, NPC placements and quest interactions from current requirements and quest state. It also derives incomplete `REACH_SCENARIO` targets and compares them only with currently unlocked adjacent routes and available paid-transport destinations.

The `[!]` marker is presentation-only derived state. It may propagate from actionable NPCs to their visible narrative location and the current city's location action, or from a pending scenario target to an immediately usable destination and its parent travel action. It is never stored in definitions, mutable campaign state or save snapshots and does not perform multi-route pathfinding.

### Gameplay quest integration

Runtime `Enemy` instances retain the canonical ID of their `EnemyDefinition`. `Battle` records successfully analyzed enemy IDs once per battle, while `BattleCoordinator` converts the completed battle into one ordered event batch. Analysis events are submitted first. A victory then contributes defeated-enemy quantities grouped by canonical ID, `BATTLE_WON`, and only the item quantities actually collected as rewards. Escape and defeat contribute confirmed analysis events only.

Item-producing flows submit `ITEM_OBTAINED` only after their mutation succeeds. Purchases, crafting, exploration rewards, collected battle drops and quest rewards are valid sources. Rejected operations, discarded rewards, rollback restoration, save restoration and internal transfers do not represent new acquisition.

`World` submits `SCENARIO_REACHED` only after the destination has become the current scenario. It submits `DUNGEON_COMPLETED` after either a narrative or recurring boss victory has updated `DungeonProgress` and completed the exploration. Supporting both boss forms prevents a quest activated after the narrative victory from becoming permanently blocked.

### Level-7 class-path architecture

The class-path system belongs to `rpg.classes.paths` and separates immutable external definitions, transient questionnaire resolution and validated campaign mutation.

The protagonist uses two required canonical class definitions:

```text
flagelo_iniciante
    -> initial protagonist class
    -> natural level-1 and level-3 skills

flagelo
    -> final selectable Flagelo path
    -> final natural skill roster beginning at level 7
```

`ClassPathRequiredClassValidator` runs immediately after playable-class loading and before dependent skills or equipment. It requires both canonical IDs and verifies that they share player-facing name, symbol, base statistics, primary and secondary attributes and armor weight. Their descriptions and natural-skill rosters may differ.

The obsolete `STARTER` skill-acquisition type is removed. Beginner and final natural skills use the ordinary `LEVEL` acquisition rule while remaining attached to their respective internal classes.

Final path equipment is external:

```text
data/class_paths.txt
    -> ClassPathLoader
    -> ClassPathDefinition
    -> ClassPathCatalog
```

Every non-beginner playable class has exactly one path definition containing one level-7 introductory weapon and armor. `ClassPathLoader` validates complete final-class coverage, equipment categories, level requirements and armor-weight compatibility. `flagelo_iniciante` is explicitly forbidden as a final path.

The questionnaire is assembled from two external files:

```text
data/class_path_questions.txt
data/class_path_answers.txt
    -> ClassPathQuestionnaireLoader
    -> ordered ClassPathQuestionDefinition
        -> ordered ClassPathAnswerDefinition
    -> ClassPathQuestionnaireCatalog
```

A question is an immutable aggregate that owns at least two consecutive ordered answers. Each answer references exactly one recommendable final class and contributes exactly one point. Final Flagelo cannot be an answer target, although it remains available during unrestricted final selection.

Question and answer text reuses `NarrativeTextProcessor` for literal line breaks, supported placeholders and complete pre-presentation resolution.

`ClassPathRecommendationService` validates one canonical answer for every question, totals points by class ID and returns `ClassPathRecommendationResult`. The result preserves every highest-scoring path in stable `ClassPathCatalog` order. It does not choose randomly or make the final player decision.

A single leader proceeds directly to revelation. Two-way and three-way ties remain explicit transient results and are resolved by the player through the interface. Rejecting a direct or tied recommendation opens unrestricted selection among all seven final paths.

`PlayableCharacter.changeClass(...)` is the controlled character-level transition boundary. It requires empty equipment slots, replaces the internal class and natural class-skill definitions, preserves learned scroll skills, recalculates natural statistics at the existing level, learns newly available natural skills and fully restores resources. It does not transfer equipment, grant items or set campaign flags.

Campaign consequences are coordinated separately:

```text
ClassPathTransitionService.prepare(...)
    -> eligibility validation
    -> returned-equipment calculation
    -> introductory-item calculation
    -> complete inventory-capacity validation
    -> ClassPathTransitionPlan or ordered failures

ClassPathTransitionService.apply(...)
    -> complete revalidation
    -> equipment return
    -> introductory-item grant
    -> controlled character class change
    -> permanent completion flag
```

`ClassPathTransitionPlan` retains the protagonist, target class, target natural skills, returned equipment, introductory items and total inventory additions. Exposing the combined additions allows Part 8 to validate them together with recruitment equipment and other transaction consequences before any mutation.

The standalone transition rejects an already completed ceremony, a non-beginner protagonist, insufficient level and item-capacity overflow. Application reconstructs and compares the current plan before mutation so a stale plan cannot silently commit against changed campaign state.

Class change and recruitment remain dedicated typed behavior rather than generic quest effects. Part 8 composes the validated protagonist plan with mandatory ally recruitment, quest progression, rewards and effects so the final Quest 4 resolution does not commit either character outcome independently.

## Recruitment candidate and first-ally architecture

Recruitment separates immutable dispatch data, mutable candidate identity, transient ally preparation and committed party state.

```text
data/quest_recruitments.txt
    -> QuestRecruitmentLoader
    -> QuestRecruitmentDefinition
    -> QuestRecruitmentCatalog
        -> lookup by quest and step
        -> lookup by candidate ID
```

`QuestRecruitmentDefinition` associates one real quest step with a canonical candidate ID and an externally configured playable-entry level. It does not store generated names, recruited state, class results, equipment or resources.

`QuestRecruitmentCatalog` preserves at most three records, matching the non-leader positions in a maximum-size party. Quest-and-step keys and candidate IDs are both unique.

Candidate identity belongs to mutable campaign state:

```text
RecruitmentCandidateState
├── canonical candidate ID
├── persistent generated name
└── recruited state

RecruitmentCandidateStateCatalog
└── one state per declared candidate already introduced
```

`RecruitmentCandidateIntroductionService` creates this state only at the candidate's first narrative introduction. It gathers every current party name and every existing candidate name, delegates uniform selection to the shared `CharacterNameSelector`, and registers the state only after a unique name is available. Repeated introduction returns the existing object without rerolling.

Before recruitment, candidate state contains no class, level, EXP, resources, skills, equipment or party position. Those properties belong to the transient ally constructed only when recruitment is prepared.

`DialogueLoader` validates each `RECRUIT` speaker ID through `QuestRecruitmentCatalog`. `World` scans a complete dialogue for recruit speakers and introduces each distinct candidate before asking `DialoguePresenter` to resolve the dialogue. The presenter performs no candidate mutation.

Generic ally preparation is separated from the first ceremony:

```text
AllyRecruitmentService.prepare(...)
├── resolve canonical recruitment definition
├── require existing unrecruited candidate
├── validate party capacity and name uniqueness
├── AllyClassSelector
│   ├── exclude beginner Flagelo
│   ├── exclude final Flagelo
│   ├── exclude every class already in Party
│   └── exclude additional caller-provided classes
└── RecruitmentAllyFactory
    ├── create the configured-level PlayableCharacter
    ├── calculate natural attributes and skills
    ├── equip introductory weapon and armor directly
    ├── leave the trinket slot empty
    └── fully restore HP, MP and SP
```

`AllyClassSelector`, `RecruitmentAllyFactory`, `AllyRecruitmentPlan` and `AllyRecruitmentService` are generic recruitment components. They do not know that the first recruit shares the protagonist's ceremony. Later narrative recruitments can reuse them without creating numbered second- or third-ally service classes.

The first recruitment adds ceremony-specific composition:

```text
World.executeActiveQuestInteraction(...)
├── ordinary quest step
│   └── World.executeStandardQuestInteraction(...)
│
└── first recruitment step
    ├── ClassPathChoiceCoordinator.chooseClassPath()
    ├── ClassPathRecruitmentService.prepare(...)
    │   ├── ClassPathTransitionService.prepare(...)
    │   ├── QuestProgressService.validateNpcInteractionCompletion(
    │   │       pending protagonist inventory additions)
    │   └── AllyRecruitmentService.prepare(
    │           additionally excluded protagonist target class)
    ├── present transition or quest failures
    ├── display required COMPLETE dialogue
    ├── AllyRecruitmentMenu.confirm(...)
    └── ClassPathRecruitmentService.apply(...)
```

`ClassPathRecruitmentPlan` composes the already validated protagonist transition and generic ally-recruitment plan under one canonical recruitment definition. It verifies that the protagonist and ally target different classes.

`ClassPathRecruitmentPreparation` contains either one complete composite plan or expected class-transition and quest-completion failures. Expected failures return to the interface without campaign mutation.

The final commit order is deliberate:

```text
ClassPathRecruitmentService.apply(...)
1. Revalidate the complete composite plan.
2. Apply the protagonist class transition.
3. Complete the quest, rewards and permanent effects.
4. Insert the ally into Party.
5. Mark the candidate as recruited.
```

The ally is inserted after quest rewards so the first ally remains at zero EXP and does not receive the completing quest's `PARTY_EXP`. Every capacity-sensitive or content-dependent condition is validated before step 2, so expected failure preserves the protagonist, candidate, party, inventory, quest progress, flags and routes.

`AllyRecruitmentMenu` presents the prepared ally's persistent name and selected class with one mandatory action, `Aceitar sua companhia`. It does not offer a false refusal branch and does not mutate campaign state.

Transient questionnaire answers, recommendations, confirmed class choice, random ally class, prepared ally and composite plans are not persisted. The already introduced candidate's ID, generated name and recruited state are mutable campaign data scheduled for version-3 persistence in V6 Part 9.

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
- `ClassPathQuestionnaireMenu`
- `ClassPathTieMenu`
- `ClassPathRecommendationMenu`
- `ClassPathSelectionMenu`
- `ClassPathConfirmationMenu`
- `ClassPathChoiceCoordinator`
- `SkillDetailFormatter`
- `EquipmentDetailFormatter`
- `RewardModifierFormatter`
- `PartyRescueScreen`
- `ShopMenu`
- `NarrativeLocationMenu`
- `NpcInteractionMenu`
- `DialoguePresenter`
- `QuestOfferMenu`
- `QuestMenu`
- `QuestProgressCoordinator`
- `QuestProgressPresenter`
- `QuestRewardSummaryFormatter`

`CharacterCreationMenu` controls manual or random protagonist naming, name changes, cancellation and confirmation. It does not create partial character state: the confirmed name returns to `Main`, which constructs the protagonist and `Party`. Its class presentation reuses `PlayableClassDetailFormatter` so initial creation and later class-path selection cannot diverge.

`PlayableClassDetailFormatter` produces one compact class summary containing icon, player-facing name, armor weight and primary and secondary attribute tendencies. Its complete presentation adds the multiline class description. Character creation, recommendation details and class-path confirmation reuse these formats without owning class rules.

`ClassPathQuestionnaireMenu` resolves every question and answer template before displaying the first question. It presents questions sequentially, repeats only the current question after invalid input and returns an ordered immutable list of canonical answers without calculating scores or changing campaign state.

`ClassPathTieMenu` presents the two or three highest-scoring marks in stable class-path order. Its timed narrative sequence runs once; invalid input repeats only the prompt. Selecting zero rejects every tied mark and returns control for unrestricted path selection.

`ClassPathRecommendationMenu` presents the resolved direct or player-selected recommendation through a one-time timed revelation. The subsequent decision screen contains only the compact class summary, optional complete description access, acceptance and rejection. Opening the description or entering invalid input does not replay the revelation.

`ClassPathSelectionMenu` lists all seven final paths derived from `ClassPathCatalog`; the beginner Flagelo is structurally absent. `ClassPathConfirmationMenu` displays the selected class and permits confirmation or another unrestricted selection without offering ceremony cancellation.

`ClassPathChoiceCoordinator` owns the transient interface sequence: questionnaire, score evaluation, optional tie resolution, recommendation revelation, rejection and unrestricted selection. It returns one confirmed `PlayableClassDefinition` but does not change the protagonist, inventory, flags, quest state or recruitment state.

`PartyRescueScreen` receives already resolved rescue information from `World`. It does not calculate costs, spend gold, restore characters, discover scenarios or change the current scenario.

`World` selects the appropriate menu according to the runtime scenario type. Combat actions are built incrementally from exploration progress, available branches, unlocked routes and paid-transport availability, then displayed by `CombatScenarioMenu`. City actions are built from functional locations, unlocked exits and paid-transport availability, then displayed by `CityMenu`. Both menus preserve continuous numbering, omit unavailable actions and separate action groups through whitespace.

Narrative-location navigation remains iterative. `NarrativeLocationMenu` displays the city and location headers, visible NPCs and the empty-location message. `NpcInteractionMenu` displays the same contextual headers, the selected NPC's identity and description, currently available quest interactions in stable quest-catalog order and the permanent `Conversar` option after them.

`DialoguePresenter` resolves every speaker and placeholder before displaying the first utterance. This prevents a later invalid line from interrupting a dialogue after partial presentation or after a related mutation. Each utterance advances only after Enter. Narrative-location dialogues include city and location headers, while the presenter remains reusable for dialogues initiated outside those locations.

`QuestOfferMenu` owns only the generated accept-or-decline choice. External dialogue data provides the surrounding `OFFER`, optional `ACCEPT` and optional `DECLINE` sequences without defining a generic branching language.

`QuestMenu` is a read-only view over `QuestProgress` and canonical quest and reward data. It separates active and completed quests. Active entries resolve the quest description, current step, current objective counters and material reward summaries; completed entries expose only the resolved quest description. A completed final step without `TALK_TO_NPC` may offer a manual completion retry through `QuestProgressCoordinator`. Menu navigation does not advance world time or mutate progress.

`QuestProgressPresenter` displays grouped objective, step and quest updates returned by the domain. Simple progress produced during battle may be appended inline to the battle outcome, while quest completion, permanent effects and completion failures retain dedicated presentation. It also presents quest-start confirmation. Objective-completion notifications deliberately omit the full objective description.

`QuestRewardSummaryFormatter` derives player-facing reward descriptions from `QuestRewardDefinition` objects and canonical item data. Quest definitions therefore do not maintain a second free-text reward summary that could diverge from the transaction.

`RouteMenu` selects among unlocked adjacent routes and receives only the destination scenario IDs that should display a quest marker. `CityTransportMenu` receives the same presentation-only destination set while separately displaying configured discovered cities, validating affordability, requesting confirmation and controlling payment before returning the selected paid destination. Neither menu inspects quest definitions or progress directly.

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

Narrative text currently uses shared automatic wrapping at 90 console columns. Intentional line breaks decoded from literal `\n` remain paragraph boundaries. Applying the same wrapping and consistent screen-clearing behavior to every older description screen is deferred to the post-V6 interface review.

`Main` creates one shared `ConsoleInput`, which is passed to every component that reads player commands. `World` does not retain the input object after constructing its menus and battle services.

`ConsoleInput` decodes input explicitly as UTF-8 and uses one daemon reader thread with an internal blocking queue. This allows timed UI pauses to discard complete commands entered while the game is not accepting actions. On Windows, it reads through `CONIN$` so an accidental `Ctrl+Z` can be ignored without terminating the campaign; other environments fall back to `System.in`.

`ConsoleUI.pause(long, ConsoleInput)` marks the complete pause interval as input-discarding time. Characters may still be echoed by the native terminal, but discarded commands never become actions in a later menu.

## Currency presentation

`GameCurrency` centralizes the player-facing currency name and numeric formatting. The current name is `GOLD`, and numeric values use the `G` suffix without a separating space, such as `100G`.

It does not own party gold, perform transactions or define prices. Gold remains mutable party state, while prices remain part of the systems that charge or reward currency.

Menus and screens call `GameCurrency.getName()` when the currency is referenced directly and `GameCurrency.format(int)` whenever a numeric amount is displayed. Changing the visible currency convention therefore requires changing only `GameCurrency`.

## Persistence foundation

The persistence foundation separates mutable runtime state from immutable serialized data.

`CampaignSnapshotFactory` reads the current runtime objects and creates this immutable hierarchy:

```text
CampaignSnapshot
├── PartySnapshot
│   ├── PlayableCharacterSnapshot
│   │   └── EquipmentSnapshot
│   ├── InventoryEntrySnapshot
│   └── RewardModifierSnapshot
├── WorldSnapshot
│   └── DungeonProgressSnapshot
└── ShopSnapshot
    └── ShopOfferSnapshot
```

Snapshots contain only the canonical IDs and mutable values required to reconstruct the campaign. They do not contain catalog definitions, presentation values, cryptographic metadata or transient gameplay state.

The current encoding flow is:

```text
CampaignSnapshot
    -> CampaignPropertiesSerializer
    -> Java Properties
    -> PropertyPayloadEncoder
    -> UTF-8 plaintext
    -> SaveEncryptionService
    -> EncryptedSavePayload
    -> SaveContainerCodec
    -> textual encrypted container
```

The decoding flow performs the reverse transformation and produces a complete `CampaignSnapshot`. `CampaignSnapshotRestorer` then validates its catalog-backed references and reconstructs an isolated `RestoredCampaign`, including complete `DungeonProgress`, without modifying active runtime state. The candidate is activated only after the complete operation succeeds.

`StrictPropertyReader` requires every property of the current internal format, parses decimal numeric values and canonical booleans, tracks consumed fields and rejects unknown properties.

`PropertyPayloadDecoder` performs strict UTF-8 decoding and rejects duplicated property names before one value can overwrite another.

`SaveEncryptionService` uses `AES/GCM/NoPadding` with the stable 256-bit application key, a new secure random 12-byte IV for every encryption and a 128-bit authentication tag. The textual outer container exposes only its identifier, container version, Base64 IV and Base64 authenticated ciphertext.

`AtomicSaveFileWriter` completes and flushes a temporary file in the target directory before replacing `save.txt`. It prefers an atomic move and uses a backup-assisted replacement fallback when atomic moves are unavailable.

`CampaignSaveService` and `CampaignLoadService` expose explicit operation results without logging the encryption key or decrypted payload. Missing, unreadable and invalid save files remain distinct load outcomes.

`CampaignLoadService` reports success only after decryption, strict decoding, catalog-backed validation and complete runtime reconstruction succeed. The startup flow uses `CampaignSelectionMenu` to continue a valid campaign or begin a new one. Saving is exposed exclusively through INNs, and ordinary game exits require confirmation without performing an implicit save.

Quest definitions, rewards, effects, requirements, narrative content, dialogue associations, recruitment definitions, class-path definitions and questionnaire content are immutable external data and do not belong in campaign snapshots. Mutable `QuestProgress`, `CampaignFlags` and `RecruitmentCandidateStateCatalog` require coordinated versioned persistence in V6 Part 9. Until that integration is implemented, quest, flag and candidate state is created empty when a campaign runtime is constructed and the authenticated internal save format remains version 2. Questionnaire answers, recommendations, confirmed transient choices, random ally classes and prepared recruitment plans are never snapshot state.

## Current temporary boundaries

The following implementations remain intentionally temporary:

- Reward-modifier potions are deferred. Their future item behavior will activate the existing `PartyRewardModifierState` rather than duplicate reward calculation or duration state.
- New games currently begin with only the confirmed protagonist because canonical recruitment quest content remains V6 Part 10 work. The mandatory first-ally runtime transaction is implemented and validated, while party-title acquisition remains future narrative work.
- `character_names.txt` currently contains only temporary entries pending creative-team curation.
- Playable-class definitions, natural class growth and the complete approved 30-skill class-progression roster are integrated; skill powers and resource costs remain provisional pending the general balance pass.
- Enemy actions currently use basic attacks; enemy skill selection can reuse the skill executor later but is outside the current scope.
- Every registered city provides an INN, while other system and narrative locations depend on their external definitions. Canonical campaign narrative-location and NPC content remains scheduled with the first five quests.
- Quest, objective, reward, effect, flag, requirement, narrative, dialogue, class-path and recruitment infrastructure is implemented. Temporary validation content was removed after representative end-to-end testing; the first five canonical quests remain V6 Part 10 content.
- Canonical class-path equipment and questionnaire content are integrated. The protagonist transition is composed with candidate identity, quest completion and mandatory first-ally recruitment, but normal campaign access remains absent until canonical Quest 4 is authored.
- `quest_recruitments.txt` is intentionally header-only until canonical recruitment steps are authored.
- `RECRUIT` dialogue references are valid only for candidate IDs declared by `QuestRecruitmentCatalog`. No canonical recruit dialogue exists while the recruitment catalog remains empty.
- The first recruitment composes with the level-7 class-path ceremony. Later recruitments remain narrative events and will reuse the generic recruitment components without repeating the ceremony.
- `QuestProgress`, `CampaignFlags` and recruitment-candidate state remain outside campaign snapshots until the complete internal version-3 integration in V6 Part 9.
- Narrative text uses automatic wrapping, while older map, enemy, item and other description screens retain their existing presentation until the post-V6 interface review.
- `Battle` remains large and may later be divided into more focused action services.
- Permanent skill-scroll items, skills and curated enemy drops remain deferred until their draft effects receive the required combat mechanics and final content review. Scroll-skill preservation across class transition is structurally implemented but cannot yet be exercised with canonical content.
- Only the first ceremony-composed recruitment currently has runtime orchestration. Although `quest_recruitments.txt` structurally accepts up to three definitions, second- and third-ally records must wait for their narrative recruitment dispatcher.

These limitations are tracked in `V6_ROADMAP.md` and should be replaced incrementally instead of through one large rewrite.
