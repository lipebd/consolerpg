# RPGConsole — Data Formats

This document describes the external text files used to load game data.

## General rules

- Files use UTF-8 encoding.
- Fields are separated by semicolons (`;`).
- Semicolons are reserved separators and cannot appear inside field text.
- Where explicitly supported, the literal sequence `\n` creates an intentional line break.
- A backslash not immediately followed by `n` is invalid in V6 external narrative text.
- Placeholders are supported only in the fields explicitly documented for them.
- Empty lines are ignored.
- Lines beginning with `#` are comments.
- Stable IDs use lowercase letters and underscores.
- Enum values use their Java constant names.
- Display texts may use Brazilian Portuguese characters and accents.
- Java identifiers and technical comments are written in English.
- Texts displayed to the player are written in Brazilian Portuguese.
- Content IDs are written in Brazilian Portuguese without accents.
- Invalid references or malformed lines stop game initialization instead of creating partially invalid data.

## Campaign save container

`save.txt` is generated campaign data rather than an external game-definition file. The complete persistence contract is documented in `SAVE_STATE.md`.

The implemented outer container is UTF-8 text containing exactly:

```properties
container=RPGCONSOLE_SAVE
containerVersion=1
iv=<Base64>
ciphertext=<Base64>
```

The IV and authenticated ciphertext use canonical basic Base64 encoding. No campaign property is exposed outside the ciphertext.

After authenticated AES-GCM decryption, the current internal version-3 campaign payload uses strict `name=value` properties encoded as UTF-8. In addition to the existing party, world, dungeon, clock and shop structures, version 3 contains:

```properties
quest.active.count=<non-negative integer>
quest.active.<index>.id=<quest ID>
quest.active.<index>.currentStep=<positive integer>
quest.active.<index>.objective.count=<non-negative integer>
quest.active.<index>.objective.<objectiveIndex>.id=<objective ID>
quest.active.<index>.objective.<objectiveIndex>.currentQuantity=<non-negative integer>

quest.completed.count=<non-negative integer>
quest.completed.<index>.id=<quest ID>

flag.count=<non-negative integer>
flag.<index>.id=<flag ID>

recruitment.candidate.count=<non-negative integer>
recruitment.candidate.<index>.id=<candidate ID>
recruitment.candidate.<index>.name=<persistent character name>
recruitment.candidate.<index>.recruited=<true or false>
```

`save.formatVersion=3` is the only supported internal version. Version 2 is rejected without migration, while the encrypted outer container remains at `containerVersion=1`. Property names, indexed collection structures, cross-state invariants and complete validation rules are defined centrally in `SAVE_STATE.md`.

The encrypted codec, atomic file services, catalog-backed runtime reconstruction and gameplay integration are implemented.

## Character-name definitions

Names available for random playable-character naming are loaded from:

`data/character_names.txt`

Header:

```text
# name
```

Each non-comment line contains one complete character name:

```text
Ari
Lina
Nilo
Yara
```

Rules:

- The entire active line represents one name.
- Names are normalized to Unicode NFC.
- Outer ordinary spaces are removed.
- Repeated ordinary spaces are collapsed into one.
- A normalized name may contain at most 16 Unicode code points.
- Unicode letters are allowed.
- Single spaces, hyphens and apostrophes may appear only between letters.
- Blank names, numbers, controls, tabs, unrelated punctuation and malformed separators are rejected.
- Names must be unique without regard to letter case after normalization.
- Accents remain significant, so `Jose` and `José` are different names.
- At least four valid unique names are required to support a complete party.
- The current four entries are temporary placeholders pending creative-team curation.

Loading flow:

```text
character_names.txt
    -> CharacterNameLoader
    -> CharacterNameCatalog
    -> CharacterNameSelector
    -> CharacterCreationMenu
```

`CharacterNameRules` is shared by external loading and runtime `PlayableCharacter` construction. `CharacterNameSelector` chooses uniformly after excluding names supplied by the caller.

## Playable-class definitions

Playable-class data is loaded from:

`data/playable_classes.txt`

Header:

```text
# id;name;symbol;baseHp;baseMp;baseSp;baseAttack;baseDefense;baseMagic;baseResistance;baseSpeed;baseLuck;primaryAttribute;secondaryAttribute;armorWeight;description
```

Each valid line contains exactly 16 fields:

| Position | Field | Java type | Example |
|---:|---|---|---|
| 1 | Class ID | `String` | `flagelo` |
| 2 | Display name | `String` | `Flagelo` |
| 3 | Presentation symbol | `String` | `◆` |
| 4 | Base maximum HP | `int` | `120` |
| 5 | Base maximum MP | `int` | `25` |
| 6 | Base maximum SP | `int` | `115` |
| 7 | Base ATK | `int` | `15` |
| 8 | Base DEF | `int` | `15` |
| 9 | Base MAG | `int` | `8` |
| 10 | Base RES | `int` | `15` |
| 11 | Base VEL | `int` | `9` |
| 12 | Base LCK | `int` | `8` |
| 13 | Primary attribute | `CharacterAttribute` | `ATK` |
| 14 | Secondary attribute | `CharacterAttribute` | `DEF` |
| 15 | Compatible armor weight | `ArmorWeight` | `HEAVY` |
| 16 | Description | `String` | `Descrição da classe.` |

Example shape:

```text
classe_exemplo;Classe Exemplo;◆;120;25;115;15;15;8;15;9;8;ATK;DEF;HEAVY;Descrição da classe.
```

Rules:

- Class IDs must be unique and stable.
- IDs use lowercase letters, numbers and single underscores.
- Display names cannot be empty.
- `symbol` must contain exactly one Unicode code point.
- Blank symbols and ISO control characters are rejected.
- Symbols are presentation data and do not affect class mechanics.
- Base HP must be at least `50`.
- Base MP and SP must each be at least `25`.
- Base HP, MP and SP must total exactly `260`.
- ATK, DEF, MAG, RES, VEL and LCK must each be between `8` and `15`.
- The six regular base attributes must total exactly `70`.
- `primaryAttribute` and `secondaryAttribute` must be valid `CharacterAttribute` values.
- Primary and secondary attributes must be different.
- `armorWeight` must be `LIGHT`, `MEDIUM` or `HEAVY`.
- Descriptions cannot be empty.
- Literal `\n` sequences in descriptions are converted into line breaks.
- Player-facing descriptions of `ATTRIBUTE_STAGE` effects use one `↑` or `↓` per stage for each affected attribute, such as `ATK↑↑` or `DEF↓`. This is an editorial convention for the description field, not a loader validation rule.
- Descriptions keep effect conditions and durations explicit. VEL stage descriptions identify that the change is pending for the next round.
- Invalid definitions report the source file, line and relevant class ID during initialization.

Loading flow:

```text
playable_classes.txt
    -> PlayableClassLoader
    -> PlayableClassDefinition
    -> PlayableClassCatalog
```

