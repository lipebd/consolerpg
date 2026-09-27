# Skill creation guide — RPGConsole

This guide turns a skill idea into entries in `data/skills.txt` and `data/skill_effects.txt`. It is for content creators: you do not need to know the game's Java classes. Each example is a separate recipe. Choose an unused ID, an appropriate playable class and balanced numbers before adding it to the actual data files.

## Start with the description

Write the action as steps: “increase your Luck; strike a random enemy three times; the third strike is stronger if its target is wounded.” Each step usually becomes a line in `skill_effects.txt`. Order matters: a boost before damage can strengthen that damage; a boost after damage prepares future actions.

| Phase | When it happens | Example |
|---|---|---|
| `PRE_ACTION` | Before the main action. | Raise ATK before striking. |
| `PRIMARY` | During the main action, in line order. | Strike, heal or apply a debuff. |
| `POST_ACTION` | After the user's turn ends. | Grant a buff that starts after the user's action. |

Use positive, unique, increasing sequence numbers. Gaps are allowed. Phases cannot go back from `PRIMARY` to `PRE_ACTION`, or from `POST_ACTION` to an earlier phase. By `POST_ACTION`, the user's older effects have already gone through their end-of-turn duration decrease.

## First file: `skills.txt`

```text
# id;classId;acquisitionType;levelRequirement;mpCost;spCost;name;description
example_echo;bardo;LEVEL;7;20;0;Eco de Exemplo;Atinge um inimigo com dano mágico.\nSe ele ficar ferido, reduz sua DEF.
```

| Column | What to write |
|---|---|
| `id` | Unique lowercase ID using letters, digits and `_`, such as `example_echo`. |
| `classId` | ID of an existing playable class. |
| `acquisitionType` | `LEVEL` to learn at the required level; `SCROLL` to learn from a compatible scroll. |
| `levelRequirement` | Acquisition level or minimum level for the scroll, within the game's level range. |
| `mpCost`, `spCost` | Nonnegative integer costs; enter `0` when there is no cost. |
| `name` | Name displayed to the player. |
| `description` | Player-facing explanation; write `\n` to add a line break. |

A `SCROLL` skill also needs a valid, unique link in `skill_scrolls.txt`; a `skills.txt` row does not create the scroll. Every skill needs at least one component that can affect a target: damage modifiers alone are insufficient. Before copying a `LEVEL` example, check the class's intended natural skill slots and levels.

## Second file: `skill_effects.txt`

```text
# skillId;sequence;phase;effectType;targetType;damageType;power;attribute;stages;duration;triggerChancePercent;specialStateType;conditionType;conditionValue
example_echo;1;PRIMARY;DAMAGE;SINGLE_ENEMY;MAGICAL;30;;;;;;NONE;
example_echo;2;POST_ACTION;ATTRIBUTE_STAGE;SINGLE_ENEMY;;;DEF;-1;2;;;TARGET_HP_LESS_THAN_PERCENT;51
```

Each line contains **14 fields**, including empty intermediate and final fields. Keep every `;`. All rows sharing a `skillId` form one skill, and that ID must exactly match an entry in `skills.txt`. The header above lists the fields in order. In the actual game data, player-facing names and descriptions remain in Brazilian Portuguese, as in the example.

| Column | Meaning |
|---|---|
| `sequence` | This component's execution order. |
| `phase` | `PRE_ACTION`, `PRIMARY` or `POST_ACTION`. |
| `effectType` | The action; see the effect table below. |
| `targetType` | Who receives it; see the target table. |
| `damageType` | `PHYSICAL` or `MAGICAL`, for `DAMAGE` only. |
| `power` | A number whose meaning depends on the effect: strength, percentage or number of attributes. |
| `attribute` | Attribute used for damage, healing or stages. |
| `stages` | Positive or negative stage count for `ATTRIBUTE_STAGE` only. |
| `duration` | Conventional stage duration; leave empty for VEL. |
| `triggerChancePercent` | Trigger chance from `1` to `100`; blank means `100`. |
| `specialStateType` | `DISTORTED_ATTRIBUTES` for `SPECIAL_STATE` only. |
| `conditionType` | One of the conditions below; choose `NONE` if unconditional. |
| `conditionValue` | HP percentage or attribute name used by the condition; blank with `NONE`. |

