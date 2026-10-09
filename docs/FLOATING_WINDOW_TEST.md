# Floating Window Diagnostic Procedure

Primary device: **Motorola Edge 50 Ultra**

## Steps

1. Install FocusGuard and enable the accessibility service.
2. Open **Diagnostics** → **START RECORDING**.
3. Open a test application normally (e.g. Chrome or a non-critical app).
4. Press **Home**.
5. Open **Recents**.
6. On the test app card, choose **Floating window** / **Pop-up window** (Motorola wording).
7. Interact with the app inside the floating window for ~10 seconds.
8. Close the floating window.
9. Return to FocusGuard → **STOP RECORDING** → **Export diagnostic log**.

## What to look for in the export

- `TYPE_WINDOWS_CHANGED` followed by `TYPE_APPLICATION` with smaller `bounds` than the display.
- `presentation` field transitioning to `Floating`.
- `transitionNote` values such as `floating_window_detected` or `presentation_changed`.
- `foregroundPackageHeuristic` matching the test app while floating.

## Blocking validation (after blocking is configured)

Repeat the same steps with the test app in the blocked list. Expected: `BlockingActivity` appears over the floating window. Record detection delay from diagnostic timestamps if available.
