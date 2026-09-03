# SnowmanGame — player return / mobile loop plan

Updated: 2026-09-03
Current Android build: v18.17

## Current state
- Core snowman construction loop is playable: roll 3 balls, place 6 accessories, score build/decor, complete one daily mission, receive coins on up to 3 rewarded builds per local calendar date.
- After the 3 rewarded builds, sculpting remains fully playable as free work with no coin/progression payout.
- School-week branch is player-paced; pre-school ice cream is a one-time story scene before school only.
- DONE/dinner phone layouts are enlarged and `app/build.gradle` no longer rewrites Java source during preBuild.
- A separate HTML mobile prototype explored a useful order loop: 3 paid jobs per day, unlimited free sculpting, 3 client choices and snow conditions.

## Confirmed problems
1. **Daily reward farming — fixed in v18.13.** Android stores `reward_day` + `rewarded_builds_today`; only the first 3 completed builds of the local calendar date award coins/progression.
2. **Prototype daily counter is still not actually daily.** `snowman_completed_today` is stored without a date key/reset, so the HTML prototype can remain exhausted on following days. Do not port this bug back into Android.
3. **Mission reroll — fixed in v18.13.** Mission is deterministic for local calendar day + year and stays unchanged through replays.
4. **Forced 6/6 completion — fixed in v18.14.** Eyes + carrot + any third part unlock completion; the remaining parts stay optional and add style/score.
5. **Task-first HUD — improved in v18.14.** Current tactile step and daily reward/free status are primary; year/wallet are secondary.
6. **Daily snow condition — fixed in v18.15.** `ПУХКИЙ / МОКРИЙ / КРИЖАНИЙ` is deterministic for the local calendar date, shown compactly, and slightly changes rolling/placement feel.
7. **Midnight-open Activity stale day — fixed in v18.15.** The view notices a local date change without requiring an Activity restart and refreshes reward quota, mission and snow together.
8. **Build source mutation — fixed in v18.12.** Dinner UI is canonical in source; Gradle no longer patches Java before compilation.

## Direction to keep
- No hard limit on creative/free sculpting.
- Limit only paid/progression rewards to 3 rewarded builds per local calendar day.
- Missing a day must never delete progress or break a streak.
- Daily return should be curiosity-driven (new condition/client/material/story), not punishment-driven.
- Phone UI: one dominant action, 48dp+ touch targets, no important interaction behind system bars, minimal text while sculpting.
- Story integrations must be clearly fictional/inspired; never claim an official brand partnership.

## Completed milestones
### DONE v18.12 — pre-school story placement
- Ice cream appears once before school, then is permanently marked complete.
- It no longer appears as a generic reward button after ordinary snowmen.

### DONE v18.13 — daily reward integrity
- Date-aware Android state: `reward_day`, `rewarded_builds_today`.
- First 3 completions can award coins/progression; later builds are free/scored play.
- Result card states either `НАГОРОДИ N/3` or `ВІЛЬНА РОБОТА • НАГОРОДИ 3/3`.

### DONE v18.13 — mission anti-reroll
- One deterministic mission for local calendar day + year.
- Replays on the same date keep the same mission.

### DONE v18.14 — expressive completion + mobile action hierarchy
- Eyes + carrot + any third accessory unlock the finish action.
- Remaining accessories stay visible and optional; each still adds score/character.
- The finish button has a reserved bottom touch area instead of replacing the accessory tray.
- Speed mission target is rebalanced for fewer mandatory interactions.
- HUD prioritizes current sculpting step and today's 3-reward/free-work status; year/wallet are secondary.

## Next implementation priorities
### DONE v18.15 — daily snow feel + midnight refresh
- Added date-stable `ПУХКИЙ / МОКРИЙ / КРИЖАНИЙ` snow.
- Powder is slightly easier to roll, wet snow takes slightly more movement, icy snow slightly tightens placement tolerance.
- Snow is shown in the existing HUD line and opening tip; no extra card/panel.
- A date change while the Activity remains open refreshes quota, mission and snow together.

### DONE v18.16 — daily curiosity visitor
- One deterministic visitor per local calendar day asks for a compact style goal: майстриня / фотограф / дитина / сусід.
- Replay and same-day year progression cannot reroll the visitor because it derives from local calendar date only.
- Fulfilling the request stores only a date-keyed memory marker; it awards no coins and never gates progression.
- The request uses the existing opening tip; the result uses a small block inside the existing result card, so no new menu is added.

