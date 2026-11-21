# Insulia

An Android app for people living with diabetes: log glucose readings, keep food
notes, track clinic appointments and review care guidance.

The app is built with Kotlin and Jetpack Compose (Material 3). Patient data is
stored in Firebase Authentication and Cloud Firestore, with the Firestore
offline cache enabled. Demo content is only an in-memory fallback used when
Firebase cannot be reached at all; it is never written to a real account.

## Requirements

- Android Studio with the Android SDK installed
- JDK 17+
- A device or emulator running Android 6.0 (API 23) or newer
- A Firebase project on the **Spark (free)** plan. Cloud Functions are not
  required.

## Build and run

```bash
./gradlew assembleDebug        # build the debug APK
./gradlew installDebug         # build and install on a connected device
```

## Firebase setup (Spark plan)

1. Create the Firebase project and add the Android app
   `com.example.insulia`. Put the generated `google-services.json` in
   `app/`.
2. In **Authentication → Sign-in method**, enable **Anonymous** and
   **Email/password**. Anonymous sign-in lets a patient start immediately;
   email/password is used later to link the anonymous account or sign back in
   on a new device.
3. In **Firestore Database**, create the database. The app works with the
   default database.
4. Deploy the rules from this repository:

   ```bash
   firebase deploy --only firestore:rules --project insulia-fd4ad
   ```

   The rules can also be pasted into **Firestore Database → Rules** in the
   Firebase Console.

No Cloud Functions deployment is needed and the Blaze plan is not required.

### Patient IDs

The Patient ID is a permanent, automatically assigned value in the
`INS-XXXXXXXX` format. It is generated on the device with `SecureRandom` and
reserved in Firestore before it is saved:

- `/patientIds/{patientId}` is the uniqueness index. Its document ID is the
  Patient ID, so Firestore itself prevents two reservations of the same value.
- The app runs one Firestore transaction that reads the profile, checks
  candidate reservation documents, writes the reservation and updates
  `users/{uid}.patientId`. If a candidate is already reserved, it generates
  another candidate and retries inside the transaction.
- `getAfter()` in `firestore.rules` makes the reservation and profile state
  agree after the transaction: a profile may only receive an ID when the
  matching reservation exists after the transaction, and a reservation is only
  accepted when the caller's profile has that ID after the transaction.
- Once assigned, `patientId` cannot be changed or deleted by a client. The
  Profile screen shows the ID with a copy button; the Edit Profile screen does
  not expose it for editing.

Existing profiles keep their current ID. If an older profile has an ID but no
reservation, the app creates the reservation on the next load without changing
the ID or any other profile field. A profile with no ID is assigned one on the
next load. A profile saved while offline is assigned an ID as soon as the
profile reaches Firestore; WorkManager retries with exponential backoff so the
assignment survives process death.

### Deferred background work

WorkManager's scheduler holder is initialized in `InsuliaApplication`. It is
used only for work that must be guaranteed and can be deferred:

- `insulia_ensure_patient_id_<uid>` runs the Patient ID transaction when a
  profile is blank or needs a legacy reservation. It requires a network
  connection because Firestore transactions cannot run offline.
- `insulia_reconcile_notifications_<uid>` rebuilds the derived AlertsScreen
  documents from Firestore after readings, medications or appointments change,
  and once after the source collections first load. It also removes stale
  reminder documents.

Both jobs are unique per UID, idempotent, and use `ExistingWorkPolicy.KEEP` so
repeated triggers do not pile up. Each request carries the UID; a worker that
runs after the patient switched accounts exits without touching the new
account's data. Firestore snapshot listeners and ordinary record saves remain
in the ViewModel/repository; Firestore's offline cache already queues those
writes.

## Tests

```bash
./gradlew testDebugUnitTest            # JVM tests for the shared state
./gradlew connectedAndroidTest         # UI tests on a connected device
./gradlew pixel2api30DebugAndroidTest  # UI tests on a managed emulator image
```

The managed device (`pixel2api30`) downloads its system image on the first run,
so the first execution takes a while.

