# RPGConsole — Naming Conventions

## Purpose

This document defines the naming conventions used by RPGConsole.

Its purpose is not only to standardize capitalization, but also to make names communicate the role, scope and lifetime of each element. A contributor should be able to understand why a class exists and where it belongs by reading its name.

These conventions apply to new code and should be considered during refactoring. Existing names should not be changed only for cosmetic reasons if doing so creates unnecessary risk.

## Language convention

RPGConsole separates technical language from game content.

| Context | Language | Example |
|---|---|---|
| Java identifiers | English | `getLivingSlots()` |
| Packages | English | `rpg.world.encounters` |
| Technical comments and Javadoc | English | `// Loads enemy definitions...` |
| Developer-facing exception messages | English | `Enemy ID cannot be empty.` |
| Text displayed to the player | Brazilian Portuguese | `Você encontrou uma Merumã!` |
| Display names for game content | Brazilian Portuguese | `Gruta de Relamparrocha` |
| Stable content IDs | Brazilian Portuguese without accents | `gruta_relamparrocha` |

Do not mix languages inside one identifier:

```java
// Avoid
displayInimigoStatus();
enemyVidaAtual;

// Prefer
displayEnemyStatus();
enemyCurrentHp;
```

## General naming principles

### Prefer intention over implementation

A name should describe why something exists, not merely how it is implemented.

```java
// Avoid
enemyMap
listProcessor
fileThing

// Prefer
enemyCatalog
battleRewardService
enemyDropLoader
```

### Use the narrowest accurate name

Do not name a class `GameManager` if it only coordinates battles. Use `BattleCoordinator`.

Do not name a method `process()` if it specifically grants rewards. Use `grantRewards()`.

### Avoid unexplained abbreviations

Common game abbreviations are acceptable when they are part of the domain vocabulary:

```java
maxHp
currentMp
spCost
```

Avoid local inventions:

```java
// Avoid
eDef
encGen
currScn

// Prefer
enemyDefinition
encounterGenerator
currentScenario
```

### Singular and plural must communicate cardinality

Use singular names for one object and plural names for collections:

```java
Enemy enemy;
List<Enemy> enemies;

DropEntry dropEntry;
List<DropEntry> dropTable;

EnemyBattleSlot selectedTarget;
List<EnemyBattleSlot> livingTargets;
```

Do not use a singular variable name for a collection.

## Packages

Package names use lowercase letters without underscores:

```text
rpg.characters
rpg.battle
rpg.battle.rewards
rpg.items.equipment
rpg.world.encounters
rpg.flags
rpg.requirements
rpg.quests
rpg.quests.rewards
rpg.quests.effects
rpg.narrative
```

Packages represent cohesive areas of responsibility, not arbitrary folder organization.

Subpackages are separate Java packages. A class in `rpg.world` must import classes from `rpg.world.scenarios`.

Use a new subpackage when a group of classes has a clear shared responsibility and enough content to justify the separation. Do not create a package for every individual class.

## Classes

Class names use `PascalCase` and are normally nouns or noun phrases:

```java
GameCharacter
EnemyDefinition
ExplorationSession
BattleRewardService
ShopDefinition
ShopOfferState
ShopTransactionService
EquipmentDefinition
EquipmentService
```

The filename must exactly match the public class name:

```text
EnemyDropCatalog.java
```

Avoid generic suffixes such as `Manager`, `Helper`, `Utility`, `Processor` or `Handler` unless they describe a precise and established responsibility. Prefer one of the project roles below.

## Class role vocabulary

The following suffixes have specific meanings in RPGConsole.

