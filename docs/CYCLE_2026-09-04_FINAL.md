# SnowmanGame — final three-day cycle summary

Date: 2026-09-04

## Working direction confirmed
Keep the return loop based on curiosity rather than punishment:
- first 3 completed snowmen per local day may reward/progress;
- unlimited free sculpting remains after the reward cap;
- at most one counted school-life day per local date;
- no energy bar, paid retry, punitive streak or extra farming currency;
- daily snow/mission/visitor remain deterministic for an active run;
- missed days never erase progression.

## Fixed during the cycle
- Reward farming was capped without blocking free sculpting.
- Forced 6/6 decoration completion was removed; eyes + carrot + one more detail are enough, extra decoration is optional style.
- Daily snow condition and visitor requests became deterministic and restart-safe for an active run.
- Midnight rollover no longer swaps the active challenge context.
- School intro no longer resets first-day progress.
- Existing installs resume saved school state after app update/restart.
- Old inconsistent school saves self-repair 5+2 completion and grade state.
- School path 2→11 is deterministic and CI-guarded.
- Grades 7–11 have distinct lesson content instead of generic repeated copy.
- Dinner was rebuilt around 3 visible bites; deruny were redrawn as food rather than brown blobs.
- Approved in-world `ГАЛИЧИНА` sour-cream reference was added to the deruny dinner without claiming an official partnership.
- SnowSwim preserves its session date/type and now its stroke count + expected side across process death.
- Optional school integration sessions persist across process death, can be intentionally skipped, and never own school progression keys.
- Integration headings/card labels were adjusted for narrow phones.

## Integrations now in the live content pool
- Winter ornament workshop.
- School tech picnic.
- Gift workshop.
- Five-minute fair.
- Maker-to-shelf project.
- School-break memory.
- Winter yard.
- Snowman shadow photo.
- Regional workshop.
- `МІСТО ДЛЯ СНІГОВИКА` for grades 10–11: choose light, wind protection or an easier passage; memory-only and no real-partnership claim.

## Keep
- 3 rewarded builds/day + unlimited free sculpting.
- One counted school day/date.
- Deterministic daily curiosity.
- Optional visitor with no economy reward.
- 5 school days + 2 weekend days per grade.
- Senior-grade variety and memory-only integrations.
- Real brands only when explicitly approved; no fake official collaborations.

## Remove / do not add
- Punitive streaks.
- Energy meters.
- Paid retries.
- Random paid reward loops.
- A new currency for integrations.
- Repeated generic senior-school questions.
- Dead/redundant school branches once stability work is complete.

## Remaining P1
1. Rebuild `MainActivity.drawFinish()` into one vertical flow for short 430–487dp usable heights. Current implementation still mixes top-anchored result/visitor blocks with bottom-anchored progression/actions.
2. Real-device/emulator process-death pass for MORNING, LESSON1, BREAK, LESSON2, integration, HOME, DINNER, SnowSwim, year transition and grade-11 terminal state.
3. 320/360dp and short-height visual pass for snowman HUD/tray, result card, dinner, senior answers, integration cards and SnowSwim controls.
4. Remove/merge dead `WEEKEND_MINI` only after runtime stability is confirmed.

## Working-state definition after this cycle
Use the latest commit only after both `Build APK` and `Validate school progression` are green. The current code architecture is acceptable for normal testing when update/resume, grade 2→11, integration-session recovery and SnowSwim recovery all remain green. Do not call the UI final until the `drawFinish()` compact-height issue and real-device 320/360dp pass are closed.
