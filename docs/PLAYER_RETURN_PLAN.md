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
- Snow-swim weekend sessions snapshot their start date/day type and persist that snapshot across process restart, so a Sunday session finishing after midnight is still evaluated as the Sunday it started on.
- An abandoned older snow-swim session cannot be counted after a newer school-life day; stale session metadata is cleared instead of corrupting chronology.
- Added a separate school regression workflow. It now blocks changes that remove stage save/restore keys, 2→11 reachability, grade-11 terminal state, senior content packs, or compact senior answer labels.

## Current P1 problems
### P1.1 — senior grades feel repeated
The day state machine is usable, but grades 7–11 still reuse the old visible lesson text because the new packs are not wired into `SchoolWeekActivity` yet.

Direction:
- keep the tested 6-stage school-day shell;
- move grade variety into data/content, not progression logic;
- grades 7–11 need distinct themes, questions and small story hooks;
- no extra currency, grind meter or mandatory extra taps.

Implementation state: `SchoolGradeContent.java` already contains distinct themes, 5 day hooks and two lesson questions per school day for grades 7–11. `tools/validate_restart_and_content.py` now protects those packs from silent regression. Next step remains wiring them into `SchoolWeekActivity` without changing save keys/state transitions.

### P1.2 — restart coverage inside the normal school day
Static coverage is now guarded in CI for stage and mini-state persistence, but device/process-death behavior still needs explicit runtime regression at:
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

A new CI guard rejects senior answer labels longer than 26 characters because three-column answer cards become unreadable first on narrow phones. Runtime rendering still needs device/emulator verification.

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
- School Nostalgia charity food initiative → fictional `Записка з перерви`, a tiny paper puzzle/memory after an ordinary school meal, without copying restaurant/artist branding.
- PrivateLabel&FMCG Master 2026 (Kyiv, 3–4 Sep): maker-to-shelf thinking → grade-9/10 mini-story `Від майстерні до полиці`, cosmetic/story only.
- DOU Day Picnic 2026 (Kyiv, 5 Sep): technology + workshops + community → fictional senior-grade `Шкільний технопікнік` with one optional station and a scrapbook memory.
- World of Gifts, Kyiv, 9–11 Sep 2026: handmade, author stationery, creative goods and gift wrapping → fictional `Шкільна майстерня подарунка`. The player chooses a handmade winter card, paper wrap or small classroom souvenir for a friend. It changes only the scrapbook composition; no shop, paid loot or real exhibitor branding.

## Next tasks
1. **P1** Wire `SchoolGradeContent` into grades 7–11 while leaving progression state untouched.
2. **P1** Run device/process-death checks for MORNING → LESSON1 → BREAK → LESSON2 → HOME → DINNER → year transition → grade-11 finish.
3. **P1** Run 320/360dp + short-height UI regression, starting with senior questions and snow-swim controls.
4. **P1** Keep the new `Validate school progression` workflow green; treat a failure as a release blocker.
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
8. grades 7–11 are meaningfully different in visible content;
9. primary screens remain usable at 320–360dp widths;
10. core daily limits remain unchanged.
