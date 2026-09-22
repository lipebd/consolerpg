# RPGConsole V7 Roadmap

> **General scope checklist:** The release boundary and order below are proposed for approval. Each part receives its detailed behavior and implementation decisions when that part begins. A checked box means the described work has been implemented and verified, except in the explicitly study-only part.

## Release goal

V7 polishes the released V6 game: readable and consistent console screens, clearer battle feedback, smoother transitions, and selected additions to the existing item and skill effects. Enemy decision making is studied during V7 but is not implemented in this release.

Continue using the existing UI, battle, item and skill responsibilities. Reuse methods with the same semantics before adding another one. Add a Java class only when an independent responsibility cannot reasonably live in the existing structure. Keep code and technical identifiers in English and player-facing text in Brazilian Portuguese.

The checklist fixes **what belongs to V7**, not every rule for implementing it. At the start of each part, examine only its relevant code, discuss alternatives and consequences, freeze its detailed contract, then implement and test it. New requests discovered during a frozen part go to the end of the queue unless they block or critically break that part. Update affected documentation after the part's behavior is approved.

## 1. Shared text and screen transitions

* [ ] Use the existing `ConsoleUI` text-wrapping behavior for player-facing prose and feedback throughout the UI and `World`/`Battle` presentation paths.
* [ ] Keep intentional paragraph breaks in external text; remove manual line breaks used solely to fit a long sentence within the screen width.
* [ ] Preserve the layout of numbered options, aligned columns, tables and prompts while wrapping prose around them.
* [ ] Audit screen-clearing and flushing order so a transition does not erase its own heading or content.
* [ ] Verify representative long text, intentional paragraphs and one aligned menu after the change.

## 2. Consistent location headings

* [ ] Define which screens have a current city, a current local area, or no meaningful location context.
* [ ] Show the applicable `[CIDADE]` and `[LOCAL]` headings consistently on world, service, narrative and battle-related screens.
* [ ] Add missing headings and correct headings shown in the wrong position or with the wrong context.
* [ ] Preserve useful screen titles such as inventory, quest and shop titles beneath the location context where applicable.
* [ ] Check representative city, facility, narrative, exploration and battle screens, including screens without a city context.

## 3. City destinations

* [ ] Display functional city destinations and quest/narrative locations in two columns on the existing city-destination screen.
* [ ] Include the currently available shop, crafting and INN destinations on the functional side; allow the same presentation to accommodate a future trading post without implementing one in V7.
* [ ] Preserve one selection flow, stable option-to-destination mapping, visibility requirements and actionable quest markers.
* [ ] Handle one empty column and long or varying destination names without breaking the menu layout.
* [ ] Verify destination selection, return, hidden locations and quest markers.

## 4. Dialogue presentation

* [ ] Keep dialogue screens at a stable height with a space for up to three wrapped text lines per spoken segment, plus consistent spacing around headings, speaker and action prompt.
* [ ] Decide how dialogue longer than that space is presented without truncating or losing text.
* [ ] Animate dialogue text one character at a time while preserving accents, symbols, manual paragraph breaks and wrapping.
* [ ] Define the behavior of Enter during and after animation so dialogue advances predictably and does not accidentally skip a segment.
* [ ] Verify narrator and named-speaker segments, short and long segments, and the transition between consecutive segments.

## 5. Battle screen and action feedback

* [ ] Keep the current enemy and party HP details visible and consistent while battle actions and their outcomes are presented.
* [ ] Show an explicit defeat message when a participant reaches zero HP while the battle continues.
* [ ] Replace the action menu with the current action's messages during resolution instead of extending a running log below it.
* [ ] Reuse the existing battle-status formatting and coordinate redraw/flush behavior without a separate cursor-positioning mechanism.
* [ ] Review the presentation and input responsibilities currently inside `Battle.java`; move them to `rpg.ui` only where the battle-screen changes give the extracted code a clear responsibility and avoid duplicating existing menus or formatters.
* [ ] Show active buffs and debuffs on the attack target-selection screen.
* [ ] Identify which effect was resisted in resistance feedback.
* [ ] Suppress the player-facing message for an effect that did not trigger, without changing the effect's rules or resource/turn consumption.
* [ ] Verify one continuing battle with a defeated enemy or ally, multi-effect skill output, target selection and the final battle result.

## 6. Skill presentation and focused correction

* [ ] Display empty skill slots alongside learned and automatically acquired skills instead of a list containing only unlocked skills.
* [ ] Show attribute-stage changes in skill descriptions as stage arrows, so `VEL +1` is not mistaken for one attribute point.
* [ ] Diagnose and correct the observed behavior of Grito de Guerra after its expected and actual outcomes are pinned down.
* [ ] Verify a character with unfilled slots, a character with learned scroll skills, a stage-modifying skill and Grito de Guerra.

## 7. Travel, saving and text polish

* [ ] Add a brief transition during travel that communicates movement without changing travel cost, destination or world-state rules.
* [ ] Add a brief saving transition without changing when a save succeeds, fails or becomes persistent.
* [ ] Review player-facing dash usage and remove accidental string concatenations such as `"texto" + "texto"` in the touched presentation paths.
* [ ] Verify travel cancellation and completion, successful saving and saving failure, and the resulting screens.

## 8. Additional consumable and skill effects

* [ ] Select the specific additional consumable effects for V7 and freeze their rules before implementing them.
* [ ] Select the specific additional skill effects for V7 and freeze their rules before implementing them.
* [ ] Extend the existing definition, validation, execution and presentation paths only as required by the approved effects.
* [ ] Verify each distinct new effect behavior and relevant failure boundary without repeating the same execution path for every item or skill.

This part reserves room for selected new effects; it does not approve an unlimited effect catalog. Enemy action selection and enemy use of magic, buffs or debuffs are handled as a study in Part 9.

## 9. Enemy decision-making study — no V7 implementation

* [ ] Map the current enemy turn, available attacks, targeting rules and battle-effect execution paths.
* [ ] Compare practical approaches to target selection, target persistence and attack/action selection for a deliberately simple enemy AI.
* [ ] Study what enemy magical attacks, buffs and debuffs would require in definitions, execution, feedback and validation.
* [ ] Record a bounded recommendation, implementation order and open decisions for a later version.
* [ ] Make no enemy-AI or enemy-action expansion in V7 as a consequence of this study alone.

## 10. V7 verification, documentation and release

* [ ] Run focused tests when each part closes, covering distinct branches and boundaries rather than every data combination.
* [ ] Run representative regressions for city navigation, dialogue, travel, saving, combat, items and skills after their affected parts close.
* [ ] Update the roadmap and only the architecture, mechanics, data-format and project-state documents actually affected by the approved behavior.
* [ ] Review the finished V7 diff for duplicate responsibilities, unintended temporary code and unrelated changes.
* [ ] Build and check the portable Windows package, including Unicode presentation and an extracted-package playthrough of the changed flows.
* [ ] Prepare the V7 release notes, tag and publish the release after the final verification and documentation are approved.
