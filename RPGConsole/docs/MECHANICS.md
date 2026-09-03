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

An action is consumed only when its effect is successfully executed or when a failed flee attempt explicitly consumes the acting character's turn.

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

## Skill recovery

Recovery skills separate the recovery effect from their target.

Recovery types:

- `NONE`
- `HEAL`
- `REVIVE`

Skill target types currently include:

- `NONE`
- `SELF`
- `SINGLE_ALLY`
- `ALL_ALLIES`
- `ALL_BUT_SELF`
- `SINGLE_ENEMY`
- `ALL_ENEMIES`

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
- Restore HP according to the skill's base healing value.
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

Purification is already represented by item data but remains inactive until status effects are implemented.

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
- Displays level progression compactly.
- Aggregates equal item drops into one quantity.

## Level-up resource restoration

A playable character who survives a victorious battle and gains at least one level restores:

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

City locations and city exits remain separate navigation paths. Adjacent exits reuse unlocked `TravelRouteDefinition` entries, preserve their display names and descriptions, and do not charge the party. Paid transport remains a future system.

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

A merchant may appear at most once during one exploration session. If another roll falls within the merchant range after that encounter, it becomes `Nothing`.

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