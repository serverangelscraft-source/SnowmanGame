# SnowmanGame — player return / mobile loop plan

Updated: 2026-09-03
Current Android baseline: v18.19
Latest milestone: dinner changed from a static 5-tap form into a 3-bite visual evening scene

## Current playable loop
- Build a snowman by rolling 3 balls and placing accessories.
- Eyes + carrot + any third accessory unlock completion; the remaining accessories are optional style/score.
- First 3 completed snowmen per local calendar day may award coins/progression; after that, unlimited free sculpting remains available.
- Daily mission, snow condition and visitor are stable for the active snowman. Midnight cannot change rules mid-run.
- Missing a day never deletes progress and there is no punitive streak.
- School life is player-paced: at most one counted school-life day per local calendar date.
- School dinner now finishes in 3 visible bites plus one final action; food quantity changes on every bite.

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

### v18.19 — dinner scene rework
- Replaced five empty taps with **three visible bites**.
- `dinnerBites` is clamped to `0..3`, so old saves at 4/5 or 5/5 open safely as dinner-complete.
- The plate visibly loses food after each bite for all five dishes.
- Deruny remain flat golden potato pancakes with browned edges and sour cream; the old brown-circle rendering stays removed.
- Varenyky now read as crescent dumplings instead of generic ovals.
- Holubtsi now have wrapped-roll shape/seam cues.
- Borshch and banosh visibly shrink as the meal is eaten.
- Hero, table, plate and warm evening window are now one scene instead of detached objects on a white card.
- Dinner header is simplified and the long daily-rule sentence is hidden during active eating.
- Final action remains `ЗАВЕРШИТИ ДЕНЬ`; day-counting logic is unchanged.

## Current problems
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
- new dinner scene.

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

### School-food nostalgia inspiration — added 2026-09-03
A current Ukrainian charity food campaign uses school nostalgia, a limited food item and a small puzzle/memory object to make an ordinary snack feel like an event. Safe adaptation: **`Смак зі шкільної перерви`** — on rare school days, dinner or a break can reveal one fictional paper-note/puzzle memory after the normal interaction. No real restaurant names, logos, charity claims, purchases or random paid prizes. The useful pattern is *food + tiny memory*, not the campaign branding.

### Urban design inspiration
A fictional **`Зимовий двір`** week can rotate tiny courtyard details — bench, lamp, mailbox, snow sculpture location — and ask the player to choose where the finished snowman is photographed. This changes composition/background only; no extra economy.

### Photographer / light-and-shadow prompt
Keep **`Тінь сніговика`** as an optional visitor idea: dusk lighting and a silhouette-friendly hat/scarf request, rewarded only as a scrapbook memory.

## Next tasks for implementation
**P1.1** Refactor snowman result card into a vertical responsive flow for short phones.

**P1.2** Run 320/360dp width and ~430–487dp usable-height regression checks for the snowman result and the new dinner scene.

**P1.3** If dinner still looks crowded below ~430dp usable height, shorten subtitle/instruction copy before shrinking the plate or the 48dp action.

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

## Cycle update — 2026-09-03 evening
- Closed the largest school-dinner UX problem instead of adding another retention mechanic.
- Reduced dinner repetition from 5 taps to 3 and made every bite change the plate.
- Simplified the dinner HUD and turned the white-card layout into a warm table scene.
- Preserved existing saves and all daily limits.
- Next P1 is the snowman result-card height collision, then 320–360dp regression testing.
