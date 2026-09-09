#!/usr/bin/env python3
"""Add UI hardcoded strings to all locale JSON files."""
import json
import os

from calendar_i18n import CALENDAR
from ui_strings_patches import CD_OPEN_MENU, LOCALE_PATCHES, NEW_EN

LOCALES_DIR = os.path.join(os.path.dirname(__file__), "locales")

for filename in os.listdir(LOCALES_DIR):
    if not filename.endswith(".json"):
        continue
    code = filename[:-5]
    path = os.path.join(LOCALES_DIR, filename)
    with open(path, encoding="utf-8") as f:
        data = json.load(f)

    patch_code = "tr" if code == "tr-CY" else code
    explicit = patch_code in LOCALE_PATCHES
    patch = LOCALE_PATCHES.get(patch_code, NEW_EN)
    applied = 0
    for key, value in patch.items():
        if explicit:
            data[key] = value
            applied += 1
        elif key not in data or data.get(key) == NEW_EN.get(key):
            data[key] = value
            applied += 1

    cal_code = "tr" if code == "tr-CY" else code
    if cal_code in CALENDAR:
        data.update(CALENDAR[cal_code])

    data["cd_open_menu"] = CD_OPEN_MENU.get(cal_code, CD_OPEN_MENU["en"])

    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")
    print(f"Patched {filename} (+{applied} ui keys)")
