# RPGConsole V8 Roadmap — Draft for discussion

> **Status: active roadmap with individually frozen blocks.** The INN carryover and enemy-level block are implemented and verified. Enemy skills, action choice and target choice remain proposals until their gameplay rules are discussed and frozen.

## Release aim and boundaries

Make enemy encounters vary meaningfully by location and enemy level, let enemies use physical/magical attacks and selected combat effects, and give them understandable action and target choices. Reuse the existing calculations and ordered skill effects where their semantics genuinely match.

V8 does not require a clever general-purpose AI. Keep enemy decisions explainable and avoid per-enemy strategy subclasses, threat histories, spatial positioning, behavior trees and generic event systems. `LEGENDARY` remains a reserved late-game concept; copying party skills is an unapproved idea for the final boss and is **outside the initial V8 scope**.

### Candidate implementation order

1. [x] Resolve the known INN rest-display issue carried over from V7.
2. [x] Implement enemy levels and per-encounter stat generation.
3. [ ] Add reusable enemy skill headers, enemy-to-skill links and shared ordered effects.
4. [ ] Add enemy action choice, execution and player feedback.
5. [ ] Add individual target selection and a small set of behavior profiles.
6. [ ] Complete remaining content balancing, regression and release verification.

The order is intentional: target choice must know what action will be performed and whether it requires a physical, magical, ally, self, group or random target.

## V7 carryover: INN rest presentation

* [x] Resolve the V7 INN rest-result issue: animate the wrapped message and keep the contextual heading and result within the established screen bounds.
* [x] Verify the short already-rested feedback and the wrapped rest result at the supported terminal size. Preserve rest cost and resource-restoration behavior.

> **INN rest presentation completed and verified.** A successful rest opens a bounded contextual result screen with animated wrapped text. Enter reveals the complete message; a subsequent Enter returns to the INN. Short feedback remains in the menu. Payment and restoration retain their existing rules.

## 1. Enemy levels and encounter strength

### Frozen and implemented rules

* [x] Treat every numeric stat in `enemies.txt` as an intentional level-1 natural value. Use shared authoring profiles only as a balancing reference; keep the file as the single source of species data and use lore-specific base adjustments sparingly.
* [x] Give every runtime enemy a positive hidden level. Enemy levels may exceed the playable level-20 boundary and have no global gameplay cap.
* [x] Store independently optional primary, secondary and weakness attributes in each enemy definition. Every configured attribute must be distinct from the others.
* [x] Centralize enemy growth in `EnemyGrowthCalculator`. HP gains 10 per level; MP and SP remain fixed; ATK and MAG gain 3 per level; DEF, RES, VEL and LCK gain 1 per level; primary and secondary bonuses follow the existing two- and three-level schedules; weakness replaces ordinary growth with one point every two levels.
* [x] Apply rank stat multipliers after natural growth. Ranks modify HP, ATK and MAG while leaving other stats unchanged. `LEGENDARY` remains reserved without configured stat multipliers.
* [x] Add inclusive `minLevel` and `maxLevel` fields to encounter entries and roll every generated copy independently. Preserve position, area, weight, copy and group-size behavior.
* [x] Carry each selected definition and level through immutable `EnemySpawn` values until `BattleCoordinator` creates runtime enemies.
* [x] Give every dungeon a fixed narrative-boss level plus recurring-boss and epic level ranges. All enemies in one boss or epic group share the level selected for that encounter.
* [x] Use exact arithmetic and validate every encounter and dungeon reference at its highest configured level during initialization.
* [x] Rebase all current enemy species to level-1 stats and configure their approved optional specializations.
* [x] Rebalance and verify Verdes Ermos and Gruta Relamparrocha through normal campaign play, including the level-2 Mestre Eletrogoblin narrative encounter.

### Remaining balance work

* [ ] Decide whether enemy level changes EXP or GOLD. Current rewards continue to depend on scenario and rank, while drops remain independent entries.
* [ ] Validate and tune encounter ranges outside Verdes Ermos and Gruta Relamparrocha through representative regional play.
* [ ] Revisit high-level VEL, initiative and the usefulness of an authored content ceiling when later regions receive final level ranges.

> **Enemy levels and opening-region balance completed and verified.** The implemented formulas and rank multipliers are recorded in `MECHANICS.md`; external fields and validation rules are recorded in `DATA_FORMATS.md`.

## 2. Enemy skills using shared effects

### Working direction

* [ ] Investigate `enemy_skills.txt` for reusable enemy skill headers without playable class, acquisition type or per-enemy ownership. Skill name, resource costs and description remain available when needed for feedback.
* [ ] Investigate `enemy_skillsets.txt` to link enemy IDs to skill IDs and minimum enemy level. A skill can be shared by multiple enemy species without copying its effect lines.
* [ ] Reuse `skill_effects.txt` and its ordered, validated components for both playable and enemy skill IDs. Enforce unique skill IDs across both catalogs and reject effects without a matching skill header.
* [ ] Preserve the existing meaning of `SELF`, ally and enemy **relative to the acting side**: for an enemy user, their allies are enemies in battle, and their adversaries are party members. Avoid duplicating effect calculations and status logic.
* [ ] Review player-only assumptions in skill header validation, execution context, target selection, weapon snapshot, UI and skill-slot ownership before adapting shared execution. Preserve the playable six-slot and scroll contracts.
* [ ] Decide how enemy attacks obtain their base damage. Currently an enemy's basic attack uses zero skill power and zero weapon base damage; skill damage can add its own `DAMAGE` power. Compare leaving the weapon contribution at zero while tuning ATK/MAG, adding a base-damage value to the enemy itself, and relying on component power for individual skills. Do not fix DBA at zero or add an enemy stat before comparing normal-attack and skill damage across representative levels and enemy species. Power defined only in a shared enemy skill does not strengthen that species' basic attack.
* [ ] Decide which effects and target types enemies may author initially. Reusing a shared effect parser need not imply enabling every combination in enemy content immediately.

