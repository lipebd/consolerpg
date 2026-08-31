# RPGConsole — Project State

## Current version

V2 feature implementation completed.

Current stage:

`Release preparation and documentation`

Current Git branch:

`feature/v2-data-driven-enemies`

The V1 MMMVP is preserved by the tag:

`v1.0-mmmvp`

## Project objective

Console RPG developed in Java as an academic project.

The project uses object-oriented programming, including inheritance, abstraction, encapsulation, polymorphism and external text files for game data.

Code identifiers and technical comments are written in English. Texts displayed to the player are written in Brazilian Portuguese.

## Implemented in V1

- Title screen and console clearing.
- Initial scenario: Verdes Ermos.
- Interactive world loop.
- Random exploration encounters.
- One player against one enemy.
- Attack, defend, special skill, analyze, items and flee actions.
- HP, MP and SP.
- Skills with MP and SP costs.
- Enemy ranks and reward multipliers.
- EXP, gold and level progression with excess EXP preservation.
- Battle result screen.
- Item inventory and Merumã healing item.
- Items usable inside and outside battle.
- Game Over screen.
- Executable distribution with bundled Java runtime.

## V2 implemented features

### Data-driven enemies

- Complete immutable `CharacterStats` definitions.
- External enemy definitions in `data/enemies.txt`.
- `EnemyLoader`, `EnemyCatalog` and `EnemyDefinition`.
- Stable enemy and scenario IDs.
- Runtime `Enemy` instances created from reusable definitions.
- Enemy ranks: common, veteran, leader, boss, epic and legendary.

Enemy creation flow:

```text
enemies.txt
    -> EnemyLoader
    -> EnemyCatalog
    -> EnemyDefinition
    -> Enemy
```

### Data-driven encounters

- External encounter entries in `data/encounters.txt`.
- Entries grouped by scenario ID.
- Exploration-position ranges.
- Relative selection weights.
- Normal, special or unrestricted area availability.
- Per-entry maximum copies.
- Per-scenario minimum and maximum enemy counts.
- Weighted enemy-group generation.
- Groups containing one to three enemies.

### Linear exploration

- Exploration advances through numbered positions.
- Each combat scenario defines its exploration length and event chances.
- Scenario-specific messages for normal and special areas.
- Supported random events: nothing, battle, item, merchant and optional epic encounter.
- Merchant limited to one appearance per exploration session.
- Epic encounter limited to one offer per dungeon session.
- Exploration progress survives battles.
- Exploration sessions reset when a new combat scenario session begins.

### Scenarios and travel

- Scenario inheritance hierarchy with `Scenario`, `CombatScenario`, `Field`, `Dungeon` and `City`.
- `ScenarioCatalog` stores scenarios by stable ID.
- `TravelCatalog` stores directed routes between scenarios.
- `TravelMenu` supports multiple destinations.
- Combat-scenario travel remains locked until exploration is complete.
- Bree, Verdes Ermos and Gruta de Relamparrocha are connected.
- Scenario descriptions are displayed when entering a map and after returning from battle.

### Gruta de Relamparrocha

- Eleven-position dungeon session.
- Random normal and special-area encounters.
- Optional Relampagolem epic encounter.
- Epic encounter may be accepted or avoided.
- Mestre Eletrogoblin forced as the final boss.
- Fleeing from the boss returns the party to the previous position and keeps travel locked.
- Victory unlocks travel from the dungeon.

### External enemy drops

- External drop definitions in `data/enemy_drops.txt`.
- `EnemyDropLoader` and `EnemyDropCatalog`.
- Item lookup through stable IDs in `ItemCatalog`.
- Independent chance and quantity range for each drop.
- Multiple possible drops per enemy.
- Empty drop tables for enemies without registered drops.
- Equal items consolidated across all defeated enemies.
- Rewards granted from lists of defeated enemies.

Drop flow:

```text
enemy_drops.txt
    -> EnemyDropLoader
    -> EnemyDropCatalog
    -> Enemy
    -> BattleRewardService
    -> BattleRewards
    -> Party Inventory
```

### Party system

- `Player` renamed to `PlayableCharacter`.
- `Party` supports one to four playable characters.
- Inventory and gold belong to the party.
- HP, MP, SP, EXP, level and skills remain individual.
- Every living party member is controlled by the player.
- Defeated party members cannot act.
- Items may be used by one ally on another.
- Defeated party members may still receive EXP.
- Every party member receives the complete EXP reward.
- Gold and items are granted once to the party.
- Multiple level-up results are displayed compactly.
- Surviving characters fully restore HP, MP and SP after leveling up.
- Defeated characters remain defeated even after leveling up.
- Party and individual character status menus are available outside battle.
- Shared gold is displayed in the party status menu.

### Recovery skills

Supported recovery types:

- Healing.
- Revival.

Supported skill targets:

- Self.
- One ally.
- All allies.
- All allies except the user.
- One enemy.
- All enemies.

