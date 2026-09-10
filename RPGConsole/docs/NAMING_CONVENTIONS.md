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
rpg.world.encounters
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
| `Execution` | Mutable state limited to one execution of a composite action | `SkillExecution` |
| `Context` | Inputs and shared selections used throughout one execution | `SkillExecutionContext` |
| `Generator` | Selects or creates results using rules and randomness | `EncounterGenerator`, `ShopStateGenerator` |
| `Calculator` | Centralizes one domain formula without coordinating the complete flow | `DamageCalculator`, `RecoveryCalculator`, `EffectResistanceCalculator`, `CharacterGrowthCalculator` |
| `Coordinator` | Orchestrates several existing components without owning all their internal rules | `BattleCoordinator` |
| `Service` | Applies a cohesive domain operation, usually without console input | `BattleRewardService`, `ShopTransactionService` |
| `Menu` | Reads player input and manages one player-facing decision flow | `RouteMenu`, `EnemyTargetMenu`, `ShopMenu` |
| `Screen` | Displays a complete console view or result | `TitleScreen`, `BattleRewardScreen` |
| `Result` / `Rewards` | Represents the outcome of an operation | `BattleResult`, `BattleRewards`, `ShopPurchaseResult` |
| `Outcome` | Closed enum describing how an attempted operation concluded | `SkillEffectOutcome` |
| `Formatter` | Produces shared presentation text without deciding domain behavior | `CombatEffectFormatter`, `ItemMenuFormatter`, `SkillDetailFormatter` |
| `Group` | Owns and queries a collection that acts as one domain unit | `EnemyBattleGroup` |
| `Slot` | Represents one uniquely identifiable position inside a runtime group | `EnemyBattleSlot` |

- `Session` represents the temporary progress of a bounded execution of a mechanic, such as an exploration.
- `State` represents mutable data linked to an entity over a broader lifecycle, such as a shop's current inventory.
- `Execution` is narrower than `Session`: it exists for one composite action and is discarded when that action finishes.
- `Calculator` owns a formula; its caller owns sequencing, mutation and presentation.

Use `naturalStats` for the character values produced by class and level before equipment bonuses and temporary combat effects. Use `effective` only for values after every currently applicable modifier has been considered.

A `Definition` does not require a parallel mutable class with the same root name. Create a runtime counterpart only when the concept has persistent instance state. For example, `SkillDefinition` can be referenced directly by a character because per-use state belongs to `SkillExecution`; an otherwise empty `Skill` wrapper would add no responsibility.

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
```

Header field names use `camelCase` and should match the corresponding Java vocabulary:

```text
# enemyId;itemId;dropChance;minQuantity;maxQuantity
# id;scenarioId;accessType;offerSlots;name;description
# id;shopId;itemId;shopSalePricePercent;stock;weight
# id;classId;acquisitionLevel;mpCost;spCost;name;description
# skillId;sequence;phase;effectType;targetType;damageType;power;attribute;stages;duration;triggerChancePercent;specialStateType;conditionType;conditionValue
```

Enum fields use their exact Java constant names:

```text
COMMON
SPECIAL_ONLY
```

Do not rename or reorder fields without updating:

- the loader;
- `DATA_FORMATS.md`;
- every existing data row;
- relevant validation and tests.

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
- [ ] Have related documentation and data headers been updated?

When a convention does not clearly cover a new case, the team should agree on the vocabulary before several competing names enter the codebase.