> **Frozen resource direction:** enemies retain finite MP and SP, and enemy skills consume their configured resources. The basic attack remains available without MP or SP cost and provides a safe fallback when no skill is usable. Exact skill costs, resource pools and action-selection frequency remain part of the enemy-skill balance pass.

### Proposed available-skill ceilings, including the ever-available basic attack

| Enemy rank | Maximum additional available skills, subject to discussion |
|---|---:|
| `COMMON` | 2 |
| `VETERAN` | 3 |
| `LEADER` | 4 |
| `BOSS` | 5 |
| `EPIC` | 6 |
| `LEGENDARY` | Deferred; no number approved. |

These are **maximums**, not required counts. Distinguish all skills registered for a species from the skills available to one spawned instance. If a boss can draw a subset from a larger pool, decide whether defining moves must be guaranteed and how many remaining places are randomized. Do not permit a random loadout to omit a move required by the encounter design.

**Open decisions:** exact enemy skill header; link format; fixed versus variable loadouts; guaranteed skills and random subset behavior; initial supported effects; per-instance skill selection timing; resource costs and pools; enemy DBA or equivalent base-attack power. The final boss's proposed party-skill copying is separate, later design work.

## 3. Enemy action choice

* [ ] Decide how an enemy chooses from basic attack and its currently available, usable skills. Action selection happens **before** target selection.
* [ ] Make unavailable resources, missing eligible targets and ineffective conditional actions safe: the enemy must be able to fall back to a valid basic attack instead of losing a turn accidentally or looping.
* [ ] Implement physical and magical damage through the existing damage rules. Apply buffs/debuffs, recovery, purification and dissipation through shared effect behavior only when their enemy-side targeting and feedback are defined.
* [ ] Respect the existing `PRE_ACTION`, `PRIMARY`, `POST_ACTION`, resistance, trigger, duration, pending VEL and defeat rules when enemies use applicable effects.
* [ ] Show the action and its result clearly to the player without exposing hidden enemy level, skill selection weights or target scores.

**Open decisions:** initial action-selection policy and any per-enemy tuning; whether enemy skills consume MP/SP as playable skills do; whether basic magical attacks exist separately from magical skills; selection frequency with multiple actions per round.

## 4. Enemy target choice

* [ ] Replace the current rule that always targets the living party member with the lowest **absolute current HP**.
* [ ] Study a small number of per-enemy behavior profiles: `INSTINCTIVE` (draw and hold), `PREDATOR` (prefer a wounded or unguarded target) and `TACTICAL` (also consider the defense relevant to the selected action). These names and weights are proposals, not a final TXT contract.
* [ ] Let each enemy instance keep its own chosen target while that target remains valid. Draw a new one if defeated; permit voluntary retargeting only when the profile's meaningful improvement rule is met.
* [ ] Compare lost-HP **percentage**, not raw HP, so a naturally low-HP class is not automatically the focus while healthy.
* [ ] Use effective DEF for physical attacks and effective RES for magical attacks when the selected profile examines defenses. Consider the current defend state without reproducing attribute-stage or damage formulas.
* [ ] Keep some uncertainty when a target is acquired, while making target persistence and retargeting understandable. Recheck cases where several enemies independently act in the same round.
* [ ] Keep `EnemyRank` independent from target behavior. Do not add historic damage, healing, critical hits, party roles, coordination, taunt or spatial positioning to the initial selector.

**Open decisions:** final profile list and weights; acquisition and retarget threshold; interaction with Defend, which currently remains active until its owner is hit; targets of non-damaging skills. The selector should serve single-opponent actions and avoid being forced into self, ally or area actions.

## 5. Content and release checks

* [x] Migrate all current enemy base stats and encounter level ranges away from their historical placeholder balance. Opening-region values are verified; later regional ranges remain preliminary.
* [ ] Assign enemy skills with concrete low-/mid-/late-game examples after the skill contract is frozen.
* [x] Check two copies with distinct rolled levels and the fixed narrative-boss entry point.
* [ ] Check one species reused in early and late regions plus recurring-boss and epic level-range entry points during their regional balance passes.
* [ ] Test resource exhaustion, fallback basic attack, enemy magic, buffs/debuffs, purification/dissipation, targeting each side, defeated targets, random loadouts and target persistence.
* [ ] Check progression pacing, initiative/action counts, EXP/GOLD, drops and normal/boss/epic battle outcomes. Preserve existing playable skill, inventory and save/load behavior.
* [ ] Update architecture, mechanics, data formats, class guides and project state after each approved block is implemented and verified. Do not describe deferred legendary abilities as implemented.
* [ ] Review the complete diff, package a portable release and check Unicode text and representative playthroughs before tagging V8.

## Questions remaining for later V8 blocks

1. What exact fields belong in enemy skill headers and enemy-to-skill links?
2. Which skills are guaranteed, and which may be randomly selected for one spawned enemy?
3. Which shared effects and target types are enabled in the first enemy-skill content pass?
4. Should an enemy contribute its own base damage to basic and skill attacks, or should ATK/MAG and per-skill power suffice?
5. What is the smallest explainable action-selection rule before individual tuning?
6. Which target profiles and retarget thresholds are frozen after action choice exists?
7. Before release, should enemy level adjust EXP or GOLD through a bounded rule?