`PlayableClassDefinition` stores the immutable class data. `CharacterGrowthCalculator` uses its base stats and primary and secondary attributes, while console interfaces use its name, symbol and description.

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
- `playerTradable` marks whether the item is eligible for the future player-to-player trading system; it does not implement trading behavior by itself.
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
# itemId;targetType;hpRecovery;mpRecovery;spRecovery;reviveHpRecovery;purifiesStatusEffects;attribute;stages;duration
```

Examples:

```text
meruma;SINGLE_ALLY;25;0;0;0;false;;;
gole_de_vida;SINGLE_ALLY;100%;0;0;0;false;;;
marola;SINGLE_ALLY;0;0;0;0;false;ATK;1;1
pocao_da_celeridade;SINGLE_ALLY;0;0;0;0;false;VEL;3;
pocao_da_normalizacao_sorrateira;SINGLE_ALLY;0;0;0;0;VEL,LCK;;;
```

Rules:

- `itemId` must reference an item defined in `items.txt`.
- The referenced base item must use the `CONSUMABLE` category.
- The same item cannot have more than one recovery definition.
- The HP, MP, SP and revival fields accept a non-negative fixed integer or a percentage from `1%` to `100%`. A fixed `0` means that component is absent.
- Percentages use the target's corresponding maximum resource; revival percentages use maximum HP. Fractional results are rounded up.
- Recovery is capped by the resource missing from the target; `100%` therefore fills it completely.
- A recovery item must have at least one effective component.
- An effective component is positive HP, MP, SP or revival recovery; a nonempty purification set; or a valid positive attribute-stage effect.
- `purifiesStatusEffects` accepts `false` for none, `true` for all six attributes, or a comma-separated list such as `ATK,MAG`. Only listed negative stages are removed; positive stages remain untouched. Duplicate or unknown attributes are invalid.
- `attribute`, `stages` and `duration` are all empty when no stage effect is defined.
- A stage effect targets `SINGLE_ALLY`, defines one of the six attributes and uses `stages` from `1` to `3`. Non-VEL effects require a positive `duration`; VEL requires an empty `duration` because its stages are pending for the next turn-order calculation.
- Attribute-stage items can be used only during battle. Their stage effect is applied after the acting character finishes the consumed turn. Recovery and purification components take effect during item use.
- `targetType` must be `SINGLE_ALLY` or `ALL_ALLIES`.
- Revival is applied before ordinary HP recovery, allowing both components to affect a defeated ally.
- Group effects are evaluated independently for every eligible party member.
- The item is consumed only when at least one component affects at least one target.

Definition composition and runtime flow:

```text
ItemDefinitionCatalog
RecoveryItemDefinitionCatalog
SkillScrollDefinitionCatalog
SkillCatalog
    -> ItemFactory
    -> ItemCatalog
    -> Inventory, drops and item menus
```

`ItemDefinition` contains the immutable properties shared by every item.

`RecoveryItemDefinition` contains only the specialized recovery behavior.

`ItemFactory` receives the base definition and the available specialized catalogs. A matching recovery definition creates `RecoveryItem`; a matching skill-scroll definition creates `SkillScroll`; a matching reward-modifier definition in `reward_modifier_items.txt` creates `RewardModifierItem`; items without specialized runtime behavior become `BasicItem`. The same item cannot receive more then one specialization, between recovery, skill-scroll and reward-modifier.

`ItemCatalog` stores the resulting runtime items and resolves the stable IDs referenced by enemy drops, exploration events and inventories.

## Reward modifier item definitions

Temporary party reward items are defined in `data/reward_modifier_items.txt`:

```text
# itemId;experiencePercent;goldPercent;rewardedVictories
pocao_da_ascensao;50;0;15
pocao_da_prosperidade;0;50;15
pocao_do_sucesso;25;25;15
```

- `itemId` must reference a `CONSUMABLE` item in `items.txt`, and IDs cannot be repeated in this file.
- EXP and GOLD percentages are non-negative integers; at least one must be positive. `0` means the item does not grant that modifier.
- `rewardedVictories` must be positive. Each active modifier loses one remaining victory only when a battle grants rewards.
- These definitions create `RewardModifierItem` objects. They do not use recovery targets or fields in `recovery_items.txt`.

## Skill-scroll definitions

Skill-scroll specialization is loaded from:

`data/skill_scrolls.txt`

Header:

```text
# itemId;skillId;classId
```

Each valid line contains exactly 3 fields:

| Position | Field | Java type | Empty value |
|---:|---|---|---|
| 1 | Item ID | `String` | Not allowed |
| 2 | Skill ID | `String` | Not allowed |
| 3 | Compatible playable-class ID | `String` | Not allowed |

Example shape:

```text
pergaminho_habilidade_exemplo;habilidade_exemplo;druida
```

Rules:

- `itemId` must reference an item defined in `items.txt`.
- The referenced item must use the `SKILL_SCROLL` category.
- `skillId` must reference a skill defined in `skills.txt`.
- The referenced skill must use the `SCROLL` acquisition type.
- `classId` must reference a class defined in `playable_classes.txt`.
- The compatible class must match the linked skill's class.
- One item ID cannot define more than one skill-scroll specialization.
- One skill ID cannot be linked to more than one scroll item.
- Every `SKILL_SCROLL` item must have exactly one specialized row.
- Every `SCROLL` skill must have exactly one linked scroll item.
- The file may contain only its header while no permanent `SKILL_SCROLL` items or `SCROLL` skills are registered.
- The scroll's item definition may omit both NPC commercial prices.
- `playerTradable` remains defined by the linked base item in `items.txt`.

Loading flow:

```text
items.txt
    -> ItemDefinitionCatalog

skills.txt + skill_effects.txt
    -> SkillCatalog

playable_classes.txt
    -> PlayableClassCatalog

skill_scrolls.txt
    -> SkillScrollLoader
    -> SkillScrollDefinitionCatalog

ItemDefinitionCatalog
SkillScrollDefinitionCatalog
SkillCatalog
    -> ItemFactory
    -> SkillScroll
    -> ItemCatalog
```

`SkillScrollDefinition` stores only the stable item, skill and class IDs. `ItemFactory` resolves the canonical `SkillDefinition` from `SkillCatalog` when creating the runtime `SkillScroll`.

The current file contains no permanent definitions. Temporary synthetic rows were used to validate loading and runtime behavior and were removed afterward.

## Equipment definitions

Equipment identity and commercial data remain in `data/items.txt`. Each equipment item must also have exactly one specialized row in `data/weapons.txt`, `data/armors.txt` or `data/trinkets.txt` using the same `itemId`.

All three formats support the same nine optional nonnegative bonuses:

```text
hpBonus;mpBonus;spBonus;attackBonus;defenseBonus;magicBonus;resistanceBonus;speedBonus;luckBonus
```

An empty optional numeric field is interpreted as zero. Equipment level requirements must remain within the supported playable-character range, currently levels 1 through 20. Other required numeric fields must be positive. The loader rejects unknown base IDs, category mismatches, duplicate specializations and malformed enum values during initialization.

Loading flow:

```text
items.txt -> ItemDefinitionCatalog
weapons.txt + armors.txt + trinkets.txt
    -> EquipmentLoader
    -> EquipmentCatalog
