# RPGConsole V8 Roadmap — Draft for discussion

> **Status: active roadmap with individually frozen blocks.** The INN carryover, enemy levels, reusable enemy skills, initial enemy action choice and individual target selection are implemented and verified. The contributed Bestiary has also been adapted to the current V8 architecture. Later combat-intelligence refinements, remaining balance and release verification remain pending.

## Release aim and boundaries

Make enemy encounters vary meaningfully by location and enemy level, let enemies use physical/magical attacks and selected combat effects, and give them understandable action and target choices. Reuse the existing calculations and ordered skill effects where their semantics genuinely match.

V8 does not require a clever general-purpose AI. Keep enemy decisions explainable and avoid per-enemy strategy subclasses, threat histories, spatial positioning, behavior trees and generic event systems. `LEGENDARY` remains a reserved late-game concept; copying party skills is an unapproved idea for the final boss and is **outside the initial V8 scope**.

### Candidate implementation order

1. [x] Resolve the known INN rest-display issue carried over from V7.
2. [x] Implement enemy levels and per-encounter stat generation.
3. [x] Add reusable enemy skill headers, enemy-to-skill links and shared ordered effects.
4. [x] Add enemy action choice, execution and player feedback.
5. [x] Add individual target selection and a small set of behavior profiles.
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
* * [x] Centralize enemy growth in `EnemyGrowthCalculator`. HP gains 10 per level; MP and SP each gain 5 per level; ATK and MAG gain 3 per level; DEF, RES, VEL and LCK gain 1 per level; primary and secondary bonuses follow the existing two- and three-level schedules; weakness replaces ordinary growth with one point every two levels. MP and SP do not receive rank, primary-attribute or secondary-attribute bonuses.
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

### Frozen and implemented rules

* [x] Store reusable enemy skill headers in `enemy_skills.txt` with stable ID, MP cost, SP cost and player-facing name. Enemy skills do not define playable class, acquisition type, level requirement, description or skill-slot ownership.
* [x] Introduce `CombatSkillDefinition` as the shared execution contract implemented by playable and enemy skill definitions without weakening the playable-only progression and six-slot contracts.
* [x] Store enemy-to-skill links in `enemy_skillsets.txt` with enemy ID, skill ID and positive minimum enemy level. One skill may be shared by multiple species.
* [x] Reuse ordered `skill_effects.txt` components across both skill catalogs. Skill IDs are globally unique, and every effect must reference exactly one known playable or enemy skill.
* [x] Reject duplicate headers, duplicate enemy-skill links, unknown references, enemy skills without any species link and effectless skills during initialization.
* [x] Validate that each species can pay every linked skill at that link's `minimumLevel`. Resource validation uses the same natural-stat calculation used to create runtime enemies.
* [x] Build the eligible skill pool from links whose minimum level is not greater than the spawned enemy level.
* [x] Select each runtime enemy's loadout without repetition and cap it through `EnemyRank`. The basic attack remains separate and does not occupy a skill position.
* [x] Preserve `SELF`, ally and enemy meanings relative to the acting side. Enemy users treat other enemies as allies and party members as adversaries.
* [x] Generalize `SkillExecutionContext`, `SkillExecutor` and `SkillUseState` only as far as required to execute the shared `CombatSkillDefinition` contract with any `GameCharacter`.
* [x] Keep enemy weapon base damage at zero. Enemy skill damage uses the component's declared `power`, ATK or MAG and the existing physical or magical defense formula.
* [x] Keep enemy basic attacks physical, using ATK against DEF with zero weapon base damage. Magical enemy identity is expressed through skills; ATK therefore remains relevant even for primarily magical species.
* [x] Restrict the initial authored enemy content to clear `SINGLE_ENEMY` and `SELF` uses of damage, attribute stages and self-recovery. Shared engine support does not require every playable effect or target combination to appear in the first enemy content pass.
* [x] Give every current enemy species at least one skill. The first content pass contains 83 reusable headers, 90 enemy-skill links and 123 ordered effect components across 47 species.