| Suffix | Responsibility | Example |
|---|---|---|
| `Definition` | Reusable immutable content template | `EnemyDefinition`, `ShopDefinition` |
| `Catalog` | Registry that stores and resolves objects by stable ID or grouping key | `EnemyCatalog`, `TravelCatalog` |
| `Loader` | Reads external data and converts it into definitions or catalog entries | `EnemyLoader`, `EnemyDropLoader` |
| `Entry` | One immutable association or rule stored inside a catalog or external-data table | `EncounterEntry`, `DropEntry`, `ShopStockEntry` |
| `Settings` | Immutable configuration values controlling a feature | `ExplorationSettings`, `DungeonSettings` |
| `Session` | Mutable progress that lasts for one execution of a feature | `ExplorationSession` |
| `State` | Mutable runtime data preserved for the lifetime of a domain object or the current program execution | `ShopState`, `ShopOfferState` |
| `Execution` | Mutable state limited to one execution of a composite action | `SkillUseState` |
| `Context` | Inputs and shared selections used throughout one execution | `SkillExecutionContext` |
| `Generator` | Selects or creates results using rules and randomness | `EncounterGenerator`, `ShopStateGenerator` |
| `Calculator` | Centralizes one domain formula without coordinating the complete flow | `DamageCalculator`, `RecoveryCalculator`, `EffectResistanceCalculator`, `CharacterGrowthCalculator` |
| `Coordinator` | Orchestrates several existing components without owning all their internal rules | `BattleCoordinator` |
| `Service` | Applies a cohesive domain operation, usually without console input | `BattleRewardService`, `ShopTransactionService`, `EquipmentService` |
| `Menu` | Reads player input and manages one player-facing decision flow | `RouteMenu`, `EnemyTargetMenu`, `ShopMenu` |
| `Screen` | Displays a complete console view or result | `TitleScreen`, `BattleRewardScreen` |
| `Result` / `Rewards` | Represents the outcome of an operation | `BattleResult`, `BattleRewards`, `ShopPurchaseResult` |
| `Outcome` | Closed enum describing how an attempted operation concluded | `SkillEffectOutcome` |
| `Formatter` | Produces shared presentation text without deciding domain behavior | `CombatEffectFormatter`, `ItemMenuFormatter`, `SkillDetailFormatter`, `EquipmentDetailFormatter` |
| `Group` | Owns and queries a collection that acts as one domain unit | `EnemyBattleGroup` |
| `Slot` | Represents one uniquely identifiable position inside a runtime group | `EnemyBattleSlot` |
| `Progress` | Mutable campaign progression separated from immutable definitions | `QuestProgress`, `DungeonProgress`, `TravelProgress` |
| `Event` | Immutable confirmed gameplay occurrence supplied to a synchronous consumer | `QuestProgressEvent` |
| `Update` | Immutable description of one state change produced by processing | `QuestProgressUpdate` |
| `Evaluator` | Purely derives a result from definitions and current state without mutation | `RequirementEvaluator` |
| `Validator` | Performs one validation that may require several completed catalogs | `QuestAvailabilityValidator`, `NarrativeReferenceValidator` |
| `Resolver` | Derives currently applicable domain choices from existing definitions and state | `QuestNpcInteractionResolver` |
| `Processor` | Performs one narrowly named text or data transformation without owning the surrounding flow | `NarrativeTextProcessor` |
| `Presenter` | Converts prepared domain results into an output sequence without deciding domain rules | `DialoguePresenter`, `QuestProgressPresenter` |

- `Session` represents the temporary progress of a bounded execution of a mechanic, such as an exploration.
- `State` represents mutable data linked to an entity over a broader lifecycle, such as a shop's current inventory.
- `Execution` is narrower than `Session`: it exists for one composite action and is discarded when that action finishes.
- `Calculator` owns a formula; its caller owns sequencing, mutation and presentation.

Use `naturalStats` for the character values produced by class and level before equipment bonuses and temporary combat effects. Use `effective` only for values after every currently applicable modifier has been considered.

A `Definition` does not require a parallel mutable class with the same root name. Create a runtime counterpart only when the concept has persistent instance state. For example, `SkillDefinition` can be referenced directly by a character because per-use state belongs to `SkillUseState`; an otherwise empty `Skill` wrapper would add no responsibility.

Before creating a new class, check whether its intended role matches one of these established terms.

## Abstract classes and inheritance

Base-class names describe the general domain concept. Do not add an `Abstract` prefix merely because the class is abstract:

```java
// Prefer
public abstract class Scenario
public abstract class CombatScenario

// Avoid
public abstract class AbstractScenario
```

Subclass names describe a more specific concept:

```text
Scenario
├── City
└── CombatScenario
    ├── Field
    └── Dungeon
```

Use inheritance only for a genuine “is a” relationship. Use composition for “has a” relationships:

```text
Enemy is a GameCharacter.
Battle has an EnemyBattleGroup.
```

## Interfaces

Interface names also use `PascalCase` and should describe a capability or contract. Do not use an `I` prefix.

```java
// Prefer
Usable
Targetable

// Avoid
IUsable
ITargetable
```

Only create an interface when more than one implementation or a meaningful contract is expected. Do not create interfaces only to mirror every class.

## Enums

Enum type names use singular `PascalCase`:

```java
EnemyRank
DamageType
ExplorationEventType
```

Enum constants use `UPPER_SNAKE_CASE`:

```java
COMMON
SPECIAL_ONLY
EPIC_ENCOUNTER
```

Use enum constant names in external data files because loaders call `valueOf()`.

If a player-facing translation is required, store a display label in the enum instead of translating the constant at every call site:

```java
PHYSICAL("Físico")
```

## Fields and local variables

Fields and local variables use `camelCase`:

```java
currentScenario
enemyDropCatalog
specialAreaChance
```

Names should identify both the subject and meaning when context is not obvious:

```java
// Avoid
current
maximum
selected

// Prefer
currentPosition
maximumEnemies
selectedTarget
```

Short loop variables such as `index` are preferred over single letters in application code:

```java
for (int index = 0; index < enemies.size(); index++) {
    // ...
}
```

Avoid vague temporary names such as:

```text
aux
temp
obj
data2
thing
x
```

## Constants

Constants use `UPPER_SNAKE_CASE` and must be declared with `static final`:

```java
public static final int MAXIMUM_SIZE = 3;
private static final int HEAL_MP_COST = 10;
```

A global limit and a content-specific setting are different concepts:

```java
EnemyBattleGroup.MAXIMUM_SIZE // Global supported limit
settings.getMaxEnemies()     // Maximum configured for one scenario
```

Avoid repeating the same literal limit in several classes. Other validations should reference the established constant whenever doing so does not create an inappropriate package dependency.

## Methods

Method names use `camelCase` and normally begin with a verb:

```java
startEncounter()
displayEnemyStatus()
collectEnemyDrops()
calculateAvailableCapacity()
```

Use consistent verbs for recurring responsibilities:

| Verb | Meaning | Example |
|---|---|---|
| `get` | Returns an existing value without performing a search operation visible to the caller | `getCurrentHp()` |
| `getById` | Resolves a registered object and fails if the ID is unknown | `getById("lobermo")` |
| `create` | Constructs a new runtime object | `createEnemy()` |
| `load` | Reads and converts external data | `load(filePath)` |
| `register` | Adds an entry to a catalog | `register(enemyDefinition)` |
| `generate` | Produces a result using rules or randomness | `generateGroup(...)` |
| `select` | Asks for or determines one option among candidates | `selectTarget(...)` |
| `display` | Prints information without asking for a decision | `displayBattleResult()` |
| `execute` | Performs one complete action or event | `executePlayerTurn()` |
| `start` | Begins a longer process or lifecycle | `startEncounter()` |
| `grant` | Calculates/applies a reward or permission | `grantRewards()` |
| `collect` | Gathers values into a result collection | `collectEnemyDrops()` |
| `validate` | Checks a rule and throws when invalid | `validateGroupSize()` |
| `determine` | Derives one result from current state | `determineBattleResult()` |
| `purchase` | Attempts to buy from a shop and returns the transaction outcome | `purchase(shopState, selectedOffer, quantity)` |
| `sell` | Attempts to sell an owned item to a shop and returns the transaction outcome | `sell(selectedItem, quantity)` |
| `decrease` | Reduces an existing mutable quantity after validation | `decreaseStock(quantity)` |

