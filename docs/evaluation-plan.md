# Evaluation plan

Use a clean test device, then repeat on a shared device with two learners. Content is unreviewed; obtain educator review and study consent before evaluation with children.

## Offline and recovery

- [ ] Install APK, enable airplane mode, disable Wi-Fi; cold launch without any prior app launch.
- [ ] Select each language using native names. Confirm the speaker button never opens a download prompt when voices are absent.
- [ ] Create two learners with different avatars; switch quickly and confirm their XP/sessions stay separate.
- [ ] Finish both lessons in all three subjects in English, Hindi and Marathi; verify the four activity types and lesson unlocks.
- [ ] Submit one wrong answer; force-stop. Reopen, tap Continue lesson. The retry count and feedback must be restored.
- [ ] Answer correctly on retry: +5 XP. Wrong twice: answer + explanation, no activity XP. Next advances only after feedback.
- [ ] Force-stop immediately after a correct answer and after final Next. Check no duplicate XP and completion restored.
- [ ] Reboot device mid-lesson. Repeat previous check. Rotate at cards, keypad, feedback, result and survey.
- [ ] Change language mid-lesson; confirm matching IDs keep the same answers and activity index.
- [ ] Start a different lesson while one is incomplete; confirm replacement prompt and Cancel behavior.
- [ ] Replay a completed lesson; best score/stars never decrease; COMEBACK appears. Complete in another language for BILINGUAL.

## Research, packs and sync

- [ ] Submit all three emoji survey answers and clarity feedback. Reopen result; no duplicate survey records.
- [ ] Long-press logo. Wrong PIN rejected; 2468 opens aggregate metrics. Home and Back leave the gate locked.
- [ ] Export CSV through system document picker; cancel once, then save. Open externally and verify quoting, headers and absence of names.
- [ ] Downloads: cancel consent; no file installed. Accept, check installed state, size and restart persistence.
- [ ] Corrupt a demo checksum in a test build: install fails without marking the pack installed.
- [ ] Simulate sync online; verify success. Third fake exchange fails and retries with exponential backoff. Core learning still works.
- [ ] Schedule sync then go offline: learning continues, unique work waits for connectivity, local progress remains intact.

## Accessibility and low-resource operation

- [ ] Set OS font scale to 1.5×; repeat in all languages. Verify no clipped text or unreachable controls on 360dp-wide screen.
- [ ] TalkBack: meaningful icon labels, button ordering, selected avatars/pairs and spoken feedback. All actions ≥56dp targets.
- [ ] Inspect high-contrast palette outdoors; all feedback must contain text/symbols, never color alone.
- [ ] Enable reduced motion: no navigation/plant animations. Disable audio: no sound. Text always available.
- [ ] Use Android 7/API24 and a 1 GB emulator/device, then a 2 GB device. Capture cold start, frame jank and memory after 20 lessons.
- [ ] Run `adb shell am start -S -W -n org.rural.learn/.MainActivity` at least five times using a locally signed release build. Record TotalTime median and maximum. Target <2500ms on low-end hardware; host acceleration alone does not establish this claim.
- [ ] Verify release universal APK ≤15,000,000 bytes and inspect merged manifest for no INTERNET permission.

Automated results and measurement limits are recorded separately in phase-verification.md and perf-budget.md. Unchecked items are manual evaluation tasks, not claims of completed testing.
