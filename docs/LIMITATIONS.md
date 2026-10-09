# Known Limitations

## Floating / freeform windows

Android does **not** expose a public API for third-party apps to prevent another app from entering OEM floating/pop-up window modes. FocusGuard detects when a blocked app's window becomes visible/interactive and shows a **full-screen accessibility overlay** (recommended) plus `BlockingActivity` when needed.

A brief flash of the blocked app may occur before the accessibility event is delivered.

## Launcher icon gray-out / disable

Stock Android launchers do **not** let normal apps gray out or hide other apps' icons. FocusGuard:

- Shows **grayed icons** in its own Blocked Apps list.
- Blocks launches via accessibility when an app window becomes visible (icon tap, recents, float, etc.).
- Offers an **experimental** Dashboard toggle that calls `PackageManager.setApplicationEnabledSetting`; this typically fails with `SecurityException` on consumer devices unless the app is Device Owner / privileged.

Do not rely on launcher icon hiding on Motorola or other OEM launchers without enterprise provisioning.

## Motorola Edge 50 Ultra (primary device)

Recents → Floating Window behavior must be validated on-device using Diagnostics export. Emulator behavior is not a substitute for Motorola-specific recents UI.

**Status:** Requires on-device capture (see `FLOATING_WINDOW_TEST.md`). Tune `WindowStateDetector` / exposure heuristics from exported JSONL if floating windows are active but not focused.

## PIN

PIN protects **Settings** (including master blocking switches, Block now, and overlay), **Apps**, and **Schedules**. It does not affect detection or blocking logic at runtime, and does not prevent uninstall or disabling the accessibility service from Android system settings.

## Device Owner

Stronger enforcement via Device Policy Manager is documented as an optional future mode and is **not** enabled for normal installs.

## Battery

Detection is event-driven from window accessibility events. There is no continuous screen content scraping loop.
