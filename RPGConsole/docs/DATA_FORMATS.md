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
| 4 | Maximum MP | `int` | `1` |
| 5 | Maximum SP | `int` | `1` |
| 6 | Attack | `int` | `8` |
| 7 | Defense | `int` | `5` |
| 8 | Magic | `int` | `1` |
| 9 | Resistance | `int` | `1` |
| 10 | Speed | `int` | `5` |
| 11 | Luck | `int` | `1` |
| 12 | Enemy rank | `EnemyRank` | `COMMON` |
| 13 | Description | `String` | `Uma criatura lupina...` |

Header:

```text
# id;name;maxHp;maxMp;maxSp;attack;defense;magic;resistance;speed;luck;rank;description
```

Example:

```text
lobermo;Lobermo;50;1;1;8;5;1;1;5;1;COMMON;Uma criatura lupina que percorre os Verdes Ermos em busca de presas.
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

Rules:

- Maximum HP must be positive.
- Maximum MP and SP cannot be negative.
- ATK, DEF, MAG, RES, VEL and LCK must be positive.
- Literal `\n` sequences in descriptions are converted into line breaks.
- Invalid definitions report their source line and enemy ID.

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
| 7 | Area restriction | `ExplorationAreaRestriction` | `ANY` |

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
- The referenced enemy ID must exist in `EnemyCatalog`.

Loading flow:

```text
encounters.txt
    -> EncounterLoader
    -> EncounterCatalog
    -> EncounterGenerator
    -> EnemyDefinition
```

## Exploration reward definitions

File:

`data/exploration_rewards.txt`

Each valid exploration reward line contains exactly 7 fields:

| Position | Field | Java type | Example |
|---:|---|---|---|
| 1 | Scenario ID | `String` | `verdes_ermos` |
| 2 | Reward type | `ExplorationRewardType` | `ITEM` |
| 3 | Reward ID | `String` | `meruma` |
| 4 | Minimum quantity | `int` | `1` |
| 5 | Maximum quantity | `int` | `2` |
| 6 | Weight | `int` | `60` |
| 7 | Area restriction | `ExplorationAreaRestriction` | `NORMAL_ONLY` |

Header:

```text
# scenarioId;rewardType;rewardId;minQuantity;maxQuantity;weight;areaRestriction
```

Examples:

```text
verdes_ermos;ITEM;meruma;1;2;60;NORMAL_ONLY
verdes_ermos;ITEM;pelo_lupino;1;3;25;ANY
verdes_ermos;GOLD;;5;15;15;ANY
```

Rules:

- Blank lines and lines beginning with `#` are ignored.
- Every active definition must contain exactly 7 fields.
- `scenarioId` must reference an existing combat scenario.
- `rewardType` must be `ITEM` or `GOLD`.
- `ITEM` rewards require a non-empty `rewardId` referencing an item in the runtime `ItemCatalog`.
- `GOLD` rewards require an empty `rewardId`.
- `minQuantity` must be at least 1.
- `maxQuantity` cannot be lower than `minQuantity`.
- `weight` must be greater than zero.
- Weights are relative and do not need to total 100.
- `areaRestriction` must be `NORMAL_ONLY`, `SPECIAL_ONLY` or `ANY`.
- `ANY` entries are eligible in both normal and special areas and complement area-specific entries.
- Exactly one eligible entry is selected for each exploration reward event.
- The granted quantity is selected inclusively between `minQuantity` and `maxQuantity`.
- Entries are grouped by scenario and filtered according to the current area type.
- Completely identical entries are rejected.
- The same item may appear in multiple entries when at least one other field differs.
- Rewards are repeatable and do not represent unique, narrative or quest rewards.
- A scenario with a positive `rewardChance` must have at least one eligible entry for every area type that can occur.
- A scenario with zero `rewardChance` does not require a reward pool.
- Temporarily disabled entries should be commented out instead of receiving zero weight.
- Exploration reward selection remains independent from enemy battle-drop rolls.

Loading and selection flow:

