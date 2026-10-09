# FocusGuard Architecture

## Layers

```
FocusGuardAccessibilityService
        ↓
AccessibilityEventTap + WindowSnapshotScanner
        ↓
WindowStateDetector + BlockedWindowExposure
        ↓
BlockingPolicy (+ ScheduleEvaluator)
        ↓
BlockingEngine
        ↓
BlockingActivity
```

## Accessibility layer

- `FocusGuardAccessibilityService` listens for `TYPE_WINDOW_STATE_CHANGED` and `TYPE_WINDOWS_CHANGED`.
- Optional verbose `TYPE_WINDOW_CONTENT_CHANGED` can be enabled for diagnostics recording.
- `WindowSnapshotScanner` calls `getWindows()` and builds immutable `WindowSnapshot` / `WindowFrame` models.
- No text/password collection; root nodes are used only for `packageName` and `className`.

## Detection

- `DefaultWindowStateDetector` classifies windows as full screen, floating (bounds smaller than display), split (divider + multiple app windows), or PiP.
- `BlockedWindowExposure` treats a package as exposed when its `TYPE_APPLICATION` window is active/focused, floating-visible, or in PiP.

## Policy and persistence

- Proto DataStore (`focus_guard_settings.pb`) stores blocked packages, schedules, PIN hash, and diagnostic flags.
- `BlockingPolicy` is independent of PIN.
- `ScheduleEvaluator` applies local timezone day/time windows, including overnight ranges.

## UI

- Jetpack Compose + Material 3 single-activity navigation.
- PIN gates settings routes only (blocked apps, schedules); not used by the blocking engine.

## Service health

- `ServiceBridge` exposes runtime connection state from the bound accessibility service.
- `AccessibilityServiceHelper` reads system settings to detect whether the user enabled the service.
