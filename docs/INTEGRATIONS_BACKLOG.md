# SnowmanGame — integration backlog

Updated: 2026-09-04
Status: active implementation backlog

## Rule for all integrations
- Use integrations as optional story/visual variety, not as mandatory progression gates.
- Do not add a new currency, energy, paid retries, lootboxes or streak punishment.
- Do not claim an official partnership unless it is actually confirmed.
- Real brand names may appear only where intentionally approved for the game; otherwise use fictional/inspired concepts.
- Every integration must fit mobile UI: one dominant action, 48dp+ primary targets, short Ukrainian copy, no overflow on 320–360dp widths.
- Integrations must not reset class, school-day, snowman reward or save-state progression.

## Already approved / already partially implemented

### 1. Галичина — sour cream with deruny
Status: implemented in school dinner scene, keep and polish.

Use:
- deruny dinner can show a small sour-cream cup with `ГАЛИЧИНА` and `СМЕТАНА`;
- white/blue visual direction with a small green Carpathian accent;
- keep it as an in-world food reference, not an "official collaboration" claim.

Next polish:
- verify wordmark readability on 320dp;
- do not let the cup cover the food;
- preserve 3-bite dinner flow.

### 2. Зимова майстерня орнаменту
Inspiration: contemporary Ukrainian craft / fashion activity.
Status: approved for implementation.

Gameplay:
- optional school event for middle/senior grades;
- player chooses one scarf/hat pattern from 3 broad regional-style motifs;
- result appears on the snowman for a school photo or scrapbook memory;
- no economy reward.

Mobile action flow:
1. `ОБРАТИ ВІЗЕРУНОК`
2. 3 large pattern cards
3. `ЗБЕРЕГТИ ОБРАЗ`

### 3. Шкільний технопікнік
Inspiration: Ukrainian tech/community event format.
Status: approved for implementation.

Gameplay:
- best fit: grades 9–11;
- choose one station: `ЗБУДУВАТИ`, `ОФОРМИТИ`, `ПРЕЗЕНТУВАТИ`;
- one short interaction per station;
- final result is a photo/memory, not currency.

Purpose:
- reduce repetition in 9–11 classes;
- give the player a choice without branching the whole save system.

### 4. Шкільна майстерня подарунка
Inspiration: handmade, stationery and gift-packaging themes in Ukraine.
Status: approved for implementation.

Gameplay:
- choose one: winter postcard, paper wrapping, tiny handmade souvenir;
- 2–3 taps maximum;
- finished object becomes a scrapbook item or background prop;
- no shop and no random paid reward.

Best placement:
- grades 7–9 or a winter school event.

### 5. Шкільний ярмарок за 5 хвилин
Inspiration: fast-food / compact service / packaging event themes.
Status: approved for implementation.

Gameplay:
- best fit: grades 9–10;
- choose a role: `СТРАВА`, `ПАКУВАННЯ`, `СТІЙКА`;
- player assembles one simple winter snack concept, paper pack or service stand;
- result is visual/story progress only.

Important:
- keep food fictional unless a separately approved real brand is used;
- do not introduce buying/selling grind.

### 6. Від майстерні до полиці
Inspiration: Ukrainian FMCG/private-label business themes.
Status: approved for implementation.

Gameplay:
- short senior-school project;
- choose product shape, readable label and delivery order;
- show cause/effect: confusing label -> character reacts; clear label -> project succeeds;
- no real retailer branding unless separately approved.

Best placement:
- grade 10 or 11 as a practical-life lesson.

### 7. Смак зі шкільної перерви
Inspiration: food + nostalgia + small memory/puzzle campaign pattern.
Status: approved concept, not yet implemented.

Gameplay:
- rare school-break variant;
- normal snack interaction reveals one tiny paper note, riddle or memory;
- no purchase and no random prize economy;
- memory goes to scrapbook.

### 8. Зимовий двір
Inspiration: urban design / courtyard arrangement.
Status: approved concept.

Gameplay:
- player chooses where the snowman is photographed: bench, lamp, mailbox, snow sculpture corner;
- changes only composition/background;
- can rotate by day or school year.

Purpose:
- visible return variety without another grind system.

### 9. Тінь сніговика
Inspiration: photography/light-and-shadow prompt.
Status: approved concept.

Gameplay:
- optional dusk visitor/event;
- choose hat/scarf pose that creates a readable silhouette;
- reward is a scrapbook memory only.

### 10. Регіональна майстерня
Inspiration: broad Ukrainian regional craft categories.
Status: approved concept.

Gameplay:
- occasional maker/teacher brings a broad motif category;
- player picks a safe stylized pattern, not an exact copied branded design;
- can alter scarf, mittens or classroom decoration.

## Integration order

### P1 — implement into live gameplay now
1. Connect `SchoolGradeContent` to visible grades 7–11 first so senior classes stop repeating.
2. Add `Шкільний технопікнік` as one 9–11 class event.
3. Add `Шкільна майстерня подарунка` as one 7–9 class event.
4. Add `Від майстерні до полиці` or `Шкільний ярмарок за 5 хвилин` as one 10–11 class practical event.
5. Keep `Галичина` dinner integration and only polish mobile readability.

### P2 — add as return-variety after P1 is stable
6. `Зимова майстерня орнаменту`.
7. `Смак зі шкільної перерви`.
8. `Зимовий двір`.
9. `Тінь сніговика`.
10. `Регіональна майстерня`.

## Save-state rules
Do not create a new progression subsystem for every integration. Prefer these keys/patterns:
- one date-stable event id for optional daily/weekly variety;
- one small completion flag or memory id when needed;
- never reuse or overwrite `school_grade`, `school_year_school_done`, `school_year_weekend_done`, `school_player_last_completed_day`, reward-day keys or snowman build counters;
- integration completion must not count as an extra school day;
- if the app closes mid-event, return to the same event or safely fall back to the current school stage without losing the counted day.

## Daily-limit rule
Keep unchanged:
- first 3 completed snowmen per local date may award coins/progression;
- unlimited free snowman sculpting afterward;
- maximum 1 counted school-life day per local date;
- optional integrations do not create extra paid/farmable daily rewards.

## Definition of done for any integration
An integration is accepted only if:
1. it is understandable without a tutorial wall of text;
2. it takes no more than a few taps unless it is a deliberate mini-game;
3. it survives app restart without corrupting school/snowman progress;
4. it fits 320–360dp width and short usable heights;
5. it does not block the path to grade 11;
6. it does not repeat the exact same interaction too often;
7. it does not claim a real partnership without confirmation;
8. it adds visible/story variety instead of another grind meter.

## Immediate next implementation pass
- wire senior-grade content into `SchoolWeekActivity`;
- implement one integration event for grades 7–9 and one for grades 9–11;
- run restart tests on both event paths;
- run 320/360dp text/button checks;
- only then add the next integration from this backlog.