```

The specialized definition and the runtime `Item` share one stable ID. Equipment items currently remain `BasicItem` objects in `ItemCatalog`; their equippable behavior is resolved through `EquipmentCatalog`.

### Weapon definitions

File:

`data/weapons.txt`

Header:

```text
# itemId;damageType;weaponType;levelRequirement;normalAttackName;baseWeaponDamage;hpBonus;mpBonus;spBonus;attackBonus;defenseBonus;magicBonus;resistanceBonus;speedBonus;luckBonus
```

Each valid line contains exactly 15 fields:

| Position | Field | Java type | Empty value |
|---:|---|---|---|
| 1 | Item ID | `String` | Not allowed |
| 2 | Damage type | `DamageType` | Not allowed |
| 3 | Weapon type | `WeaponType` | Not allowed |
| 4 | Level requirement | `int` | Not allowed |
| 5 | Normal-attack name | `String` | Not allowed |
| 6 | Base weapon damage | `int` | Not allowed |
| 7–15 | HP, MP, SP, ATK, DEF, MAG, RES, VEL and LCK bonuses | `int` | Defaults to `0` |

Supported damage types:

- `PHYSICAL`
- `MAGICAL`

Supported weapon types:

- `DAGGER`
- `SCEPTER`
- `GAUNTLET`
- `LUTE`
- `SCYTHE`
- `PISTOL`
- `RIFLE`
- `SWORD`
- `BOW`
- `STAFF`
- `GRIMOIRE`

Rules:

- `itemId` must reference an item with category `WEAPON` in `items.txt`.
- Level requirement and base weapon damage must be positive.
- Normal-attack name cannot be empty.
- Every playable class may use every weapon when the level requirement is met.
- Damage type selects the offensive and defensive attributes used by the normal attack; it does not change the damage type declared by a skill component.
- Player-facing screens call `baseWeaponDamage` **Poder**. `DBA` is only an internal design abbreviation.

Example:

```text
adaga_improvisada;PHYSICAL;DAGGER;1;Corte Rápido;1;;;10;;;;;;
```

### Armor definitions

File:

`data/armors.txt`

Header:

```text
# itemId;armorWeight;levelRequirement;hpBonus;mpBonus;spBonus;attackBonus;defenseBonus;magicBonus;resistanceBonus;speedBonus;luckBonus
```

Each valid line contains exactly 12 fields. The final nine positions are the common optional bonuses in their standard order.

Supported armor weights:

- `LIGHT`
- `MEDIUM`
- `HEAVY`

Rules:

- `itemId` must reference an item with category `ARMOR` in `items.txt`.
- Level requirement must remain within the supported playable-character range, currently levels 1 through 20.
- `armorWeight` must match the equipped character's class weight exactly.
- Armor protection is represented by regular stat bonuses; there is no separate base-armor field.

Example shape:

```text
armadura_exemplo;MEDIUM;7;25;;;;5;;3;;
```

### Trinket definitions

File:

`data/trinkets.txt`

Header:

```text
# itemId;trinketType;levelRequirement;hpBonus;mpBonus;spBonus;attackBonus;defenseBonus;magicBonus;resistanceBonus;speedBonus;luckBonus;experienceModifierPercent;goldModifierPercent
```

Each valid line contains exactly 14 fields. Positions 4–12 are the common optional bonuses.

Supported trinket types:

- `JEWELRY`
- `AMULET`
- `TALISMAN`

Rules:

- `itemId` must reference an item with category `TRINKET` in `items.txt`.
- Level requirement must be positive.
- EXP and GOLD modifiers are optional nonnegative integer percentages; empty values become zero.
- A trinket must provide at least one stat or reward bonus.
- Its EXP modifier applies only to the wearer.
- Its GOLD modifier joins the party-wide total while the trinket remains equipped.
- Every class may equip every trinket when the level requirement is met.

Example shape:

```text
talisma_exemplo;TALISMAN;1;;;;;;;;;;5;10
```

### Equipment coverage

The current specialized files contain 67 weapons, 25 armors and 22 trinkets, totaling 114 equipment definitions. Their IDs were cross-checked against `items.txt`: every specialized row has one matching base item, and no item ID is specialized twice.

## Representative item IDs

| ID | Name | Category | Runtime type | Effect |
|---|---|---|---|---|
| `meruma` | Merumã | `CONSUMABLE` | `RecoveryItem` | Recovers 25 HP from one ally |

Equipment entries are intentionally documented through their schemas and the dedicated creation guide instead of duplicating all 114 rows here.

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

Each active combat-scenario line contains exactly 16 fields:

```text
# scenarioId;baseExperienceReward;baseGoldReward;explorationLength;battleChance;rewardChance;merchantChance;epicEncounterChance;specialAreaChance;minEnemies;maxEnemies;normalAreaMessage;specialAreaMessage;preferredRescueScenarioId;fallbackRescueScenarioId;rescueDescription
```

Example:

```text
verdes_ermos;25;5;5;70;15;15;0;30;1;2;Você avança pelos vastos campos verdes.;Você encontrou uma área especial de mini-floresta!;setegalhos;bree;Uma caravana encontrou os feridos e providenciou seu resgate.
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
- `preferredRescueScenarioId` is optional and must be represented by an empty field when absent.
- `fallbackRescueScenarioId` is required and cannot be empty.
- Every configured rescue scenario ID must reference an existing `CITY` with a registered INN.
- Preferred and fallback rescue scenario IDs must be different when a preferred destination is present.
- `rescueDescription` is required and cannot be empty.
- Rescue descriptions support literal `\n`.
- Rescue descriptions must remain appropriate for any party size.
- `CombatScenarioLoader` reports invalid definitions with their source file, line number and scenario ID whenever available.

## Dungeon definitions

File:

`data/dungeons.txt`

Each active dungeon line contains exactly 8 fields:

```text
# scenarioId;bossEnemyIds;recurringBossEnemyIds;recurringBossCooldownHours;epicEnemyIds;epicMinPosition;epicAreaRestriction;epicEnemyCooldownHours
```

Example:

```text
gruta_relamparrocha;mestre_eletrogoblin;mestre_eletrogoblin;4;relampagolem;1;ANY;6
```

| Position | Field | Type | Example |
|---|---|---|---|
| 1 | Dungeon scenario ID | Stable ID | `gruta_relamparrocha` |
| 2 | Narrative boss enemy IDs | Comma-separated ID group | `mestre_eletrogoblin` |
| 3 | Recurring boss enemy IDs | Comma-separated ID group | `mestre_eletrogoblin` |
| 4 | Recurring boss cooldown | Positive integer world hours | `4` |
| 5 | Epic enemy IDs | Comma-separated ID group | `relampagolem` |
| 6 | Epic minimum position | Positive integer | `1` |
| 7 | Epic area restriction | `ExplorationAreaRestriction` | `ANY` |
| 8 | Epic cooldown | Positive integer world hours | `6` |

Rules:

- `scenarioId` must reference an existing `DUNGEON`.
- The dungeon must also have combat-scenario data.
- Every dungeon must define one narrative boss group, one recurring boss group and one epic group.
- Each enemy group contains one through three comma-separated stable enemy IDs.
- Narrative and recurring boss entries must reference enemies with the `BOSS` rank.
- Epic entries must reference enemies with the `EPIC` rank.
- Narrative and recurring boss groups may reference the same enemy IDs.
- An epic enemy cannot also belong to either boss group.
- Empty IDs and duplicates inside the same group are invalid.
- `recurringBossCooldownHours` and `epicEnemyCooldownHours` must be positive.
- `epicMinPosition` must be at least `1` and lower than the dungeon's final exploration position.
- `epicAreaRestriction` must be `NORMAL_ONLY`, `SPECIAL_ONLY` or `ANY`.
- A `SPECIAL_ONLY` epic requires a positive special-area chance and at least one intermediate exploration position.
- Every dungeon must define a positive `epicEncounterChance` in `combat_scenarios.txt`.
- Each dungeon can have only one dungeon definition.

The narrative boss controls the dungeon's first permanent completion. Later explorations use the recurring boss group after its cooldown. Epic encounters remain random and optional for the player, independently from the mandatory final boss.

## Travel route definitions

File:

`data/travel_routes.txt`

Each active travel-route line contains exactly 7 fields:

```text
# id;sourceScenarioId;destinationScenarioId;initiallyUnlocked;displayName;description;destinationExplorationPosition
```

Examples:

