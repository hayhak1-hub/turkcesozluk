#!/usr/bin/env python3
"""Apply calendar/day translations to all locale JSON files."""
import json
import os
from calendar_i18n import CALENDAR, CALENDAR_KEYS

LOCALES_DIR = os.path.join(os.path.dirname(__file__), "locales")

for filename in sorted(os.listdir(LOCALES_DIR)):
    if not filename.endswith(".json"):
        continue
    code = filename[:-5]
    if code == "tr-CY":
        code = "tr"  # reuse Turkish calendar
    patch = CALENDAR.get(code)
    if not patch:
        print(f"SKIP {filename}: no calendar patch")
        continue
    path = os.path.join(LOCALES_DIR, filename)
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    data.update(patch)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")
    print(f"Patched {filename} ({len(CALENDAR_KEYS)} calendar keys)")
