# FocusGuard

FocusGuard is an Android app/site blocker focused on detecting blocked applications across normal launch, recents, floating windows, split screen, PiP, and related multi-window paths using **AccessibilityService** window APIs.

## Requirements

- Android 10+ (API 29+)
- JDK 17 for local builds
- Android SDK (API 35)

## Build

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Debug APK:

`app/build/outputs/apk/debug/app-debug.apk`

Release APK:

```bash
./gradlew :app:assembleRelease
```

## Install

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Enable Accessibility (required)

1. Open **Settings → Accessibility**.
2. Find **FocusGuard** and enable the service.
3. Confirm the dashboard shows **Accessibility Service: CONNECTED**.

Blocking does **not** run when the service is disconnected.

## Schedules (groups)

1. Open **Schedules** → **+** to create a block group.
2. **Edit apps in group** to add Instagram, etc.
3. **Add schedule or limit**:
   - **Block on a weekly schedule** — week heatmap, quick templates, multiple time slots.
   - **Screen time limit** — daily per weekday or hourly cap (requires **Usage access** in system settings).

Groups replace the old package-name schedule form. Apps in a group with **manual-only** (no schedule/limit) are blocked only when **Block now** is on.

## Diagnostic testing

1. Open **Diagnostics** in the app.
2. Tap **START RECORDING**.
3. Run the floating-window procedure in [`docs/FLOATING_WINDOW_TEST.md`](docs/FLOATING_WINDOW_TEST.md).
4. Tap **STOP RECORDING** and **Export diagnostic log**.

## Architecture

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Known limitations

See [`docs/LIMITATIONS.md`](docs/LIMITATIONS.md).

## Manual test checklist

See [`docs/MANUAL_TEST_CHECKLIST.md`](docs/MANUAL_TEST_CHECKLIST.md).
