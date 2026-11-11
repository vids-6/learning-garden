# Phase verification

| Phase | Gate | Result |
|---|---|---|
| P1 | `./gradlew assembleDebug testDebugUnitTest` | PASS, setup + locales + Room + loader + lesson rendering |
| P2 | `./gradlew assembleDebug testDebugUnitTest` | PASS, 13 tests total; activity engine, rewards, path and result |
| P3 | `./gradlew assembleDebug testDebugUnitTest`; content validator | PASS, 17 tests total; 9 bundled packs and optional demo |
| P4 | `./gradlew assembleDebug testDebugUnitTest` | PASS, 21 tests total; analytics, CSV escaping, survey persistence, fake sync and merge |
| P5 | `./gradlew assembleDebug assembleRelease testDebugUnitTest lintDebug connectedDebugAndroidTest` | PASS; 21 JVM tests + 3 instrumented tests; R8 + shrink + compiled baseline profile |

Final verification: 2026-10-30. Android 17 / API37 Small Phone emulator, 720×1280 / 320dpi. Emulator enforces 4 GB RAM. Wi-Fi and mobile data disabled, airplane mode set. Instrumented UI completes all four activity types in en/hi/mr at 1.5× text size, recreates the Activity, checks 240 XP across three runs and BILINGUAL. Persistence tests close and reopen an actual on-disk Room database, verify retry state and exactly-once completion/survey rewards, and reject corrupted optional packs.

`python3 tools/device_smoke.py`: PASS on the installed debug APK: a wrong first answer and pending retry feedback survived actual `am force-stop`, relaunch and Continue lesson. LessonSession, LessonProgress and XpLedger rows were identical before and after. This is separate from Activity recreation and database reopen testing.

`python3 tools/validate_content.py`: PASS: 9 bundled language/subject packs, 18 lessons, 90 activities; identical IDs and answers; matching byte sizes and checksums. `python3 tools/report_apk.py`: PASS: universal APK, baseline profile, permission and size checks.

Lint task succeeds with warnings about pinned dependency versions, backup metadata and four unused localized labels; no lint errors. Initial instrumentation harness failures were resolved by using AndroidX runner 1.7.0, JUnit extension 1.3.0 and Espresso 3.7.0, and ensuring JUnit test methods return Unit. Final reports have zero test failures.

Reports: `app/build/reports/tests/testDebugUnitTest/index.html`, `app/build/reports/androidTests/connected/debug/index.html`, `app/build/reports/lint-results-debug.html`. Do not treat automated tests as a substitute for the manual API24, physical low-memory and TalkBack checklist.