```text
bree_portao_sul;bree;verdes_ermos;false;Portão Sul;A estrada ao sul conduz aos Verdes Ermos.;1
verdes_ermos_bree;verdes_ermos;bree;false;Retornar a Bree;Você segue pela estrada de volta a Bree.;
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

Each active branch line contains exactly 7 fields:

```text
# id;sourceScenarioId;position;destinationScenarioId;initiallyUnlocked;displayName;description
```

Example:

```text
verdes_ermos_bifurcacao_setegalhos;verdes_ermos;3;setegalhos;false;Seguir para Setegalhos;Você encontra uma bifurcação entre os campos.\nUma estrada parcialmente escondida pela vegetação conduz ao Povoado de Setegalhos.
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
bree_inn;bree;Pensão da Dona Fábula;Uma estalagem simples e acolhedora.\nSeus quartos oferecem o descanso necessário antes da próxima jornada.;25
```

Rules:

- `id` is the stable INN ID.
- `scenarioId` must reference an existing `CITY`.
- IDs, names and descriptions cannot be empty.
- INN IDs must be unique.
- Descriptions support literal `\n`.
- `price` must be a valid integer greater than zero.
- Every registered city must have exactly one INN.
- `InnCatalog` rejects multiple INNs for the same city.
- `InnLoader` rejects game initialization when any registered city has no INN.
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

Every shop receives an initial `ShopState` when the campaign runtime is created:

```text
ShopCatalog + ShopStockCatalog
    -> ShopStateGenerator
    -> ShopStateCatalog
    -> World
    -> ShopMenu
```

`World` retains the same `ShopStateGenerator` for later global restocking.

Runtime rules:

- Initial active offers are generated when the campaign runtime is created.
- Each selected offer receives its configured initial stock.
- Selected offers and remaining quantities are preserved throughout the current restocking cycle.
- An exhausted offer disappears from the visible purchase list.
- Exhausting an offer does not activate an unused entry from `shop_stock.txt` during the same cycle.
- A shop remains accessible when every active offer is exhausted.
- Whenever world time crosses midnight, every registered shop receives a complete new weighted offer selection.
- Newly selected offers begin with their configured initial stock.
- Old offers and remaining quantities are not merged into the new cycle.
- A complete replacement `ShopStateCatalog` is generated before world time or active shop state changes.
- Failed replacement generation preserves the previous world time and shop states.
- A new campaign generates every shop state. Loading a campaign preserves surviving saved offers and generates initial offers only for shops added since the save.
- Loading does not advance fictional time or restock existing shops. Offers removed from current definitions are discarded, remaining stock is capped at the current initial stock, and an existing shop may load with no active offers.

Loading flow:

```text
shop_stock.txt
    -> ShopStockLoader
    -> ShopStockEntry
    -> ShopStockCatalog
```

## Crafting recipe definitions

Crafting recipes are loaded from:

`data/crafting_recipes.txt`

Header:

```text
# id;outputItemId;name;description;materialItemId;requiredQuantity;...
```

Each valid line contains four fixed fields followed by one or more item-ID and quantity pairs:

| Position | Field                                        | Java type       | Empty value |
| -------: | -------------------------------------------- | --------------- | ----------- |
|        1 | Recipe ID                                    | `String`        | Not allowed |
|        2 | Output item ID                               | `String`        | Not allowed |
|        3 | Name                                         | `String`        | Not allowed |
|        4 | Description                                  | `String`        | Not allowed |
| 5 onward | Material item ID and required quantity pairs | `String`, `int` | Not allowed |

Rules:

* Recipe IDs follow the project stable-ID convention.
* Recipe IDs must be unique.
* Every output item ID must reference an existing item definition.
* Every material item ID must reference an existing item definition.
* Every recipe requires at least one material.
* Material fields must occur in complete item-ID and quantity pairs.
* Required quantities must be positive integers.
* The same material item ID cannot occur more than once in one recipe.
* Item categories do not restrict crafting participation.
* Items from any category may be consumed or produced.
* Every successful operation produces exactly one unit of the configured output item.
* Recipe names and descriptions belong to the crafting process and remain separate from the output item's name and description.
* Literal `\n` inside descriptions becomes a line break when loaded.
* Recipe and material order follows declaration order.

Examples:

```text
pocao_de_meruma;pocao_de_meruma;Preparar Poção de Merumã;Quatro Merumãs são maceradas e combinadas em uma poção restauradora.;meruma;4
espada_de_ferro;espada_de_ferro;Forjar Espada de Ferro;Um cabo de madeira é combinado a uma lâmina moldada em ferro.;madeira;3;ferro;5
```

Loading flow:

```text
crafting_recipes.txt
    -> CraftingRecipeLoader
    -> CraftingRecipeDefinition
        -> ordered CraftingMaterial requirements
    -> CraftingRecipeCatalog
```

## Crafter definitions

Crafter NPCs are loaded from:

`data/crafters.txt`

Header:

```text
# id;scenarioId;name;description;recipeId;...
```

Each valid line contains four fixed fields followed by one or more recipe IDs:

| Position | Field       | Java type | Empty value |
| -------: | ----------- | --------- | ----------- |
|        1 | Crafter ID  | `String`  | Not allowed |
|        2 | Scenario ID | `String`  | Not allowed |
|        3 | Name        | `String`  | Not allowed |
|        4 | Description | `String`  | Not allowed |
| 5 onward | Recipe IDs  | `String`  | Not allowed |

Rules:

* Crafter IDs follow the project stable-ID convention.
* Crafter IDs must be unique.
* The scenario ID must reference an existing `CITY` scenario.
* More than one crafter may reference the same city.
* Every crafter requires a non-empty name and description.
* Every crafter provides at least one recipe.
* Every recipe ID must reference an existing crafting recipe.
* A crafter cannot repeat the same recipe ID.
* Recipe IDs preserve their declaration order.
* Different crafters in the same city may expose independent recipe sets.
* Literal `\n` inside descriptions becomes a line break when loaded.
* Crafter is the generic system concept; individual NPCs may represent blacksmiths, alchemists, cooks or other crafting professions.

Examples:

```text
ferreiro_bree;bree;Senhor das Armas;Um sábio ferreiro com conhecimento dos metais de toda a região.;espada_de_ferro
alquimista_bree;bree;Alquimista de Bree;Uma estudiosa das propriedades restauradoras de frutas e ervas.;pocao_de_meruma
```

Loading flow:

```text
crafters.txt
    -> CrafterLoader
    -> CrafterDefinition
    -> CrafterCatalog
        -> lookup by ID
        -> ordered lookup by city scenario ID
```

## Skill definitions

General skill data is loaded from:

`data/skills.txt`

Header:

```text
# id;classId;acquisitionType;levelRequirement;mpCost;spCost;name;description
```

Each valid line contains exactly 8 fields:

| Position | Field | Java type | Example |
|---:|---|---|---|
| 1 | Skill ID | `String` | `investida` |
| 2 | Playable-class ID | `String` | `flagelo` |
| 3 | Acquisition type | `SkillAcquisitionType` | `STARTER` |
| 4 | Level requirement | `int` | `1` |
| 5 | MP cost | `int` | `0` |
| 6 | SP cost | `int` | `10` |
| 7 | Name | `String` | `Investida` |
| 8 | Description | `String` | `Ataca o alvo e pode causar recuo.` |

Rules:

- Skill and class IDs must use stable lowercase IDs.
- `classId` must reference a valid playable class.
- `acquisitionType` must be `STARTER`, `LEVEL` or `SCROLL`.
- Level requirement must be positive.
- For `STARTER` and `LEVEL`, the requirement is the level at which the skill is learned automatically.
- For `SCROLL`, the requirement is the minimum level for using the linked scroll; reaching it does not grant the skill.
- MP and SP costs cannot be negative.
- Name and description cannot be empty.
- Literal `\n` sequences in descriptions are converted into line breaks.
- Every skill must receive at least one component from `skill_effects.txt`.
- Skill IDs must be unique.
- Every `SCROLL` skill must be linked exactly once in `skill_scrolls.txt`.

Example:

```text
investida;flagelo;STARTER;1;0;10;Investida;Ataca o alvo e pode causar recuo.
```

Current acquisition coverage:

- `STARTER`: 2 temporary Flagelo skills.
- `LEVEL`: 28 permanent class-progression skills.
- `SCROLL`: no permanent definitions yet.

Loading flow:

```text
skills.txt + skill_effects.txt
    -> SkillLoader
    -> SkillDefinition
    -> ordered SkillEffectDefinition components
    -> SkillCatalog
