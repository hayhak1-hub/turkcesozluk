#!/usr/bin/env python3
import glob
import os
import re

BASE = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res")

def parse(path):
    data = {}
    text = open(path, encoding="utf-8").read()
    for m in re.finditer(r'<string name="([^"]+)">([^<]*)</string>', text):
        data[m.group(1)] = (
            m.group(2)
            .replace("\\'", "'")
            .replace("\\n", "\n")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&quot;", '"')
        )
    return data

en = parse(os.path.join(BASE, "values-en", "strings.xml"))

# Keys intentionally identical across all locales (URLs, etc.)
SKIP_SAME_AS_EN = {"privacy_policy_url"}

for path in sorted(glob.glob(os.path.join(BASE, "values-*", "strings.xml"))):
    loc = os.path.basename(os.path.dirname(path))
    if loc == "values-en":
        continue
    data = parse(path)
    same_en = [
        k for k, v in data.items()
        if k not in SKIP_SAME_AS_EN and en.get(k) == v
    ]
    missing = [k for k in en if k not in data]
    print(f"{loc}: {len(same_en)} untranslated, {len(missing)} missing")
    if same_en:
        print("  " + ", ".join(same_en[:12]) + ("..." if len(same_en) > 12 else ""))

print(f"\nTotal keys: {len(en)}")
