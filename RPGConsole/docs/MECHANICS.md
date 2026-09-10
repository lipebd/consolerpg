# RPGConsole — Game Mechanics

This document records the game mechanics that are currently implemented and considered stable.

Planned mechanics remain in the roadmap until they are implemented and validated.

## Battle groups

A battle supports:

- One to four playable characters.
- One to three enemies.
- Defeated participants remain visible in battle status.
- Defeated participants cannot act.
- Defeated enemies cannot be selected by regular attacks or offensive skills.
- Repeated enemies receive numbered display names.

Example:

- `Passavento 1`
- `Passavento 2`
- `Lobermo`

The global enemy limit is controlled by:

`EnemyBattleGroup.MAXIMUM_SIZE`

Each combat scenario may define a smaller minimum and maximum enemy count.

## Battle rounds

Battles use rounds containing one unified action order.

All living allies and enemies participate in the same round queue.

The queue is rebuilt at the beginning of every round. Consequently:

- Speed changes affect only the following round.
- Participants defeated during the current round lose their remaining actions.
- Revived allies enter the action order only in the following round.
- The round queue preserves its original snapshot internally.
- Defeated entries are skipped when the queue advances.

## Actions per round

A participant may receive more than one action during the same round.

The number of actions is calculated by comparing the participant's effective Speed with the average effective Speed of the living opposing side.

Formula:

`actionCount = round(participantSpeed / opponentAverageSpeed)`

Limits:

- Minimum: one action per round.
- Maximum: three actions per round.

The current maximum is defined by:

`BattleTurnOrderGenerator.MAXIMUM_ACTIONS_PER_PARTICIPANT`

The opposing average is calculated before additional action entries are created.

## Turn initiative

Each action receives an initiative score.

Formula:

`initiativeScore = participantSpeed / actionNumber`

Examples:

- First action: `VEL / 1`
- Second action: `VEL / 2`
- Third action: `VEL / 3`

This distributes additional actions across the round instead of always placing them consecutively.

The action order follows these rules:

1. Highest initiative score.
2. Highest effective Luck when initiative scores are equal.
3. Random order when initiative and Luck are both equal.

The random final tie-breaker is recalculated every round.

## Round visualization

Before each action, the battle displays:

- The current participant.
- The next living participant.

Example:

```text
Vez de Passavento!
A seguir: Bardo
```

Playable characters can inspect the current round through the `Rodada` battle action.

The detailed round screen:

- Does not consume the current action.
- Places `[AGORA]` at the top.
- Places `[A SEGUIR]` in the second position.
- Hides actions that have already occurred.
- Skips participants defeated before their actions.
- Displays up to five upcoming actions.
- Displays the number of additional hidden actions.
- Does not predict the following round.

The following round is not predicted because its order may change due to defeats, revives, Speed modifiers and a new random tie result.

## Playable battle actions

The current battle menu contains:

1. Attack — `Atacar`
2. Defend — `Defender`
3. Special — `Especial`
4. Analyze — `Analisar`
5. Items — `Itens`
6. Round — `Rodada`
7. Flee — `Fugir`

Analyzing, inspecting the round, cancelling target selection and returning from a submenu do not consume the current action.

Basic attacks, defending, successfully used items and flee attempts consume the acting character's action.

A skill consumes its resources and action when at least one component constitutes a valid attempt. Applied, resisted and not-triggered components are valid attempts. Components whose condition does not match or that cannot affect any selected target are not attempts.

## Defending

Defending belongs to one individual character, not to the entire party.

The defensive state:

- Reduces the first received damage by half.
- Is consumed by that first damage.
- Does not protect other allies.
- Does not reduce every attack received during the round.

This behavior is intentional for balance.

## Enemy target selection

The current enemy strategy selects the living playable character with the lowest current HP.

Enemies:

- Never select defeated allies.
- Recalculate their target when their action begins.
- May focus the same injured ally repeatedly.

Additional targeting strategies may be implemented in future versions.

## Fleeing

A flee attempt is performed by the playable character whose action is currently active.

A successful attempt removes the entire party from battle.

A failed attempt:

- Consumes the acting character's action.
- Does not end the battle.
- Allows the round queue to continue normally.

Fleeing uses two sequential checks.

### Direct Luck check