```

`SkillLoader` creates every canonical `SkillDefinition` regardless of acquisition type. Automatic learning belongs to `PlayableCharacter`; `SCROLL` linkage and specialization completeness belong to `SkillScrollLoader`.

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
| 14 | Condition value | `String` (percentage or attribute name) | Allowed when inapplicable |

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
- `HP_SACRIFICE`
- `HP_BALANCE`

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

- `DAMAGE` requires a damage type and positive power. Its optional `attribute` selects any effective offensive attribute; an empty value defaults to ATK for physical damage or MAG for magical damage. Damage type still selects the target's DEF or RES.
- Sequences must be positive, unique and declared in ascending order within each skill.
- Phases cannot move backwards from `POST_ACTION` to `PRIMARY` to `PRE_ACTION`.
- Empty `triggerChancePercent` means `100`; explicit values must be between `1` and `100`.
- Trigger chance is configured per component and rolled once for all of that component's applicable targets.
- Recoil damage uses `PRIMARY`, targets `SELF`, leaves `damageType` empty and defines a percentage from `1` to `100` in `power`.
- A `RECOIL_DAMAGE` component must immediately follow a `SINGLE_ENEMY` damage component in the same phase. It uses the actual HP damage dealt by that component and cannot defeat the user.
- HP recovery and revival require positive power and an `ATK` or `MAG` scaling attribute.
- HP-percentage conditions require a value between `1` and `100`.
- MP and SP recovery use fixed positive power and leave the attribute field empty.
- Attribute-stage components require an attribute and stages from `-3` to `3`, excluding zero.
- Non-VEL stages require a positive duration; VEL stages leave duration empty.
- `DISTORTED_ATTRIBUTES` uses `SPECIAL_STATE` and currently targets `SINGLE_ENEMY`.
- `HP_SACRIFICE` uses `PRIMARY`, targets `SELF` and sets `power` to a percentage from `1` to `100` of current HP. The sacrificed amount is rounded down and cannot reduce the user below 1 HP. At 1 HP, the component remains applicable and sacrifices 0 HP.
- `HP_BALANCE` uses `PRIMARY`, targets `ALL_ALLIES`, leaves effect data empty and requires condition `NONE`. It averages current HP among living allies, rounds to the nearest integer and sets each living ally to the smaller of that average and their own maximum HP. Defeated allies are excluded.
- `PURIFICATION` and `DISSIPATION` leave `power` empty to clear all eligible attribute states, or define a positive limit to clear up to that many randomly chosen attributes without repetition. All stages of each selected attribute are cleared.
- A `PRE_ACTION` `DAMAGE_POWER_MODIFIER` applies to later compatible damage against the same target during that skill use.
- A `DAMAGE_POWER_MODIFIER` in `PRE_ACTION` must precede compatible damage with the same target type. Its bonus remains available against the same target for later damage components during that skill use.
- A `DAMAGE_POWER_MODIFIER` in `PRIMARY` targets `SINGLE_ENEMY` or `RANDOM_ENEMY` and must immediately precede a `PRIMARY;DAMAGE` component with the same target type. Both components use the same enemy. If the modifier's condition matches, its bonus applies only to that following damage.
- Each `RANDOM_ENEMY` or `RANDOM_ALLY` component draws independently when it executes. Enemy targets must still be alive; `REVIVE;RANDOM_ALLY` draws from defeated allies, while other random-ally effects draw from living allies. Conditions are checked after selection and do not cause another draw. A paired `PRIMARY` modifier and damage component share one target.
- `NONE` leaves `conditionValue` empty. HP conditions use an integer percentage from `1` to `100` with strict `<` or `>` comparison: `SELF_HP_LESS_THAN_PERCENT`, `SELF_HP_GREATER_THAN_PERCENT`, `TARGET_HP_LESS_THAN_PERCENT` and `TARGET_HP_GREATER_THAN_PERCENT`.
- `SELF_GREATER` and `SELF_LOWER` compare the user's effective attribute with the target's same effective attribute. Their `conditionValue` is one of `ATK`, `DEF`, `MAG`, `RES`, `VEL` or `LCK`; the effect's `attribute` field remains available for its own purpose.

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

## Quest definitions

Complete immutable quest definitions are assembled from three external files.

### Quest headers

Quest identity and availability are loaded from:

`data/quests.txt`

Header:

```text
# id;title;description;startingNpcId;availabilityRequirementSetId
```

Each valid line contains exactly 5 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | Quest ID | `String` | Not allowed |
| 2 | Title | `String` | Not allowed |
| 3 | Description | `String` | Not allowed |
| 4 | Starting NPC ID | `String` | Not allowed |
| 5 | Availability requirement-set ID | `String` | Allowed |

Rules:

- Quest IDs follow the stable-ID convention and must be unique.
- Titles cannot contain placeholders or backslashes.
- Descriptions support literal `\n` and the documented party placeholders.
- Every starting NPC ID is validated after `NpcCatalog` is available.
- A blank availability requirement-set ID means that the quest offer has no reusable access requirement.
- A non-empty requirement-set ID must reference `RequirementCatalog`.
- Availability dependencies cannot refer to the same quest or form direct or indirect cycles.
- Every quest must define at least one sequential step.
- Declaration order is preserved by `QuestCatalog`.

### Quest steps

Sequential quest steps are loaded from:

`data/quest_steps.txt`

Header:

```text
# questId;step;title;description
```

Each valid line contains exactly 4 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | Quest ID | `String` | Not allowed |
| 2 | Step number | `int` | Not allowed |
| 3 | Step title | `String` | Not allowed |
| 4 | Step description | `String` | Not allowed |

Rules:

- Every quest ID must reference `quests.txt`.
- Step numbers begin at 1 and are unique and consecutive inside each quest.
- Step titles cannot contain placeholders or backslashes.
- Step descriptions support literal `\n` and the documented party placeholders.
- Every step must define at least one objective.
- Quests are linear; V6 does not define alternative step branches.

### Quest objectives

Ordered objectives are loaded from:

`data/quest_objectives.txt`

Header:

```text
# id;questId;step;order;objectiveType;targetId;requiredQuantity;text
```

Each valid line contains exactly 8 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | Objective ID | `String` | Not allowed |
| 2 | Quest ID | `String` | Not allowed |
| 3 | Step number | `int` | Not allowed |
| 4 | Objective order | `int` | Not allowed |
| 5 | Objective type | `QuestObjectiveType` | Not allowed |
| 6 | Target ID | `String` | Type-dependent |
| 7 | Required quantity | `int` | Not allowed |
| 8 | Player-facing text | `String` | Not allowed |

The initial objective types are exactly:

- `TALK_TO_NPC`
- `DEFEAT_ENEMY`
- `ANALYZE_ENEMY`
- `OBTAIN_ITEM`
- `DELIVER_ITEM`
- `REACH_SCENARIO`
- `COMPLETE_DUNGEON`
- `WIN_BATTLE`

Target and quantity rules:

| Objective type | Target | Required quantity |
| --- | --- | --- |
| `TALK_TO_NPC` | Canonical NPC ID | Exactly `1` |
| `DEFEAT_ENEMY` | Canonical enemy ID | Positive |
| `ANALYZE_ENEMY` | Canonical enemy ID | Exactly `1` |
| `OBTAIN_ITEM` | Canonical item ID | Positive |
| `DELIVER_ITEM` | Canonical item ID | Positive |
| `REACH_SCENARIO` | Canonical scenario ID | Exactly `1` |
| `COMPLETE_DUNGEON` | Canonical dungeon scenario ID | Exactly `1` |
| `WIN_BATTLE` | Blank for any scenario, or a canonical combat-scenario ID | Positive |

- `WIN_BATTLE` with a blank target counts victories in any combat scenario.
- `WIN_BATTLE` with a combat-scenario target counts victories only in that scenario.

Additional rules:

- Objective IDs are globally unique.
- Objective order begins at 1 and is unique and consecutive inside each step.
- One step cannot repeat the same objective type and target.
- One step supports at most one `TALK_TO_NPC` objective.
- A step containing `DELIVER_ITEM` must contain exactly one `TALK_TO_NPC` objective identifying the receiving NPC.
- Objective text supports literal `\n` and the documented party placeholders.
- Target references are validated against their canonical catalogs before gameplay.
- `OBTAIN_ITEM` records confirmed acquisition and does not regress after later consumption.
- `DELIVER_ITEM` describes a transactional NPC interaction rather than an acquisition event.

Loading flow:

```text
quests.txt
quest_steps.txt
quest_objectives.txt
        ↓
