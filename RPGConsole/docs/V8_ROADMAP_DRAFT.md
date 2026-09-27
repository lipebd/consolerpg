# RPGConsole V8 Roadmap — Draft for discussion

> **Status: proposal, not approved implementation rules.** This document organizes work after the V7 release. It does not expand V7 scope. Mark a rule as frozen only after discussing its gameplay consequences and reviewing the affected code and data.

## Release aim and boundaries

Make enemy encounters vary meaningfully by location and enemy level, let enemies use physical/magical attacks and selected combat effects, and give them understandable action and target choices. Reuse the existing calculations and ordered skill effects where their semantics genuinely match.

V8 does not require a clever general-purpose AI. Keep enemy decisions explainable and avoid per-enemy strategy subclasses, threat histories, spatial positioning, behavior trees and generic event systems. `LEGENDARY` remains a reserved late-game concept; copying party skills is an unapproved idea for the final boss and is **outside the initial V8 scope**.

### Candidate implementation order

1. Enemy levels and per-encounter stat generation.
2. Reusable enemy skill headers, enemy-to-skill links and shared ordered effects.
3. Enemy action choice, execution and player feedback.
4. Individual target selection and a small set of behavior profiles.
5. Content migration, balancing, regression and release verification.

The order is intentional: target choice must know what action will be performed and whether it requires a physical, magical, ally, self, group or random target.

## 1. Enemy levels and encounter strength

### Working direction

* [ ] Replace provisional enemy numbers in `enemies.txt` with intentionally chosen **level-1 base attributes**; do not preserve current arbitrary values as a balancing contract. The level-1 meaning is approved as a design starting point, not yet a finalized set of base numbers.
* [ ] Define the growth rule and numerical safety range before changing existing content. Enemy level is gameplay power, can exceed the player level-20 cap and is not displayed to the player.
* [ ] Prototype the playable-character growth schedule with primary and secondary attributes at enemy levels 1, 20 and 40. Adopt or adapt its individual growth rates only after checking HP, damage, resources, speed and initiative in representative battles. Reuse the existing calculation only if the chosen enemy rule truly matches it; its current implementation validates playable levels and cannot directly accept higher enemy levels.
* [ ] Consider the effect of VEL growth on the current turn-order rule: large speed gaps can grant up to three actions per round. Test concrete low-, mid- and high-level encounters before adopting the player's growth values unchanged.
* [ ] Add `minLevel` and `maxLevel` to each applicable entry in `encounters.txt`, validate their range and draw the level for **each spawned instance** from the selected entry. Different copies of one enemy may have different levels. Encounter rules do not change the immutable species definition.
* [ ] Preserve encounter weight, position, area restriction and maximum-copy behavior. Carry the selected encounter entry and the drawn level through enemy creation; today encounters pass only enemy definitions to battle creation.
* [ ] Decide how boss, epic and scripted battles receive their level, since they may be created directly from enemy IDs rather than `encounters.txt`.
* [ ] Decide whether level affects EXP and GOLD. Today these rewards depend on the scenario and enemy rank; a stronger enemy in the same scenario would otherwise yield the same rewards. Review drops separately before introducing any level-based drop rule.
* [ ] Ensure resource maxima, stats, damage, initiative, action count and reward math cannot overflow or silently wrap at the highest supported enemy level.

**Open decisions:** formula for HP, MP, SP and the six attributes; primary/secondary choices in enemy data; safe enemy-level ceiling; levels for non-random encounters; EXP/GOLD adjustment. Do not add growth-related columns beyond those justified by the chosen formula.

## 2. Enemy skills using shared effects

### Working direction

* [ ] Investigate `enemy_skills.txt` for reusable enemy skill headers without playable class, acquisition type or per-enemy ownership. Skill name, resource costs and description remain available when needed for feedback.
* [ ] Investigate `enemy_skillsets.txt` to link enemy IDs to skill IDs and minimum enemy level. A skill can be shared by multiple enemy species without copying its effect lines.
* [ ] Reuse `skill_effects.txt` and its ordered, validated components for both playable and enemy skill IDs. Enforce unique skill IDs across both catalogs and reject effects without a matching skill header.
* [ ] Preserve the existing meaning of `SELF`, ally and enemy **relative to the acting side**: for an enemy user, their allies are enemies in battle, and their adversaries are party members. Avoid duplicating effect calculations and status logic.
* [ ] Review player-only assumptions in skill header validation, execution context, target selection, weapon snapshot, UI and skill-slot ownership before adapting shared execution. Preserve the playable six-slot and scroll contracts.
* [ ] Decide how enemy attacks obtain their base damage. Currently an enemy's basic attack uses zero skill power and zero weapon base damage; skill damage can add its own `DAMAGE` power. Compare leaving the weapon contribution at zero while tuning ATK/MAG, adding a base-damage value to the enemy itself, and relying on component power for individual skills. Do not fix DBA at zero or add an enemy stat before comparing normal-attack and skill damage across representative levels and enemy species. Power defined only in a shared enemy skill does not strengthen that species' basic attack.
* [ ] Decide which effects and target types enemies may author initially. Reusing a shared effect parser need not imply enabling every combination in enemy content immediately.

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

**Open decisions:** exact enemy skill header; link format; fixed versus variable loadouts; guaranteed skills and random subset behavior; initial supported effects; per-instance skill selection timing; resource eligibility; enemy DBA or equivalent base-attack power. The final boss's proposed party-skill copying is separate, later design work.

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

* [ ] Migrate enemy base stats, encounter level ranges and enemy skill assignments with concrete low-/mid-/late-game examples rather than filling every entry with placeholder values.
* [ ] Check an enemy species reused in early and late regions, two copies with distinct rolled levels, and all non-random battle entry points.
* [ ] Test resource exhaustion, fallback basic attack, enemy magic, buffs/debuffs, purification/dissipation, targeting each side, defeated targets, random loadouts and target persistence.
* [ ] Check progression pacing, initiative/action counts, EXP/GOLD, drops and normal/boss/epic battle outcomes. Preserve existing playable skill, inventory and save/load behavior.
* [ ] Update architecture, mechanics, data formats, class guides and project state after each approved block is implemented and verified. Do not describe deferred legendary abilities as implemented.
* [ ] Review the complete diff, package a portable release and check Unicode text and representative playthroughs before tagging V8.

## Questions to settle before the first V8 implementation

1. Starting from level-1 base attributes, do enemies use the same progression numbers as players after separating level validation, or a different growth schedule?
2. Which exact level range is safe and useful, especially for VEL and its extra actions?
3. How are boss, epic and scripted enemy levels assigned?
4. Does enemy level change EXP/GOLD, and by what bounded rule?
5. Which skills are guaranteed, and which may be randomly chosen per encounter?
6. What is the smallest usable enemy action-selection rule before tuning individual content?
7. Should an enemy contribute its own base damage to basic and skill attacks, or should ATK/MAG and per-skill power suffice? Compare actual results at several levels before deciding.