Patient ID tests cover the `INS-XXXXXXXX` format, the unambiguous alphabet,
collision retries and the maximum-attempt safety net. A notification test checks
that derived alerts keep stable document IDs. The rules were also
validated against the Firestore emulator for concurrent registration, duplicate
reservations, immutability and access control. Cross-account checks should be
run against the real project or the Firestore emulator:

1. Create two accounts and confirm each profile receives a different
   `INS-XXXXXXXX` value.
2. Try to write the same value to `/patientIds/{id}` from the second account;
   it must be rejected.
3. Link the first anonymous account to email/password and confirm the UID,
   Patient ID and all records are unchanged.
4. Sign in to that account on a second install and confirm the same Patient ID
   and records load.
5. Edit the profile; the ID must remain unchanged. Deleting the profile must be
   rejected by the rules.

## Project structure

```
app/src/main/java/com/example/insulia/
├── MainActivity.kt              Activity entry point, applies the theme
├── InsuliaApp.kt                Navigation host, back stack, dialogs
├── data/
│   ├── Models.kt                GlucoseReading, FoodNote, Appointment, ...
│   ├── InsuliaViewModel.kt      All app state in one place
│   ├── PatientIdFormat.kt       INS-XXXXXXXX validation
│   ├── PatientIdGenerator.kt    SecureRandom candidate generation
│   ├── PatientIdAllocator.kt    Collision retry loop
│   ├── NotificationReconciler.kt Shared derived-alert rules
│   ├── DateFormats.kt           Date helpers (no java.time, API 23 safe)
│   ├── remote/                  Firebase Auth and Firestore access
│   ├── repository/              ViewModel-facing data facade
│   └── work/                    WorkManager workers and scheduler
├── navigation/Screen.kt         The screens of the app
└── ui/
    ├── components/              Cards, bottom bar, header, chart, quick actions
    ├── dialogs/                 Log reading, food note and booking dialogs
    ├── screens/                 One file per screen
    └── theme/                   Colours, typography and the Material theme
```

All user facing text lives in `app/src/main/res/values/strings.xml`.

## How it works

**State.** `InsuliaViewModel` holds the readings, food notes, appointments,
medications and notifications. Every screen reads from it, so a reading that is
logged on the home screen immediately appears in the trend chart, the glucose
levels screen and the personal records screen. Writes go through
`PatientRepository` to Firestore and failed writes are surfaced to the patient.

**Offline support.** Firestore's persistent cache is configured in
`InsuliaApplication`. Records created without a connection are queued and
synchronised automatically. The Patient ID transaction and notification
reconciliation need the server, so WorkManager retries them when connectivity
returns instead of inventing unreserved IDs or stale alerts.

**Navigation.** `InsuliaApp` keeps a back stack of `Screen` values. The in-app
back arrow and the Android system back gesture both pop the same stack.
Selecting a bottom navigation tab replaces the stack instead of growing it, so
the stack never fills up with repeated screens.

**Theming.** The Insulia brand colours live in `ui/theme/Color.kt` and are
applied through `InsuliaTheme`. Dynamic (wallpaper) colour is deliberately not
used so the app looks the same on every device.

## Security model

- Every record is stored under `/users/{uid}/...`; a signed-in patient can only
  read and write their own tree.
- `/patientIds` is readable only as individual documents (for collision
  detection), cannot be listed, and is never writable by update or delete.
- A reservation can only be created when the caller's profile has the same ID
  after the transaction. This includes an existing legacy profile that is only
  being reserved, and prevents arbitrary reservations and duplicate IDs.
- Patient IDs are immutable after assignment and profiles cannot be deleted by
  a client. Account erasure, if added later, must use a trusted server process.
- The reservation documents contain only the owner UID and a timestamp; no
  clinical data is stored there.

## Prototype limitations

- The clinical content (target ranges, meal timetable, care guidance) is
  illustrative and must be replaced with content reviewed by a clinician
  before the app is used with real patients.
- Account linking and sign-in require the Email/Password provider to be enabled
  in the Firebase Console.
- A device without full Google Play services may not be able to complete
  Firebase Authentication or reCAPTCHA flows.