QuestLoader
        ↓
private quest and step drafts
        ↓
QuestDefinition
└── ordered QuestStepDefinition
    └── ordered QuestObjectiveDefinition
        ↓
QuestCatalog
```

## Permanent-flag definitions

Permanent flag identities are loaded from:

`data/flags.txt`

Header:

```text
# id;description
```

Each valid line contains exactly 2 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | Flag ID | `String` | Not allowed |
| 2 | Description | `String` | Not allowed |

Rules:

- Flag IDs follow the stable-ID convention and must be unique.
- Descriptions support literal `\n`.
- Definitions describe stable permanent world consequences.
- Every flag begins absent from a new `CampaignFlags` state.
- Flags are set-only and are never removed in V6.
- Quest lifecycle is represented by `QuestProgress` and is not duplicated through flags.

Loading flow:

```text
flags.txt
    -> FlagLoader
    -> FlagDefinition
    -> FlagCatalog
```

## Reusable requirement definitions

Reusable requirement sets are loaded from:

`data/requirements.txt`

Header:

```text
# setId;order;requirementType;targetId;requiredValue
```

Each valid line contains exactly 5 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | Requirement-set ID | `String` | Not allowed |
| 2 | Order | `int` | Not allowed |
| 3 | Requirement type | `RequirementType` | Not allowed |
| 4 | Target ID | `String` | Type-dependent |
| 5 | Required value | `int` | Type-dependent |

The initial requirement types are exactly:

- `LEADER_LEVEL_AT_LEAST`
- `ITEM_QUANTITY_AT_LEAST`
- `QUEST_NOT_STARTED`
- `QUEST_ACTIVE`
- `QUEST_COMPLETED`
- `FLAG_SET`
- `FLAG_UNSET`

Field rules by type:

| Requirement type | Target ID | Required value |
| --- | --- | --- |
| `LEADER_LEVEL_AT_LEAST` | Blank | Positive supported character level |
| `ITEM_QUANTITY_AT_LEAST` | Canonical item ID | Positive quantity |
| `QUEST_NOT_STARTED` | Canonical quest ID | Blank |
| `QUEST_ACTIVE` | Canonical quest ID | Blank |
| `QUEST_COMPLETED` | Canonical quest ID | Blank |
| `FLAG_SET` | Canonical flag ID | Blank |
| `FLAG_UNSET` | Canonical flag ID | Blank |

Additional rules:

- Every row belonging to one set is combined through logical AND.
- Order begins at 1 and is unique and consecutive inside each set.
- Requirement-set declaration order follows the first appearance of each set ID.
- Duplicate or contradictory requirements inside one set are rejected.
- References are validated against item, quest and flag catalogs.
- A blank requirement-set reference in a consuming definition means unrestricted access; it is not represented by an empty set row.
- Sets cannot reference other sets.
- V6 does not support OR groups, nested expressions or scripts.
- Item requirements inspect current ownership without reserving or consuming items.
- Requirement evaluation is pure, uncached and repeated immediately before protected actions.
- Evaluation results are derived and are never persisted.

Loading flow:

```text
requirements.txt
    -> RequirementLoader
    -> ordered RequirementDefinition objects
    -> RequirementSetDefinition
    -> RequirementCatalog
```

## Quest reward definitions

Material quest rewards are loaded from:

`data/quest_rewards.txt`

Header:

```text
# questId;order;rewardType;rewardId;quantity
```

Each valid line contains exactly 5 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | Quest ID | `String` | Not allowed |
| 2 | Reward order | `int` | Not allowed |
| 3 | Reward type | `QuestRewardType` | Not allowed |
| 4 | Reward ID | `String` | Type-dependent |
| 5 | Quantity | `int` | Not allowed |

The initial reward types are exactly:

- `GOLD`
- `PARTY_EXP`
- `ITEM`

Field rules by type:

| Reward type | Reward ID | Quantity |
| --- | --- | --- |
| `GOLD` | Blank | Positive and at most the supported party GOLD maximum |
| `PARTY_EXP` | Blank | Positive `int` |
| `ITEM` | Canonical item ID | Positive and at most the per-item inventory maximum |

Additional rules:

- Every quest ID must reference `QuestCatalog`.
- Reward order begins at 1 and is unique and consecutive inside each quest.
- Duplicate `(rewardType, rewardId)` entries for one quest are rejected.
- A quest may define no material reward.
- `PARTY_EXP` is awarded to every party member present when the quest completes.
- Player-facing reward summaries are derived from these definitions.
- Reward text is not duplicated in `quests.txt`.
- Receiving capacity is validated before any reward is granted.

Loading flow:

```text
quest_rewards.txt
    -> QuestRewardLoader
    -> QuestRewardDefinition
    -> QuestRewardCatalog