Current rules:

- Healing cannot target defeated allies.
- Revival targets only defeated allies.
- Skills do not consume resources when no valid target exists.
- Revived allies enter the initiative queue only in the following round.
- Temporary healing and revival skills remain hardcoded for V2 testing.

### Multiple enemies

- Battles support one to three enemies.
- Duplicate enemies receive numbered battle names.
- Defeated enemies remain visible and are marked as defeated.
- Defeated enemies cannot act or be targeted.
- Attacks, skills and analysis support enemy target selection.
- Rewards and equal item drops are consolidated across the enemy group.
- Enemy groups respect scenario limits and per-enemy copy limits.
- Enemies currently target a living ally using a simple selection rule.

### Battle initiative

- Allies and enemies share one unified round queue.
- Greater effective Speed acts first.
- Greater effective Luck resolves the first tie.
- Remaining ties are resolved randomly.
- Turn order is rebuilt at the beginning of every round.
- Speed changes affect only the following round.
- Participants may receive multiple actions according to Speed.
- Additional actions are distributed through decreasing initiative scores.
- Defeated future entries are skipped.
- The current and next participants are displayed.
- The `Round` action displays up to five queue entries without consuming an action.

### Battle escape

- The acting playable character performs the flee attempt.
- The first check uses effective Luck directly as a percentage.
- A failed first check triggers a comparative Speed and Luck calculation.
- Living enemy averages are used by the comparative check.
- Comparative chance has configured minimum and maximum limits.
- A failed attempt consumes the acting character's action.
- A successful attempt removes the entire party from battle.
- Fleeing from a dungeon boss returns the exploration session by one position.

### Battle rewards and progression

- Base EXP and gold come from the current combat scenario.
- Enemy rank applies its reward multiplier.
- Rewards from multiple enemies are aggregated.
- Every party member receives the complete EXP amount.
- Defeated party members also receive EXP.
- Excess EXP is preserved after leveling.
- Individual level progression is stored in `CharacterProgressionResult`.
- Surviving characters who level up fully restore HP, MP and SP.
- Defeated characters may level up but remain defeated.
- Gold and collected items are added once to shared party resources.

### World and UI refactoring

Responsibilities previously concentrated in `World` were extracted into dedicated components, including:

- `ScenarioActionMenu`
- `TravelMenu`
- `EpicEncounterMenu`
- `BattleCoordinator`
- `BattleRewardService`
- `BattleRewards`
- `BattleRewardScreen`
- `EnemyTargetMenu`
- `AllyTargetMenu`
- `ItemMenu`
- `BattleTurnOrderScreen`
- `PartyStatusMenu`
- `CharacterStatusMenu`

`World` coordinates scenario state, exploration events and the consequences of battle results. It no longer directly controls console input.

## Character attributes

The complete character attribute set is:

- HP — Health Points
- MP — Mana Points
- SP — Stamina Points
- ATK — Physical Attack
- DEF — Physical Defense
- MAG — Magic
- RES — Magic Resistance
- VEL — Speed
- LCK — Luck

Initial values are grouped in immutable `CharacterStats`.

Current HP, MP and SP remain inside `GameCharacter` because they change during gameplay.

Healing power is not a character attribute. Healing values belong to skills or items, while `GameCharacter.recoverHp()` applies recovery.

## Current limitations and temporary implementations

- The exploration `ITEM` event grants `ItemCatalog.MERUMA` directly.
- The merchant event has no merchandise or transaction system.
- Items are defined as constants in `ItemCatalog`.
- Scenarios, exploration settings, dungeon settings and travel routes are created in `Main`.
- Playable characters and skills are created in `Main`.
- Temporary healing and revival skills exist for V2 testing.
- Playable classes are not represented as data yet.
- Equipment, buffs and debuffs are not implemented.
- Bree does not yet provide its complete INN, shop and tavern functionality.
- Defeated allies require revival and remain unavailable between battles.
- Full-party defeat still uses the temporary Game Over flow.
- Save and load are not implemented.
- `Battle` remains a large class and is a candidate for later responsibility extraction.

## Current next step

Complete V2 release preparation:

1. Update project documentation.
2. Generate and test the V2 release build.
3. Create the V2 Git tag.
4. Open the V3 development branch.

The first V3 implementation goal is external scenario loading, removing Bree, Verdes Ermos and Gruta de Relamparrocha construction from `Main`.

## Important conventions

- Stable IDs use lowercase letters and underscores.
- Display names may be translated or changed without changing IDs.
- External data fields are separated by semicolons.
- Lines beginning with `#` are comments.
- Definitions describe reusable immutable data.
- Runtime objects represent individual mutable game instances.
- External references are validated during game initialization.
- Inventory and gold belong to `Party`.
- Character resources and progression belong to `PlayableCharacter`.
- Git operations are performed through Git Bash.
- Eclipse is used to edit, compile and run the Java project.