The allowed attributes, where applicable, are `ATK`, `DEF`, `MAG`, `RES`, `VEL` and `LCK`. Leave fields that do not belong to an effect blank; filling them “just in case” can cause loading to fail.

## Target types

| Value | Who receives the component |
|---|---|
| `SELF` | The skill user. |
| `SINGLE_ALLY` | One ally selected for the skill. |
| `ALL_ALLIES` | All eligible allies, including the user. |
| `ALL_BUT_SELF` | Other eligible allies; there may be none if the user is alone. |
| `RANDOM_ALLY` | One eligible ally randomly selected **for each component**; `REVIVE` draws from defeated allies. |
| `SINGLE_ENEMY` | One enemy selected for the skill. |
| `ALL_ENEMIES` | All eligible enemies; target conditions are checked separately for each enemy. |
| `RANDOM_ENEMY` | One living enemy randomly selected **for each component**. Different strikes may hit the same enemy. |

When a random strike defeats its target, the next strike draws among survivors. If no enemies remain, further strikes have no eligible target. A paired `PRIMARY` modifier and damage component share **one** draw; the modifier checks its condition against that target. A failed condition does not reroll. A random target reserved for a `POST_ACTION` component before ending the turn remains that component's target afterward.

## Effect types

| Value | What happens | Main fields |
|---|---|---|
| `DAMAGE` | Physical or magical damage. | `damageType`, positive `power`; optional offensive `attribute`. |
| `DAMAGE_POWER_MODIFIER` | Adds a percentage to compatible damage power. | Positive `power`; see positioning rules below. |
| `RECOIL_DAMAGE` | User loses a percentage of HP actually removed by the preceding strike, but cannot be knocked out. | `PRIMARY`, `SELF`, `power` from `1` to `100`. |
| `HP_SACRIFICE` | Sacrifices up to a percentage of the user's **current** HP; cannot leave them below 1 HP. | `PRIMARY`, `SELF`, `power` from `1` to `100`. |
| `HP_RECOVERY` | Restores HP scaled by user's effective ATK or MAG. | Positive `power`, `attribute=ATK` or `MAG`, ally target. |
| `MP_RECOVERY`, `SP_RECOVERY` | Restore a fixed amount of MP or SP. | Positive `power`, ally target; blank `attribute`. |
| `REVIVE` | Revives a defeated ally with HP scaled by ATK or MAG. | Positive `power`, `attribute=ATK` or `MAG`, ally target except `SELF`. |
| `HP_BALANCE` | Averages living allies' current HP and sets each HP up to that ally's individual maximum. | `PRIMARY`, `ALL_ALLIES`, blank `power` and no condition. |
| `ATTRIBUTE_STAGE` | Applies positive or negative attribute stages. | `attribute`; `stages` from `-3` to `3`, excluding zero; positive `duration` except for VEL. |
| `PURIFICATION` | Removes negative attribute stages from an ally. | Ally target; blank `power` for all affected attributes, or positive `power` for up to that many. |
| `DISSIPATION` | Attempts to remove positive attribute stages from an enemy; can be resisted. | Enemy target; blank `power` for all affected attributes, or positive `power` for up to that many. |
| `SPECIAL_STATE` | Applies the Ancestral Distortion special state. | `SINGLE_ENEMY`, `specialStateType=DISTORTED_ATTRIBUTES`. |

### Damage, healing and sacrifice

`PHYSICAL` defaults to ATK for offense and targets DEF; `MAGICAL` defaults to MAG for offense and targets RES. Setting `attribute` on `DAMAGE` changes **only the offensive attribute**: `MAGICAL` with `attribute=ATK` still targets RES. Listed power is not the final HP lost: effective attributes, defense, weapon and other effects affect the outcome. `HP_RECOVERY` and `REVIVE` remain restricted to `ATK` or `MAG` for scaling; MP and SP recovery remain fixed.

