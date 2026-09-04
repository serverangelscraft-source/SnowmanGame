# SnowmanGame — player return / mobile loop plan

Updated: 2026-09-04
Current Android baseline: v18.21 source line
Current focus: stable play from existing save through grade 11 with visible senior variety, restart-safe optional events and compact-phone usability.

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
- Dinner uses 3 visible bites, not 5 empty taps; old dinner saves clamp safely.
- Update-safe launcher/resume path and first-school-day persistence.
- Old-save repair reconstructs missing completed-year state from 5+2 counters.
- Deterministic 2→11 progression is guarded in CI.
- Snow-swim snapshots its start date and weekend type, and now also persists strokes + expected side across process death.
- Grades 7–11 are wired into the live school flow through `IntegratedSchoolWeekActivity` + `SchoolContentBridge`; they no longer depend on the generic visible lesson copy.
- Optional school integrations are wired into the live senior flow after lesson 2 without owning any school progression keys.
- Integration sessions now persist active grade/day/selected card. Process death resumes the unfinished event; intentional Back clears it as an optional skip; saving the memory clears the active session atomically.
- Narrow-phone integration headings and card labels use separate width constraints instead of shrinking the whole screen to card width.
- `МІСТО ДЛЯ СНІГОВИКА` is now in the grade 10–11 integration pool using the same memory-only framework.

## Current P1 problems
### P1.1 — runtime restart coverage
Static CI now guards school stage keys, snow-swim state and integration-session state. Still verify real device/process-death behavior at:
- MORNING;
- LESSON1;
- BREAK;
- LESSON2;
- optional integration (before and after choosing a card);
- HOME;
- DINNER;
- SnowSwim mid-session;
- year transition;
- grade-11 terminal state.

Rule: reopening must reconstruct the same logical place without replaying an earlier story scene or silently consuming a day.

### P1.2 — compact mobile regression
Verify 320–360dp widths and short usable heights for:
- snowman HUD/accessory tray;
- result card;
- dinner scene;
- senior-grade questions/options;
- integration cards;
- weekend snow-swim controls.

Rules:
- primary touch targets 48dp+;
- shorten copy before shrinking controls;
- no important control under system bars;
- one dominant action per scene.

Senior three-column answer labels are CI-limited to 26 characters. Runtime rendering still needs device/emulator verification.

### P1.3 — result-card vertical crowding
`MainActivity.drawFinish()` remains the next known compact-height risk. Rebuild it as one vertical flow instead of mixing top-anchored visitor copy with bottom-anchored progression/actions. Shorten optional copy first; do not shrink action targets below the mobile rule.

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
Use active Ukrainian business/culture activity as inspiration only. Never claim a partnership, copy campaign art/logos, or attach a real brand to paid/random rewards without permission. `Галичина` remains the explicitly approved in-world food reference.

### Current inspiration — September 2026
Already adapted into the integration system: craft/fashion workshop, school tech picnic, gift workshop, five-minute fair, maker-to-shelf project, break memory, winter yard, shadow-photo prompt, regional workshop and `МІСТО ДЛЯ СНІГОВИКА`.

`МІСТО ДЛЯ СНІГОВИКА` is inspired by current Ukrainian urban-design themes. In grade 10–11 the player chooses one courtyard improvement — `СВІТЛО`, `ЗАХИСТ ВІД ВІТРУ`, or `ЗРУЧНИЙ ПРОХІД`. It remains a fictional in-game school project, memory-only, with no organizer/developer branding and no economy reward.

## Next tasks
1. **P1** Fix `MainActivity.drawFinish()` short-height crowding.
2. **P1** Run 320/360dp + short-height checks for senior questions, integration cards, dinner and SnowSwim controls.
3. **P1** Runtime process-death pass across school/integration/SnowSwim states.
4. **P1** Keep latest build + `Validate school progression` green after every save/state change.
5. **P2** Add a lightweight graduation/path map after stability. No new currency or streak.
6. **P2** Remove/merge dead `WEEKEND_MINI` after runtime coverage is green.

## Working-state definition
A build is ready for normal play when all are true:
1. APK update/restart preserves location and progression;
2. origin intro and first-school intro are one-time;
3. grade can never regress;
4. 2→11 transitions are deterministic;
5. every school stage survives process restart;
6. optional integration survives process death or can be intentionally skipped without consuming progress;
7. cross-midnight completion cannot lose or double-count a day;
8. 11th grade reaches a terminal graduation state;
9. grades 7–11 are visibly different;
10. primary screens remain usable at 320–360dp widths;
11. core daily limits remain unchanged.
