# SnowmanGame — player return / mobile loop plan

Updated: 2026-09-03
Current Android baseline: v18.18
Latest post-baseline UI patch: realistic deruny rendering, CI green

## Current playable loop
- Build a snowman by rolling 3 balls and placing accessories.
- Eyes + carrot + any third accessory unlock completion; the remaining accessories are optional style/score.
- First 3 completed snowmen per local calendar day may award coins/progression; after that, unlimited free sculpting remains available.
- Daily mission, snow condition and visitor are stable for the active snowman. Midnight cannot change rules mid-run.
- Missing a day never deletes progress and there is no punitive streak.
- School life is player-paced: at most one counted school-life day per local calendar date.

## Confirmed stable systems — keep
1. **3 rewarded snowmen/day + unlimited free work.** Do not add a hard cap to creative sculpting.
2. **Daily mission anti-reroll.** Replay does not replace today's mission.
3. **Daily snow feel.** `ПУХКИЙ / МОКРИЙ / КРИЖАНИЙ` changes feel slightly, never enough to block play.
4. **Daily visitor.** One optional date-stable request, no extra farmable currency.
5. **Active-run snapshot.** Mission/snow/visitor remain fixed after the player starts a snowman.
6. **Expressive completion.** Do not restore the old mandatory 6/6 accessory gate.
7. **School pacing.** One counted day per date; skipped dates do not consume life progress.
8. **Pseudo-collaborations.** Fictional/inspired only; no fake logos or claims of real partnership.

## Recently fixed
### v18.13–v18.18 logic line
- closed unlimited coin/progression farming;
- closed mission rerolling;
- removed forced 6/6 decoration;
- added date-stable snow and visitor variety;
- fixed the midnight-open Activity conflict;
- split actual reward date from active challenge snapshot;
- clarified `+250 БАЛІВ` / `+150 БАЛІВ` so score is not confused with coins;
- removed real-retailer wording from fictional story integration.

### Dinner visual hotfix — 2026-09-03
- Player screenshot exposed that the deruny looked like three brown pellets.
- Replaced them with four flat overlapping golden potato pancakes with browned edges/highlights and sour cream.
- GitHub Actions build for commit `9831e932f2154d7bc2fc9eaf864717acab9e27f7` completed successfully.

## Newly confirmed problems
### P1 — dinner is still a weak game scene
The food drawing hotfix fixed the most obvious visual joke, but `drawDinner()` still has structural problems:
- the full plate remains visible even after all bites;
- five taps on the same place do not create meaningful interaction;
- white bite markers compete visually with the food;
- the snowman is detached from the table/plate;
- large fixed gaps create dead space on tall phones and poor composition on short phones;
- the header shows too many counters during a simple end-of-day action.

Implementation spec: `docs/DINNER_SCENE_REWORK.md`.

Target: **3 visible bites**, each changing the plate, then one dominant `ЗАВЕРШИТИ ДЕНЬ` action. Existing saves with `dinnerBites >= 3` must be treated as dinner-complete.

### P1 — snowman result-card responsive height
`drawFinish()` mixes top-fixed and bottom-fixed blocks. On short usable heights, visitor memory and year progress can overlap.

Fix direction:
- one top-to-bottom vertical flow;
- short-copy mode before shrinking controls;
- keep primary actions 48dp+;
- preserve hierarchy: score → reward/free state → mission → visitor → year progress → journey → replay.

### P1 — narrow-width regression
Verify 320–360dp widths for:
- snowman HUD;
- accessory tray;
- finish button;
- visitor line;
- result card;
- school dinner scene.

Shorten Ukrainian copy before shrinking touch targets.

### P2 — memory presentation
Expose visitor memories as a lightweight scrapbook. No currency, no streak loss, no mandatory menu before sculpting.

## Return-motivation rule
The reason to come back should be curiosity, not obligation. A new day may change one or two of these:
- snow feel;
- visitor/request;
- background/weather/light;
- one cosmetic material or regional pattern;
- one short story continuation.

Do **not** stack all of them every day. Too many daily systems make the game feel like chores.

## Daily limits
Keep:
- 3 rewarded/progression snowmen per local date;
- unlimited free sculpting afterward;
- at most 1 counted school-life day per local date.

Do not add:
- energy bars;
- lives consumed by mistakes;
- paid retries;
- mandatory daily streaks;
- a fourth daily farming currency.

## Phone UI principles
- one dominant action per scene;
- 48dp+ primary touch targets;
- no important control under system bars;
- use available height, not desktop-like fixed vertical gaps;
- reduce repeated counters during active play;
- food/objects should communicate state visually instead of requiring `5/5`, `3/3`, etc. everywhere.

## Safe pseudo-collaboration ideas
### Regional craft / contemporary craft
Current Ukrainian fashion activity around Ukrainian Fashion Week SS27 includes a strong focus on contemporary reinterpretation of craft. Safe adaptation: **`Зимова майстерня`** — occasional fictional makers bring a scarf/hat pattern inspired by a broad regional craft category. No copying specific garments, logos, collection imagery or claiming participation in Ukrainian Fashion Week.

### Urban design inspiration
A fictional **`Зимовий двір`** week can rotate tiny courtyard details — bench, lamp, mailbox, snow sculpture location — and ask the player to choose where the finished snowman is photographed. This changes composition/background only; no extra economy.

### Photographer / light-and-shadow prompt
Keep **`Тінь сніговика`** as an optional visitor idea: dusk lighting and a silhouette-friendly hat/scarf request, rewarded only as a scrapbook memory.

## Next tasks for implementation
**P1.1** Rework dinner from five empty taps to three visible bites; make portion disappear and connect hero + table + plate into one scene.

**P1.2** Simplify the school header specifically during DINNER so the player sees the meal and action, not four simultaneous counters.

**P1.3** Refactor snowman result card into a vertical responsive flow for short phones.

**P1.4** Run 320/360dp width and ~430–487dp usable-height regression checks before introducing any new mechanic.

**P2.1** Add optional scrapbook presentation only after the above phone regressions are clean.

## Definition of next working state
The next build should be considered a real improvement only when all of these are true:
1. dinner food visibly changes as it is eaten;
2. dinner takes at most 3 taps plus one final action;
3. deruny and the other dishes are recognizable without relying only on their text label;
4. dinner and result screens remain readable on 320–360dp widths and short usable heights;
5. no primary button is below 48dp;
6. the 3/day snowman reward rule and one-school-day-per-date rule are unchanged;
7. no new currency, streak punishment or fake brand partnership is introduced.