The first check uses effective Luck directly as a percentage.

`directEscapeChance = effectiveLuck%`

Effective Luck is intended to include equipment, buffs and debuffs.

The chance is limited between 0% and 100%.

If this check succeeds, the remaining escape calculation is ignored.

### Attribute comparison check

This check occurs only if the direct Luck check fails.

Formula:

```text
escapeChance = 50
             + 3 × (characterSpeed - enemyAverageSpeed)
             + 2 × (characterLuck - enemyAverageLuck)
```

Limits:

- Minimum: 10%.
- Maximum: 90%.

The calculation uses effective Speed and effective Luck.

Enemy averages consider only living enemies.

### Boss escape

Fleeing from a dungeon boss is allowed.

When the party flees from the final boss:

- The boss is not defeated.
- The dungeon is not completed.
- The exploration session returns to the previous position.
- The party may prepare before attempting the final position again.

## Contextual battle-result messages

Battle-result messages adapt to the party and enemy-group sizes.

When only one playable character is present, messages use that character's name.

When multiple playable characters are present, messages identify the decisive character and their companions.

The system records:

- The character who successfully fled.
- The character who delivered the final attack.
- The last playable character defeated.

These references prevent collective battle results from depending on a permanently fixed party leader.

Future party titles will be reserved primarily for collective narrative events rather than individual combat actions.

## Temporary attribute effects

Temporary buffs and debuffs modify effective combat attributes without changing base, natural or equipment values.

Character statistics observe these structural invariants:

- Maximum HP must be positive.
- Maximum MP and SP cannot be negative.
- ATK, DEF, MAG, RES, VEL and LCK must be positive.

Invalid statistics prevent game-data initialization. File loaders identify the affected definition and source line.

The stage-modifiable attributes are:

- ATK
- DEF
- MAG
- RES
- VEL
- LCK

HP, MP and SP do not use temporary stages.

### Attribute stages

Positive and negative stages are stored independently for each attribute.

Each polarity is limited between `0` and `3`.

The effective stage is calculated as:

```text
effectiveStage = positiveStages - negativeStages
```

Opposite polarities therefore cancel only in the effective result. They do not remove one another because their durations remain independent.

Stage multipliers:

| Effective stage | Multiplier |
| ---: | ---: |
| `+3` | `1.50` |
| `+2` | `1.30` |
| `+1` | `1.15` |
| `0` | `1.00` |
| `-1` | `0.85` |
| `-2` | `0.70` |
| `-3` | `0.50` |

The effective attribute is calculated as:

```text
effectiveAttribute =
    roundHalfUp(structuralAttribute × stageMultiplier)
```

Half-up rounding converts only the fractional result to the nearest integer. Integer results remain unchanged.

Stage multipliers are internal balance rules and are not exposed through external data files.

### Stage accumulation and duration

Applications of the same polarity add their stages up to the limit of `3`.

When same-polarity effects accumulate, the state preserves the greatest remaining duration.

Example:

```text
ATK +1 for 4 turns
ATK +2 for 2 turns
Result: ATK +3 for 4 turns
```

An application produces an effect when it increases the stored stage or remaining duration. An application that changes neither is ineffective.

Positive and negative states of the same attribute preserve independent durations.

### Turn-based duration

Except for VEL, effect duration is measured in consumed turns of the affected character.

The duration decreases only after that character completes an action that consumes its turn.

The following do not decrease duration:

- Opening or cancelling menus.
- Viewing the turn order.
- Analyzing an enemy.
- Cancelling a target selection.
- Attempting an action for which no component can produce an effect.
- Actions consumed by other battle participants.

A character with multiple actions in the same round may decrease its durations multiple times during that round.

### Ordered skill effects

A skill may apply components before or after its primary damage or recovery component.

Pre-action effects are applied before the primary component and participate in its calculations. Their duration is decreased at the end of that same consumed turn when they affect the acting character.

Post-action effects are applied after the acting character's duration decrease. They do not lose duration immediately and remain active until the affected character completes a later consumed turn.

Effects applied to another participant are decreased only when that affected participant completes one of its own consumed turns.

### Composite skill attempts

Every attempted component-target pair produces one explicit result:

- `APPLIED`: the component affected the target.
- `RESISTED`: the component triggered, but the target resisted it.
- `NOT_TRIGGERED`: the component was applicable, but its configured trigger roll failed.

A component whose condition does not match or that cannot affect its target produces no result and does not constitute an attempt.

A skill consumes its resources and the acting character's turn once when at least one component is attempted. A resisted component and a component that does not trigger still count as valid attempts.

If no component can be attempted, the skill consumes neither resources nor the turn.

### Speed stages

VEL stages do not use conventional turn duration.

A VEL buff or debuff applied during the current round remains pending for the next turn-order calculation.

The current order is never reorganized retroactively.

At the start of the following round:

1. The complete turn order is generated using effective VEL.
2. The VEL states used by the calculation are removed.
3. The generated order remains unchanged for the entire round.
4. Any VEL state applied after that generation remains pending for the subsequent round.

Pending positive VEL stages may be dissipated before order generation.

Pending negative VEL stages may be purified before order generation.

If removed before generation, they do not affect the next round.

### Purification and dissipation

Purification removes all negative attribute states from the target.

Dissipation removes all positive attribute states from the target.

Pending VEL states are eligible for these operations.

Purification or dissipation is ineffective when the target has no state of the corresponding polarity.

Special battle states are not automatically treated as positive or negative attribute states.

### Effect cleanup

When a character is defeated, all of that character's temporary attribute states and special temporary battle states are removed immediately.

Revival does not restore removed effects.

At the end of a battle, all remaining temporary battle states are removed from every participant.

Temporary effects cannot persist into an unrelated battle.

## Damage calculation

Physical and magical damage use internal balance factors that are not exposed through external data files.

Current factors:

```text
KF = 20
KM = 20
```

Physical damage uses effective ATK against effective DEF.

Magical damage uses effective MAG against effective RES.

Effective attributes include all applicable natural values, equipment bonuses and temporary attribute stages.

Damage power is calculated as:

```text
basePower =
    skillPower
    + weaponBaseDamage
    + effectiveOffensiveAttribute
```

Basic attacks use the physical formula with zero skill power, zero weapon base damage and no conditional power modifier. Both playable-character and enemy basic attacks use the same centralized calculation.

When an applicable conditional power modifier exists:

```text
modifiedPower =
    basePower
    × (1 + powerModifierPercent / 100)
```

A `DAMAGE_POWER_MODIFIER` affects every later compatible damage component:

- within the same skill execution;
- against the same selected target;
- until that skill use ends.

It does not affect earlier damage, other targets or future skill uses. Multiple compatible modifiers add their percentages using overflow-safe arithmetic.

Physical damage before defending is:

```text
physicalDamage =
    modifiedPower
    × KF
    / (effectiveDefense + KF)
```

Magical damage before defending is:

```text
magicalDamage =
    modifiedPower
    × KM
    / (effectiveResistance + KM)
```

Defending divides the calculated damage by two before rounding.

The final result is rounded to the nearest integer using half-up rounding, with a minimum of 1 damage and a maximum equal to the target's current HP.

The defending state is consumed when the character receives damage.

## Skill execution

Skills are immutable external definitions composed of one or more ordered effects.

Each use creates one mutable execution. Targets are selected once for each target type used by the skill and are reused by all compatible effects.

Effects are executed in this order:

```text
PRE_ACTION
PRIMARY
end-of-turn duration reduction
POST_ACTION
```

`PRE_ACTION` effects are fully applied before `PRIMARY` effects. Therefore, an attribute change applied during `PRE_ACTION` can affect damage, recovery or resistance calculations performed later in the same skill.

`POST_ACTION` effects are executed only after the acting character finishes the consumed turn and all conventional effect durations are reduced.

Skill resources are consumed only after target selection and when the execution is able to consume the turn.

Components are evaluated in this order:

```text
condition
applicability
trigger chance
resistance, when applicable
application
```

An applicable component that is resisted or does not trigger still counts as an attempted component and consumes the turn. A component whose condition does not match or that cannot affect any selected target does not constitute an attempt.

## Skill-effect trigger chance

Each skill component may define its own `triggerChancePercent` in `skill_effects.txt`.

An empty trigger field means `100%`. Explicit values must be integers between `1` and `100`.

