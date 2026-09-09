#!/usr/bin/env python3
"""Generate Android strings.xml for supported app locales from scripts/locales/*.json."""
import json
import os

BASE = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res")
LOCALES_DIR = os.path.join(os.path.dirname(__file__), "locales")

FOLDERS = {
    "tr": "values-tr",
    "az": "values-az",
    "kk": "values-kk",
    "ky": "values-ky",
    "uz": "values-uz",
    "tk": "values-tk",
    "en": "values-en",
    "es": "values-es",
    "de": "values-de",
    "fr": "values-fr",
    "ar": "values-ar",
    "hi": "values-hi",
    "zh-CN": "values-zh-rCN",
    "bn": "values-bn",
    "pt": "values-pt",
    "ru": "values-ru",
    "ur": "values-ur",
    "id": "values-in",
    "ja": "values-ja",
    "sw": "values-sw",
    "mr": "values-mr",
    "te": "values-te",
    "ta": "values-ta",
    "vi": "values-vi",
    "ko": "values-ko",
}

REQUIRED_KEYS = [
    "app_name", "tab_dictionary", "tab_quiz", "tab_favorites", "tab_profile",
    "preparing_label", "search_placeholder", "word_count_loaded",
    "dictionary_title_synonyms", "dictionary_title_verbs", "dictionary_title_all",
    "settings_dictionary_mode", "mode_synonyms", "mode_verbs", "mode_definitions",
    "mode_idioms", "mode_adjectives", "mode_all",
    "label_synonym_text", "label_meaning_text", "label_definition_text",
    "label_synonyms_multiple_text", "label_meanings_multiple_text",
    "quiz_question_synonym", "quiz_question_meaning",
    "daily_word_title", "btn_try_luck", "recent_searches", "no_result_found",
    "add_new_word", "label_word", "label_synonym", "btn_add", "btn_ok", "btn_cancel",
    "btn_start_quiz", "quiz_duration_title", "game_over_title", "btn_try_again",
    "profile_title", "weekly_activity", "weekly_total", "weekly_average", "weekly_empty",
    "stat_favorites", "stat_searches", "stat_my_words", "stat_dictionary",
    "achievements_title", "settings_title", "appearance_mode",
    "theme_light", "theme_dark", "theme_system",
    "achievement_first_step_title", "achievement_first_step_desc",
    "achievement_hunter_title", "achievement_hunter_desc",
    "achievement_writer_title", "achievement_writer_desc",
    "stat_correct", "stat_wrong", "quiz_duration_unlimited", "quiz_next_question",
    "quiz_score_label", "quiz_answered_questions", "tab_definition",
    "definition_word_count", "definition_random_word", "rate_us_button", "settings_about",
    "settings_version_label", "settings_rate_us", "settings_rate_us_desc",
    "settings_share", "settings_share_desc", "settings_share_text",
    "settings_privacy_policy", "settings_privacy_policy_desc",
    "settings_rate_open_failed", "settings_link_open_failed",
    "settings_update", "settings_update_desc",
    "update_check_checking", "update_check_up_to_date", "update_check_unavailable",
    "update_dialog_title", "update_dialog_message", "update_dialog_positive", "update_dialog_later",
    "settings_help", "settings_help_desc", "settings_help_body",
    "rate_dialog_positive", "rate_dialog_later", "cd_back", "cd_open_menu", "privacy_policy_url",
    "tdk_source_label", "tdk_loading", "tdk_speak", "tdk_proverbs", "tdk_compounds",
    "tdk_fallback_hint", "tdk_discovery_hint",
    "settings_language", "language_system", "language_show_all", "language_show_less",
    "exit_dialog_title", "exit_dialog_message",     "exit_dialog_confirm", "exit_dialog_cancel",
    "favorites_title", "search_short_placeholder", "btn_study", "cd_clear",
    "favorites_empty_title", "favorites_empty_search_title", "favorites_empty_desc", "favorites_empty_search_desc",
    "cd_delete", "learning_no_favorites_title", "learning_no_favorites_message",
    "learning_cards_title", "learning_word_label", "learning_synonym_label",
    "learning_finish", "learning_next_word", "learning_flip_hint", "loading_subtitle",
    "cd_close", "cd_add_word", "cd_settings", "mode_hint_title", "mode_hint_body", "mode_hint_ok",
    "menu_show_daily_word", "share_word_title", "word_tree_title", "cd_reset",
    "cd_favorite", "cd_listen", "cd_share", "cd_speak", "cd_voice_search", "voice_search_prompt",
    "weekly_last_7_days", "weekly_day_detail", "day_mon", "day_tue", "day_wed", "day_thu", "day_fri", "day_sat", "day_sun",
    "day_mon_full", "day_tue_full", "day_wed_full", "day_thu_full", "day_fri_full", "day_sat_full", "day_sun_full",
    "notification_channel_name", "notification_title", "notification_body",
    "share_mode_verbs", "share_mode_definitions", "share_mode_idioms", "share_mode_adjectives", "share_mode_all",
]


def load_locale(code: str) -> dict:
    path = os.path.join(LOCALES_DIR, f"{code}.json")
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    missing = [k for k in REQUIRED_KEYS if k not in data]
    if missing:
        raise ValueError(f"{code}.json missing keys: {', '.join(missing)}")
    extra = [k for k in data if k not in REQUIRED_KEYS]
    if extra:
        raise ValueError(f"{code}.json has unknown keys: {', '.join(extra)}")
    return data


def android_escape(value: str) -> str:
    return (
        value.replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace('"', "&quot;")
    )


def write_locale(code: str, folder: str, strings: dict) -> None:
    lines = ["<resources>"]
    for key in REQUIRED_KEYS:
        value = strings[key]
        lines.append(f'    <string name="{key}">{android_escape(value)}</string>')
    lines.append("</resources>")
    path = os.path.join(BASE, folder, "strings.xml")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(lines) + "\n")
    print(f"Wrote {path} ({len(REQUIRED_KEYS)} keys)")


def main() -> None:
    for code, folder in FOLDERS.items():
        write_locale(code, folder, load_locale(code))
    print(f"Done: {len(FOLDERS)} locales, {len(REQUIRED_KEYS)} keys each")


if __name__ == "__main__":
    main()
