#!/usr/bin/env python3
"""Generate multilingual privacy-policy.html for web and in-app assets."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LOCALES_DIR = Path(__file__).resolve().parent / "privacy_locales"
ASSET_PATH = ROOT / "app" / "src" / "main" / "assets" / "privacy-policy.html"

LANG_LABELS = {
    "tr": "Türkçe",
    "en": "English",
    "de": "Deutsch",
    "az": "Azərbaycan",
    "kk": "Қазақша",
    "ky": "Кыргызча",
    "uz": "Oʻzbek",
    "tk": "Türkmen",
    "zh-CN": "中文",
    "hi": "हिन्दी",
    "es": "Español",
    "fr": "Français",
    "ar": "العربية",
    "bn": "বাংলা",
    "pt": "Português",
    "ru": "Русский",
    "ur": "اردو",
    "id": "Bahasa Indonesia",
    "ja": "日本語",
    "sw": "Kiswahili",
    "mr": "मराठी",
    "te": "తెలుగు",
    "ta": "தமிழ்",
    "vi": "Tiếng Việt",
    "ko": "한국어",
}

SHARED_STYLES = """
  :root { color-scheme: light dark; }
  body {
    font-family: -apple-system, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    max-width: 720px;
    margin: 0 auto;
    padding: 32px 20px 80px;
    line-height: 1.6;
    color: #1e1b2e;
    background: #f5f4fb;
  }
  .lang-bar {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
    font-size: 0.9rem;
  }
  .lang-bar select {
    flex: 1;
    max-width: 280px;
    padding: 8px 12px;
    border-radius: 10px;
    border: 1px solid #c7c9d9;
    background: #fff;
    font-size: 0.95rem;
  }
  h1 { font-size: 1.6rem; color: #4f46e5; margin-bottom: 4px; }
  .updated { color: #6b7280; font-size: 0.9rem; margin-bottom: 24px; }
  h2 { font-size: 1.15rem; color: #312e81; margin-top: 32px; margin-bottom: 8px; }
  p, li { font-size: 1rem; }
  ul { padding-left: 20px; }
  a { color: #4f46e5; }
  .card {
    background: #ffffff;
    border-radius: 16px;
    padding: 24px 28px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  }
  footer {
    margin-top: 40px;
    font-size: 0.85rem;
    color: #6b7280;
    text-align: center;
  }
  @media (prefers-color-scheme: dark) {
    body { background: #0f172a; color: #e2e8f0; }
    .card { background: #1e293b; }
    h1 { color: #818cf8; }
    h2 { color: #a5b4fc; }
    .updated, footer { color: #94a3b8; }
    a { color: #a5b4fc; }
    .lang-bar select { background: #1e293b; color: #e2e8f0; border-color: #475569; }
  }
"""

RENDER_SCRIPT = """
    const LANG_LABELS = {lang_labels};
    const I18N = {i18n_json};
    const FALLBACK = "en";

    function resolveLang(requested) {{
      if (!requested) return "tr";
      if (I18N[requested]) return requested;
      if (requested.startsWith("tr")) return "tr";
      const primary = requested.split("-")[0];
      if (I18N[primary]) return primary;
      return FALLBACK;
    }}

    function renderContent(lang) {{
      const resolved = resolveLang(lang);
      const t = I18N[resolved] || I18N[FALLBACK] || I18N.tr;
      document.documentElement.lang = resolved;
      document.title = t.pageTitle;
      const langLabel = document.getElementById("lang-label");
      if (langLabel) langLabel.textContent = t.langLabel;
      document.getElementById("title").textContent = t.title;
      document.getElementById("meta").innerHTML =
        t.metaApp + ': <strong>Türkçe Sözlük</strong> (' + t.metaPackage +
        ': <code>com.hayhak.turkcesozluk</code>)<br>' +
        t.metaDeveloper + ': ThunderCraft<br>' +
        t.metaUpdated + ': ' + t.updatedDate;

      let html = '<p>' + t.intro + '</p>';
      for (const s of t.sections) {{
        html += '<h2>' + s.title + '</h2>';
        for (const p of s.paragraphs) {{
          if (p) html += '<p>' + p + '</p>';
        }}
        if (s.items && s.items.length) {{
          html += '<ul>';
          for (const item of s.items) html += '<li>' + item + '</li>';
          html += '</ul>';
        }}
      }}
      document.getElementById("content").innerHTML = html;
      document.getElementById("footer").textContent = t.footer;

      const select = document.getElementById("lang-select");
      if (!select) return resolved;
      select.innerHTML = "";
      for (const [code, label] of Object.entries(LANG_LABELS)) {{
        const opt = document.createElement("option");
        opt.value = code;
        opt.textContent = label;
        select.appendChild(opt);
      }}
      select.value = lang in LANG_LABELS ? lang : resolved;
      return resolved;
    }}

    window.renderPrivacyPolicy = function(lang) {{
      renderContent(lang);
    }};
"""

WEB_BOOTSTRAP = """
    function getRequestedLang() {{
      return new URLSearchParams(location.search).get("lang") || "tr";
    }}

    function setLang(lang) {{
      const url = new URL(location.href);
      url.searchParams.set("lang", lang);
      history.replaceState(null, "", url);
      renderContent(lang);
    }}

    document.getElementById("lang-select").addEventListener("change", (e) => {{
      setLang(e.target.value);
    }});

    renderContent(getRequestedLang());
"""


def load_translations() -> dict[str, dict]:
    translations: dict[str, dict] = {}
    for path in sorted(LOCALES_DIR.glob("*.json")):
        translations[path.stem] = json.loads(path.read_text(encoding="utf-8"))
    if not translations:
        raise SystemExit(f"No locale JSON files found in {LOCALES_DIR}")
    return translations


def build_html(
    translations: dict[str, dict],
    *,
    include_lang_picker: bool,
    bootstrap: str,
) -> str:
    tr = translations["tr"]
    lang_bar = """
  <div class="lang-bar">
    <label for="lang-select" id="lang-label">{default_lang_label}</label>
    <select id="lang-select" aria-label="Language"></select>
  </div>
""".format(default_lang_label=tr["langLabel"]) if include_lang_picker else ""

    script = RENDER_SCRIPT.format(
        lang_labels=json.dumps(dict(sorted(LANG_LABELS.items(), key=lambda item: item[1].casefold())), ensure_ascii=False),
        i18n_json=json.dumps(translations, ensure_ascii=False),
    ) + bootstrap

    return f"""<!DOCTYPE html>
<html lang="tr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{tr["pageTitle"]}</title>
<style>{SHARED_STYLES}</style>
</head>
<body>
{lang_bar}
  <h1 id="title">{tr["title"]}</h1>
  <div class="updated" id="meta"></div>
  <div class="card" id="content"></div>
  <footer id="footer"></footer>
  <script>{script}</script>
</body>
</html>
"""


def main() -> None:
    translations = load_translations()
    missing = set(LANG_LABELS) - set(translations)
    if missing:
        raise SystemExit(f"Missing privacy locale files: {sorted(missing)}")

    web_html = build_html(translations, include_lang_picker=True, bootstrap=WEB_BOOTSTRAP)
    app_html = build_html(translations, include_lang_picker=False, bootstrap="\n    renderContent('tr');\n")

    for name in ("privacy-policy.html", "index.html"):
        path = ROOT / name
        path.write_text(web_html, encoding="utf-8")
        print(f"Wrote {path}")

    ASSET_PATH.parent.mkdir(parents=True, exist_ok=True)
    ASSET_PATH.write_text(app_html, encoding="utf-8")
    print(f"Wrote {ASSET_PATH}")


if __name__ == "__main__":
    main()