Trigger chance is rolled once per component execution, after condition and applicability checks. The result is shared by every applicable target of that component.

When the trigger fails:

- the component is recorded as `NOT_TRIGGERED` for its applicable targets;
- no target performs a resistance check;
- the component still constitutes a valid attempt.

A guaranteed component does not consume a random value for its trigger.

## Resistible effect application

The following effects can be resisted:

- negative attribute stages;
- dissipation of positive attribute stages.

Damage, recovery, revival, positive attribute stages, purification and ancestral distortion do not perform a resistance check.

Application chance uses the effective LCK of the applying character and the effective RES of each receiving character at the moment the component is executed.

```text
applicationChance =
    MIN(
        0.85,
        ROUND(
            applyingLCK
            / (
                applyingLCK
                + targetRES / KR
            ),
            2
        )
    )
```

Current resistance balance factor:

```text
KR = 1
```

The maximum application chance is 85%.

After a component triggers, each applicable target performs an independent resistance check. A component may therefore trigger once and still be applied to some targets while being resisted by others.

Application chance against resistance is calculated at runtime and is independent from the component's externally configured trigger chance.

## Distorted attributes

Ancestral distortion swaps the structural values used by these effective attributes:

```text
ATK ↔ MAG
DEF ↔ RES
```

Attribute stages remain attached to their original attributes. For example, an ATK stage continues modifying effective ATK even when its structural value comes from MAG.

Applying ancestral distortion again to the same target restores the original structural arrangement.

Ancestral distortion cannot be resisted. Purification and dissipation do not remove it. It remains active until toggled again or until temporary battle effects are cleared at the end of battle.

## Skill recovery

HP recovery and revival components define their scaling attribute directly. They currently accept effective ATK or effective MAG.

The calculated amount is:

```text
recovery =
    power
    × (1 + (effectiveAttribute - 10) / 100)
```

The result is rounded once using half-up rounding. An applicable recovery has a minimum calculated value of 1.

Effective attributes include temporary stages and ancestral distortion. HP recovery is then limited by the target's missing HP. Revival is limited by the target's maximum HP.

MP and SP recovery currently use fixed component power and do not define a scaling attribute.

Skill target types currently include:

- `SELF`
- `SINGLE_ALLY`
- `ALL_ALLIES`
- `ALL_BUT_SELF`
- `RANDOM_ALLY`
- `SINGLE_ENEMY`
- `ALL_ENEMIES`
- `RANDOM_ENEMY`

### Healing

Regular healing:

- Affects only living characters.
- Cannot target defeated characters.
- Cannot increase HP beyond maximum HP.
- Does not consume resources when no valid target exists.
- Does not consume the action when no valid target exists.

### Reviving

Revive effects:

- Affect only defeated allies.
- Restore HP according to the component's scaled recovery value.
- Cannot target living allies.
- Do not restore removed temporary battle effects.
- Add the revived ally to the turn order only in the following round.

### Recovery targets

`SELF` affects only the acting character.

`SINGLE_ALLY` allows one valid party member to be selected.

`ALL_ALLIES` affects every valid party member, including the acting character.

`ALL_BUT_SELF` affects every valid party member except the acting character.

## Shared inventory and item usage

Inventory and gold belong to the party.

The inventory:

- Is shared by every playable character.
- Stores one runtime `Item` entry with its owned quantity.
- Stacks equal items using their stable IDs.
- Has no weight mechanic.
- Has no capacity limit.
- Does not require inventory expansions or external storage.
- Keeps item prices hidden outside shop, merchant and trading contexts.

## General inventory

Outside battle, `InventoryMenu` organizes owned items by category.

The general inventory:

- Displays every item category in deterministic enum order.
- Keeps empty categories selectable.
- Reports that a category is empty only after the player selects it.
- Keeps the inventory and empty-category screens open until the player explicitly returns or closes them.
- Does not display item counts in the category-selection screen.
- Sorts items deterministically by name.
- Displays at most eight item rows per page.
- Preserves a fixed list height on partially filled pages.
- Truncates long names only in the list view.
- Displays the complete name and description in the details view.
- Provides contextual actions according to the runtime item type.

Current and planned detail actions include:

