#!/usr/bin/env python3
"""Check universal APK size, offline permission contract and bundled baseline profile."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
apk = root / "app/build/outputs/apk/release/app-release-unsigned.apk"
data = apk.read_bytes()
assert len(data) <= 15_000_000, f"APK exceeds 15MB: {len(data)}"
with ZipFile(apk) as z:
    assert "assets/dexopt/baseline.prof" in z.namelist()
    abis = {p.split('/')[1] for p in z.namelist() if p.startswith('lib/')}
    assert {"arm64-v8a", "armeabi-v7a", "x86", "x86_64"} <= abis
manifest = root / "app/build/intermediates/merged_manifests/release/processReleaseManifest/AndroidManifest.xml"
ns = "{http://schemas.android.com/apk/res/android}"
permissions = {e.get(ns+"name") for e in ET.parse(manifest).getroot().findall("uses-permission")}
assert "android.permission.INTERNET" not in permissions
assert not any("STORAGE" in (p or "") for p in permissions)
print(f"PASS: universal release {len(data):,} bytes ({len(data)/1_000_000:.3f} MB); baseline profile bundled; no INTERNET/storage permission.")
print("SHA256:", hashlib.sha256(data).hexdigest())