```text
exploration_rewards.txt
    -> ExplorationRewardLoader
    -> ExplorationRewardCatalog
    -> ExplorationRewardGenerator
    -> ExplorationReward
    -> shared Inventory or party gold
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
- The referenced item ID must exist in the runtime `ItemCatalog` built from the external item definitions.
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

## Item definitions

Base item data is loaded from:

`data/items.txt`

Header:

```text
# id;category;playerTradable;baseShopSalePrice;shopPurchasePrice;name;description
```

Example:

```text
meruma;CONSUMABLE;true;20;6;Merumã;Uma fruta que recupera 25 pontos de HP.
pelo_lupino;MATERIAL;true;;15;Pelo Lupino;Um tufo espesso de pelo obtido de um Lobermo.
```

Rules:

- Every item must have a unique and stable ID.
- IDs must use lowercase letters, numbers and single underscores, following the stable content ID conventions.
- `category` must correspond to a valid `ItemCategory`.
- `playerTradable` is reserved exclusively for future player-to-player trading.
- `baseShopSalePrice` is the base amount charged by shops before an offer-specific percentage is applied.
- `shopPurchasePrice` is the fixed amount paid by any shop when purchasing the item from the player.
- Either commercial price may remain empty when the corresponding transaction is unavailable.
- Every present commercial price must be a positive integer.
- The presence of `shopPurchasePrice` determines whether the item can be sold to shops.
- Items in the `KEY_ITEM` category cannot define `shopPurchasePrice`.
- Shop-specific availability, price variation and stock are defined outside `ItemDefinition`.
- Names and descriptions cannot be empty.
- Literal `\n` sequences in descriptions are converted into line breaks.
- `items.txt` stores only the properties shared by every item.
- Category-specific behavior is loaded from specialized data files.
- An item without specialized behavior is created as a `BasicItem`.

Loading flow:

```text
items.txt
    -> ItemLoader
    -> ItemDefinitionCatalog
```

## Recovery item definitions

Recovery item behavior is loaded from:

`data/recovery_items.txt`

Header:

```text
# itemId;targetType;hpRecovery;mpRecovery;spRecovery;reviveHpRecovery;purifiesStatusEffects
```

Example:

```text
meruma;SINGLE_ALLY;25;0;0;0;false
```

Rules:

- `itemId` must reference an item defined in `items.txt`.
- The referenced base item must use the `CONSUMABLE` category.
- The same item cannot have more than one recovery definition.
- Recovery values cannot be negative.
- A recovery item must have at least one effective component.
- An effective component is positive HP, MP, SP or revival recovery, or enabled purification.
- `targetType` must be `SINGLE_ALLY` or `ALL_ALLIES`.
- Revival is applied before ordinary HP recovery, allowing both components to affect a defeated ally.
- Group effects are evaluated independently for every eligible party member.
- The item is consumed only when at least one component affects at least one target.
- Purification is represented in the data model, but its runtime effect will be connected after status effects are implemented.

Definition composition and runtime flow:

```text
ItemDefinitionCatalog
RecoveryItemDefinitionCatalog
    -> ItemFactory
    -> ItemCatalog
    -> Inventory, drops and item menus
