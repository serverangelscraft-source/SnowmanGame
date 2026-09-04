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

## Current P1 problems
### P1.1 — senior grades feel repeated
The day state machine is usable, but grades 7–11 reuse too much of the same lesson text and therefore feel like the same year with a different number.

Direction:
- keep the tested 6-stage school-day shell;
- move grade variety into data/content, not progression logic;
- grades 7–11 need distinct themes, questions and small story hooks;
- no extra currency, grind meter or mandatory extra taps.

Implementation started: `SchoolGradeContent.java` now contains distinct packs for grades 7–11. Next step is wiring it into `SchoolWeekActivity` without changing save keys/state transitions.

### P1.2 — cross-midnight / restart edge cases
`SnowSwimActivity` decides weekend status using the clock at completion time. A weekend activity started before midnight can finish after midnight and become non-countable. School stages also need explicit restart checks across midnight.

Direction:
- snapshot the counted school-life date/day-type when an activity starts;
- completion uses the snapshot, not a later clock value;
- never allow the same date to count twice.

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
- PrivateLabel&FMCG Master 2026 (Kyiv, 3–4 Sep) focuses on how everyday products move from maker to shelf → safe grade-9/10 mini-story `Від майстерні до полиці`: choose package shape, label clarity and delivery order for a fictional winter-school product; cosmetic/story only, no real retailer names.

## Next tasks
1. **P1** Wire `SchoolGradeContent` into grades 7–11 while leaving progression state untouched.
2. **P1** Fix cross-midnight snapshot logic for weekend/school completion.
3. **P1** Run restart/update checks at MORNING → LESSON1 → BREAK → LESSON2 → HOME → DINNER → weekend → year transition → grade-11 finish.
4. **P1** Run 320/360dp + short-height UI regression.
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
