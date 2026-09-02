# SnowmanGame — player return / mobile loop plan

Updated: 2026-09-02
Current Android build: v18.10

## Current state
- Core snowman construction loop is playable: roll 3 balls, place 6 accessories, score build/decor, complete one random mission, receive coins.
- School-week branch is player-paced and the DONE/dinner phone layouts were recently enlarged.
- A separate HTML mobile prototype explored a useful order loop: 3 paid jobs per day, unlimited free sculpting, 3 client choices and snow conditions.

## Confirmed problems
1. **Unlimited coin farming in MainActivity.** Every replay resets `coinsAwarded=false`, so every completed snowman can award coins again. There is no per-calendar-day paid reward cap.
2. **Prototype daily counter is not actually daily.** `snowman_completed_today` is stored without a date key/reset, so after reaching 3 the paid quota can stay exhausted on following days.
3. **Random mission has weak return motivation.** It changes per replay, not per day; therefore it feels like rerolling rather than a daily reason to come back.
4. **Completion requires all 6 accessories.** This makes every run structurally similar and reduces expressive builds; it also makes speed missions partially artificial.
5. **Top HUD prioritizes year/coins but does not show the day's meaningful allowance or today's special condition.
6. **Build system debt:** `app/build.gradle` contains a preBuild source-rewrite patch for the dinner UI. It is idempotent, but canonical source should eventually contain the final UI directly so builds do not mutate Java source.

## Direction to keep
- No hard limit on creative/free sculpting.
- Limit only *paid/progression* rewards: target 3 rewarded builds per calendar day.
- Missing a day must never delete progress or break a streak.
- Daily return should be curiosity-driven (new client/weather/material/story), not punishment-driven.
- Phone UI: one dominant action, 48dp+ touch targets, no important interaction behind system bars, minimal text while sculpting.

## Next implementation priorities
### P0 — daily reward integrity
- Add date-aware daily state to Android (`reward_day`, `rewarded_builds_today`).
- First 3 completed builds of the local calendar date can award coins; later builds remain fully playable and scoreable but show `Вільна робота • нагорода завтра`.
- Daily state resets only when local date changes; never by elapsed 24h timer.
- Surface `НАГОРОДИ 0/3` in the finish/result area, not as permanent HUD clutter while sculpting.

### P0 — mission anti-reroll
- Seed/select one daily mission from local date + year and persist it for that date.
- Replays on the same day keep the same mission; tomorrow changes it.
- Mission bonus should also be paid once per rewarded build, not generate an additional unlimited reward path.

### P1 — expressive completion
- Change mandatory accessories from 6/6 to a minimum of 3 meaningful parts; allow optional decoration for score/style.
- Keep eyes/nose/accessibility guidance available but avoid forcing identical snowmen.
- Result scoring should acknowledge shape, stability and character separately.

### P1 — phone UI
- During sculpting show only: current step, short tactile hint, and one contextual action.
- Move wallet/year metadata to a compact secondary strip or finish screen.
- Ensure bottom controls remain above gesture navigation and can be reached one-handed.

### P2 — daily variety
- Daily snow condition: пухкий / мокрий / крижаний, each changing rolling/placement tolerance slightly.
- One optional daily client card tied to a play style (stable / funny / photo-friendly), with no brand dependency.
- Keep free mode available after paid quota.

## Safe pseudo-collaboration ideas (do not present as real partnerships)
### Nova Poshta × ETNODIM inspiration
Nova Poshta and ETNODIM launched the cultural project “Взори України” in August 2026 around regional Ukrainian ornament traditions. Safe adaptation: **“Візерунки областей”** — each day one region offers a scarf/hat ornament palette for the snowman; collectible as an in-game memory, clearly labelled `НАТХНЕННО УКРАЇНСЬКИМИ ВІЗЕРУНКАМИ`, with no logos or claim of partnership.

### Uklon inspiration
Uklon continues campaigns based on rides/challenges/reward loops in 2026. Safe adaptation: **“Зимовий маршрут”** — after finishing a snowman, choose one of three short neighborhood routes (двір / парк / школа) to deliver a photo or gift. Reward comes from completing the route choice/story, not from real-world travel or branded services.

## Avoid
- Fake logos or wording that implies an official partnership.
- Real-money/lottery-like mechanics tied to business brands.
- Punitive login streaks or loss of progress for missing days.
- More than 3 monetized/progression completions per day until the loop is tested for farming.

## Definition of next working state
A build is considered the next stable gameplay milestone when: daily reward reset is date-correct; replay cannot farm coins indefinitely; the same daily mission persists through replays; free sculpting still works after 3 rewarded builds; and the phone finish screen clearly explains whether the run was rewarded or free.
