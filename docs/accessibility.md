# Accessibility implementation and review

- Primary, option, keypad, navigation, audio, avatar, switch and dialog actions have minimum 56dp height. Switch target width is at least 56dp.
- Every drawable icon has a localized description. The plant Canvas exposes its localized growth stage. Avatar and selected pair controls expose selected state and visible check/arrow markers. Audio buttons contain both an icon and visible text.
- Success, retry, revealed answers, locks, matches, stars and installed packs use words or symbols as well as color. Feedback uses a polite live region.
- A high-contrast, warm light palette uses dark green text and white-on-dark-green actions; content never relies on dynamically chosen wallpaper colors.
- The effective text scale is the larger of system scale and the in-app preference. The in-app switch selects 1× or 1.5×. All primary screens scroll; text does not use fixed heights or ellipsis. The full four-type lesson flow is instrumented at 1.5× in en/hi/mr on a 360dp-wide emulator.
- Navigation transitions and plant drawings are static, including when reduced motion is enabled. There are no confetti or loss animations.
- Audio is optional; text equivalents remain available for every listening activity. The number keypad needs no system keyboard. Matching uses tap-tap, never drag-and-drop.

TalkBack ordering, outdoor contrast, larger device-specific font settings and Android 7 hardware remain manual checklist items in evaluation-plan.md. Automated completion does not establish screen-reader usability.