`HP_SACRIFICE` uses a percentage of **current** HP and is still a valid attempt at 1 HP, where it removes 0. It is different from recoil based on enemy damage. `HP_BALANCE` adds living allies' current HP, divides by the number of living allies and rounds to the nearest integer. Each living ally's HP is then set to that average, capped at **their own** maximum. With allies at `300/300` and `100/150`, the average is `200`: their final HP is `200/300` and `150/150`. Defeated allies are excluded. If no HP would change and this is the only applicable component, the skill does not make a valid attempt.

### Damage modifiers and recoil

A `PRE_ACTION` `DAMAGE_POWER_MODIFIER` prepares an increase for later compatible damage to the same target during this skill use. In `PRIMARY`, the modifier must appear **immediately before** `DAMAGE`, also `PRIMARY`, with the same `SINGLE_ENEMY` or `RANDOM_ENEMY` target type. That increase applies only to the paired strike. If the modifier's condition fails, the strike still hits, without the increase. Applicable modifier percentages add together; `50` means +50% power, not 50 final damage points.

`RECOIL_DAMAGE` must appear immediately after a `PRIMARY DAMAGE` against `SINGLE_ENEMY`, targeting `SELF`. `power=50` uses half of the HP **actually lost by the enemy**. No actual damage means no recoil. Recoil cannot knock out the user and may have its own trigger chance.

### Stages, cleansing and resistance

In `ATTRIBUTE_STAGE`, positive `stages` apply a buff; negative ones apply a debuff. Use `-1`, `-2`, `-3` for penalties and `1`, `2`, `3` for boosts. Every attribute except VEL requires a positive `duration`. VEL is pending for the next round's calculation, so leave its duration **blank**. A purification or dissipation with `power=2` randomly chooses up to two **affected attributes**, removing all stages of the relevant sign from each. Blank `power` removes all affected attributes. Debuffs and dissipation are resistible; dissipation rolls resistance once per target and component even if several attributes are removed. Purification, buffs, damage and recovery are not resisted. Ancestral Distortion is reversible under its own rules and is not removed by purification or dissipation.

`triggerChancePercent` is rolled **once per component** after its condition and targets allow an attempt. For multiple targets, each resistible target gets an individual resistance check. Failed triggers and resisted effects may still consume the turn and resources; a false condition or no applicable target does not constitute a valid attempt.

## Conditions

HP thresholds refer to maximum HP. Comparisons are **strict**: `<50` excludes exactly 50%; `>50` also excludes exactly 50%. To include exactly 50% and everything below it, use `<51`. With some maximum-HP values, that also admits fractional percentages between 50% and 51%.

| `conditionType` | `conditionValue` | When it passes |
|---|---|---|
| `NONE` | Blank | Whenever there is an applicable target and effect. |
| `SELF_HP_LESS_THAN_PERCENT` | `1` to `100` | User's HP is below the threshold. |
| `SELF_HP_GREATER_THAN_PERCENT` | `1` to `100` | User's HP is above the threshold. |
| `TARGET_HP_LESS_THAN_PERCENT` | `1` to `100` | Target's HP is below the threshold. |
| `TARGET_HP_GREATER_THAN_PERCENT` | `1` to `100` | Target's HP is above the threshold. |
| `SELF_GREATER` | `ATK`, `DEF`, `MAG`, `RES`, `VEL` or `LCK` | User's effective attribute exceeds that same effective target attribute. |
| `SELF_LOWER` | `ATK`, `DEF`, `MAG`, `RES`, `VEL` or `LCK` | User's effective attribute is less than that same effective target attribute. |

`TARGET_...` checks the target when **that component** executes: earlier damage can change the result. With `ALL_ENEMIES`, each enemy is evaluated separately. In `SELF_GREATER`/`SELF_LOWER`, `conditionValue` specifies **only the comparison**; `attribute` can independently specify the attribute used by the effect. `NONE` requires an empty `conditionValue`.

## Valid recipes — one skill at a time

The following are separate samples: each named `skillId` needs its own `skills.txt` entry, and any additional components must respect the same order rules. Values illustrate the format and are not balance recommendations.