- `RecoveryItem` — use.
- Equipment — equip or remove after equipment behavior is implemented.
- Materials, quest items and key items — return without a use action.
- Skill scrolls — learn after skill-scroll behavior is implemented.

## Battle inventory

Battle item selection is separate from the general inventory.

During battle:

- Only consumables currently usable in battle are listed.
- Equipment, materials, quest items, key items and unrelated categories cannot be browsed.
- Selecting an item first opens its details.
- Cancelling returns to the battle item list.
- A failed or ineffective item attempt does not consume the acting character's turn.
- A successful item use consumes the acting character's action.
- The item is removed only after producing at least one effect.

## Recovery items

A recovery item may combine:

- HP recovery.
- MP recovery.
- SP recovery.
- Revival.
- Status-effect purification.

A recovery item definition must contain at least one effective component.

Recovery effects follow these rules:

- `SINGLE_ALLY` allows one eligible party member to be selected.
- `ALL_ALLIES` evaluates every eligible party member independently.
- Revival is applied before ordinary HP recovery.
- A combined revival and HP recovery item may therefore revive a defeated ally and then restore additional HP.
- Ordinary HP, MP and SP recovery affect only living characters.
- A component that cannot change the target produces no effect.
- A group item is consumed only when at least one party member is affected.
- An individual item is consumed only when its selected target is affected.
- The result records the effects actually produced instead of inferring them from the item definition.

Purification removes eligible negative attribute states. If no selected target has an eligible negative state or another applicable recovery component, the item is not consumed.

## Battle rewards

Gold and item drops are granted once to the party.

Experience is granted individually.

Every party member receives the complete experience reward:

- Living members receive EXP.
- Defeated members also receive EXP.
- EXP is not divided by party size.
- Excess EXP is preserved after leveling up.

Future individual EXP multipliers will be calculated separately for each character.

The reward screen:

- Condenses equal EXP values into `EXP recebida por aliado`.
- Lists individual values when character multipliers produce different rewards.
- Displays every character who gained one or more levels.
- Displays level progression and natural-stat growth compactly.
- Announces every skill unlocked while crossing the gained level interval.
- Aggregates equal item drops into one quantity.

## Natural character progression

Every playable class defines its level-1 natural stats in `playable_classes.txt` together with one primary and one secondary regular attribute.

Natural growth is calculated directly from the class definition and the target level. It is not applied incrementally to the character's previous values.

For a target level:

```text
gainedLevels = level - 1
primaryGrowthOccurrences = floor(level / 2)
secondaryGrowthOccurrences = floor(level / 3)
```

Every level after level 1 grants:

```text
HP  +25
MP  +10
SP  +10
ATK +1
DEF +1
MAG +1
RES +1
VEL +1
LCK +1
```

The primary attribute gains one additional point at levels 2, 4, 6 and every two levels thereafter.

The secondary attribute gains one additional point at levels 3, 6, 9 and every three levels thereafter.

Attribute specialization also increases the associated natural resource:

| Specialized attribute | Additional resource growth |
|---|---|
| `ATK` | `SP +10` per corresponding primary or secondary occurrence |
| `MAG` | `MP +10` per corresponding primary or secondary occurrence |
| `DEF` or `RES` | `HP +25` per corresponding primary or secondary occurrence |

Resource bonuses are cumulative. A class whose primary and secondary attributes are `DEF` and `RES` receives the defensive HP bonus from both schedules.

Natural stats exclude equipment bonuses, temporary stages and Distorção Ancestral. Combat systems use effective attributes after applicable modifiers, while progression and reward comparison use immutable natural-stat snapshots.

If several levels are gained at once, the final level is determined first and the complete natural-stat set is calculated and installed once. Arithmetic overflow is rejected instead of wrapping silently.

## Level-based skill availability

A playable character's available skills are derived from its class skill definitions and current level. A skill is available when its acquisition level is less than or equal to the character's level.

When one reward crosses several levels, every skill whose acquisition level is above the previous level and at or below the new level is reported as newly unlocked. Skill definitions remain unique catalog entries; they are not copied into a mutable learned-skill list.

## Level-up resource restoration

A playable character who survives a victorious battle and gains at least one level restores these resources after the new natural maximums are installed:

- 100% of maximum HP.
- 100% of maximum MP.
- 100% of maximum SP.

The restoration occurs only when the character:

1. Gains at least one level.
2. Is alive when battle rewards are granted.

A defeated character:

- Still receives the complete EXP reward.
- May gain one or more levels.
- Remains defeated.
- Does not recover HP, MP or SP.

Characters do not automatically revive after victory.

This preserves the importance of:

- Revival skills.
- Revival items.
- City INNs.
- Future exploration checkpoints and camps.

Characters who survive without leveling preserve their current HP, MP and SP.

## City INNs

An INN is a functional city location configured through `data/inns.txt`.

Each INN defines:

- A stable ID.
- Its city scenario ID.
- Its display name and description.
- A fixed price for the whole party.

The price does not change according to party size.

The player may enter an INN even when the party cannot afford a stay. This keeps the location accessible and allows the service to explain why resting is unavailable.

A stay is prevented when:

- Every party member is already alive with maximum HP, MP and SP.
- The party does not have enough gold.
- The player cancels the confirmation.

No gold or character resources change in any of these cases.

After all requirements succeed, the player must confirm the stay. The configured price is then deducted once from the shared party gold.

Restoration occurs in this order:

1. Every defeated party member is revived.
2. Every party member has HP, MP and SP fully restored.

The INN flow does not change the general behavior of `GameCharacter.restoreAllResources()`, which still refuses to restore defeated characters. The INN explicitly revives them before calling that method.

After a successful stay, the player remains inside the INN. Feedback from successful, unnecessary and unaffordable attempts is displayed inside the current INN screen, between the party's gold information and the available actions.

City locations and city exits remain separate navigation paths. Adjacent exits reuse unlocked `TravelRouteDefinition` entries, preserve their display names and descriptions, and do not charge the party. Fixed city shops are available through the same location-selection flow as INNs, while paid transport remains a separate city action.

## Party status

The party status interface is available outside battle.

The summary screen displays:

- Every current party member.
- Level.
- Current and maximum HP.
- Current and maximum MP.
- Current and maximum SP.
- Defeated state.
- Shared party gold.

A defeated character is displayed compactly as `DERROTADO`.

Every party member, including a defeated member, may be selected for individual inspection.

The individual character screen displays:

- Level.
- Current EXP and next-level requirement.
- HP, MP and SP.
- ATK, DEF and VEL.
- MAG, RES and LCK.
- Learned skills.

Future versions will add:

- Playable class.
- Equipment.
- Character history.
- Individual reward multipliers.

## Currency presentation

The party owns one shared gold balance. `GameCurrency` centralizes only how that currency is presented:

- The current display name is `GOLD`.
- Numeric values use the `G$ ` prefix.
- Prices, balances and rewards use `GameCurrency.format(int)`.
- Screens that name the currency directly use `GameCurrency.getName()`.

Changing the visible name or symbol does not alter stored gold, prices or transaction rules.

## Shops and merchants

Fixed city shops and exploration merchants share the same definitions, runtime state, interface and transaction rules.

A `CITY` shop:

- Is associated with a city scenario.
- Appears as a functional location alongside the city's INN.
- Remains accessible even when every active sale offer is exhausted.

An `EXPLORATION` shop:

- Is associated with a field or dungeon.
- Is accessed through the existing merchant exploration event.
- Is selected uniformly when multiple eligible merchants exist in the current scenario.
- Uses the same persistent in-memory state whenever encountered again during the current program execution.

### Commercial item prices

An item may define two independent commercial prices:

- `baseShopSalePrice`: base value used when shops sell the item to the player.
- `shopPurchasePrice`: fixed unit value paid when any shop buys the item from the player.

Either price may be absent when the corresponding transaction is unavailable.

A `KEY_ITEM` cannot define `shopPurchasePrice` and therefore cannot be sold to shops.

The actual unit price of a shop offer is calculated from its configured percentage:

```text
roundedPrice = floor((baseShopSalePrice × shopSalePricePercent + 50) / 100)
```

This produces half-up rounding for positive values. The calculated price must remain positive and within the supported integer range.

For every item, `shopPurchasePrice` cannot exceed its lowest configured shop sale price. This prevents the player from buying an item cheaply and immediately selling it for profit.

### Active shop offers

Every shop defines a number of active offer slots. Its active offers are generated once when the application starts.