Avoid methods whose names hide several unrelated operations. If `processGame()` loads files, starts a battle and saves progress, its responsibilities must be separated.

## Boolean names

Boolean fields and methods should read like true-or-false questions:

```java
playerFled
specialArea
isAlive()
isComplete()
hasLivingEnemies()
canEncounterMerchant()
shouldDisplayScenarioDescription
```

Preferred prefixes:

- `is`: current state or classification;
- `has`: possession or existence;
- `can`: permission or capability;
- `should`: decision or pending behavior.

Avoid negative boolean names because double negatives are difficult to read:

```java
// Avoid
if (!enemy.isNotAlive())

// Prefer
if (enemy.isAlive())
```

## Collection names

Name collections after what they contain, not their implementation:

```java
List<EnemyDefinition> generatedGroup;
Map<Item, Integer> collectedItems;
Map<String, List<EncounterEntry>> entriesByScenario;
```

Common mapping pattern:

```text
valueByKey
valuesByGroup
```

Examples:

```java
definitionById
dropsByEnemyId
entriesByScenario
maximumCopiesByEnemy
```

## Stable content IDs

Stable IDs are technical identifiers for game content. They are separate from display names.

Rules:

- use lowercase letters and underscores;
- do not use accents;
- do not use spaces or hyphens;
- use Portuguese canonical names;
- keep IDs stable even if the display text changes;
- prefer meaningful names over sequential numbers.

Examples:

| Content | Stable ID | Display name |
|---|---|---|
| Scenario | `verdes_ermos` | `Verdes Ermos` |
| Dungeon | `gruta_relamparrocha` | `Gruta de Relamparrocha` |
| Enemy | `mestre_eletrogoblin` | `Mestre Eletrogoblin` |
| Item | `meruma` | `Merumã` |
| Shop | `mercado_bree` | `Mercado de Bree` |
| Shop stock entry | `bree_meruma_discount` | Not displayed directly |
| Playable class | `flagelo` | `Flagelo` |
| Quest | `ordem_dos_alquimistas` | `A Ordem dos Alquimistas` |
| Quest objective | `falar_com_mestre_alquimista` | `Converse com o mestre alquimista.` |
| Permanent flag | `investigacao_alquimista_iniciada` | Not displayed as an ID |
| Requirement set | `acesso_investigacao_alquimista` | Not displayed directly |
| Narrative location | `taverna_bree` | `Taverna de Bree` |
| Narrative NPC | `mestre_alquimista` | Display name defined externally |
| Dialogue | `oferta_ordem_dos_alquimistas` | Not displayed as an ID |

Quest-related IDs should describe stable narrative meaning rather than current numeric position:

```text
# Prefer
ordem_dos_alquimistas
falar_com_mestre_alquimista

# Avoid
quest_1
objective_3
```

Step numbers and objective order are structural values and are not substitutes for stable quest and objective IDs.

Flag IDs should describe the permanent consequence that became true. Requirement-set IDs should describe the access or availability condition they represent. Dialogue IDs should identify their narrative purpose or context rather than repeat only the speaker ID.

A playable-class symbol is presentation data, not a stable ID. Code must never use the symbol for catalog lookup, equality, class compatibility or progression rules. Different internal definitions may intentionally share a symbol when they represent the same player-facing class.

A shop-stock entry ID identifies one possible commercial offer, not merely an item. It should remain meaningful when the same shop contains multiple offers for the same item with different prices or quantities.

Do not translate stable IDs when translating display names.

### Content variants

Create a separate ID when a variant has different attributes, behavior or drops:

```text
lobermo
lobermo_da_montanha
```

Do not make enemy drops depend on a scenario merely to simulate a statistically different enemy variant. Register the appropriate variant in that scenario's encounter table.

The display names may remain related:

```text
Lobermo
Lobermo da Montanha
```

### Runtime labels are not IDs

Labels used to distinguish repeated battle participants are temporary:

```text
Passavento 1
Passavento 2
```

They must not change the enemy's stable ID or canonical display name.

### Playable-character names

