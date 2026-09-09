#!/usr/bin/env python3
"""Export values/strings.xml to scripts/locales/tr.json and tr-CY.json."""
import json
import os
import re

ROOT = os.path.join(os.path.dirname(__file__), "..")
STRINGS = os.path.join(ROOT, "app", "src", "main", "res", "values", "strings.xml")
OUT = os.path.join(os.path.dirname(__file__), "locales")

text = open(STRINGS, encoding="utf-8").read()
data = {}
for m in re.finditer(r'<string name="([^"]+)">([^<]*)</string>', text):
    value = (
        m.group(2)
        .replace("\\'", "'")
        .replace("\\n", "\n")
        .replace("&amp;", "&")
    )
    data[m.group(1)] = value

# Cyprus Turkish uses the same standard UI strings as Turkey.
cy = dict(data)

for name, payload in [("tr", data), ("tr-CY", cy)]:
    path = os.path.join(OUT, f"{name}.json")
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)
        f.write("\n")
    print(f"Wrote {path} ({len(payload)} keys)")
