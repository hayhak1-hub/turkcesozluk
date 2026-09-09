#!/usr/bin/env python3
"""Add language_show_all / language_show_less to all locale JSON files."""
import json
import os

LOCALES_DIR = os.path.join(os.path.dirname(__file__), "locales")

PATCHES = {
    "en": ("Show all languages", "Show less"),
    "es": ("Mostrar todos los idiomas", "Mostrar menos"),
    "de": ("Alle Sprachen anzeigen", "Weniger anzeigen"),
    "fr": ("Afficher toutes les langues", "Afficher moins"),
    "ar": ("عرض كل اللغات", "عرض أقل"),
    "hi": ("सभी भाषाएँ दिखाएँ", "कम दिखाएँ"),
    "zh-CN": ("显示所有语言", "收起"),
    "bn": ("সব ভাষা দেখান", "কম দেখান"),
    "pt": ("Mostrar todos os idiomas", "Mostrar menos"),
    "ru": ("Показать все языки", "Показать меньше"),
    "ur": ("تمام زبانیں دکھائیں", "کم دکھائیں"),
    "id": ("Tampilkan semua bahasa", "Tampilkan lebih sedikit"),
    "ja": ("すべての言語を表示", "折りたたむ"),
    "sw": ("Onyesha lugha zote", "Onyesha chache"),
    "mr": ("सर्व भाषा दाखवा", "कमी दाखवा"),
    "te": ("అన్ని భాషలు చూపించు", "తక్కువ చూపించు"),
    "ta": ("அனைத்து மொழிகளையும் காட்டு", "குறைவாக காட்டு"),
    "vi": ("Hiển thị tất cả ngôn ngữ", "Thu gọn"),
    "ko": ("모든 언어 표시", "간단히 보기"),
}

for code, (show_all, show_less) in PATCHES.items():
    path = os.path.join(LOCALES_DIR, f"{code}.json")
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    data["language_show_all"] = show_all
    data["language_show_less"] = show_less
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")
    print(f"Patched {code}.json")