Playable-character names are mutable runtime identity, not stable content IDs.

`CharacterNameRules` is the single owner of their normalization, validation, maximum length and case-insensitive comparison. Menus, loaders and party operations must reuse it instead of defining local regular expressions or length constants.

Valid names:

- Contain at most 16 Unicode code points after normalization.
- Use Unicode letters.
- May use an ordinary space (` `), hyphen (`-`) or straight apostrophe (`'`) only between letters.
- Are normalized to Unicode NFC.
- Have outer ordinary spaces removed.
- Collapse repeated ordinary spaces.

Names containing numbers, controls, tabs, unrelated punctuation or malformed separators are invalid.

`CharacterNameLoader`, `CharacterNameCatalog` and `CharacterNameSelector` keep their shared `CharacterName` prefix because they participate in the same narrowly defined naming subsystem. Their suffixes continue to describe distinct responsibilities: loading, storage and random selection.

## External data fields

Data filenames use lowercase `snake_case` and normally describe plural content:

```text
enemies.txt
encounters.txt
enemy_drops.txt
shops.txt
shop_stock.txt
skills.txt
skill_effects.txt
skill_scrolls.txt
weapons.txt
armors.txt
trinkets.txt
character_names.txt
playable_classes.txt
quests.txt
quest_steps.txt
quest_objectives.txt
flags.txt
requirements.txt
quest_rewards.txt
quest_effects.txt
narrative_locations.txt
npcs.txt
npc_placements.txt
dialogues.txt
quest_dialogues.txt
```

Header field names use `camelCase` and should match the corresponding Java vocabulary:

```text
# enemyId;itemId;dropChance;minQuantity;maxQuantity
# id;scenarioId;accessType;offerSlots;name;description
# id;shopId;itemId;shopSalePricePercent;stock;weight
# id;classId;acquisitionType;levelRequirement;mpCost;spCost;name;description
# itemId;skillId;classId
# skillId;sequence;phase;effectType;targetType;damageType;power;attribute;stages;duration;triggerChancePercent;specialStateType;conditionType;conditionValue
# itemId;damageType;weaponType;levelRequirement;normalAttackName;baseWeaponDamage;hpBonus;mpBonus;spBonus;attackBonus;defenseBonus;magicBonus;resistanceBonus;speedBonus;luckBonus
# itemId;armorWeight;levelRequirement;hpBonus;mpBonus;spBonus;attackBonus;defenseBonus;magicBonus;resistanceBonus;speedBonus;luckBonus
# itemId;trinketType;levelRequirement;hpBonus;mpBonus;spBonus;attackBonus;defenseBonus;magicBonus;resistanceBonus;speedBonus;luckBonus;experienceModifierPercent;goldModifierPercent
# name
# id;name;symbol;baseHp;baseMp;baseSp;baseAttack;baseDefense;baseMagic;baseResistance;baseSpeed;baseLuck;primaryAttribute;secondaryAttribute;armorWeight;description
# id;title;description;startingNpcId;availabilityRequirementSetId
# questId;step;title;description
# id;questId;step;order;objectiveType;targetId;requiredQuantity;text
# id;description
# setId;order;requirementType;targetId;requiredValue
# questId;order;rewardType;rewardId;quantity
# questId;step;trigger;order;effectType;targetId;displayText
# id;cityId;name;description;requirementSetId
# id;name;description;defaultDialogueId
# npcId;narrativeLocationId;requirementSetId
# dialogueId;line;speakerType;speakerId;text
# questId;step;context;dialogueId
```

`character_names.txt` is intentionally different from semicolon-based definition files: each active line is one complete name.

Use `symbol` for the single Unicode presentation marker stored by a playable class. Do not call it `icon`, because it is console text rather than an image or graphical asset. A symbol must not replace the class ID in code or the complete class name where detailed identification is required.

Use `baseWeaponDamage` in Java and external headers. `DBA` may appear in internal design discussion, but the player-facing label is `Poder`.

Use `ArmorWeight` for both class compatibility and armor definitions. The former name `PlayableClassWeight` is obsolete.

