# SnowmanGame — player return / mobile loop plan

Updated: 2026-09-03
Current Android build: v18.18

## Current state
- Core snowman construction loop is playable: roll 3 balls, place up to 6 accessories, score build/decor, complete one daily mission, receive coins on up to 3 rewarded builds per local calendar date.
- Eyes + carrot + any third accessory unlock completion; remaining accessories are optional style/score.
- After the 3 rewarded builds, sculpting remains fully playable as free work with no coin/progression payout.
- Daily mission, snow condition and visitor are snapshotted for an active snowman so midnight cannot change its rules mid-run; the reward quota still uses the real local completion date.
- School-week branch is player-paced; pre-school ice cream is a one-time story scene before school only.
- DONE/dinner phone layouts are enlarged and `app/build.gradle` no longer rewrites Java source during preBuild.
- The signed v18.18 APK passed canonical validation, Gradle release build, zipalign and APK signature verification.
- A separate HTML mobile prototype explored a useful order loop: 3 paid jobs per day, unlimited free sculpting, 3 client choices and snow conditions.

## Confirmed problems
1. **Daily reward farming — fixed in v18.13.** Android stores `reward_day` + `rewarded_builds_today`; only the first 3 completed builds of the local calendar date award coins/progression.
2. **Prototype daily counter is still not actually daily.** `snowman_completed_today` is stored without a date key/reset, so the HTML prototype can remain exhausted on following days. Do not port this bug back into Android.
3. **Mission reroll — fixed in v18.13.** Mission is deterministic for local calendar day + year and stays unchanged through replays.
4. **Forced 6/6 completion — fixed in v18.14.** Eyes + carrot + any third part unlock completion; the remaining parts stay optional and add style/score.
5. **Task-first HUD — improved in v18.14.** Current tactile step and daily reward/free status are primary; year/wallet are secondary.
6. **Daily snow condition — fixed in v18.15.** `ПУХКИЙ / МОКРИЙ / КРИЖАНИЙ` is deterministic for the local calendar date, shown compactly, and slightly changes rolling/placement feel.
7. **Midnight-open Activity stale day — fixed in v18.15 and hardened in v18.18.** An idle Activity refreshes on a local date change, while a snowman already in progress keeps the mission/snow/visitor it started with until completion/reset.
8. **Build source mutation — fixed in v18.12.** Dinner UI is canonical in source; Gradle no longer patches Java before compilation.
9. **Ambiguous score copy — fixed in v18.18.** Mission and story bonuses explicitly say `БАЛІВ`, so they cannot be mistaken for coin rewards.
10. **Pseudo-collaboration wording — hardened in v18.18.** Gameplay scenes no longer name a real retailer as if it supplied an item; integration copy explicitly states there is no real partnership.
11. **Short-result-card overlap — confirmed after v18.18 audit.** `drawFinish()` caps the normal compact card at 478dp but can shrink below that when usable screen height is smaller. The visitor block ends around `card.top + 292dp`, while year progress begins at `card.bottom - 178dp`; when card height drops below about 470dp these regions overlap. Width-only testing would miss this. Fix should stack result sections from one vertical flow or switch to a shorter copy preset before any 48dp action target is reduced.

## Direction to keep
- No hard limit on creative/free sculpting.
- Limit only paid/progression rewards to 3 rewarded builds per local calendar day.
- Missing a day must never delete progress or break a streak.
- Daily return should be curiosity-driven (new condition/client/material/story), not punishment-driven.
- Phone UI: one dominant action, 48dp+ touch targets, no important interaction behind system bars, minimal text while sculpting.
- Story integrations must be clearly fictional/inspired; never claim an official brand partnership.
- Once a player starts a snowman, its mission/snow/visitor rules must remain stable until that run ends.
- Compact UI fixes should shorten/reflow text before shrinking controls or the snowman itself.

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

### DONE v18.15 — daily snow feel + midnight refresh
- Added date-stable `ПУХКИЙ / МОКРИЙ / КРИЖАНИЙ` snow.
- Powder is slightly easier to roll, wet snow takes slightly more movement, icy snow slightly tightens placement tolerance.
- Snow is shown in the existing HUD line and opening tip; no extra card/panel.
- An idle Activity notices a local date change without requiring a restart.

### DONE v18.16/v18.17 — daily curiosity visitor
- One deterministic visitor per local calendar day asks for a compact style goal: майстриня / фотограф / дитина / сусід.
- Replay and same-day year progression cannot reroll the visitor because it derives from local calendar date only.
- Fulfilling the request stores only a date-keyed memory marker; it awards no coins and never gates progression.
- The request uses the existing opening tip; the result uses a small block inside the existing result card, so no new menu is added.

### DONE v18.18 — active-run stability + clearer rewards
- Added a separate `challengeDay` snapshot for mission, snow and visitor state.
- Before the first interaction, a new local date refreshes normally.
- After the run starts, midnight cannot replace the current tip, mission, snow physics or visitor target.
- On completion, the 3/day coin/progression quota still synchronizes against the actual local completion date.
- Visitor memory remains keyed to the run's challenge day, preventing a pre-midnight request from being saved as the following day's visitor.
- `+250` mission and `+150` story bonuses are explicitly labelled as points.
- Real retailer naming was removed from the fictional carrot scene and the scene now states that no real partnership exists.

## Next implementation priorities
### P1 — result-card responsive height
- Fix `drawFinish()` so visitor, year progress and journey action cannot overlap on roughly 430–487dp usable-height phones.
- Prefer a single top-to-bottom vertical layout calculation instead of mixing `card.top + fixedOffset` and `card.bottom - fixedOffset` for adjacent sections.
- Add a short-copy mode before shrinking any action below 48dp: e.g. `СПОГАД • ФОТОГРАФ`, `РІК 2/3`, `ДО ВОКЗАЛУ`.
- Preserve result hierarchy: score → reward/free status → mission → visitor memory → year progress → dominant journey action → replay.