**Magical damage using ATK and a debuff after the strike:**

```text
example_echo;1;PRIMARY;DAMAGE;SINGLE_ENEMY;MAGICAL;30;ATK;;;;;NONE;
example_echo;2;POST_ACTION;ATTRIBUTE_STAGE;SINGLE_ENEMY;;;DEF;-1;2;;;TARGET_HP_LESS_THAN_PERCENT;51
```

**Three independent draws; the third hit gets +30% if its chosen enemy is below 50%:**

```text
example_three_hits;1;PRIMARY;DAMAGE;RANDOM_ENEMY;PHYSICAL;20;;;;;;NONE;
example_three_hits;2;PRIMARY;DAMAGE;RANDOM_ENEMY;PHYSICAL;20;;;;;;NONE;
example_three_hits;3;PRIMARY;DAMAGE_POWER_MODIFIER;RANDOM_ENEMY;;30;;;;;;TARGET_HP_LESS_THAN_PERCENT;50
example_three_hits;4;PRIMARY;DAMAGE;RANDOM_ENEMY;PHYSICAL;20;;;;;;NONE;
```

**One-hit modifier against the selected enemy; the hit still happens when its condition fails:**

```text
example_finisher;1;PRIMARY;DAMAGE_POWER_MODIFIER;SINGLE_ENEMY;;50;;;;;;TARGET_HP_LESS_THAN_PERCENT;31
example_finisher;2;PRIMARY;DAMAGE;SINGLE_ENEMY;PHYSICAL;40;;;;;;NONE;
```

**Limited purification, limited dissipation and full purification** (three different skills):

```text
example_cleanse;1;PRIMARY;PURIFICATION;SINGLE_ALLY;;2;;;;;;NONE;
example_dispel;1;PRIMARY;DISSIPATION;SINGLE_ENEMY;;2;;;;;;NONE;
example_full_cleanse;1;PRIMARY;PURIFICATION;ALL_ALLIES;;;;;;;;NONE;
```

**HP sacrifice followed by a strike; HP balance for living allies** (two different skills):

```text
example_sacrifice;1;PRIMARY;HP_SACRIFICE;SELF;;50;;;;;;NONE;
example_sacrifice;2;PRIMARY;DAMAGE;SINGLE_ENEMY;MAGICAL;25;;;;;;NONE;
example_balance;1;PRIMARY;HP_BALANCE;ALL_ALLIES;;;;;;;;NONE;
```

**MAG-scaled healing, fixed SP recovery, recoil and pending VEL:**

```text
example_heal;1;PRIMARY;HP_RECOVERY;SINGLE_ALLY;;30;MAG;;;;;NONE;
example_breath;1;PRIMARY;SP_RECOVERY;ALL_ALLIES;;30;;;;;;NONE;
example_charge;1;PRIMARY;DAMAGE;SINGLE_ENEMY;PHYSICAL;20;;;;;;NONE;
example_charge;2;PRIMARY;RECOIL_DAMAGE;SELF;;50;;;;25;;NONE;
example_speed;1;POST_ACTION;ATTRIBUTE_STAGE;SELF;;;VEL;2;;;;NONE;
```

**Compare VEL while physical damage uses LCK; random revival; special state** (three separate skills):

```text
example_duel;1;PRIMARY;DAMAGE;SINGLE_ENEMY;PHYSICAL;25;LCK;;;;;SELF_GREATER;VEL
example_return;1;PRIMARY;REVIVE;RANDOM_ALLY;;30;MAG;;;;;NONE;
example_distortion;1;PRIMARY;SPECIAL_STATE;SINGLE_ENEMY;;;;;;;DISTORTED_ATTRIBUTES;NONE;
```

In the first case, `VEL` in `conditionValue` checks whether the user is faster than the target; `LCK` in `attribute` supplies the offensive value. The condition does not switch the offensive attribute.

## Rejected examples and choices to reconsider

**Rejected on loading** (row fragments showing only the relevant columns):

