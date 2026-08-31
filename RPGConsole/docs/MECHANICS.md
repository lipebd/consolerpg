# RPGConsole — Game Mechanics

This document records the game mechanics that are currently implemented
and considered stable.

Planned mechanics must remain in the roadmap until they are implemented
and validated.

## Battle groups

A battle supports:

- One to four playable characters.
- One to three enemies.
- Defeated participants remain visible in the battle status.
- Defeated participants cannot act or be selected by regular attacks.
- Repeated enemies receive numbered display names.

Example:

- `Passavento 1`
- `Passavento 2`
- `Lobermo`

The global enemy limit is controlled by
`EnemyBattleGroup.MAXIMUM_SIZE`.

Each scenario may define a smaller minimum and maximum enemy count.

## Battle rounds

Battles use rounds containing a unified action order.

All living allies and enemies participate in the same round queue.

The queue is rebuilt at the beginning of every round. Consequently:

- Speed changes affect only the following round.
- Participants defeated during the current round lose their remaining
  actions.
- Revived allies enter the action order only in the following round.
- The round queue preserves its original snapshot internally.
- Defeated entries are skipped when the queue advances.

## Actions per round

A participant may receive more than one action during the same round.

The number of actions is calculated by comparing the participant's
effective Speed with the average effective Speed of the living opposing
side.

Formula:

`actionCount = round(participantSpeed / opponentAverageSpeed)`

Limits:

- Minimum: one action per round.
- Maximum: three actions per round.

The current maximum is defined by:

`BattleTurnOrderGenerator.MAXIMUM_ACTIONS_PER_PARTICIPANT`

The opposing average is calculated before additional action entries are
created.

## Turn initiative

Each action receives an initiative score.

Formula:

`initiativeScore = participantSpeed / actionNumber`

Examples:

- First action: `VEL / 1`
- Second action: `VEL / 2`
- Third action: `VEL / 3`

This distributes additional actions naturally across the round instead
of always placing them consecutively.

The action order follows these rules:

1. Highest initiative score.
2. Highest effective Luck when initiative scores are equal.
3. Random order when both initiative and Luck are equal.

The random tie is recalculated every round.

## Round visualization

Before each action, the battle displays:

- The current participant.
- The next living participant.

Example:

`Vez de Passavento!`

`A seguir: Bardo`

Playable characters can inspect the current round through the `Rodada`
battle action.

The detailed round screen:

- Does not consume the current action.
- Always places `[AGORA]` at the top.
- Places `[A SEGUIR]` in the second position.
- Hides actions that have already occurred.
- Skips participants defeated before their actions.
- Displays up to five upcoming actions.
- Displays the number of additional hidden actions.
- Does not predict the following round.

The following round is not predicted because its order may change due
to defeats, revives, Speed modifiers and a new random tie result.

## Playable battle actions

The current battle menu contains:

1. Attack `Atacar`
2. Defend `Defender`
3. Special `Especial`
4. Analyze `Analizar`
5. Items `Itens`
6. Round `Rodada`
7. Flee `Fugir`

Analyzing, inspecting the round, cancelling target selection and
returning from a submenu do not consume the current action.

## Defending

Defending belongs to one individual character, not to the entire party.

The defensive state:

- Reduces the first received damage by half.
- Is consumed by that first damage.
- Does not protect other allies.
- Does not last for every attack in the round.

This behavior is intentional for balance.

## Enemy target selection

The current enemy strategy selects the living playable character with
the lowest current HP.

Enemies:

- Never select defeated allies.
- Recalculate their target when their action begins.
- May focus the same injured ally repeatedly.

Additional target strategies may be implemented in future versions.

## Fleeing

A flee attempt is performed by the playable character whose action is
currently active.

A successful attempt removes the entire party from battle.

A failed attempt:

- Consumes the acting character's action.
- Does not end the battle.
- Allows the round queue to continue normally.

Fleeing uses two independent checks.

### Direct Luck check

The first check uses effective Luck directly as a percentage.

`directEscapeChance = effectiveLuck%`

Effective Luck includes future equipment, buffs and debuffs.