```

`ItemDefinition` contains the immutable properties shared by every item.

`RecoveryItemDefinition` contains only the specialized recovery behavior.

`ItemFactory` combines both definitions and creates the complete runtime object. Items without a recovery definition currently become `BasicItem`; matching specialized definitions become `RecoveryItem`.

`ItemCatalog` stores the resulting runtime items and resolves the stable IDs referenced by enemy drops, exploration events and inventories.

## Current item IDs

| ID | Name | Category | Runtime type | Effect |
|---|---|---|---|---|
| `meruma` | Merumã | `CONSUMABLE` | `RecoveryItem` | Recovers 25 HP from one ally |

## Scenario definitions

File:

`data/scenarios.txt`

Each active scenario line contains exactly 4 fields:

```text
# id;type;name;description
```

Example:

```text
bree;CITY;Bree;Uma cidade movimentada, erguida no encontro de várias estradas.
```

Rules:

- `id` is the stable scenario ID.
- `type` must be `CITY`, `FIELD` or `DUNGEON`.
- IDs, names and descriptions cannot be empty.
- Scenario IDs must be unique.
- Descriptions support literal `\n`.
- `ScenarioLoader` produces immutable `ScenarioDefinition` entries.
- Runtime `City`, `Field` and `Dungeon` objects are later created by `ScenarioFactory`.

## Combat scenario definitions

File:

`data/combat_scenarios.txt`

Each active combat-scenario line contains exactly 13 fields:

```text
# scenarioId;baseExperienceReward;baseGoldReward;explorationLength;battleChance;rewardChance;merchantChance;epicEncounterChance;specialAreaChance;minEnemies;maxEnemies;normalAreaMessage;specialAreaMessage
```

Rules:

- `scenarioId` must reference an existing `FIELD` or `DUNGEON`.
- Cities cannot have combat-scenario data.
- Each combat scenario can have only one definition.
- Numeric fields must contain valid integers.
- Exploration lengths, chances and enemy-count limits are validated by `ExplorationSettings`.
- Only dungeons may define a positive `epicEncounterChance`.
- Normal and special area messages cannot be empty.
- Area messages support literal `\n`.
- Base EXP and gold rewards belong to the combat scenario rather than the common scenario definition.

## Dungeon definitions

File:

`data/dungeons.txt`

Each active dungeon line contains exactly 3 fields:

```text
# scenarioId;bossEnemyIds;epicEnemyIds
```

Example:

```text
gruta_relamparrocha;mestre_eletrogoblin;relampagolem
```

Rules:

- `scenarioId` must reference an existing `DUNGEON`.
- The dungeon must also have combat-scenario data.
- Enemy groups use comma-separated stable enemy IDs.
- Referenced enemies must exist.
- Boss entries must reference enemies with the `BOSS` rank.
- Epic entries must reference enemies with the `EPIC` rank.
- Group sizes, empty IDs and repeated IDs are validated by `DungeonDefinition`.
- Each dungeon can have only one dungeon definition.

## Travel route definitions

File:

`data/travel_routes.txt`

Each active travel-route line contains exactly 7 fields:

```text
# id;sourceScenarioId;destinationScenarioId;initiallyUnlocked;displayName;description;destinationExplorationPosition
```

Examples:

```text
bree_portao_sul;bree;verdes_ermos;true;Portão Sul;A estrada ao sul conduz aos Verdes Ermos.;1
verdes_ermos_bree;verdes_ermos;bree;true;Retornar a Bree;Você segue pela estrada de volta a Bree.;
```

Rules:

- Route IDs must be unique.
- Source and destination IDs must reference existing scenarios.
- A route cannot lead back to its own source.
- `initiallyUnlocked` must be `true` or `false`.
- Display names and descriptions cannot be empty.
- Descriptions support literal `\n`.
- Routes from the same source cannot repeat a destination.
- `destinationExplorationPosition` must be empty when the destination is a city.
- It is required when the destination is a combat scenario.
- Combat-scenario destination positions must fall within that scenario's exploration length.
- Route display names and descriptions also represent city-exit labels and flavor text.
- Adjacent travel through these routes is free.

## City transport definitions

Paid transport between discovered cities is loaded from:

`data/city_transport.txt`

Header:

```text
# destinationScenarioId;price;travelDescription
```

Example:

```
setegalhos;50;A viagem termina sob a copa colossal de Setegalhos.
```

Rules:

`destinationScenarioId` must reference an existing scenario whose type is `CITY`.
Each destination city may appear only once.
`price` must be a positive integer.
`travelDescription` cannot be empty.
Literal `\n` sequences in `travelDescription` are converted into line breaks.
Transport definitions describe configured destinations, not routes between specific origins.
The same destination price is therefore used regardless of the city, field or dungeon from which paid travel begins.
Scenario discovery is runtime state and is not stored in this file.
Reino de Altaneve remains absent until its scenario definition is approved.

Loading flow:

```
city_transport.txt
    -> CityTransportLoader
    -> CityTransportDefinition
    -> CityTransportCatalog