### Runtime skill ceilings and current authored content

| Enemy rank | Runtime maximum | Current first-pass content |
|---|---:|---:|
| `COMMON` | 2 | 1–2 |
| `VETERAN` | 3 | 2–3 |
| `LEADER` | 4 | 2–3 |
| `BOSS` | 5 | 3 |
| `EPIC` | 6 | 3 |
| `LEGENDARY` | Deferred | None |

The runtime values are maximum additional skills and do not include the ever-available basic attack. Current content intentionally stays at or below those ceilings. No current species requires guaranteed-move handling because its authored pool does not exceed its runtime maximum.

Common enemies with one skill use a damaging move. A two-skill common combines damage with self-buffing or self-recovery. Veterans and leaders have at least two skills; three-skill definitions include self-support. Epic and boss enemies currently use two damaging skills plus one self-buff or self-recovery skill.

Related species reuse appropriate general techniques without losing their specialized actions. Current examples include goblins, herbalísticos, lobermos, crustaceans and mine-security robots.

> **Enemy skills completed and verified.** Header loading, shared effects, minimum-level links, rank ceilings, per-instance loadouts, global ID validation, resource-capacity validation and current content coverage are implemented. Later content may use higher minimum levels or pools above the runtime ceiling only after guaranteed-move behavior is explicitly designed.

## 3. Enemy action choice

### Frozen and implemented rules

* [x] Choose the enemy action before resolving its targets.
* [x] Keep the basic attack permanently available as action index zero.
* [x] Filter linked skills by the acting enemy's current MP and SP before action selection.
* [x] Select uniformly between the basic attack and every currently affordable skill. The initial implementation has no action weights, priorities, memory or species-specific decision code.
* [x] Build enemy-side execution targets relative to the acting side and let the shared executor resolve random effect targets where applicable.
* [x] Begin execution before spending resources. If the selected skill cannot produce a valid attempt, preserve MP, SP and the turn, then execute the basic attack as fallback.
* [x] Spend MP and SP only after the shared execution state confirms that the action can consume the turn.
* [x] Reuse shared damage, recovery, resistance, attribute-stage, duration, pending-VEL, trigger and ordered-phase behavior without duplicating combat formulas.
* [x] Preserve defeats caused by enemy primary and post-action effects, including the existing battle-ending and zero-HP feedback rules.
* [x] Present the enemy's selected skill name followed by the shared formatted effect results.
* [x] Keep playable skill execution unchanged through the same generalized contract.
* [x] Keep the current individual enemy-target rule unchanged until the dedicated target-choice block. Initial action selection does not inspect HP thresholds, defenses or tactical usefulness.

The simple selector prevents invalid turns but does not attempt to choose the best action. A recovery skill may be selected while ineffective and fall back to a basic attack; while applicable, it competes uniformly with the other affordable actions. Contextual usefulness, action weights and species-specific preferences remain later refinements rather than hidden exceptions.

Representative testing covered enemy physical skill damage, MP/SP consumption and exhaustion, playable-skill regression, self-buffing, resistible enemy debuffs, pending VEL, multiple skills in one loadout, fallback behavior and startup rejection of invalid cross-catalog content. Self-recovery and pools above a rank ceiling are structurally covered and may receive additional opportunistic gameplay observation without blocking this block.

> **Initial enemy action choice completed and verified.** Individual target selection remains the next independent combat block.

## 4. Enemy target choice

