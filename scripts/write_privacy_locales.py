#!/usr/bin/env python3
"""Write privacy locale JSON files for all supported languages."""

from __future__ import annotations

import json
import sys
from pathlib import Path

OUT = Path(__file__).resolve().parent / "privacy_locales"
sys.path.insert(0, str(Path(__file__).resolve().parent))

from generate_privacy_policy import build_de, build_en, build_tr  # noqa: E402
from privacy_translations_all import TRANSLATIONS  # noqa: E402

LOCALE_CODES = [
    "tr", "en", "de", "az", "kk", "ky", "uz", "tk", "zh-CN", "hi", "es", "fr",
    "ar", "bn", "pt", "ru", "ur", "id", "ja", "sw", "mr", "te", "ta", "vi", "ko",
]


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    all_data = {
        "tr": build_tr(),
        "en": build_en(),
        "de": build_de(),
        **TRANSLATIONS,
    }
    written: list[str] = []
    for code in LOCALE_CODES:
        data = all_data[code]
        path = OUT / f"{code}.json"
        path.write_text(
            json.dumps(data, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )
        written.append(path.name)
        print(f"Wrote {path.name}")
    print(f"\nTotal: {len(written)} files")


if __name__ == "__main__":
    main()