```

`CityTransportLoader` validates destination references against `ScenarioDefinitionCatalog`. `World` later combines the catalog with `WorldProgress` to expose only configured destinations already discovered by the player.

## Scenario branch definitions

File:

`data/scenario_branches.txt`

Each active branch line contains exactly 6 fields:

```text
# id;sourceScenarioId;position;destinationScenarioId;displayName;description
```

Example:

```text
verdes_ermos_bifurcacao_setegalhos;verdes_ermos;3;setegalhos;Seguir para Setegalhos;Você encontra uma bifurcação entre os campos.\nUma estrada parcialmente escondida pela vegetação conduz ao Povoado de Setegalhos.
```

Rules:

- Branch IDs must be unique.
- Source and destination IDs must reference existing scenarios.
- The source must be a combat scenario.
- `position` must be a valid integer within the source exploration length.
- A branch cannot occupy the final position of a dungeon.
- The same destination cannot be repeated at the same source position.
- Display names and descriptions cannot be empty.
- Descriptions support literal `\n`.
- Branches represent fixed alternatives discovered during exploration.

## INN definitions

File:

`data/inns.txt`

Each active INN line contains exactly 5 fields:

```text
# id;scenarioId;name;description;price
```

Example:

```text
bree_inn;bree;INN de Bree;Uma estalagem simples e acolhedora.\nSeus quartos oferecem o descanso necessário antes da próxima jornada.;25
```

Rules:

- `id` is the stable INN ID.
- `scenarioId` must reference an existing `CITY`.
- IDs, names and descriptions cannot be empty.
- INN IDs must be unique.
- Descriptions support literal `\n`.
- `price` must be a valid integer greater than zero.
- At most one INN may be registered for each city.
- The price applies to the whole party and is independent of party size.
- Prices remain externally configurable for future balancing.
- `InnLoader` produces immutable `InnDefinition` entries registered in `InnCatalog`.

## Shop definitions

File:

`data/shops.txt`

Each active shop line contains exactly 6 fields:

```text
# id;scenarioId;accessType;offerSlots;name;description
```

Examples:

```text
mercado_bree;bree;CITY;2;Mercado de Bree;Um mercado movimentado onde comerciantes de várias regiões oferecem suas mercadorias.
viajante_verdes_ermos;verdes_ermos;EXPLORATION;2;Mercador Viajante;Um mercador itinerante que atravessa os campos em busca de ingredientes raros e novos clientes.
```

Supported access types:

- `CITY`: a fixed shop accessed through the functional locations of a city.
- `EXPLORATION`: a merchant accessed through the merchant event of a field or dungeon.

Rules:

- Shop IDs must be unique and stable.
- IDs must use lowercase letters, numbers and single underscores.
- `scenarioId` must reference an existing scenario.
- `CITY` shops must reference city scenarios.
- `EXPLORATION` shops must reference field or dungeon scenarios.
- Multiple shops or merchants may reference the same scenario.
- `offerSlots` must be a positive integer.
- `offerSlots` defines the maximum number of offers selected for the shop at application initialization.
- Names and descriptions cannot be empty.
- Literal `\n` sequences in descriptions are converted into line breaks.
- Every combat scenario with a positive `merchantChance` must have at least one associated `EXPLORATION` shop.
- An `EXPLORATION` definition associated with a scenario whose `merchantChance` is zero is valid but inaccessible through the current exploration event.

Loading flow:

```text
shops.txt
    -> ShopLoader
    -> ShopDefinition
    -> ShopCatalog
