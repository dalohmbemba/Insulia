# Insulia

An Android app for people living with diabetes: log glucose readings, keep food
notes, track clinic appointments and review care guidance.

The app is built with Kotlin and Jetpack Compose (Material 3). It is currently
a **prototype**: the data is example data held in memory, there is no server,
and no data is stored between app restarts.

## Requirements

- Android Studio with the Android SDK installed
- JDK 17+
- A device or emulator running Android 6.0 (API 23) or newer

## Build and run

```bash
./gradlew assembleDebug        # build the debug APK
./gradlew installDebug         # build and install on a connected device
```

## Tests

```bash
./gradlew testDebugUnitTest            # JVM tests for the shared state
./gradlew connectedAndroidTest         # UI tests on a connected device
./gradlew pixel2api30DebugAndroidTest  # UI tests on a managed emulator image
```

The managed device (`pixel2api30`) downloads its system image on the first run,
so the first execution takes a while.

## Project structure

```
app/src/main/java/com/example/insulia/
├── MainActivity.kt              Activity entry point, applies the theme
├── InsuliaApp.kt                Navigation host, back stack, dialogs
├── data/
│   ├── Models.kt                GlucoseReading, FoodNote, Appointment, ...
│   ├── InsuliaViewModel.kt      All app state in one place
│   └── DateFormats.kt           Date helpers (no java.time, API 23 safe)
├── navigation/Screen.kt         The screens of the app
└── ui/
    ├── components/              Cards, bottom bar, header, chart, quick actions
    ├── dialogs/                 Log reading, food note and booking dialogs
    ├── screens/                 One file per screen
    └── theme/                   Colours, typography and the Material theme
```

All user facing text lives in `app/src/main/res/values/strings.xml`.

## How it works

**State.** `InsuliaViewModel` holds the readings, food notes, appointments and
notifications. Every screen reads from it, so a reading that is logged on the
home screen immediately appears in the trend chart, the glucose levels screen
and the personal records screen.

**Navigation.** `InsuliaApp` keeps a back stack of `Screen` values. The in-app
back arrow and the Android system back gesture both pop the same stack.
Selecting a bottom navigation tab replaces the stack instead of growing it, so
the stack never fills up with repeated screens.

**Theming.** The Insulia brand colours live in `ui/theme/Color.kt` and are
applied through `InsuliaTheme`. Dynamic (wallpaper) colour is deliberately not
used so the app looks the same on every device.

## Prototype limitations

- Data is example data and is not stored between app restarts.
- There is no sign-in and no server, so there is no sync between devices.
- The clinical content (target ranges, meal timetable, care guidance) is
  illustrative and must be replaced with content reviewed by a clinician
  before the app is used with real patients.
