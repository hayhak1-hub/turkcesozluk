#!/usr/bin/env python3
"""Generate store listing CSVs from app locales (25) and Play Console subset (23)."""

from __future__ import annotations

import csv
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(Path(__file__).resolve().parent))

from store_listing_i18n import APP_LOCALES, APP_TO_PLAY, STORE_LISTINGS  # noqa: E402

APP_OUTPUT = ROOT / "app-locales-listing.csv"
PLAY_OUTPUT = ROOT / "play-store-listing.csv"
PLAY_IMPORT_OUTPUT = ROOT / "play-store-listing-import.csv"
PT_BR_MANUAL = ROOT / "play-store-pt-BR.txt"


def _flatten(text: str) -> str:
    return " ".join(text.split())


def main() -> None:
    app_rows: list[dict[str, str]] = []
    play_rows: list[dict[str, str]] = []
    import_rows: list[dict[str, str]] = []

    for app_tag in APP_LOCALES:
        entry = STORE_LISTINGS[app_tag]
        play_tag = APP_TO_PLAY[app_tag] or ""
        row = {
            "App locale": app_tag,
            "Play locale": play_tag,
            "Title": entry["title"],
            "Short description": entry["short_description"],
            "Full description": entry["full_description"],
        }
        app_rows.append(row)
        if play_tag:
            play_row = {
                "Language code": play_tag,
                "Title": entry["title"],
                "Short description": entry["short_description"],
                "Full description": entry["full_description"],
            }
            play_rows.append(
                {
                    "Locale": play_tag,
                    "Title": entry["title"],
                    "Short description": entry["short_description"],
                    "Full description": entry["full_description"],
                }
            )
            import_rows.append(
                {
                    **play_row,
                    "Full description": _flatten(entry["full_description"]),
                }
            )

    with APP_OUTPUT.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(
            f,
            fieldnames=[
                "App locale",
                "Play locale",
                "Title",
                "Short description",
                "Full description",
            ],
            quoting=csv.QUOTE_MINIMAL,
        )
        writer.writeheader()
        writer.writerows(app_rows)

    for path, fieldnames, rows, quoting in (
        (
            PLAY_OUTPUT,
            ["Locale", "Title", "Short description", "Full description"],
            play_rows,
            csv.QUOTE_MINIMAL,
        ),
        (
            PLAY_IMPORT_OUTPUT,
            ["Language code", "Title", "Short description", "Full description"],
            import_rows,
            csv.QUOTE_ALL,
        ),
    ):
        with path.open("w", encoding="utf-8-sig", newline="") as f:
            writer = csv.DictWriter(f, fieldnames=fieldnames, quoting=quoting)
            writer.writeheader()
            writer.writerows(rows)

    pt = STORE_LISTINGS["pt"]
    PT_BR_MANUAL.write_text(
        "\n".join(
            [
                "Play Console > Haupteintrag > Portugiesisch (Brasilien) pt-BR",
                "",
                "Title:",
                pt["title"],
                "",
                "Short description:",
                pt["short_description"],
                "",
                "Full description:",
                pt["full_description"],
            ]
        ),
        encoding="utf-8",
    )

    print(f"Wrote {APP_OUTPUT} ({len(app_rows)} app locales)")
    print(f"Wrote {PLAY_OUTPUT} ({len(play_rows)} Play Console locales)")
    print(f"Wrote {PLAY_IMPORT_OUTPUT} (single-line, for import)")
    print(f"Wrote {PT_BR_MANUAL} (manual pt-BR copy/paste)")


if __name__ == "__main__":
    main()