`DamageType` belongs to `rpg.battle`, because physical and magical damage are shared by weapon attacks and skills rather than owned by `rpg.skills`.

Enum fields use their exact Java constant names:

```text
COMMON
SPECIAL_ONLY
```

Skill acquisition data uses the exact `SkillAcquisitionType` constants:

```text
STARTER
LEVEL
SCROLL
```

`levelRequirement` is the automatic learning level for `STARTER` and `LEVEL`, and the minimum scroll-use level for `SCROLL`.

Do not rename or reorder fields without updating:

- the loader;
- `DATA_FORMATS.md`;
- every existing data row;
- relevant validation and tests.

### Quest and narrative vocabulary

Use these terms consistently:

| Name | Meaning |
|---|---|
| `quest` | Complete immutable narrative-progression definition |
| `step` | One sequential phase belonging to a quest |
| `objective` | One measurable condition belonging to the current step |
| `progress` | Mutable campaign state for active counters and completed quests |
| `event` | Confirmed gameplay occurrence submitted synchronously |
| `update` | Description of progress produced by processing |
| `reward` | Material value granted to the party or its current members |
| `effect` | Permanent campaign consequence such as a flag or route unlock |
| `flag` | Set-only permanent world consequence |
| `requirement` | Pure condition evaluated from current campaign state |
| `requirementSet` | Ordered AND group of reusable requirements |
| `narrativeLocation` | City space dedicated to narrative NPC interaction |
| `npc` | Canonical ordinary non-hostile character identity |
| `npcPlacement` | Fixed association between one NPC and one narrative location |
| `dialogue` | Ordered immutable sequence of utterances |
| `dialogueLine` | One external utterance inside a dialogue |
| `questDialogue` | Association between quest context and a canonical dialogue |

Do not call quest lifecycle fields flags:

```java
// Prefer
questStatus
completedQuestIds

// Avoid
questStartedFlag
questCompletedFlag
```

Do not call requirement results unlock flags. Availability remains derived unless a real permanent consequence is represented through `CampaignFlags`.

Use `startingNpcId` for the NPC that offers a quest. Use `npcId` for an ordinary canonical NPC reference. Use `speakerId` only inside one dialogue line whose `speakerType` determines the referenced catalog.

Use `displayText` for optional text attached to a permanent effect. Use `text` for one objective description or dialogue utterance. Use `description` for longer content describing a definition.

### External narrative text

Literal `\n` is the only supported external narrative-text escape sequence in V6.

A backslash not immediately followed by `n` is invalid. Semicolons remain reserved field separators and cannot appear inside text fields.

Supported placeholders use lower camel case inside braces:

```text
{leaderName}
{groupMember2}
{groupMember3}
{groupMember4}
```

Placeholder names are case-sensitive. They may appear only in the documented description and dialogue fields. They must not appear in technical IDs, names or titles.

`{groupName}` is reserved for future use and remains invalid in V6.

### Commercial vocabulary

Commercial names use the shop's perspective consistently.

| Name | Meaning |
|---|---|
| `baseShopSalePrice` | Base price used when the shop sells the item to the player |
| `shopSalePricePercent` | Percentage applied to the base price of an offer sold by the shop |
| `finalShopSalePrice` | Final unit price charged by the shop |
| `shopPurchasePrice` | Unit price paid when the shop purchases the item from the player |
| `initialStock` | Quantity assigned when one offer becomes active |
| `remainingStock` | Mutable quantity still available during the current execution |
| `offerSlots` | Maximum number of possible offers selected as active offers |
| `weight` | Relative selection weight, not a percentage |

Do not rename `shopPurchasePrice` to `salePrice` based on the player's perspective. The project names both sides of a commercial transaction from the shop's perspective.

Use `stockEntry` for an immutable possible offer loaded from external data and `offerState` for its selected mutable runtime state.

## Player-facing display text

Player-facing text uses natural Brazilian Portuguese with accents and punctuation:

```java
System.out.println("Você encontrou uma Merumã!");
```

