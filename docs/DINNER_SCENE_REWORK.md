# SnowmanGame — dinner scene rework

Updated: 2026-09-03
Status: implementation spec after the deruny visual hotfix

## Why this is P1
The current dinner stage is mechanically valid but visually weak on a phone: a large white card, a full plate in the middle, a small hero below it, and five invisible-progress taps. The deruny themselves were fixed after player feedback, but the scene still behaves like a form rather than a small evening game moment.

## Confirmed problems
1. `drawDinner()` always draws the full dish, even after `dinnerBites == 5`; the food does not visually disappear as it is eaten.
2. Five taps on the same large plate have no decision, timing, aim or visible per-bite consequence, so the interaction is repetitive rather than playful.
3. The five white progress circles are drawn on top of the meal area and compete visually with the food.
4. The hero is visually detached from the table/plate and does not look like they are eating.
5. The card uses large fixed vertical gaps, which makes the scene look empty on tall phones and can crowd elements on short phones.
6. The global school header exposes several counters at once (`day`, `school/weekend`, stage, dinner progress), so dinner has too much status text for a simple end-of-day scene.

## Target interaction
- Keep dinner mandatory for a counted school day, but make it short: **3 meaningful bites instead of 5 empty taps**.
- Each tap removes a visible third of the food and briefly moves the hero/hand toward the plate.
- On the third bite, show one short reaction (`Смачно. День майже завершено.`) and reveal the single dominant action `СПАТИ / ЗАВЕРШИТИ ДЕНЬ`.
- Old saves with `dinnerBites >= 3` should be treated as dinner-complete; do not strand players who already had 4/5 or 5/5 from an older build.

## Phone layout
Use the available card height rather than fixed offsets.

Top 18–20%:
- compact stage label: `ВЕЧЕРЯ • 4/5`;
- dish name as the only large title;
- one short subtitle max.

Middle 50–55%:
- warm evening wall/window background inside the card rather than plain white;
- table across the scene;
- plate on the table, not floating;
- hero seated/standing directly behind the table;
- dish is the visual focus.

Bottom 25–30%:
- before completion: small instruction `Торкнись тарілки • залишилось N`;
- after completion: 48dp+ primary button `ЗАВЕРШИТИ ДЕНЬ`;
- no duplicate `З'їдено 3/3` counter if the remaining-food state is already obvious.

## Food rendering rules
- Deruny: flat irregular golden potato pancakes, browned edges, overlapping stack, sour cream on the side. Never circular brown pellets.
- Varenyky: crescent/half-moon shapes, not generic ovals.
- Holubtsi: short wrapped cabbage rolls with visible seam/highlight.
- Borshch: deep bowl, red soup surface, cream swirl rather than a flat red oval.
- Banosh: soft mound/bowl with bryndza crumbs, not one flat yellow oval.
- `dinnerBites` must change the drawn quantity/portion for every dish.

## Header simplification during dinner
For the DINNER stage, keep only:
- `ШКОЛА • ЗИМА N • 2-А`;
- `День N/7`;
- optionally `навчання N/5` in small secondary text.

Hide the long daily-rule sentence during the interactive dinner scene. That rule belongs on DONE/intro/help surfaces, not beside every bite.

## Acceptance criteria
1. At 320–360dp width, food is still recognizable without text explaining what it is.
2. The hero, table and plate form one visual scene; no large dead white region separates them.
3. Every tap visibly changes the plate.
4. Dinner completes in at most three taps plus one final action.
5. No food item is represented primarily as small brown circles.
6. Primary action stays at least 48dp high and clear of system navigation insets.
7. Existing day-counting logic and one-counted-day-per-calendar-date rule remain unchanged.
8. `dinnerBites >= 3` from any existing save opens the completed-dinner state safely.

## Do not add
- another currency;
- random reward for eating;
- streak punishment;
- extra modal window after each bite;
- real food-brand logos or claims of sponsorship.