```

## Quest-effect definitions

Quest effects are loaded from:

`data/quest_effects.txt`

Header:

```text
# questId;step;trigger;order;effectType;targetId;quantity;displayText
```

Each valid line contains exactly 8 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | Quest ID | `String` | Not allowed |
| 2 | Step number | `int` | Not allowed |
| 3 | Trigger | `QuestEffectTrigger` | Not allowed |
| 4 | Effect order | `int` | Not allowed |
| 5 | Effect type | `QuestEffectType` | Not allowed |
| 6 | Target ID | `String` | Not allowed |
| 7 | Quantity | `int` | Required only for `GRANT_ITEM`; blank otherwise |
| 8 | Player-facing display text | `String` | Allowed |

Triggers:

- `QUEST_START`
- `STEP_COMPLETE`
- `QUEST_COMPLETE`

Effect types:

- `SET_FLAG`
- `UNLOCK_ROUTE`
- `GRANT_ITEM`
- `TRANSPORT_PARTY`

Rules:

- Every quest ID must reference `QuestCatalog`.
- `QUEST_START` and `QUEST_COMPLETE` use step `0`.
- `STEP_COMPLETE` uses a positive step belonging to the referenced quest.
- Effect order begins at 1 and is unique and consecutive for each quest, step and trigger location.
- `SET_FLAG` targets a canonical flag ID.
- `UNLOCK_ROUTE` targets a canonical travel-route or scenario-branch ID. Routes are unlocked by quest effects, not automatically by completing a combat scenario.
- `GRANT_ITEM` targets a canonical item ID and requires a positive quantity.
- `TRANSPORT_PARTY` targets a canonical scenario ID.
- `GRANT_ITEM` and `TRANSPORT_PARTY` cannot use `QUEST_START`.
- Other effect types leave `quantity` blank.
- Display text is optional. A blank display text does not suppress the effect.
- Step effects belong to their step transition.
- Quest-completion effects belong to quest completion.
- Class change and ally recruitment are not represented as generic effects.

Loading flow:

```text
quest_effects.txt
    -> QuestEffectLoader
    -> QuestEffectDefinition
    -> QuestEffectCatalog
```

## Narrative-location definitions

Narrative city spaces are loaded from:

`data/narrative_locations.txt`

Header:

```text
# id;cityId;name;description;requirementSetId
```

Each valid line contains exactly 5 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | Narrative-location ID | `String` | Not allowed |
| 2 | City scenario ID | `String` | Not allowed |
| 3 | Name | `String` | Not allowed |
| 4 | Description | `String` | Not allowed |
| 5 | Visibility requirement-set ID | `String` | Allowed |

Rules:

- Narrative-location IDs follow the stable-ID convention and must be unique.
- The city ID must reference an existing `CITY` scenario.
- More than one narrative location may belong to the same city.
- Names cannot contain placeholders or backslashes.
- Descriptions support literal `\n`.
- A blank requirement-set ID makes the location visible without a reusable requirement.
- A non-empty requirement-set ID must reference `RequirementCatalog`.
- A visible narrative location may contain zero visible NPCs.
- Narrative locations do not replace or duplicate INNs, shops or crafters.
- Declaration order is preserved for city-menu presentation.

Loading flow:

```text
narrative_locations.txt
    -> NarrativeLocationLoader
    -> NarrativeLocationDefinition
    -> NarrativeLocationCatalog
        -> lookup by ID
        -> ordered lookup by city scenario ID
```

## NPC definitions

Canonical narrative NPC identity is loaded from:

`data/npcs.txt`

Header:

```text
# id;name;description;defaultDialogueId
```

Each valid line contains exactly 4 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | NPC ID | `String` | Not allowed |
| 2 | Name | `String` | Not allowed |
| 3 | Description | `String` | Not allowed |
| 4 | Default dialogue ID | `String` | Not allowed |

Rules:

- NPC IDs follow the stable-ID convention and must be unique.
- Names cannot contain placeholders or backslashes.
- Descriptions support literal `\n`.
- Every NPC requires a default dialogue.
- Default dialogue references are validated after `DialogueCatalog` is available.
- NPC identity is independent from placement.
- An NPC may remain unplaced and may still speak in a dialogue.
- A non-hostile named character speaking outside a narrative location still uses the canonical NPC identity.

Loading flow:

```text
npcs.txt
    -> NpcLoader
    -> NpcDefinition
    -> NpcCatalog
```

## NPC placement definitions

Fixed V6 city placement is loaded from:

`data/npc_placements.txt`

Header:

```text
# npcId;narrativeLocationId;requirementSetId
```

Each valid line contains exactly 3 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | NPC ID | `String` | Not allowed |
| 2 | Narrative-location ID | `String` | Not allowed |
| 3 | Visibility requirement-set ID | `String` | Allowed |

Rules:

- Every NPC ID must reference `NpcCatalog`.
- Every narrative-location ID must reference `NarrativeLocationCatalog`.
- One NPC may have at most one placement in V6.
- Multiple NPCs may share one narrative location.
- A blank requirement-set ID makes the placement visible whenever its location is visible.
- A non-empty requirement-set ID must reference `RequirementCatalog`.
- Placement requirements do not override location requirements; both must succeed.
- A single NPC ID cannot move between placements. To portray the same character in two locations at different points in the story, define two NPC IDs with separate placements and complementary visibility requirements, such as `FLAG_UNSET` and `FLAG_SET` for the same flag. This is a content convention; each ID remains a distinct NPC definition.
- Declaration order is preserved inside each narrative location.

Loading flow:

```text
npc_placements.txt
    -> NpcPlacementLoader
    -> NpcPlacementDefinition
    -> NpcPlacementCatalog
        -> lookup by NPC ID
        -> ordered lookup by narrative-location ID
```

## Linear dialogue definitions

External linear dialogue is loaded from:

`data/dialogues.txt`

Header:

```text
# dialogueId;line;speakerType;speakerId;text
```

Each valid line contains exactly 5 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | Dialogue ID | `String` | Not allowed |
| 2 | Line number | `int` | Not allowed |
| 3 | Speaker type | `DialogueSpeakerType` | Not allowed |
| 4 | Speaker ID | `String` | Type-dependent |
| 5 | Utterance text | `String` | Not allowed |

The initial speaker types are exactly:

- `NPC`
- `ENEMY`
- `LEADER`
- `NARRATOR`
- `RECRUIT`

Speaker rules:

| Speaker type | Speaker ID |
| --- | --- |
| `NPC` | Canonical NPC ID |
| `ENEMY` | Canonical enemy ID |
| `LEADER` | Blank |
| `NARRATOR` | Blank |
| `RECRUIT` | Canonical recruitment-candidate ID |

Additional rules:

- Dialogue IDs follow the stable-ID convention.
- Every dialogue contains at least one line.
- Line numbers begin at 1 and are unique and consecutive inside each dialogue.
- Dialogue order follows each dialogue's first declaration, while utterances are stored and presented in validated numeric line order.
- `NPC` speakers may reference NPCs that are not placed in a narrative location.
- `ENEMY` speakers resolve their display names through `EnemyCatalog`.
- `LEADER` resolves the active party leader at presentation time.
- `NARRATOR` displays no speaker label.
- `RECRUIT` resolves the candidate's persisted generated name.
- Every `RECRUIT` speaker ID must reference a candidate declared by `QuestRecruitmentCatalog`.
- The candidate is created idempotently when first introduced by a displayed dialogue.
- `DialoguePresenter` resolves the already established candidate state and does not create or reroll candidate identity.
- Utterance text supports literal `\n` and the documented party placeholders.
- Dialogue definitions contain no choices, conditions, branching, timing, color or audio.
- One physical record represents one complete utterance.
- Each utterance advances only after Enter during presentation.

Loading flow:

```text
dialogues.txt
    -> DialogueLoader
    -> ordered DialogueLineDefinition objects
    -> DialogueDefinition
    -> DialogueCatalog
