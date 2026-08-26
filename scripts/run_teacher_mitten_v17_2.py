from pathlib import Path

path = Path("scripts/apply_teacher_mitten_v17_2.py")
code = path.read_text(encoding="utf-8")
old = 'rep("memory", "c.drawText(names[selected],", "c.drawText(memoryName(selected),", "memory detail dynamic title")'
new = 'rep("memory", "String nm=names[selected];", "String nm=memoryName(selected);", "memory detail dynamic title")'
if old not in code:
    raise SystemExit("v17.2 compatibility wrapper could not find old memory detail replacement")
code = code.replace(old, new, 1)
exec(compile(code, str(path), "exec"), {"__name__": "__main__"})
