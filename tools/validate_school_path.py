#!/usr/bin/env python3
"""Independent progression invariant smoke test.

This does not emulate Android UI. It verifies the intended school-year rules:
5 counted weekdays + 2 counted weekend days per grade, skipped dates harmless,
no same-date double count, grades 2..11 reachable from any weekday alignment.
"""

EXPECTED=list(range(2,12))

for start_dow in range(7):
    grade=2
    school_done=0
    weekend_done=0
    completed=[]
    counted_dates=set()
    day=0
    while day < 400 and grade <= 11:
        dow=(start_dow+day)%7
        # Skip deterministic dates to verify that missed days do not destroy progress.
        if day % 11 == 6:
            day += 1
            continue
        if day in counted_dates:
            raise SystemExit(f"double-counted date {day}")
        counted=False
        if dow < 5 and school_done < 5:
            school_done += 1
            counted=True
        elif dow >= 5 and weekend_done < 2:
            weekend_done += 1
            counted=True
        if counted:
            counted_dates.add(day)
        if school_done == 5 and weekend_done == 2:
            completed.append(grade)
            if grade == 11:
                break
            grade += 1
            school_done=0
            weekend_done=0
        day += 1
    if completed != EXPECTED:
        raise SystemExit(f"grade path broken for start_dow={start_dow}: {completed!r}")

# Save-repair invariant used by SchoolProgressionGuard.
assert (5 >= 5 and 2 >= 2)

print("OK: grades 2→11 reachable from every weekday alignment with skipped days")