* [x] Replace the absolute-lowest-HP rule with the external `EnemyTargetProfile` contract.
* [x] Implement `INSTINCTIVE`, `ERRATIC`, `PREDATOR` and `TACTICAL` without coupling behavior to `EnemyRank`.
* [x] Give every runtime enemy independent target memory. Instinctive enemies hold a uniformly drawn target until defeat; erratic enemies redraw uniformly before every individual action.
* [x] Score Predator acquisition with a positive base, twice the missing-HP percentage and a penalty against defending targets for damaging actions.
* [x] Score Tactical acquisition with missing-HP percentage, the relevant effective DEF/RES vulnerability and the same defending penalty.
* [x] Treat acquisition scores as relative weights rather than deterministic maximum selection.
* [x] Keep Predator and Tactical targets until the best alternative exceeds the current score by more than the frozen retarget margin.
* [x] Ignore DEF/RES for non-damaging or mixed-damage actions while still considering Defend for mixed damage.
* [x] Preserve existing `SELF`, ally, group and explicitly random target resolution outside the individual selector.
* [x] Assign one approved profile to every current enemy species, including the fourth `ERRATIC` personality requested during freezing.
* [x] Expose the player-facing target personality in enemy analysis without exceeding its fixed 17-line layout.

> **Enemy target choice completed and verified.** Compilation and deterministic coverage validate loaders, all four profiles, independent memory, defeated-target invalidation, weighted acquisition, retarget margins, Defend, effective DEF/RES, attribute stages and ties. The current 47 species use 12 `INSTINCTIVE`, 15 `ERRATIC`, 9 `PREDATOR` and 11 `TACTICAL` profiles.

## Integrated Bestiary contribution

* [x] Adapt the contributed Bestiary to the current V8 source instead of replacing newer classes from its older branch.
* [x] Reuse `Battle.getAnalyzedEnemyIds()` so one confirmed analysis feeds both quest progress and bestiary discovery without duplicating analysis rules.
* [x] Add campaign-level `BestiaryStateCatalog` and immutable `BestiarySnapshot` using canonical enemy IDs.
* [x] Add the Bestiary as a paginated third section of `Diário`, hiding undiscovered names and presenting rank, target personality, optional specializations, optional weakness, description, height and weight for discovered species.
* [x] Preserve the current enemy stats, growth attributes, skill data and target profiles while importing only the contributed height and weight values.
* [x] Evolve the strict internal save payload to version 4 and validate restored bestiary IDs through `EnemyCatalog` before activation.
* [x] Keep the encrypted outer-container version unchanged.

> **Bestiary integration completed and verified.** The complete source compiles, all 47 enemy definitions retain their V8 combat fields and receive positive measurements, menu pagination remains within the 17-line interface boundary, and bestiary properties complete a strict serialization round trip.

## 5. Content and release checks

* [x] Migrate all current enemy base stats and encounter level ranges away from their historical placeholder balance. Opening-region values are verified; later regional ranges remain preliminary.
* [x] Assign enemy skills with concrete low-/mid-/late-game examples after the skill contract is frozen.
* [x] Check two copies with distinct rolled levels and the fixed narrative-boss entry point.
* [ ] Check one species reused in early and late regions plus recurring-boss and epic level-range entry points during their regional balance passes.
* [x] Test representative enemy damage, resource consumption and exhaustion, basic-attack fallback, magical damage, self-buffs, resistible debuffs, pending VEL and playable-skill regression.
* [ ] Expand representative regional playtesting for the complete first-pass enemy-skill content, including later bosses and epics.
* [ ] Revisit purification, dissipation, ally-targeting and other shared combinations only if later enemy content actually requires them.
* [ ] Check progression pacing, initiative/action counts, EXP/GOLD, drops and normal/boss/epic battle outcomes. Preserve existing playable skill, inventory and save/load behavior.
* [ ] Update architecture, mechanics, data formats, class guides and project state after each approved block is implemented and verified. Do not describe deferred legendary abilities as implemented.
* [ ] Review the complete diff, package a portable release and check Unicode text and representative playthroughs before tagging V8.

## Questions remaining for later V8 blocks

1. How should the selector evaluate contextual usefulness, if action intelligence is expanded beyond uniform choice?
2. When a future authored pool exceeds its rank ceiling, which moves must be guaranteed and which positions may be randomized?
3. Before release, should enemy level adjust EXP or GOLD through a bounded rule?
4. Which later shared effect and target combinations are justified by concrete enemy content?