```

## Shop stock definitions

File:

`data/shop_stock.txt`

Each active shop-stock line contains exactly 6 fields:

```text
# id;shopId;itemId;shopSalePricePercent;stock;weight
```

Examples:

```text
bree_meruma_discount;mercado_bree;meruma;50;2;5
bree_meruma_regular;mercado_bree;meruma;100;10;90
bree_meruma_small;mercado_bree;meruma;100;2;20
```

Rules:

- Every offer must have a unique and stable ID.
- `shopId` must reference an existing shop.
- `itemId` must reference an existing runtime item.
- The referenced item must define `baseShopSalePrice`.
- `shopSalePricePercent`, `stock` and `weight` must be positive integers.
- Weights are relative and their total is not limited to 100.
- Every shop must have at least one possible sale offer.
- Offers in the same shop are commercially duplicated when they repeat the same item, price percentage and initial stock quantity.
- Commercially duplicated offers are rejected even when their stable IDs or weights differ.
- Distinct offers may reference the same item when their price percentages or initial stock quantities differ.
- `stock` defines the finite initial quantity of that specific offer.
- Items sold by the player are not added to shop stock and do not create buyback offers.

The final unit price charged by a shop is calculated from the item's base price:

```text
roundedFinalPrice = roundHalfUp(baseShopSalePrice * shopSalePricePercent / 100)
```

The calculation uses overflow-safe arithmetic. Half values are rounded up to the nearest integer, and every calculated final price must be at least one and no greater than the supported integer maximum.

For each item referenced by at least one sale offer:

- The lowest rounded final sale price is calculated across every offer for that item.
- `shopPurchasePrice` cannot exceed that lowest final sale price.
- Equality is allowed and produces zero resale profit.
- An item may define `shopPurchasePrice` without appearing in any shop sale offer.

### Weighted offer selection

Weights define relative selection probability, not percentages.

For each shop slot, the probability of selecting one remaining offer is:

```text
offer weight / sum of all remaining offer weights
```

After an offer is selected, that exact entry is removed from the candidate pool. Selection therefore occurs without replacement. Another offer for the same item remains eligible when it is a distinct entry.

Because the selected entry is removed, the conditional probability of every remaining offer is recalculated for the next slot. An offer with a low weight may therefore become more likely in later slots as higher-weight entries leave the pool.

Multiplying every weight in one shop by the same positive factor does not change their relative probabilities.

When `offerSlots` is equal to or greater than the number of possible offers, every offer is selected and weights have no practical effect.

Accumulated selection weights use overflow-safe arithmetic.

### Runtime stock behavior

Every shop receives one `ShopState` during application initialization:

```text
ShopCatalog + ShopStockCatalog
    -> ShopStateGenerator
    -> ShopStateCatalog
    -> World
    -> ShopMenu
```

Runtime rules:

- Active offers are generated once per program execution.
- Each selected offer receives its configured initial stock.
- Selected offers and remaining quantities are preserved throughout the execution.
- An exhausted offer disappears from the visible purchase list.
- Exhausting an offer does not activate an unused entry from `shop_stock.txt`.
- A shop remains accessible when every active offer is exhausted.
- Shops do not restock during the current execution.
- Starting a new program execution generates fresh shop states.
- Shop-state persistence and restocking rules remain postponed until save and load are implemented.

Loading flow:

```text
shop_stock.txt
    -> ShopStockLoader
    -> ShopStockEntry
    -> ShopStockCatalog
