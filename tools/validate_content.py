#!/usr/bin/env python3
"""Validate schema invariants, translations, checksums and content budgets. No dependencies."""
import hashlib
import json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "app/src/main/assets"
def validate():
    manifest = json.loads((ROOT / "content/manifest.json").read_text())
    snapshots = {}
    for pack in manifest["packs"]:
        path = (ROOT / pack["path"]).resolve()
        assert path.is_relative_to(ROOT.resolve()), "Unsafe path"
        data = path.read_bytes()
        assert len(data) == pack["sizeBytes"], f"Size: {path}"
        assert hashlib.sha256(data).hexdigest() == pack["sha256"], f"Checksum: {path}"
        if not pack["bundled"]:
            continue
        obj = json.loads(data)
        assert obj["lang"] == pack["lang"] and obj["subjectId"] == pack["subject"]
        assert len(obj["units"]) == 1
        canonical = {}
        for unit in obj["units"]:
            assert len(unit["lessons"]) == 2
            for lesson in unit["lessons"]:
                assert lesson["id"] not in canonical, "Duplicate lesson"
                assert lesson["reviewed"] is False
                assert lesson["cards"] and len(lesson["activities"]) == 5
                activities = {}
                for a in lesson["activities"]:
                    assert a["id"] not in activities, "Duplicate activity"
                    assert a["prompt"] and a["explanation"]
                    assert a["type"] in {"MCQ", "LISTEN_PICK", "MATCH_PAIRS", "NUMBER_ENTRY"}
                    if a["type"] in {"MCQ", "LISTEN_PICK"}:
                        assert 0 <= a["correctIndex"] < len(a["options"])
                    if a["type"] == "MATCH_PAIRS":
                        ids = [p["id"] for p in a["pairs"]]
                        assert len(set(ids)) == len(ids) >= 2
                    activities[a["id"]] = (a["type"], a.get("correctIndex"), a.get("answer"), tuple(p["id"] for p in a.get("pairs", [])), len(a.get("options", [])))
                canonical[lesson["id"]] = activities
        key = pack["lang"]
        snapshots.setdefault(key, {})[pack["subject"]] = canonical
    assert set(snapshots) == {"en", "hi", "mr"}
    assert snapshots["en"] == snapshots["hi"] == snapshots["mr"], "IDs or answers differ by language"
    assert set(snapshots["en"]) == {"math", "vocab", "lifeskills"}
    for image in ROOT.rglob("*.webp"):
        assert image.stat().st_size <= 40960
    print("PASS: 9 language/subject packs, 18 lessons, 90 activities; IDs, answers, sizes and SHA-256 agree.")
if __name__ == "__main__":
    validate()