```

## Dungeon-boss gate definitions

Access conditions for dungeon bosses are loaded from:

`data/dungeon_boss_gates.txt`

Header:

```text
# dungeonId;availabilityRequirementSetId;defaultBlockedDialogueId;blockedDestinationScenarioId
```

Each line contains exactly 4 fields:

| Position | Field | Required reference |
| ---: | --- | --- |
| 1 | Dungeon ID | Canonical dungeon scenario ID |
| 2 | Availability requirement-set ID | Existing set in `requirements.txt` |
| 3 | Default blocked-dialogue ID | Existing dialogue in `dialogues.txt` |
| 4 | Blocked destination scenario ID | Existing scenario ID |

When the party reaches a gated boss without satisfying the requirement set, the encounter does not begin. The game presents a matching `BOSS_GATED` dialogue from `combat_scenario_dialogues.txt`, or the default dialogue declared here, then moves the party to the configured destination.

A gate is reusable across quests: the requirement set decides access, while quest-specific dialogue associations can change the reaction without changing the gate.

## Combat-scenario dialogue associations

Dialogues triggered by combat-scenario events are associated through:

`data/combat_scenario_dialogues.txt`

Header:

```text
# scenarioId;trigger;questId;step;dialogueId
```

Each valid line contains exactly 5 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | Scenario ID | `String` | Not allowed |
| 2 | Trigger | `CombatScenarioDialogueDefinition.Trigger` | Not allowed |
| 3 | Quest ID | `String` | Allowed for a general dialogue |
| 4 | Quest step | `int` | Blank when the quest ID is blank |
| 5 | Dialogue ID | `String` | Not allowed |

Supported triggers:

- `BOSS_GATED`: the party reaches a dungeon boss whose access requirements are not satisfied.
- `BOSS_NARRATIVE`: the first, narrative boss encounter, before battle.
- `BOSS_RECURRING`: later boss encounters, before battle.

Rules:

- All currently supported triggers reference a canonical dungeon scenario.
- The dialogue ID must exist in `DialogueCatalog`.
- A quest-specific record requires a canonical quest ID and a positive existing step.
- A general record leaves both `questId` and `step` blank.
- A scenario, trigger and quest-step combination cannot be declared twice.
- A matching active quest-step dialogue takes priority over the general dialogue for the same scenario and trigger.
- If `BOSS_GATED` has no matching dialogue here, the default dialogue in `dungeon_boss_gates.txt` is used.
- `BOSS_NARRATIVE` and `BOSS_RECURRING` have no dialogue when neither a matching quest-step record nor a general record exists.
- Encounter dialogue is presented before battle. Escaping does not complete the narrative boss, so another attempt may present the same dialogue again.
- NPC speakers in these dialogues do not need a narrative-location placement. Enemy speakers use their canonical enemy IDs.
- Dialogue associations are definitions, not mutable save state.

Examples:

```text
minas_alem_tumulo;BOSS_GATED;energia_residual;6;aviso_mago_minas
gruta_relamparrocha;BOSS_NARRATIVE;;;confronto_mestre_eletrogoblin
```

Loading flow:

```text
combat_scenario_dialogues.txt
    -> CombatScenarioDialogueLoader
    -> CombatScenarioDialogueDefinition
    -> CombatScenarioDialogueCatalog
    -> DungeonBossAccessResolver
    -> DialoguePresenter
```

## Quest-dialogue definitions

Quest interaction contexts are associated with external dialogues through:

`data/quest_dialogues.txt`

Header:

```text
# questId;step;context;dialogueId
```

Each valid line contains exactly 4 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | Quest ID | `String` | Not allowed |
| 2 | Step number | `int` | Not allowed |
| 3 | Context | `QuestDialogueContext` | Not allowed |
| 4 | Dialogue ID | `String` | Not allowed |

The initial contexts are exactly:

- `OFFER`
- `ACCEPT`
- `DECLINE`
- `PENDING`
- `COMPLETE`

Context rules:

| Context | Step | Requirement |
| --- | ---: | --- |
| `OFFER` | `0` | Required exactly once for every quest |
| `ACCEPT` | `0` | Optional |
| `DECLINE` | `0` | Optional |
| `PENDING` | Positive real step | Optional and allowed only for a delivery step |
| `COMPLETE` | Positive real step | Required for every step completed through NPC interaction |

Additional rules:

- Every quest ID must reference `QuestCatalog`.
- Every positive step must belong to the referenced quest.
- Every dialogue ID must reference `DialogueCatalog`.
- One quest, step and context combination may appear at most once.
- Acceptance and refusal choices are generated by `QuestOfferMenu`; they are not dialogue-line choices.
- A declined offer does not start the quest and may be offered again while its requirements remain satisfied.
- A missing optional `ACCEPT`, `DECLINE` or `PENDING` dialogue does not prevent the corresponding interaction.
- `COMPLETE` dialogue is presented after complete transaction prevalidation and before mutation.
- The transaction is revalidated after the dialogue before state changes are applied.
- Default NPC dialogue remains available through `Conversar`, independently from quest-dialogue associations.

Loading flow:

```text
quest_dialogues.txt
    -> QuestDialogueLoader
    -> QuestDialogueDefinition
    -> QuestDialogueCatalog
```

## Quest-recruitment definitions

Recruitment dispatch is declared through:

`data/quest_recruitments.txt`

Header:

```text
# questId;step;candidateId;allyLevel
```

Each valid line contains exactly 4 fields:

| Position | Field | Java type | Empty value |
| ---: | --- | --- | --- |
| 1 | Quest ID | `String` | Not allowed |
| 2 | Step number | `int` | Not allowed |
| 3 | Candidate ID | `String` | Not allowed |
| 4 | Ally entry level | `int` | Not allowed |

Rules:

- Quest ID must reference `QuestCatalog`.
- Step must be positive and belong to the referenced quest.
- Candidate ID follows the stable lowercase ID convention.
- Ally level must remain between the supported minimum and maximum playable-character levels.
- One quest-and-step combination may appear at most once.
- One candidate ID may appear at most once.
- The catalog accepts at most three records, matching the three non-leader party positions.
- Candidate name and recruited state are mutable campaign data and are not stored in this file.
- Candidate class is selected at recruitment time and is not stored in this file.
- Introductory equipment is resolved from the selected class path and is not stored in this file.
- `speakerId` in `dialogues.txt` identifies a declared candidate but does not independently dispatch or complete recruitment.
- The first canonical recruitment uses level 7 and composes with the protagonist's class-path ceremony.
- Later narrative recruitment orchestration remains separate from the first ceremony while reusing the generic recruitment definition and domain services.
- Part 8 provides runtime dispatch only for the first ceremony-composed recruitment. Second- and third-ally records must not be authored until their narrative recruitment orchestration is implemented.

Loading flow:

```text
quest_recruitments.txt
    -> QuestRecruitmentLoader
    -> QuestRecruitmentDefinition
    -> QuestRecruitmentCatalog
        -> lookup by quest and step
        -> lookup by candidate ID
```

## Narrative placeholders

The following placeholders are supported in quest descriptions, step descriptions, objective text and dialogue utterances:

- `{leaderName}`
- `{groupMember2}`
- `{groupMember3}`
- `{groupMember4}`
- `{recruitName:candidateId}`

Rules:

- Placeholders are resolved from the current party only when text is presented.
- A referenced missing party member causes resolution to fail before related mutation.
- Unsupported placeholder names are rejected during startup.
- Unmatched or malformed braces are rejected.
- Placeholders are not supported in technical IDs, names or titles.
- `{groupName}` is reserved conceptually for future support and is rejected in V6.
- Literal `\n` decoding occurs independently from placeholder replacement.
- Complete dialogue text is resolved before its first line is displayed.

## Empty V6 content catalogs

The V6 definition files may temporarily contain only comments and headers while canonical content is still being authored.

An empty catalog is valid only when no loaded definition requires one of its entries. Once a quest, NPC, placement, dialogue, requirement, reward or effect references another catalog, that reference remains mandatory and is validated during startup.