The chance is limited between 0% and 100%.

If this check succeeds, the remaining escape calculation is ignored.

### Attribute comparison check

This check occurs only if the direct Luck check fails.

Formula:

`escapeChance = 50`
`             + 3 × (characterSpeed - enemyAverageSpeed)`
`             + 2 × (characterLuck - enemyAverageLuck)`

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
- The player may prepare before attempting the final position again.

## Skill recovery

Recovery skills separate the recovery effect from its target.

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

- Can affect only living characters.
- Cannot target defeated characters.
- Cannot increase HP beyond maximum HP.
- Does not consume resources when no valid target exists.
- Does not consume the action when no valid target exists.

### Reviving

Revive effects:

- Can affect only defeated allies.
- Restore HP according to the skill's base healing value.
- Cannot affect living allies.
- Do not restore temporary battle effects.
- Add the revived ally to the turn order only in the following round.

### Recovery targets

`SELF` affects only the acting character.

`SINGLE_ALLY` allows one valid party member to be selected.

`ALL_ALLIES` affects every valid party member, including the acting
character.

`ALL_BUT_SELF` affects every valid party member except the acting
character.

## Shared item usage

Inventory and gold belong to the party.

Items can be used:

- Inside battle.
- Outside battle.
- By one ally on another ally.

Using an item during battle consumes the acting character's action only
when the item produces a valid effect.

Healing items:

- Select only living and injured allies.
- Cannot revive defeated allies.
- Are consumed only after producing an effect.

## Battle rewards

Gold and item drops are granted once to the party.

Experience is granted individually.

Every party member receives the complete experience reward:

- Living members receive EXP.
- Defeated members also receive EXP.
- EXP is not divided by party size.
- Excess EXP is preserved after leveling up.

Future individual EXP multipliers will be calculated separately for
each character.

The reward screen:

- Condenses equal EXP values into `EXP recebida por aliado`.
- Lists individual values when character multipliers produce different
  rewards.
- Displays every character who gained one or more levels.
- Aggregates equal item drops into one quantity.

## Enemy rewards

Each combat scenario defines:

- Base experience reward.
- Base gold reward.

Each defeated enemy applies its rank multiplier to those base values.

Current enemy ranks include:

- Common
- Veteran
- Leader
- Boss
- Epic
- Legendary

Rewards from multiple defeated enemies are added together.

## Linear exploration

Exploration works like movement through spaces on a board.

Each successful `Explore` action:

- Advances the exploration position by one.
- Determines whether the position is a normal or special area.
- Generates one exploration event.
- Preserves the position after regular battles.

An exploration session ends when the final position is completed or
when the player enters another scenario.

## Special areas

Intermediate exploration positions may become special areas according
to the scenario's configured chance.

Special areas:

- Display a scenario-specific message.
- Use their own encounter restrictions.
- May contain enemies unavailable in normal areas.

Encounter restrictions are:

- `NORMAL_ONLY`
- `SPECIAL_ONLY`
- `ANY`

The first and final positions are not randomly converted into special
areas.

## Exploration events

Current exploration events include:

- Nothing
- Battle
- Item
- Merchant
- Epic encounter

The total configured event chance cannot exceed 100%.

Any remaining percentage represents the `Nothing` event.

A merchant may appear at most once during one exploration session.

An epic encounter:

- Is exclusive to dungeons.
- May be offered at most once per exploration session.
- Is optional.
- Allows the player to avoid the confrontation.

## Dungeon bosses

The final dungeon position triggers the configured boss encounter.

The boss encounter is mandatory to complete the dungeon.

If the player flees:

- The exploration position moves back by one.
- The dungeon remains incomplete.

If the boss is defeated:

- The final position remains completed.
- Travel options become available according to the travel catalog.

## Random encounters

Encounter entries are filtered by:

- Scenario ID.
- Current exploration position.
- Normal or special area.
- Maximum copies of the same enemy.

Enemy weights are relative selection weights. They are not percentages
and do not need to total 100.

The generated group size respects:

- The scenario minimum.
- The scenario maximum.
- The global enemy group limit.
- Each encounter entry's maximum copy count.