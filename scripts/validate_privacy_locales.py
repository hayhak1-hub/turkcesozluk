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
    if data.get("updatedDate") != "2026-09-19":
        errors.append(f"{code}: stale update date")
    for required_text in (
        "Firebase Analytics",
        "Firebase Crashlytics",
        "https://firebase.google.com/support/privacy",
        "https://policies.google.com/privacy",
    ):
        if required_text not in text:
            errors.append(f"{code}: no {required_text}")
    if "We do not use ad SDKs, analytics SDKs" in text or "analitik SDK’sı veya kullanıcı takip" in text:
        errors.append(f"{code}: obsolete analytics denial")
print("Errors:", errors or "none")
