# SnowmanGame — night cycle 2026-09-03

Baseline: v18.21 (versionCode 54). Latest CI run 151 is green.

## What moved today
- Update-safe launcher/resume path added: existing players no longer fall back into the snowflake intro after an APK update/restart.
- Completed school intro no longer resets into 1-A. The action now resumes the actual current grade.
- School progression save guard repairs clamped grade/year counters and reconstructs a missing `school_year_complete` flag for old saves.
- CI validates the 2→11 progression invariants and the signed APK build succeeds.
- Dinner interaction was reduced to three visible bites, preserving one counted school-life day per calendar date.

## New findings
### P1 — grades 7–11 are structurally too similar
`theme()` becomes generic after grade 6 and the five weekday lesson templates are reused across grades. This is playable, but 7→11 risks feeling like replaying the same school year with only the grade number changed.

Direction: keep the stable 6-stage day shell, but rotate grade-specific story/question packs. Do not add extra currencies or taps.

### P1 — full school completion is calendar-heavy
One counted day per date is healthy anti-grind pacing, but 10 grades × 7 counted days implies a long real-time school arc. Do not remove the one-day safety limit yet. First add stronger grade-to-grade novelty and a visible long-term graduation map; only then reassess duration from play feedback.

### P1 — phone regression remains open
Result card and dinner still need 320/360dp and short-height regression checks. Primary targets remain 48dp+.

### P2 — collaboration layer should stay cosmetic/story-only
Current safe direction: craft/pattern weeks, tiny paper-memory objects, courtyard/photo composition changes. Never claim a real partnership without permission and do not connect these events to paid/random rewards.

## Current external inspiration
- Ukrainian Fashion Week SS27 (2–6 Sep 2026) is actively centered on modern reinterpretation of Ukrainian craft/handmade work. Safe game adaptation: one optional “Майстерня орнаменту” school week where the player chooses a scarf/hat pattern for a class photo; no brand logos/names copied.
- Current Ukrainian retail activity around Independence Day campaigns shows brands using place/memory/history themes. Safe adaptation: occasional fictional “Місце сили” memory card tied to a courtyard or regional winter scene, not a commercial claim.

## Next tasks
1. P1 — build grade-specific content packs for 7, 8, 9, 10 and 11 while keeping the tested day state machine unchanged.
2. P1 — run 320/360dp + short-height UI regression on result card, dinner and school screens.
3. P1 — test restart/update at every school stage (morning, lesson, break, home, dinner, weekend, year transition, 11-grade finish).
4. P2 — add a simple graduation-map/progress view only after the above passes; no new currency or streak.

## Working-state definition
A build is “playable without developer babysitting” when: APK update preserves location/progress; intro is one-time; 2→11 transitions are deterministic; each stage survives process restart; no grade can regress; 11th grade reaches a terminal graduation state; and core screens remain usable at 320–360dp widths.