Selection uses relative weights without replacement. For one selection step, the probability of entry `i` is:

```text
P(i) = weight(i) / sum(weights of all remaining entries)
```

Weights:

- Are not percentages.
- Do not need to total 100.
- May all be scaled by the same factor without changing their relative probabilities.
- Are recalculated over the remaining pool after each selected entry is removed.

If the number of slots is equal to or greater than the number of possible entries, every entry is selected.

The generated offers and their remaining quantities are preserved for the whole program execution. When an offer is exhausted:

- It disappears from the visible purchase list.
- Its slot remains empty.
- No unused entry from `shop_stock.txt` replaces it.
- The shop itself remains accessible.

Shops and merchants do not restock during the current execution. Starting a new program execution generates a new active offer list and restores initial quantities. Persistent shop state and explicit restocking events remain postponed until save and load are implemented.

### Buying

The purchase interface:

- Displays only active offers with remaining stock.
- Sorts them by item category, name, unit price and stable offer ID.
- Keeps commercially distinct offers selectable even when they reference the same item.
- Allows a quantity up to the offer's remaining stock.
- Skips quantity selection when only one unit remains.
- Displays the complete item description, quantity, unit price and total before confirmation.

A purchase succeeds only when:

- The selected offer belongs to the current shop.
- The quantity is positive and available.
- The total price can be calculated without overflow.
- The party has enough gold.
- The resulting inventory quantity stays within the supported integer range.
- The player confirms the transaction.

Only after every validation succeeds does the transaction deduct party gold, add the items to the shared inventory and reduce the offer's remaining stock.

### Selling

Every shop and merchant may buy any owned item that defines `shopPurchasePrice`.

The sale interface:

- Organizes sellable items by the existing item categories.
- Excludes the `KEY_ITEM` category.
- Displays only owned items with a defined purchase price.
- Allows a quantity up to the amount currently owned.
- Skips quantity selection when the party owns only one unit.
- Displays the complete item description, quantity, unit price and total before confirmation.

A sale succeeds only when:

- The selected item is valid and sellable.
- The quantity is positive and owned.
- The total value can be calculated without overflow.
- The resulting party gold stays within the supported integer range.
- The player confirms the transaction.

Only after every validation succeeds does the transaction remove the items from the shared inventory and add the resulting gold.

Items sold by the player are not added to shop stock. V3 does not implement buyback.

### Transaction integrity

Purchases and sales are atomic from the player's perspective.

Invalid, unaffordable, stale, overflowing or cancelled transactions preserve all affected state:

- Party gold.
- Shared inventory.
- Remaining shop stock.

The shop remains open after successful, cancelled or rejected transactions until the player explicitly leaves.

## Scenario discovery

`WorldProgress` records each scenario entered during the active game.

- The initial scenario is discovered when `World` is created.
- Destinations entered through adjacent routes, fixed branches or paid transport are discovered.
- Discovery is permanent during the active game.
- Discovery does not unlock adjacent routes.
- An unlocked route does not automatically discover its destination.
- Discovered IDs are exposed without allowing callers to modify the internal state.

Discovery currently controls which configured cities may appear as paid-transport destinations.

## Adjacent routes and paid city transport

Adjacent routes and paid city transport are separate mechanics.

Adjacent routes:

- connect one specific source scenario to one destination;
- are directional and may require a separate reverse definition;
- are free;
- depend on `TravelProgress`;
- are displayed as `Seguir caminho`;
- remain unavailable while exploration of a combat scenario is incomplete;
- may define the exploration position used when entering a field or dungeon.

Paid city transport:

- targets configured destination cities rather than adjacent scenarios;
- depends on scenario discovery instead of route unlocking;
- is displayed as `Viajar`;
- has a fixed price determined by the destination;
- may begin from a city, field or dungeon;
- remains available during incomplete exploration;
- is unavailable during battle;
- excludes the current city when travel begins from a city;
- keeps discovered destinations visible even when the party cannot afford them;
- displays undiscovered city positions as unnumbered `--` entries.

The player selects a destination and confirms the journey before payment. Invalid selections, insufficient funds and cancellation preserve the party's gold. A confirmed valid journey deducts the exact price and moves the party directly to the destination city.