```

## Skill definitions

General skill data is loaded from:

`data/skills.txt`

Header:

```text
# id;classId;acquisitionLevel;mpCost;spCost;name;description
```

Each valid line contains exactly 7 fields:

| Position | Field | Java type | Example |
|---:|---|---|---|
| 1 | Skill ID | `String` | `quebra_armadura` |
| 2 | Playable-class ID | `String` | `flagelo` |
| 3 | Acquisition level | `int` | `3` |
| 4 | MP cost | `int` | `0` |
| 5 | SP cost | `int` | `20` |
| 6 | Name | `String` | `Quebra-Armadura` |
| 7 | Description | `String` | `Ataca e reduz a defesa do alvo.` |

Rules:

- Skill and class IDs must use stable lowercase IDs.
- Acquisition level must be positive.
- MP and SP costs cannot be negative.
- Name and description cannot be empty.
- Literal `\n` sequences in descriptions are converted into line breaks.
- Every skill must receive at least one component from `skill_effects.txt`.
- Skill IDs must be unique.

Example:

```text
quebra_armadura;flagelo;3;0;20;Quebra-Armadura;Ataca o alvo e reduz sua defesa.
```

## Skill-effect definitions

Ordered skill components are loaded from:

`data/skill_effects.txt`

Header:

```text
# skillId;sequence;phase;effectType;targetType;damageType;power;attribute;stages;duration;triggerChancePercent;specialStateType;conditionType;conditionValue
```

Each valid line contains exactly 14 fields:

| Position | Field | Java type | Empty value |
|---:|---|---|---|
| 1 | Skill ID | `String` | Not allowed |
| 2 | Sequence | `int` | Not allowed |
| 3 | Execution phase | `SkillExecutionPhase` | Not allowed |
| 4 | Effect type | `SkillEffectType` | Not allowed |
| 5 | Target type | `SkillTargetType` | Not allowed |
| 6 | Damage type | `DamageType` | Allowed when inapplicable |
| 7 | Power | `Integer` | Allowed when inapplicable |
| 8 | Attribute | `CharacterAttribute` | Allowed when inapplicable |
| 9 | Stages | `Integer` | Allowed when inapplicable |
| 10 | Duration | `Integer` | Allowed when inapplicable |
| 11 | Trigger chance | `int` | Defaults to `100` |
| 12 | Special state type | `SpecialBattleStateType` | Allowed when inapplicable |
| 13 | Condition type | `SkillConditionType` | Not allowed |
| 14 | Condition value | `Integer` | Allowed when inapplicable |

Execution phases:

- `PRE_ACTION`
- `PRIMARY`
- `POST_ACTION`

Effect types:

- `DAMAGE`
- `RECOIL_DAMAGE`
- `DAMAGE_POWER_MODIFIER`
- `HP_RECOVERY`
- `MP_RECOVERY`
- `SP_RECOVERY`
- `REVIVE`
- `ATTRIBUTE_STAGE`
- `PURIFICATION`
- `DISSIPATION`
- `SPECIAL_STATE`

Target types:

- `SELF`
- `SINGLE_ALLY`
- `ALL_ALLIES`
- `ALL_BUT_SELF`
- `RANDOM_ALLY`
- `SINGLE_ENEMY`
- `ALL_ENEMIES`
- `RANDOM_ENEMY`

Rules:

- Sequences must be positive, unique and declared in ascending order within each skill.
- Phases cannot move backwards from `PRE_ACTION` to `PRIMARY` to `POST_ACTION`.
- Empty `triggerChancePercent` means `100`; explicit values must be between `1` and `100`.
- Trigger chance is configured per component and rolled once for all of that component's applicable targets.
- Damage requires `PHYSICAL` or `MAGICAL` damage type and positive power.
- Recoil damage uses `PRIMARY`, targets `SELF`, leaves `damageType` empty and defines a percentage from `1` to `100` in `power`.
- A `RECOIL_DAMAGE` component must immediately follow a `SINGLE_ENEMY` damage component in the same phase. It uses the actual HP damage dealt by that component and cannot defeat the user.
- HP recovery and revival require positive power and an `ATK` or `MAG` scaling attribute.
- MP and SP recovery use fixed positive power and leave the attribute field empty.
- Attribute-stage components require an attribute and stages from `-3` to `3`, excluding zero.
- Non-VEL stages require a positive duration; VEL stages leave duration empty.
- `DISTORTED_ATTRIBUTES` uses `SPECIAL_STATE` and currently targets `SINGLE_ENEMY`.
- `DAMAGE_POWER_MODIFIER` must precede a compatible damage component with the same target type.
- Condition type `NONE` leaves `conditionValue` empty.
- HP-percentage conditions require a value between `1` and `100`.

Examples:

```text
investida;1;PRIMARY;DAMAGE;SINGLE_ENEMY;PHYSICAL;5;;;;;;NONE;
investida;2;PRIMARY;RECOIL_DAMAGE;SELF;;50;;;;25;;NONE;
quebra_armadura;1;PRIMARY;DAMAGE;SINGLE_ENEMY;PHYSICAL;10;;;;;;NONE;
quebra_armadura;2;PRIMARY;ATTRIBUTE_STAGE;SINGLE_ENEMY;;;DEF;-1;2;;;NONE;
perseveranca;2;POST_ACTION;HP_RECOVERY;SELF;;20;ATK;;;;;NONE;
abraco_da_floresta;1;PRIMARY;HP_RECOVERY;SINGLE_ALLY;;30;MAG;;;;;NONE;
distorcao_ancestral;1;PRIMARY;SPECIAL_STATE;SINGLE_ENEMY;;;;;;;DISTORTED_ATTRIBUTES;NONE;
```

Loading flow:

```text
skills.txt + skill_effects.txt
    -> SkillLoader
    -> SkillDefinition
    -> ordered SkillEffectDefinition components
    -> SkillCatalog
```
