# SnowmanGame — player return / mobile loop plan

Updated: 2026-09-04
Current Android baseline: v18.21
Current focus: make the game comfortably playable from current save through grade 11 without developer babysitting.

## Stable core — keep
- Build a snowman by rolling 3 balls and placing accessories.
- Eyes + carrot + any third accessory unlock completion; remaining accessories are optional style/score.
- First 3 completed snowmen per local date may award coins/progression; afterward sculpting remains unlimited and free.
- Daily mission, snow condition and visitor are stable for an active run; replay cannot reroll them.
- Missing a day never deletes progress and there is no punitive streak.
- School life counts at most one lived day per local calendar date.
- School year = 5 counted school days + 2 counted weekend days.
- School intro is one-time. Existing saves resume the real grade instead of returning to 1-A.
- APK restart/update routes through ResumeActivity and repairs known old-save progression flags before routing.

## Recently fixed
### v18.19
- Dinner reduced from 5 empty taps to 3 visible bites plus finish.
- Food visibly decreases; old 4/5 and 5/5 saves clamp safely to dinner complete.

### v18.20–18.21
- Update-safe launcher/resume path.
- No repeated snowflake origin intro for established players.
- Completed school intro no longer resets into first class.
- Current grade 2..11 is preserved.
- Save repair reconstructs a missing completed-year flag when counters are already 5/5 + 2/2.
- CI includes a deterministic 2→11 progression smoke test.
- Snow-swim weekend sessions now snapshot their start date/day type and persist that snapshot across process restart, so a Sunday session that finishes after midnight is still evaluated as the Sunday it started on.
- An abandoned older snow-swim session cannot be counted after a newer school-life day; stale session metadata is cleared instead of corrupting chronology.

## Current P1 problems
### P1.1 — senior grades feel repeated
The day state machine is usable, but grades 7–11 reuse too much of the same lesson text and therefore feel like the same year with a different number.

Direction:
- keep the tested 6-stage school-day shell;
- move grade variety into data/content, not progression logic;
- grades 7–11 need distinct themes, questions and small story hooks;
- no extra currency, grind meter or mandatory extra taps.

Implementation started: `SchoolGradeContent.java` contains distinct packs for grades 7–11. Next step is wiring it into `SchoolWeekActivity` without changing save keys/state transitions.

### P1.2 — restart coverage inside the normal school day
The weekend cross-midnight loss is closed, but every normal-day stage still needs explicit process-death regression coverage:
- MORNING;
- LESSON1;
- BREAK;
- LESSON2;
- HOME;
- DINNER;
- year transition;
- grade-11 terminal state.

Rule: reopening the app must reconstruct the same stage from SharedPreferences without replaying an earlier story scene or silently consuming a day.

### P1.3 — mobile regression
Verify 320–360dp widths and short usable heights for:
- snowman HUD/accessory tray;
- result card;
- dinner scene;
- senior-grade questions/options;
- weekend snow-swim buttons.

Rules:
- primary touch targets 48dp+;
- shorten copy before shrinking controls;
- no important control under system bars;
- one dominant action per scene.

## Motivation to return
Return should be curiosity, not obligation. A new day may change only one or two things:
- snow feel;
- visitor/request;
- background/weather/light;
- cosmetic regional pattern;
- short story continuation;
- occasional school memory object.

Do not stack all daily systems at once.

## Daily limits — keep
- 3 rewarded/progression snowmen per local date;
- unlimited free sculpting afterward;
- at most 1 counted school-life day per local date.

Do not add energy bars, paid retries, mandatory streaks, punishment for missed days, or another farming currency.

## Pseudo-collaboration rule
Use real Ukrainian business/culture activity only as inspiration. Never claim a partnership, copy protected campaign art/logos, or attach a real brand to paid/random rewards without permission.

### Current inspiration — September 2026
- Ukrainian Fashion Week SS27: reinterpretation of Ukrainian craft/handmade work → fictional `Майстерня орнаменту`, where a student chooses a scarf/hat pattern for a class photo.
- School Nostalgia charity food initiative → fictional `Записка з перерви`, a tiny paper puzzle/memory after an ordinary school meal, without copying the restaurant/artist branding.
- PrivateLabel&FMCG Master 2026 (Kyiv, 3–4 Sep): maker-to-shelf thinking → grade-9/10 mini-story `Від майстерні до полиці`, cosmetic/story only.
- DOU Day Picnic 2026 (Kyiv, 5 Sep) mixes technology talks, outdoor activities, workshops and community zones → safe fictional senior-grade event `Шкільний технопікнік`: choose one workshop station (build, design, presentation) and receive only a scrapbook memory/photo composition. No real event branding, ticketing or company logos.

## Next tasks
1. **P1** Wire `SchoolGradeContent` into grades 7–11 while leaving progression state untouched.
2. **P1** Add deterministic restart checks for MORNING → LESSON1 → BREAK → LESSON2 → HOME → DINNER → year transition → grade-11 finish.
3. **P1** Run 320/360dp + short-height UI regression, starting with senior questions and snow-swim controls.
4. **P1** Verify the new cross-midnight snow-swim snapshot build in CI and on-device update path.
5. **P2** Only after those pass: add a lightweight graduation map/progress view. No new currency or streak.

## Working-state definition
A build is ready for normal play when all are true:
1. APK update/restart preserves location and progression;
2. origin intro and first-school intro are one-time;
3. grade can never regress;
4. 2→11 transitions are deterministic;
5. every school stage survives process restart;
6. cross-midnight completion cannot lose or double-count a day;
7. 11th grade reaches a terminal graduation state;
8. grades 7–11 are meaningfully different in content;
9. primary screens remain usable at 320–360dp widths;
10. core daily limits remain unchanged.
