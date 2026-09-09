import json
from pathlib import Path

OUT = Path(__file__).parent / "privacy_locales"
required = [
    "title", "pageTitle", "metaApp", "metaPackage", "metaDeveloper",
    "metaUpdated", "updatedDate", "langLabel", "intro", "sections", "footer",
]
expected = [
    "tr", "en", "de", "az", "kk", "ky", "uz", "tk", "zh-CN", "hi", "es", "fr",
    "ar", "bn", "pt", "ru", "ur", "id", "ja", "sw", "mr", "te", "ta", "vi", "ko",
]
files = sorted(p.name for p in OUT.glob("*.json"))
print("Files:", len(files))
print("Missing:", [c for c in expected if f"{c}.json" not in files])
print("Extra:", [f for f in files if f.replace(".json", "") not in expected])
errors = []
for code in expected:
    data = json.loads((OUT / f"{code}.json").read_text(encoding="utf-8"))
    for k in required:
        if k not in data:
            errors.append(f"{code}: missing {k}")
    if len(data.get("sections", [])) != 12:
        errors.append(f"{code}: sections={len(data.get('sections', []))}")
    text = json.dumps(data)
    if "hayhak1@gmail.com" not in text:
        errors.append(f"{code}: no email")
    if "com.hayhak.turkcesozluk" not in data["footer"]:
        errors.append(f"{code}: footer")
    if "https://sozluk.gov.tr" not in text:
        errors.append(f"{code}: no tdk")
    if "ThunderCraft" not in text:
        errors.append(f"{code}: no developer")
print("Errors:", errors or "none")
