# SnowmanGame — cycle update 2026-09-04 noon

## What moved forward
- Senior-grade content (7–11) was hardened for later wiring into the live school day.
- Answer sets no longer depend on `question.contains(...)`. Copy changes can no longer silently select the wrong answers.
- Added grade/day/lesson-specific `lessonTitle(...)` plus deterministic option tables for every senior school day.
- Correct senior answer remains index 0 by contract; the regression validator now protects that contract.
- Narrow-phone guard remains active: senior answer labels longer than 26 characters fail CI.

## New issue found
The visible `SchoolWeekActivity` still uses its legacy lesson/question/options methods. `SchoolGradeContent` is now safe enough to wire in, but that wiring is still the highest P1. Until it is connected, grades 7–11 remain mechanically playable but visually/textually too repetitive.

## Logic/retention review
Keep the current economy and pacing:
- 3 rewarded snowmen per local date, then unlimited free sculpting;
- at most 1 counted school-life day per date;
- no energy, paid retries, punitive streak or extra farming currency.

The return motive should remain curiosity. Senior-grade variety should come from changed situations, class themes and occasional visual memory objects, not from another meter.

## Mobile/UI direction
Before adding more screens:
1. wire senior content into the existing 3-column answer cards without changing state/save keys;
2. verify 320/360dp widths and short-height devices;
3. keep primary actions >=48dp;
4. shorten copy before shrinking controls;
5. show only one dominant action per scene.

## New pseudo-collaboration inspiration
Fresh September topic: Kyiv's Fast Food Industry exhibition (8–9 Sep 2026) focuses on food preparation, packaging, POS/software and compact service workflows.

Safe fictional adaptation: **`Шкільний ярмарок за 5 хвилин`** for grade 9/10. The class chooses one of three roles — prepare a simple winter snack concept, design a paper wrapper, or arrange a tiny serving counter. No real brands, no paid food, no purchase mechanic. Reward is only a scrapbook scene/photo. The useful design pattern is `short workflow + clear role + visible result`.

## Next tasks
- **P1** Wire `SchoolGradeContent.theme/dayHook/lessonTitle/question/options/correct` into `SchoolWeekActivity` while preserving all current state/save keys.
- **P1** Runtime process-death test at MORNING, LESSON1, BREAK, LESSON2, HOME, DINNER, year transition and grade-11 terminal state.
- **P1** 320/360dp regression for senior answer cards, dinner and snow-swim controls.
- **P2** After those pass, add a lightweight graduation/path view only; no new currency or streak.

## Working-state rule
Do not call the school path release-ready until 7–11 visible content is wired, every stage survives restart, and the grade-11 terminal state is confirmed on-device.
