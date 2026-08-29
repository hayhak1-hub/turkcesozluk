#!/usr/bin/env python3
"""Generate Android strings.xml for supported app locales."""
import os
import xml.sax.saxutils as x

BASE = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res")

FOLDERS = {
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

# Each locale: full dict of string key -> value
LOCALES = {}

def add(locale, **strings):
    LOCALES[locale] = strings

add("en",
app_name="Turkish Dictionary", tab_dictionary="Dictionary", tab_quiz="Quiz", tab_favorites="Favorites", tab_profile="Profile",
preparing_label="Preparing", search_placeholder="Search for a word...", word_count_loaded="%1$d words loaded",
dictionary_title_synonyms="Synonyms", dictionary_title_verbs="Verbs", dictionary_title_all="Mixed Dictionary",
settings_dictionary_mode="Dictionary Mode", mode_synonyms="Synonyms", mode_verbs="Verbs", mode_definitions="Nouns",
mode_idioms="Idioms", mode_adjectives="Adjectives", mode_all="All",
label_synonym_text="Synonym", label_meaning_text="Meaning", label_definition_text="Definition",
label_synonyms_multiple_text="Synonyms (%1$d)", label_meanings_multiple_text="Meanings (%1$d)",
quiz_question_synonym="What is the synonym of", quiz_question_meaning="What is the meaning of",
daily_word_title="WORD OF THE DAY", btn_try_luck="TRY YOUR LUCK!", recent_searches="Recent Searches",
no_result_found="Word not found. Please check.", add_new_word="Add New Word", label_word="Word", label_synonym="Synonym",
btn_add="Add", btn_ok="OK", btn_cancel="Cancel", btn_start_quiz="START QUIZ",
quiz_duration_title="Set Quiz Duration", game_over_title="GAME OVER!", btn_try_again="TRY AGAIN",
profile_title="PROFILE & STATS", weekly_activity="Weekly Activity", weekly_total="This week", weekly_average="Daily avg.",
weekly_empty="No activity this week yet", stat_favorites="Favorites", stat_searches="Searches", stat_my_words="My Words",
stat_dictionary="Words", achievements_title="ACHIEVEMENTS", settings_title="SETTINGS", appearance_mode="Appearance",
theme_light="Light", theme_dark="Dark", theme_system="System",
achievement_first_step_title="First Step", achievement_first_step_desc="You searched your first word.",
achievement_hunter_title="Word Hunter", achievement_hunter_desc="You reached 10 favorite words.",
achievement_writer_title="Writer", achievement_writer_desc="You added your own word.",
stat_correct="Correct", stat_wrong="Wrong", quiz_duration_unlimited="Unlimited", quiz_next_question="NEXT QUESTION",
quiz_score_label="SCORE", quiz_answered_questions="Answered: %1$d", tab_definition="Nouns",
definition_word_count="%1$d words", definition_random_word="RANDOM WORD", rate_us_button="Rate Us", settings_about="About",
settings_version_label="Version %1$s (%2$d)", settings_rate_us="Rate us", settings_rate_us_desc="Share feedback on Google Play",
settings_share="Share", settings_share_desc="Recommend Turkish Dictionary to friends",
settings_share_text="Try Turkish Dictionary — synonyms, quiz and more.\n%1$s",
settings_privacy_policy="Privacy policy", settings_privacy_policy_desc="See how we handle your data",
settings_rate_open_failed="Could not open Play Store", settings_link_open_failed="Could not open link",
settings_update="Update", settings_update_desc="Check for a new version on Google Play",
update_check_checking="Checking…", update_check_up_to_date="You already have the latest version",
update_check_unavailable="Update check failed. Install the app from Google Play for this feature.",
update_dialog_title="Update available",
update_dialog_message="A newer version of Turkish Dictionary is on Google Play.\n\nYour version: %1$s\n\nUpdate now?",
update_dialog_positive="Update", update_dialog_later="Later", settings_help="Help", settings_help_desc="Learn how to use the app",
settings_help_body="• Search words in the Dictionary tab; switch between synonyms, verbs, adjectives, idioms and nouns.\n\n• Words not found locally can be searched via TDK Turkish Dictionary online.\n\n• Add favorites, practice with Quiz, and review with learning cards.\n\n• Manage stats and settings in Profile.",
rate_dialog_positive="Rate", rate_dialog_later="Later", cd_back="Back",
privacy_policy_url="https://hayhak1-hub.github.io/turkcesozluk/",
tdk_source_label="TDK Contemporary Turkish Dictionary", tdk_loading="Searching TDK…", tdk_speak="Read aloud",
tdk_proverbs="Proverb / idiom", tdk_compounds="Compound words",
tdk_fallback_hint="Not in local dictionary; showing TDK result.",
tdk_discovery_hint="If not found locally, TDK is searched too",
settings_language="Language", language_system="System", exit_dialog_title="Exit app?",
exit_dialog_message="You are about to leave Turkish Dictionary.", exit_dialog_confirm="Exit", exit_dialog_cancel="Cancel")

# For brevity in script: clone EN and patch key UI strings for remaining locales
def clone_from_en(code, **overrides):
    data = dict(LOCALES["en"])
    data.update(overrides)
    LOCALES[code] = data

clone_from_en("es", app_name="Diccionario Turco", tab_dictionary="Diccionario", tab_favorites="Favoritos", tab_profile="Perfil",
search_placeholder="Buscar una palabra...", settings_language="Idioma", language_system="Sistema",
exit_dialog_title="¿Salir de la app?", exit_dialog_confirm="Salir", exit_dialog_cancel="Cancelar")
clone_from_en("de", app_name="Türkisches Wörterbuch", tab_dictionary="Wörterbuch", tab_favorites="Favoriten", tab_profile="Profil",
search_placeholder="Wort suchen...", settings_language="Sprache", language_system="System",
exit_dialog_title="App beenden?", exit_dialog_confirm="Beenden", exit_dialog_cancel="Abbrechen")
clone_from_en("fr", app_name="Dictionnaire turc", tab_dictionary="Dictionnaire", tab_favorites="Favoris", tab_profile="Profil",
search_placeholder="Rechercher un mot...", settings_language="Langue", language_system="Système",
exit_dialog_title="Quitter l'app ?", exit_dialog_confirm="Quitter", exit_dialog_cancel="Annuler")
clone_from_en("ar", app_name="قاموس التركية", tab_dictionary="القاموس", tab_quiz="اختبار", tab_favorites="المفضلة", tab_profile="الملف",
search_placeholder="ابحث عن كلمة...", settings_language="اللغة", language_system="النظام",
exit_dialog_title="الخروج من التطبيق؟", exit_dialog_confirm="خروج", exit_dialog_cancel="إلغاء")
clone_from_en("hi", app_name="तुर्की शब्दकोश", tab_dictionary="शब्दकोश", tab_quiz="क्विज़", tab_favorites="पसंदीदा", tab_profile="प्रोफ़ाइल",
search_placeholder="शब्द खोजें...", settings_language="भाषा", language_system="सिस्टम",
exit_dialog_title="ऐप बंद करें?", exit_dialog_confirm="बाहर", exit_dialog_cancel="रद्द")
clone_from_en("zh-CN", app_name="土耳其语词典", tab_dictionary="词典", tab_quiz="测验", tab_favorites="收藏", tab_profile="个人",
search_placeholder="搜索词语...", settings_language="语言", language_system="系统",
exit_dialog_title="退出应用？", exit_dialog_confirm="退出", exit_dialog_cancel="取消")
clone_from_en("bn", app_name="তুর্কি অভিধান", tab_dictionary="অভিধান", tab_quiz="কুইজ", tab_favorites="পছন্দ", tab_profile="প্রোফাইল",
search_placeholder="শব্দ খুঁজুন...", settings_language="ভাষা", language_system="সিস্টেম",
exit_dialog_title="অ্যাপ বন্ধ করবেন?", exit_dialog_confirm="বের হন", exit_dialog_cancel="বাতিল")
clone_from_en("pt", app_name="Dicionário Turco", tab_dictionary="Dicionário", tab_favorites="Favoritos", tab_profile="Perfil",
search_placeholder="Pesquisar palavra...", settings_language="Idioma", language_system="Sistema",
exit_dialog_title="Sair do app?", exit_dialog_confirm="Sair", exit_dialog_cancel="Cancelar")
clone_from_en("ru", app_name="Турецкий словарь", tab_dictionary="Словарь", tab_quiz="Викторина", tab_favorites="Избранное", tab_profile="Профиль",
search_placeholder="Поиск слова...", settings_language="Язык", language_system="Система",
exit_dialog_title="Выйти из приложения?", exit_dialog_confirm="Выйти", exit_dialog_cancel="Отмена")
clone_from_en("ur", app_name="ترکی لغت", tab_dictionary="لغت", tab_quiz="کوئز", tab_favorites="پسندیدہ", tab_profile="پروفائل",
search_placeholder="لفظ تلاش کریں...", settings_language="زبان", language_system="سسٹم",
exit_dialog_title="ایپ بند کریں؟", exit_dialog_confirm="باہر", exit_dialog_cancel="منسوخ")
clone_from_en("id", app_name="Kamus Turki", tab_dictionary="Kamus", tab_quiz="Kuis", tab_favorites="Favorit", tab_profile="Profil",
search_placeholder="Cari kata...", settings_language="Bahasa", language_system="Sistem",
exit_dialog_title="Keluar dari app?", exit_dialog_confirm="Keluar", exit_dialog_cancel="Batal")
clone_from_en("ja", app_name="トルコ語辞書", tab_dictionary="辞書", tab_quiz="クイズ", tab_favorites="お気に入り", tab_profile="プロフィール",
search_placeholder="単語を検索...", settings_language="言語", language_system="システム",
exit_dialog_title="アプリを終了しますか？", exit_dialog_confirm="終了", exit_dialog_cancel="キャンセル")
clone_from_en("sw", app_name="Kamusi ya Kituruki", tab_dictionary="Kamusi", tab_quiz="Jaribio", tab_favorites="Vipendwa", tab_profile="Wasifu",
search_placeholder="Tafuta neno...", settings_language="Lugha", language_system="Mfumo",
exit_dialog_title="Toka kwenye programu?", exit_dialog_confirm="Toka", exit_dialog_cancel="Ghairi")
clone_from_en("mr", app_name="तुर्की शब्दकोश", tab_dictionary="शब्दकोश", tab_quiz="क्विझ", tab_favorites="आवडते", tab_profile="प्रोफाइल",
search_placeholder="शब्द शोधा...", settings_language="भाषा", language_system="सिस्टम",
exit_dialog_title="अॅप बंद करायचे?", exit_dialog_confirm="बाहेर", exit_dialog_cancel="रद्द")
clone_from_en("te", app_name="టర్కిష్ నిఘంటువు", tab_dictionary="నిఘంటువు", tab_quiz="క్విజ్", tab_favorites="ఇష్టమైనవి", tab_profile="ప్రొఫైల్",
search_placeholder="పదం వెతకండి...", settings_language="భాష", language_system="సిస్టమ్",
exit_dialog_title="యాప్ నుండి బయట?", exit_dialog_confirm="బయట", exit_dialog_cancel="రద్దు")
clone_from_en("ta", app_name="துருக்கிய அகராதி", tab_dictionary="அகராதி", tab_quiz="வினாடி", tab_favorites="பிடித்தவை", tab_profile="சுயவிவரம்",
search_placeholder="சொல் தேடுங்கள்...", settings_language="மொழி", language_system="கணினி",
exit_dialog_title="பயன்பாட்டிலிருந்து வெளியேற?", exit_dialog_confirm="வெளியேறு", exit_dialog_cancel="ரத்து")
clone_from_en("vi", app_name="Từ điển Thổ Nhĩ Kỳ", tab_dictionary="Từ điển", tab_quiz="Câu đố", tab_favorites="Yêu thích", tab_profile="Hồ sơ",
search_placeholder="Tìm từ...", settings_language="Ngôn ngữ", language_system="Hệ thống",
exit_dialog_title="Thoát ứng dụng?", exit_dialog_confirm="Thoát", exit_dialog_cancel="Hủy")
clone_from_en("ko", app_name="터키어 사전", tab_dictionary="사전", tab_quiz="퀴즈", tab_favorites="즐겨찾기", tab_profile="프로필",
search_placeholder="단어 검색...", settings_language="언어", language_system="시스템",
exit_dialog_title="앱을 종료할까요?", exit_dialog_confirm="종료", exit_dialog_cancel="취소")

def android_escape(value: str) -> str:
    return (
        value.replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace('"', "&quot;")
    )

KEY_ORDER = list(LOCALES["en"].keys())

def write_locale(code, folder):
    strings = LOCALES[code]
    lines = ['<resources>']
    for key in KEY_ORDER:
        value = strings.get(key, LOCALES["en"][key])
        lines.append(f'    <string name="{key}">{android_escape(value)}</string>')
    lines.append('</resources>')
    path = os.path.join(BASE, folder, "strings.xml")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(lines) + "\n")
    print("Wrote", path)

for code, folder in FOLDERS.items():
    write_locale(code, folder)

print("Done:", len(FOLDERS), "locales")