### P1 — narrow-width regression
- Verify 320–360dp widths after the larger accessory tray and visitor result line.
- Check long Ukrainian labels in the HUD, finish button, visitor line and journey button.
- If crowded, shorten copy before shrinking touch targets below 48dp.

### P2 — memory presentation
- Expose saved visitor memories as a lightweight scrapbook, not a second economy.
- No streak punishment, no extra daily currency, no extra mandatory menu before sculpting.

## Safe pseudo-collaboration ideas (do not present as real partnerships)
### Nova Poshta × ETNODIM inspiration
August 2026 cultural project “Взори України” used regional Ukrainian ornament traditions. Safe adaptation: **“Візерунки областей”** — one region-inspired scarf/hat palette on selected days, collectible as an in-game memory, labelled as inspiration and using no logos.

### Uklon inspiration
Challenge/route/reward patterns can become **“Зимовий маршрут”** — after a snowman, choose a short fictional destination such as двір / парк / школа for a tiny story continuation. No real-world travel requirement.

### Aurora RORI inspiration — added 2026-09-02
Aurora introduced RORI on 31 August 2026 as a curious explorer character used across stores/app communication. Safe game adaptation: **“Зимовий дослідник”** — a non-branded recurring character who occasionally brings one strange snowman part, material or tiny challenge. The useful mechanic is curiosity/discovery, not the mascot appearance, name, logo or prizes.

### Light-and-shadow fashion inspiration — added 2026-09-03
A current Ukrainian fashion theme is using light/shadow and cinematic contrast as a collection concept. Safe game adaptation: **“Тінь сніговика”** — an occasional fictional photographer asks for a silhouette/photo-friendly build at dusk. The mechanic can change background lighting and ask for a recognizable hat/scarf silhouette, while awarding only a scrapbook memory. Do not copy a real collection, designer marks, garment designs or campaign imagery.

## Avoid
- Fake logos or wording implying an official partnership.
- Real-money/lottery-like mechanics tied to business brands.
- Punitive login streaks or loss of progress for missing days.
- More than 3 monetized/progression completions per day until economy testing says otherwise.
- Reintroducing random mission rerolls or elapsed-24h daily timers.
- Changing a mission, snow physics or visitor target after the current snowman has started.
- Solving compact layouts by making primary actions too small to tap reliably.

## Definition of next working state
v18.18 is the current logic-stable baseline. The next working milestone should be considered ready when:
1. 320–360dp-wide and roughly 430–487dp usable-height phones keep the snowman, accessory tray, finish action and result card readable;
2. result sections never overlap when the card is shorter than its nominal 478dp compact height;
3. no label collision requires shrinking important touch targets below 48dp;
4. the visitor memory surface remains optional and has no farmable currency;
5. daily reward/free-mode rules from v18.13 and active-run snapshot rules from v18.18 remain intact;
6. school and pre-school story routing remain unaffected.

## Cycle update — 2026-09-02 late evening
- Closed unlimited Android coin/progression farming with a local-date 3/day quota.
- Closed same-day mission rerolling.
- Preserved unlimited free sculpting after quota.
- Kept the pre-school ice-cream story exactly before school and nowhere in the school loop.
- Corrected stale plan entries that still described old random missions and Gradle source mutation.

## Cycle update — 2026-09-03 early cycle
- Closed the forced 6/6 accessory gate without removing optional decoration.
- Preserved all six accessories and their score value, but made completion expressive after three meaningful parts.
- Reserved a one-handed bottom finish action and simplified the HUD hierarchy.
- Rebalanced the speed mission to avoid becoming trivial after fewer mandatory interactions.

## Cycle update — 2026-09-03 morning
- Added date-stable daily snow feel rather than another menu or currency.
- Fixed the stale-day edge case when the app stays open across local midnight.
- Kept the mechanical differences intentionally small so daily variation creates curiosity rather than punishment.

## Cycle update — 2026-09-03 visitor cycle
- Added one date-stable optional visitor request without another currency or progression gate.
- Visitor success writes a one-per-date memory marker only; replay cannot farm value or reroll the request.
- Kept the request in the existing tip and result card to protect phone screen space.

## Cycle update — 2026-09-03 visitor stability hotfix
- Found and fixed a same-day reroll edge case: v18.16 visitor selection also depended on life year.
- Visitor selection now depends on local calendar date only, matching the one-visitor-per-day rule and date-keyed memory.

## Cycle update — 2026-09-03 conflict audit v18.18
- Found a midnight conflict introduced by the earlier live-refresh fix: mission, snow physics, visitor and the active control tip could change while the player was already building.
- Split reward date from challenge snapshot: `rewardDay` follows the real local completion date; `challengeDay` stays fixed for the active snowman.
- Fixed misleading result text so score bonuses are not confused with coins.
- Removed a real retailer name from the carrot vignette and made fictional/non-partner status explicit.

## Cycle update — 2026-09-03 compact result audit
- Confirmed the v18.18 APK build/signature pipeline is green.
- Found that the next UI risk is height, not only width: a result card below about 470dp can overlap the visitor-memory and year-progress regions because the two groups use opposite fixed anchors.
- Promoted responsive result-card height to P1 and specified the fix as one vertical flow plus short-copy fallback, preserving 48dp actions.
- Kept all daily economy and active-run rules unchanged; no new currency or daily cap was added.
- Fresh visual inspiration: current Ukrainian fashion coverage around light/shadow suggests a fictional dusk-photographer memory request, used only as an aesthetic prompt rather than a claimed collaboration.
