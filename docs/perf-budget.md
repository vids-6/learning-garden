# Performance budget report

Measured 2026-10-30 on macOS Apple Silicon. Gradle 9.6.0, AGP 9.4.1, Kotlin 2.2.10, minSdk24, compile37.1/target37. R8 minification and resource shrinking are enabled for release.

| Budget | Evidence | Status |
|---|---|---|
| Universal release APK ≤15 MB | **2,307,737 bytes / 2.308 MB** (unsigned build artifact); includes arm64-v8a, armeabi-v7a, x86, x86_64 | PASS |
| Cold start <2500 ms on low-end hardware | Saved-learner release cold starts: **212, 213, 202, 195, 207 ms**, median **207 ms**, max **213 ms** | Pass on available emulator; low-end hardware unverified |
| Offline learning, no startup downloads | No INTERNET/storage permission in merged release manifest; all three languages passed offline UI tests | PASS |
| Baseline profile if feasible | `assets/dexopt/baseline.prof` and `.profm` included, AGP also emits dex metadata files | PASS, starter profile |
| Images ≤40 KB; low-resource audio | Custom vectors only; no raster/audio payloads. Local-only TTS is lazy | PASS |

APK SHA-256: `58886ee0855cf0d639fad1def8456b2d686a8635a0f6327fe03a4cca314dfacd`.

Artifact: `app/build/outputs/apk/release/app-release-unsigned.apk`. Verify again with `python3 tools/report_apk.py`. The test installation used the same minified artifact signed locally with the existing Android debug key solely for emulator testing; release output remains unsigned for your distribution signing process.

## Startup method and limits

Android 17/API37 Small Phone, 720×1280 at 320dpi (360dp width), software graphics / host CPU acceleration. Although the AVD specifies 1024 MB and an explicit `-memory 1024` override was tried, this installed emulator raises RAM to **4096 MB**. `/proc/meminfo` reports MemTotal 4,008,512 kB. A second emulator instance was rejected and was not running during saved-learner measurements. This is not a validated 1–2 GB device result.

Command: `adb -s emulator-5554 shell am start -S -W -n org.rural.learn/.MainActivity`, five times. Each reports `LaunchState: COLD`. Values are Activity TotalTime (time to initial display), not a macrobenchmark of full interactive readiness. A fictional saved learner and bundled English subjects were present; airplane mode was enabled, Wi-Fi and data disabled. Fresh language-selection starts measured 257, 248, 233, 210, 215 ms (median 233 ms). File caches were not flushed between launches. No physical low-end, API24, sustained-memory or frame-jank claim is made.

## Included hardening

- Manual startup baseline rules in `app/src/main/baseline-prof.txt` and `startup-prof.txt`, combined with dependency profiles and compiled by AGP. These are starter rules, not device-recorded journey coverage. A dedicated macrobenchmark module was intentionally avoided to preserve the single-module/library constraint.
- StrictMode disk/network and leaked-closable diagnostics in debug, log-only. Release excludes debug diagnostics through BuildConfig/R8.
- Bundled subset of Noto Sans Devanagari Regular/Bold with retained shaping tables; font license in Noto-OFL.txt. No image loader, media framework, downloaded fonts or animation payloads.
- Content parsing and file checks run on Dispatchers.IO. Room/DataStore operations are asynchronous. Audio/TTS initialize on demand, never at app start.
- No auto-downloads or auto-sync scheduling; the user explicitly invokes each simulation. WorkManager has unique connected work and exponential backoff.

Run the low-end and accessibility checklist in evaluation-plan.md before a field trial.

One idle release Home memory snapshot (`adb shell dumpsys meminfo org.rural.learn`): TOTAL PSS **26,115 KB**, TOTAL RSS **162,152 KB**, no swap. This is a single idle observation, not a sustained-memory/jank benchmark.
