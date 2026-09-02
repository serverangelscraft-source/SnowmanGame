# SnowmanGame — player return / mobile loop plan

Updated: 2026-09-02
Current Android build: v18.13

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
4. **Completion still requires all 6 accessories.** This makes every run structurally similar and reduces expressive builds; speed missions also become partly artificial.
5. **Top HUD still prioritizes year/coins.** It should emphasize the current tactile task and today's special condition instead.
6. **Build source mutation — fixed in v18.12.** Dinner UI is canonical in source; Gradle no longer patches Java before compilation.

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

## Next implementation priorities
### P1 — expressive completion
- Replace forced 6/6 completion with a minimum meaningful set: target 3 required parts, remaining parts optional.
- Optional decoration should improve style/character score rather than block completion.
- Keep placement hints for eyes/nose but allow intentionally odd snowmen.
- Rebalance speed mission after the mandatory interaction count is reduced.

### P1 — phone UI
- During sculpting show only current step, short tactile hint, and contextual action.
- Move wallet/year metadata to a secondary strip or result screen.
- Keep all bottom controls above gesture navigation and comfortably reachable one-handed.

### P2 — daily snow condition
- Add one date-stable condition: `ПУХКИЙ`, `МОКРИЙ`, or `КРИЖАНИЙ`.
- Condition changes actual feel slightly: rolling effort / placement tolerance, never making a run impossible.
- Show the condition before play and with a small icon/word during sculpting; do not add another large HUD panel.

### P2 — daily curiosity card
- One optional visitor/client per day asks for a style: stable / funny / photo-friendly / regional-pattern.
- Do not lock core progression behind the client card.
- Reward should primarily be a memory/cosmetic entry; avoid creating another farmable currency path.

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
The v18.13 reward-loop milestone is achieved. The next working milestone should be considered ready when:
1. a player may finish a valid snowman with 3 meaningful required parts;
2. optional parts improve character/style rather than gate completion;
3. a date-stable snow condition visibly and mechanically changes the run;
4. the main phone play screen remains uncluttered and one-handed;
5. daily reward/free-mode rules from v18.13 remain intact.

## Cycle update — 2026-09-02 late evening
- Closed unlimited Android coin/progression farming with a local-date 3/day quota.
- Closed same-day mission rerolling.
- Preserved unlimited free sculpting after quota.
- Kept the pre-school ice-cream story exactly before school and nowhere in the school loop.
- Corrected stale plan entries that still described old random missions and Gradle source mutation.
- Next P1 is expressive completion; next P2 is daily snow feel + curiosity visitor.