Leaving an incomplete combat scenario through paid transport discards its current `ExplorationSession`. Permanent gains already obtained—including EXP, gold, items, bestiary analysis, discoveries and route unlocks—remain unchanged. Returning later creates a fresh exploration session using the entry position of the route used to return.

## Full-party defeat

A battle ends in defeat when every playable character is defeated.

The current V2 behavior opens the temporary `GameOverScreen`.

Defeated characters are not automatically revived after battle.

A future version will replace Game Over with:

- Rescue in Bree.
- Full restoration through the rescue flow.
- Preservation of EXP, gold, inventory and progression.

## Linear exploration

Exploration works like movement through spaces on a board.

Each successful `Explore` action currently:

- Advances the exploration position by one.
- Determines whether the new position is a normal or special area.
- Generates one exploration event.
- Preserves the position after regular battles.

An exploration session ends when the final position is completed or when the party enters another scenario.

The possibility of resolving the current position before advancing will be studied for V3.

## Special areas

Intermediate exploration positions may become special areas according to the scenario's configured chance.

Special areas:

- Display a scenario-specific message.
- Filter encounters and exploration rewards according to their area restrictions.
- May contain enemies and rewards unavailable in normal areas.

Area restrictions are:

- `NORMAL_ONLY`: eligible only in normal areas.
- `SPECIAL_ONLY`: eligible only in special areas.
- `ANY`: eligible in both area types.

`ANY` entries complement the entries restricted to the current area instead of forming a separate pool.

The first and final positions are not randomly converted into special areas.

Special areas may produce the `Nothing` event normally.

## Exploration events

Current exploration events include:

- Nothing.
- Battle.
- Reward.
- Merchant.
- Epic encounter.

The total configured event chance cannot exceed 100%.

Any remaining percentage represents the `Nothing` event.

A merchant may appear at most once during one exploration session. When the event occurs, one eligible `EXPLORATION` shop associated with the current scenario is selected uniformly and opened through the shared shop interface.

If another roll falls within the merchant range after that encounter, it becomes `Nothing`. The one-merchant-per-session restriction does not reset or regenerate the selected shop's runtime state: later exploration sessions reuse the same offers and remaining quantities.

An epic encounter:

- Is exclusive to dungeons.
- May be offered at most once per exploration session.
- Is optional.
- Allows the party to avoid the confrontation.

A later epic-encounter roll becomes `Nothing` after the encounter has already been offered.

## Exploration rewards

The scenario's `rewardChance` determines whether an exploration reward event occurs. It does not determine which reward is granted.

When a reward event occurs:

1. Entries are filtered by scenario and current area type.
2. `ANY` entries are combined with entries specific to that area.
3. Exactly one eligible entry is selected using its relative weight.
4. Its quantity is randomized inclusively between the configured minimum and maximum.
5. The selected item or gold is added directly to the party's shared resources.

Reward weights:

- Are relative selection weights.
- Do not represent percentages.
- Do not need to total 100.
- Exist only within the eligible pool for the current scenario and area.

Item rewards reference runtime items by their stable IDs and are added to the shared inventory.

Gold rewards do not use a reward ID and are added directly to party gold.

Exploration rewards are repeatable. Unique, narrative and quest rewards are not part of these pools.

A scenario with zero `rewardChance` does not require reward entries. When rewards are possible, every normal or special area type that may occur must have at least one eligible entry.

Exploration reward selection is independent from battle rewards. Every enemy drop entry continues to perform its own independent roll after battle.

## Dungeon bosses

The final dungeon position triggers the configured boss encounter.

The boss encounter is mandatory to complete the dungeon.

If the party flees:

- The exploration position moves back by one.
- The dungeon remains incomplete.
- Travel remains locked.

If the boss is defeated:

- The final position remains completed.
- Travel options become available according to the travel catalog.

## Random encounters

Encounter entries are filtered by:

- Scenario ID.
- Current exploration position.
- Normal or special area.
- Maximum copies of the same enemy.

Enemy weights are relative selection weights.

They are not percentages and do not need to total 100.

The generated group size respects:

- The scenario minimum.
- The scenario maximum.
- The global enemy-group limit.
- Each encounter entry's maximum copy count.

Repeated enemies receive numbered battle names, while unique enemies retain their regular display name.