| Fragment | Why it is invalid | Fix |
|---|---|---|
| `HP_RECOVERY` with `attribute=VEL` | Scaled healing accepts ATK or MAG only. | Use `ATK` or `MAG`. |
| `DAMAGE` with `stages=2` | Stages belong to `ATTRIBUTE_STAGE`. | Make a separate buff component. |
| `ATTRIBUTE_STAGE` for `VEL` with `duration=2` | VEL duration must be blank. | Remove duration. |
| `ATTRIBUTE_STAGE` with `stages=0` | Zero is not a valid stage count. | Use `-3` to `-1` or `1` to `3`. |
| `HP_BALANCE` with `power=50`, target `SINGLE_ALLY` or a condition | Requires `ALL_ALLIES`, blank power and no condition. | Follow the single group-balancing rule. |
| `HP_SACRIFICE` with `power=0` or an enemy target | Requires a positive percentage up to 100 and `SELF`. | Use `SELF`, `PRIMARY`, power from `1` to `100`. |
| `PURIFICATION` with `power=0` | Power must be positive or blank. | Leave blank to remove all affected attributes. |
| `RECOIL_DAMAGE` without an immediately preceding `DAMAGE;SINGLE_ENEMY` | Recoil needs that hit's actual HP loss. | Place it immediately after the matching damage. |
| `PRIMARY;SINGLE_ENEMY` modifier followed by `RANDOM_ENEMY` damage | The adjacent pair must share the target type. | Make both `SINGLE_ENEMY` or both `RANDOM_ENEMY`. |
| `TARGET_HP_AT_MOST_PERCENT;50` | The old condition was replaced by strict comparisons. | Use `TARGET_HP_LESS_THAN_PERCENT;51` to include exactly 50%. |
| `NONE;50` at the end of a row | `NONE` must have a blank value. | End the row with `NONE;`. |
| A row with 13 or 15 `;`-separated fields | Every effect row needs exactly 14 fields. | Check empty columns and trailing separators. |

**May load, but needs thought:**

- A skill whose sole component uses `RANDOM_ALLY` with a target condition may draw an ally who fails that condition. It does not reroll, so the player may be able to try again without spending resources when nothing applies. Add an unconditional effect if this is not intended.
- A random hit followed by a `RANDOM_ENEMY` debuff draws **twice**: the debuff may hit another enemy. Use `SINGLE_ENEMY` when components must follow a selected target; for a one-hit bonus on a random hit, pair a `PRIMARY` modifier directly with `DAMAGE`.
- A `PRE_ACTION` `DAMAGE_POWER_MODIFIER` can strengthen multiple later compatible hits. If the description promises a boost only to the next hit, choose the `PRIMARY` pair.
- `POST_ACTION` follows duration reduction for earlier effects on the user; select the phase that matches the promised timing. VEL stays pending for the next round.
- If combining `HP_SACRIFICE` with other effects, describe a percentage of **current** HP. Do not promise fixed HP loss or damage equal to HP sacrificed; each effect has separate rules.
- `ALL_BUT_TARGET` is unavailable. For area damage use `ALL_ENEMIES`; for multiple individual hits, write separate components.

## Review checklist

- Do both files use the same unique ID, and does the class exist?
- Does a `SCROLL` skill have its scroll link? Does a `LEVEL` skill fit the intended progression?
- Does every effect row have 14 fields, including blanks? Are sequences increasing and phases nondecreasing?
- Are only relevant fields filled? Does each effect allow its chosen target?
- Does `DAMAGE` define a type and power? With a different offensive attribute, is `damageType` still the desired defense?
- Do `HP_RECOVERY` and `REVIVE` scale only from ATK or MAG? Is `attribute` blank for MP/SP recovery?
- Are stages in range, VEL duration blank and limited purification/dissipation powers positive?
- Is a `PRIMARY` modifier immediately before its damage with a matching target? Is recoil immediately after `SINGLE_ENEMY` damage?
- Do strict conditions describe the intended threshold? Does `NONE` have a blank value?
- Does the description accurately convey the number of hits, target selection and conditional effects?

After adding content, launch the game and test the skill's distinctive behavior in battle. If data loading fails, check IDs, field counts, target types, phases and required values first.