### P2 — compact-phone regression + memory presentation
- Verify 320–360dp-wide layouts after the larger accessory tray and visitor result line.
- If the result card becomes crowded, shorten copy before shrinking touch targets.
- Later expose saved visitor memories as a lightweight scrapbook, not a second economy.

## Safe pseudo-collaboration ideas (do not present as real partnerships)
### Nova Poshta × ETNODIM inspiration
August 2026 cultural project “Взори України” used regional Ukrainian ornament traditions. Safe adaptation: **“Візерунки областей”** — one region-inspired scarf/hat palette on selected days, collectible as an in-game memory, labelled as inspiration and using no logos.

### Uklon inspiration
Challenge/route/reward patterns can become **“Зимовий маршрут”** — after a snowman, choose a short fictional destination such as двір / парк / школа for a tiny story continuation. No real-world travel requirement.

### Aurora RORI inspiration — added 2026-09-02
Aurora introduced RORI on 31 August 2026 as a curious explorer character used across stores/app communication. Safe game adaptation: **“Зимовий дослідник”** — a non-branded recurring character who occasionally brings one strange snowman part, material or tiny challenge. The useful mechanic is curiosity/discovery, not the mascot appearance, name, logo or prizes.

## Avoid
- Fake logos or wording implying an official partnership.
- Real-money/lottery-like mechanics tied to business brands.
- Punitive login streaks or loss of progress for missing days.
- More than 3 monetized/progression completions per day until economy testing says otherwise.
- Reintroducing random mission rerolls or elapsed-24h daily timers.

## Definition of next working state
The v18.15 daily-snow milestone is achieved. The next working milestone should be considered ready when:
1. one optional daily curiosity/client card adds variety without another farmable currency;
2. the visitor request is deterministic for the local date and cannot be rerolled by replay;
3. compact phones keep the snowman, accessory tray and finish button readable;
4. daily reward/free-mode rules from v18.13 remain intact;
5. school and pre-school story routing remain unaffected.

## Cycle update — 2026-09-02 late evening
- Closed unlimited Android coin/progression farming with a local-date 3/day quota.
- Closed same-day mission rerolling.
- Preserved unlimited free sculpting after quota.
- Kept the pre-school ice-cream story exactly before school and nowhere in the school loop.
- Corrected stale plan entries that still described old random missions and Gradle source mutation.
- Next P1 is expressive completion; next P2 is daily snow feel + curiosity visitor.

## Cycle update — 2026-09-03 early cycle
- Closed the forced 6/6 accessory gate without removing optional decoration.
- Preserved all six accessories and their score value, but made completion expressive after three meaningful parts.
- Reserved a one-handed bottom finish action and simplified the HUD hierarchy.
- Rebalanced the speed mission to avoid becoming trivial after fewer mandatory actions.
- Next P2 is date-stable snow feel; after that, add one optional daily curiosity/client card.
- Fresh business inspiration: the August 2026 multi-brand “Покоління 91” collaboration suggests a safe non-branded “майстерня чотирьох майстрів” cosmetic-memory week: different fictional makers contribute scarf/hat/button styles, with no logos, purchases or claims of partnership.

## Cycle update — 2026-09-03 morning
- Added date-stable daily snow feel rather than another menu or currency.
- Fixed the stale-day edge case when the app stays open across local midnight.
- Kept the mechanical differences intentionally small so daily variation creates curiosity rather than punishment.
- Next P2 is one optional daily curiosity visitor/client and compact-phone regression testing.
- Fresh inspiration: Ukrainian retail coverage around late-August 2026 highlights strong familiar national brands; adapt the useful pattern as fictional rotating local makers/visitors, not logos or claimed partnerships.

## Cycle update — 2026-09-03 visitor cycle
- Added one date-stable optional visitor request without another currency or progression gate.
- Visitor success writes a one-per-date memory marker only; replay cannot farm value or reroll the request.
- Kept the request in the existing tip and result card to protect phone screen space.
- Next priority is compact-phone regression and deciding whether saved memories deserve a tiny scrapbook surface.
- Fresh business inspiration: late-August Ukrainian campaigns increasingly combine several makers/brands around one shared theme; the safe game translation is rotating fictional winter visitors with distinct craft requests, not logos or claimed partnerships.

## Cycle update — 2026-09-03 visitor stability hotfix
- Found and fixed a same-day reroll edge case: v18.16 visitor selection also depended on life year.
- Visitor selection now depends on local calendar date only, matching the one-visitor-per-day rule and date-keyed memory.
- This keeps year progression, replay and free sculpting from changing today's visitor.