Do not expose raw enum constants or stable IDs directly to the player:

```text
Avoid: SPECIAL_ONLY
Prefer: Área especial

Avoid: PHYSICAL
Prefer: Físico
```

Developer-facing exception messages remain in English because they describe programming or data configuration errors rather than game events.

Valid playable-character names are displayed completely and are not truncated.

Playable-class symbols should use broadly supported Unicode characters. Their precise appearance may vary according to the terminal, operating system and fallback font, so interfaces must not rely on exact glyph shape, color or visual width for correctness.

## Comments and Javadoc

File-level class comments are short, technical and written in English:

```java
// Loads enemy item drops from an external text file.
public final class EnemyDropLoader {
```

Comments should explain why a decision exists when the code alone cannot communicate it. Avoid narrating obvious statements:

```java
// Avoid: Adds one to index.
index++;

// Useful: The boss position does not generate a random event.
```

Use Javadoc for public APIs whose parameters, lifecycle or rules are not obvious. Do not add large Javadocs to trivial getters.

Temporary compatibility code must be explicitly marked and removed in the planned migration:

```java
/*
 * Temporary reference while Battle is being
 * migrated from one enemy to an enemy group.
 */
```

## Git branches and commits

Branch names use lowercase words separated by hyphens and begin with a category:

```text
feature/v2-data-driven-enemies
feature/enemy-groups
fix/boss-flee-position
docs/update-v2-architecture
refactor/world-responsibilities
```

Recommended categories:

- `feature/`: new behavior;
- `fix/`: bug correction;
- `refactor/`: structural change without intended behavior change;
- `docs/`: documentation-only work;
- `test/`: tests and test infrastructure.

Commit messages use a concise conventional prefix followed by an English imperative description:

```text
feat: add external enemy drop tables
refactor: simplify battle reward flow
feat: support weighted enemy groups
fix: preserve boss position after fleeing
docs: update V4 architecture documentation
```

Use `feat`, `fix`, `refactor`, `docs` or `test` according to the primary purpose of the commit.

Do not use the commit subject as a complete patch note. Detailed context can be placed in the commit body when necessary.

## Names to avoid

Avoid names that are vague, misleading or tied to a temporary implementation:

```text
GameManager
GeneralHelper
CommonUtils
DataProcessor
Thing
ObjectData
TemporaryClass
enemy1 / enemy2 / enemy3
method2
newMethod
finalVersion
```

Prefer domain-specific alternatives:

```text
BattleCoordinator
EncounterGenerator
EnemyBattleGroup
EnemyBattleSlot
selectedTarget
livingEnemies
```

## Contributor checklist

Before introducing a new name, verify:

- [ ] Is it written in the correct language for its context?
- [ ] Does its capitalization follow Java conventions?
- [ ] Does it communicate its domain responsibility?
- [ ] Is its singular or plural form accurate?
- [ ] Does it avoid unexplained abbreviations?
- [ ] Does it use an established project suffix where appropriate?
- [ ] Is a new class actually necessary?
- [ ] Is a stable ID separate from its display name?
- [ ] Is a content variant modeled with its own ID when its rules differ?
- [ ] Does the name remain meaningful if the internal implementation changes?
- [ ] Does playable-character naming reuse `CharacterNameRules`?
- [ ] Is a playable-class symbol treated only as presentation data?
- [ ] Have related documentation and data headers been updated?
- [ ] Is quest lifecycle represented through `QuestProgress` instead of ad hoc flags?
- [ ] Does a permanent flag describe a real stable consequence?
- [ ] Is derived availability kept out of mutable and persisted state?
- [ ] Does an event name describe something already confirmed?
- [ ] Does an update name describe the resulting state change?
- [ ] Are reward and effect used for material value and permanent consequence respectively?
- [ ] Do narrative IDs remain separate from display names and runtime character names?
- [ ] Are placeholders restricted to the supported vocabulary and fields?
- [ ] Does external narrative text use only literal `\n` as an escape?

When a convention does not clearly cover a new case, the team should agree on the vocabulary before several competing names enter the codebase.
