#!/usr/bin/env python3
"""Full locale audit: missing keys, untranslated strings, overflow-risk lengths."""

from __future__ import annotations

import glob
import json
import os
import re
from pathlib import Path

BASE = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "res"
PRIVACY_DIR = Path(__file__).resolve().parent / "privacy_locales"

SKIP_SAME = {"privacy_policy_url", "btn_ok", "tab_quiz"}

# UI-critical keys where long translations may clip or overflow narrow layouts
OVERFLOW_KEYS = [
    "dictionary_title_synonyms",
    "dictionary_title_verbs",
    "dictionary_title_all",
    "mode_definitions",
    "mode_idioms",
    "mode_adjectives",
    "mode_all",
    "theme_light",
    "theme_dark",
    "theme_system",
    "tab_dictionary",
    "tab_favorites",
    "tab_profile",
    "tab_quiz",
    "settings_dictionary_mode",
    "settings_show_daily_word",
    "menu_show_daily_word",
    "quiz_duration_title",
    "btn_start_quiz",
    "btn_try_luck",
    "profile_title",
    "stat_favorites",
    "stat_searches",
    "stat_my_words",
    "stat_dictionary",
    "language_system",
    "settings_update",
    "settings_help",
    "settings_rate_us",
    "settings_share",
    "settings_privacy_policy",
    "achievement_first_step_title",
    "achievement_hunter_title",
    "achievement_writer_title",
    "weekly_last_7_days",
    "loading_subtitle",
    "settings_help_body",
]


def parse_strings(path: Path) -> dict[str, str]:
    data: dict[str, str] = {}
    text = path.read_text(encoding="utf-8")
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


def main() -> None:
    tr = parse_strings(BASE / "values-tr" / "strings.xml")
    default = parse_strings(BASE / "values" / "strings.xml")
    en = parse_strings(BASE / "values-en" / "strings.xml")

    print("=== KEY COUNTS ===")
    print(f"default: {len(default)}  tr: {len(tr)}  en: {len(en)}")
    missing_default_in_tr = sorted(set(default) - set(tr))
    missing_tr_in_default = sorted(set(tr) - set(default))
    if missing_default_in_tr:
        print(f"Keys in default missing from tr: {missing_default_in_tr}")
    if missing_tr_in_default:
        print(f"Keys in tr missing from default: {missing_tr_in_default}")

    print("\n=== PER-LOCALE (baseline: tr) ===")
    issues: list[str] = []
    for path in sorted(BASE.glob("values*/strings.xml")):
        folder = path.parent.name
        if folder == "values":
            continue
        data = parse_strings(path)
        missing = sorted(k for k in tr if k not in data)
        untranslated = sorted(
            k for k in tr
            if k in data and data[k] == tr[k] and k not in SKIP_SAME
        )
        if missing or untranslated:
            print(f"{folder}: missing={len(missing)} untranslated={len(untranslated)}")
            if missing:
                print(f"  missing: {', '.join(missing)}")
                issues.append(f"{folder} missing keys")
            if untranslated:
                print(f"  untranslated: {', '.join(untranslated)}")

    cd_open = "cd_open_menu"
    print("\n=== cd_open_menu ===")
    for path in sorted(BASE.glob("values*/strings.xml")):
        folder = path.parent.name
        data = parse_strings(path)
        if cd_open not in data:
            print(f"  MISSING: {folder}")

    print("\n=== OVERFLOW RISK (length vs tr, ratio >= 1.8 or len >= 28) ===")
    locales = {}
    for path in sorted(BASE.glob("values-*/strings.xml")):
        locales[path.parent.name] = parse_strings(path)

    for key in OVERFLOW_KEYS:
        tr_len = len(tr.get(key, ""))
        if not tr_len:
            continue
        hot: list[str] = []
        for folder, data in sorted(locales.items()):
            val = data.get(key, "")
            if not val:
                hot.append(f"{folder}:MISSING")
                continue
            ratio = len(val) / tr_len
            if len(val) >= 28 or ratio >= 1.8:
                hot.append(f"{folder}:{len(val)}({ratio:.1f}x)")
        if hot:
            print(f"{key} (tr={tr_len}): {', '.join(hot[:8])}{'...' if len(hot)>8 else ''}")

    print("\n=== PRIVACY LOCALES ===")
    expected = {
        "tr", "en", "de", "az", "kk", "ky", "uz", "tk", "zh-CN", "hi", "es", "fr",
        "ar", "bn", "pt", "ru", "ur", "id", "ja", "sw", "mr", "te", "ta", "vi", "ko",
    }
    found = {p.stem for p in PRIVACY_DIR.glob("*.json")}
    missing_priv = sorted(expected - found)
    extra_priv = sorted(found - expected)
    print(f"files: {len(found)} expected: {len(expected)}")
    if missing_priv:
        print(f"missing: {missing_priv}")
    if extra_priv:
        print(f"extra: {extra_priv}")
    for code in sorted(found):
        obj = json.loads((PRIVACY_DIR / f"{code}.json").read_text(encoding="utf-8"))
        secs = len(obj.get("sections", []))
        if secs != 12:
            print(f"  {code}: sections={secs} (expected 12)")

    if issues:
        print(f"\n=== SUMMARY: {len(issues)} locale(s) with missing keys ===")
    else:
        print("\n=== SUMMARY: all locales have full key sets (except tr-rCY orphan) ===")


if __name__ == "__main__":
    main()
