#!/usr/bin/env python3
"""Apply help_i18n translations to scripts/locales/*.json."""

from __future__ import annotations

import json
from pathlib import Path

from help_i18n import HELP_I18N, HELP_KEYS

LOCALES_DIR = Path(__file__).resolve().parent / "locales"


def main() -> None:
    for path in sorted(LOCALES_DIR.glob("*.json")):
        code = path.stem
        if code not in HELP_I18N:
            print(f"SKIP {path.name}: no help translation")
            continue
        patch = HELP_I18N[code]
        missing = [k for k in HELP_KEYS if k not in patch]
        if missing:
            raise SystemExit(f"{code} missing keys: {missing}")

        data = json.loads(path.read_text(encoding="utf-8"))
        data.update({k: patch[k] for k in HELP_KEYS})
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"Updated {path.name}")


if __name__ == "__main__":
    main()
