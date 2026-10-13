# Prompts

## Prompt 1 - Project setup and scaffolding

````text
Build a native Android app in an existing, empty Android Studio project (Kotlin, Jetpack Compose). Do project setup and scaffolding ONLY. Do not implement feature logic yet.

App summary: a single-screen "Geo-Fenced Attendance" app. The user taps "Set Office Location" to save the current GPS coordinates locally, then "Mark Attendance" is enabled only when the live location is within 50 m of the saved point. A real-time distance indicator is shown. You will get an idea of the UI seeing the attached screenshot. Architecture must be MVI with unidirectional data flow, using Kotlin Flow/StateFlow.

1. Dependencies and build config
- Use a Gradle version catalog (libs.versions.toml). Look up the current stable versions of every library and plugin yourself; do not guess versions from memory. List the versions you chose in your final summary.
- Libraries: Compose BOM (Material 3, ui-tooling, lifecycle-runtime-compose, activity-compose), Hilt (with KSP, hilt-navigation-compose), Kotlin Coroutines, DataStore Preferences, Google Play Services Location, kotlinx-serialization only if you judge it necessary.
- Maps: add Maps Compose and play-services-maps, but read the API key from local.properties (key name MAPS_API_KEY) via manifestPlaceholders or the Secrets Gradle Plugin. Never hardcode the key. Add a MAPS_API_KEY=YOUR_KEY_HERE placeholder to a committed local.defaults.properties (or equivalent), and make sure local.properties is gitignored. The app must still build and run when no key is present.
- Test libraries: JUnit4, MockK, Turbine, kotlinx-coroutines-test, Compose UI test.
- Enable R8/minify and sensible ProGuard defaults for the release build type.

2. Architecture and package structure
- data/ : location (LocationRepository implementation using FusedLocationProviderClient exposed as a callbackFlow with high-accuracy priority), local (DataStore-backed OfficeLocationStore), repository implementations
- domain/ : models (GeoPoint, OfficeLocation, AttendanceStatus), repository interfaces, small use cases (ObserveDistanceToOffice, SetOfficeLocation, MarkAttendance)
- ui/attendance/ : AttendanceScreen, AttendanceViewModel, AttendanceUiState (immutable data class or sealed hierarchy covering: permission missing, permission permanently denied, GPS disabled, acquiring fix, in range, out of range), AttendanceUiEvent for one-off effects
- ui/theme/ : Material 3 theme
- di/ : Hilt modules
- Create stub classes and interfaces with TODO bodies only where logic is needed. Keep every file small and single-purpose.

3. App wiring
- Application class annotated with @HiltAndroidApp, MainActivity with @AndroidEntryPoint, registered in the manifest.
- Manifest: ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions, and the Maps meta-data entry driven by the placeholder.
- MainActivity should simply host AttendanceScreen (single screen, no navigation library needed).

4. Housekeeping
- .gitignore covering local.properties, keystores (*.jks, *.keystore), build outputs and IDE files.
- A README.md skeleton with these headings: Project Title and Description, Project Structure / Approaches, Generative AI Usage, How to Run (include the Maps key setup), Screenshots. Leave bodies as TODO.

5. Verification
- Run ./gradlew assembleDebug and ./gradlew testDebugUnitTest and fix any errors.
- Finish with a short summary: library versions chosen, the package tree, and anything you assumed.

Rules: no secrets or API keys in code or committed files; do not disable any lint or security checks to make the build pass; ask me before adding any dependency not listed above.
````

## Prompt 2 - Implementing the DATA and DOMAIN layers

````text
Before writing code, review the current package structure and the stubs created earlier, and reuse them. Tell me if you need to change any earlier decision.

1. Domain models and rules (pure Kotlin, no Android imports in domain/)
- GeoPoint(latitude, longitude), OfficeLocation(point, savedAtMillis), LocationFix(point, accuracyMeters, timestampMillis).
- A DistanceCalculator interface with a pure-Kotlin Haversine implementation (do not depend on android.location.Location in domain, so it is unit-testable on the JVM). Return metres as Double.
- Constants in one place: ALLOWED_RADIUS_METERS = 50.0 and MAX_ACCEPTABLE_ACCURACY_METERS = 30.0. The range check is inclusive (distance <= 50).
- A sealed result type for the proximity check, with at least: NoOfficeSet, AcquiringFix, WeakSignal(accuracy), InRange(distance), OutOfRange(distance). A fix whose accuracy is worse than the maximum must produce WeakSignal rather than InRange/OutOfRange, so a poor GPS fix can never enable check-in.
- An AttendanceWindow value (start and end LocalTime, defaulting to 09:00 to 10:30) and a pure function that says whether a given time is inside it. Inject java.time.Clock so tests can control time. I am treating the window as an assumption from the screenshot, so keep it easy to change.

2. Data layer
- LocationRepository interface (domain) and FusedLocationRepository implementation (data):
  - locationUpdates(): Flow<LocationFix> using FusedLocationProviderClient with Priority.PRIORITY_HIGH_ACCURACY, a sensible interval (about 2 s) and fastest interval, built with callbackFlow, removing updates in awaitClose.
  - getCurrentLocation(): suspend function returning a fresh high-accuracy fix for the "Set Office Location" action, with a timeout; return a typed failure (not an exception leaking to the UI) on timeout, missing permission or null result.
  - isLocationEnabled(): Flow<Boolean> that reacts to GPS/location provider changes (listen for provider changes via a broadcast receiver or LocationManager, cleaned up in awaitClose).
  - Catch SecurityException and expose it as a typed error rather than crashing.
- OfficeLocationStore backed by the existing DataStore: save(OfficeLocation), observe(): Flow<OfficeLocation?>. Store latitude and longitude with full Double precision (do not round them via Float or string formatting).
- AttendanceStore (same DataStore) that records the last attendance timestamp, so "marked today" can be derived. Keep it minimal.
- Hilt bindings for everything, with the Clock and DistanceCalculator provided so they can be replaced in tests.

3. Use cases (domain)
- ObserveProximity: combines office location and location updates into a Flow of the sealed proximity result described above. It should distinguish NoOfficeSet, AcquiringFix (no fix yet), and weak signal.
- SetOfficeLocation: gets a fresh fix, rejects it if accuracy is worse than the maximum (typed error), otherwise saves it.
- MarkAttendance: re-validates at the moment of the tap (fresh proximity check AND time window), not only trusting the last UI state, then records the timestamp. Return a typed result for each failure reason.

4. Unit tests (JUnit4, MockK, Turbine, coroutines-test; use fakes for repositories where cleaner than mocks)
- DistanceCalculator: known coordinate pairs with expected distances within a small tolerance, zero distance, and points just inside and just outside 50 m.
- Proximity logic: exactly 50.0 m is InRange, 50.1 m is OutOfRange, poor accuracy gives WeakSignal, no office gives NoOfficeSet, and no fix gives AcquiringFix.
- AttendanceWindow: boundaries at start and end, and outside the window.
- SetOfficeLocation and MarkAttendance: success paths and each typed failure.
- OfficeLocationStore: round-trips coordinates without losing precision (use a test DataStore with a temporary file).

5. Verification
- Run ./gradlew testDebugUnitTest and ./gradlew assembleDebug and fix failures properly. Do not delete or weaken tests to make them pass.
- Finish with a summary: the files created, the public interfaces, any assumptions, and anything that needs a physical device to verify.

Rules: no secrets in code; handle every failure explicitly instead of swallowing exceptions; keep functions small; ask me before adding any new dependency.
````

## Prompt 3 - Implementing the PRESENTATION logic

````text
Continue on the existing project. Data and domain layers are done and tested. In this step, implement the PRESENTATION LOGIC only: AttendanceViewModel, UI state, one-off events, and the permission and location-settings handling. Do not build the visual Compose UI yet (a minimal placeholder that renders the state as plain text is fine for manual testing).

Before writing code, review the existing domain interfaces, use cases and UI stubs, and reuse them. Tell me if you need to change any earlier decision.

1. UI state (ui/attendance/)
- AttendanceUiState: an immutable data class (or sealed hierarchy plus data class, your call, justify briefly) exposing:
  - permissionState: Granted, NotRequested, Denied (can ask again), PermanentlyDenied
  - isLocationEnabled: Boolean
  - officeLocation: OfficeLocation? (for the lat/lon pill)
  - proximity: the sealed proximity result from the domain layer
  - distanceMeters: Double? and a derived progress fraction (0f..1f) for the distance ring, clamped, where 0 m is full and a sensible maximum (say 200 m, as a named constant) is empty
  - isSettingOffice: Boolean (loading state for the Set Office Location button)
  - isMarkAttendanceEnabled: derived ONLY from: permission granted AND location enabled AND proximity is InRange AND inside the attendance window AND not already marked today
  - attendanceWindow, isWithinWindow, alreadyMarkedToday
  - a human-readable status model (not hardcoded strings in the ViewModel: expose a sealed StatusMessage type that the UI maps to string resources)
- AttendanceUiEvent (one-off effects via a Channel exposed as a Flow): ShowMessage(StatusMessage), RequestLocationSettingsResolution, OpenAppSettings, AttendanceMarked.

2. AttendanceViewModel (Hilt)
- Combines the following into a single StateFlow<AttendanceUiState> using stateIn(WhileSubscribed(5_000)): proximity flow from ObserveProximity, office location, location-enabled flow, a ticker or clock-driven flow that re-evaluates the attendance window (check once per minute and at the window boundaries, injectable Clock), and the last attendance timestamp.
- IMPORTANT: location updates must only be collected while the screen is active and permission is granted. When permission is missing, do not start the location flow at all. When it is granted, start it. This must react to the permission state changing without recreating the ViewModel.
- Intents (public functions): onPermissionResult(granted, shouldShowRationale), onSetOfficeLocationClicked(), onMarkAttendanceClicked(), onLocationSettingsResolved(success), onOpenSettingsClicked().
- onSetOfficeLocationClicked: guards for permission and GPS-enabled, shows loading state, calls SetOfficeLocation, maps each typed failure (timeout, weak accuracy, no permission, location off, unknown) to a distinct StatusMessage event. Prevent double taps while in progress.
- onMarkAttendanceClicked: calls MarkAttendance (which re-validates internally) and maps every typed result. Never trust isMarkAttendanceEnabled alone.
- When location is disabled, emit RequestLocationSettingsResolution once per user action, not in a loop.
- Do not leak Android framework types (Context, Activity) into the ViewModel.

3. Permission and location-settings helpers (UI layer, no new dependencies)
- A Compose state holder (for example rememberLocationPermissionState) built on ActivityResultContracts.RequestMultiplePermissions via rememberLauncherForActivityResult that requests ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION. It must correctly distinguish Denied from PermanentlyDenied using shouldShowRequestPermissionRationale, and must also handle the user granting only approximate location: treat approximate-only as a distinct state, explain it, and do not allow check-in with it (the feature needs precise location).
- Re-check permission state on ON_RESUME, since the user may change it in system settings.
- A helper that checks LocationSettingsRequest with SettingsClient (high-accuracy request) and launches the resolution through ActivityResultContracts.StartIntentSenderForResult, reporting the result back to the ViewModel.
- An intent helper to open this app's settings screen.
- Wire these into a temporary placeholder AttendanceScreen that prints the state as plain text with basic buttons, enough to exercise every state on a device.

4. Strings
- Add string resources for every StatusMessage (English). Keep wording plain and specific, e.g. distance text uses a plural-safe format like "You are 120 m away from the office".

5. Unit tests (JUnit4, MockK or fakes, Turbine, coroutines-test with a test dispatcher and a fake Clock)
- Initial state with no permission, no office, GPS off.
- Transition: permission granted starts location collection; revoked stops it.
- isMarkAttendanceEnabled is false in EACH of these cases individually: no permission, GPS off, out of range, weak signal, outside window, already marked today, and true only when everything is satisfied.
- Set office: success, weak accuracy, timeout, permission missing, GPS off, and double-tap prevention.
- Mark attendance: success, and rejection when the fresh re-validation fails even though the stale UI state said enabled.
- Distance progress fraction clamping at 0 m, exactly 200 m, and beyond.
- Window re-evaluation when the fake clock crosses the boundary.

6. Verification
- Run ./gradlew testDebugUnitTest and ./gradlew assembleDebug and fix failures properly without weakening tests.
- Finish with a summary: new files, the state model, assumptions, and a manual device test checklist (permission denied once, denied permanently, approximate only, GPS off then on, airplane mode, app backgrounded and resumed).

Rules: no secrets in code; handle every failure explicitly; keep composables and functions small; ask me before adding any new dependency.
````

## Prompt 4 - Building the AttendanceScreen UI

````text
Continue on the existing project. Data, domain and presentation logic are done and tested. In this step, build the real Jetpack Compose UI for AttendanceScreen to match the attached reference screenshot, replacing the temporary placeholder screen. Do not change domain or data logic. If the UI needs something the ViewModel does not expose, tell me first and make the smallest possible change.

Before writing code, review the existing AttendanceUiState, events and permission helpers, and reuse them.

1. Layout, matching the attached screenshot (top to bottom)
- Top app bar: back arrow and the title "Attendance" in bold dark blue. The back arrow finishes the activity. Pale blue-grey screen background.
- Step card (white, rounded corners, soft elevation):
  - Small uppercase label "STEP 1: OFFICE CONTEXT" with a small blue dot at the far right.
  - Map area (rounded corners) with a floating rounded pill centred over it showing a location icon and "Lat: <value>, Lon: <value>" in a monospace font (4 decimal places for display only; never round the stored value).
  - Body text: "To mark your attendance, ensure your current office location is correctly identified."
  - Full-width outlined button in blue with a plus-in-circle icon and the text "Set Office Location". Show a progress indicator in the button and disable it while isSettingOffice is true.
- Distance ring, centred: a circular progress indicator (thick stroke, rounded caps, light grey track) with the distance in large bold text (e.g. "120m") and a small uppercase "AWAY" below it. Drive the arc from the progress fraction in the UI state and animate changes smoothly. Red arc when out of range, green when in range, amber for weak signal, grey for acquiring or no office set.
- Status chip below the ring: pill with a coloured dot and uppercase text, e.g. "OUT OF RANGE" (red on light red background), "IN RANGE" (green), "WEAK SIGNAL" (amber), "ACQUIRING LOCATION" and "NO OFFICE SET" (grey).
- Helper text under the chip, small and grey, centred, driven by StatusMessage: "Move within 50 meters of the designated office location to enable check-in." for out of range, and an appropriate message for every other state.
- Check-in card at the bottom: dashed rounded border, a lock icon above a full-width "Mark Attendance" button, and a small uppercase caption "AVAILABLE 09:00 AM - 10:30 AM" (derive the times from the attendance window, formatted with the device locale). When the button is disabled it is a muted blue-grey and the lock is shown. When enabled, the lock changes to an unlocked icon, the button becomes solid primary blue, and the dashed border becomes solid or tinted. After attendance is marked today, show a clear "Attendance marked at <time>" state and disable the button.
- The distance text uses the string resources already defined, with "m" shown as in the screenshot for the ring and the longer sentence ("You are 120 m away from the office") as the accessibility description and a visible supporting line if there is room.

You don't have to design the screen exactly like the screenshot. Use material 3 components and make sure every components are present as expected. Use your imagination to make the UI fancy.

2. Special states (all must be designed, not left blank)
- Permission not granted or denied: replace the ring and check-in area with an explanatory card and a "Grant location access" button. Permanently denied: "Open app settings" button. Approximate-only: explain that precise location is required and offer to request it again or open settings.
- GPS off: a card with an "Enable location" button that triggers the resolution flow.
- Fix not yet acquired: skeleton or neutral ring with "Acquiring location...".
- Wire one-off events: snackbars for ShowMessage, the settings resolution launcher, opening app settings, and a brief success confirmation when attendance is marked.

3. Map handling
- Use Maps Compose (already added) only when a Maps API key is present. Expose a BuildConfig boolean (for example MAPS_KEY_AVAILABLE) computed in Gradle from local.properties, without ever putting the key itself in BuildConfig or source.
- When the key is present: a lite-mode or non-interactive GoogleMap centred on the saved office location with a marker and the office radius drawn as a Circle of 50 m. When no office is saved, show the map at a neutral position with no marker.
- When the key is absent (or Play Services are unavailable): a styled placeholder with a subtle grid or gradient background and a location icon, so the screen still looks intentional. The Lat/Lon pill shows "Lat: --, Lon: --" until an office is set.
- The screen must work and look good in both cases.

4. Quality requirements
- Split into small composables in separate files under ui/attendance/components/: OfficeStepCard, OfficeMap, DistanceRing, StatusChip, CheckInCard, PermissionCard, LocationOffCard. AttendanceScreen is stateful and wires the ViewModel; AttendanceContent is stateless and takes the state plus lambdas, so it can be previewed and tested.
- Collect state with collectAsStateWithLifecycle. Hoist all state. No business logic inside composables.
- Colours, typography, spacing and shapes come from the Material 3 theme (add semantic colour roles such as success, warning, error container where needed). No hardcoded hex values inside composables. Support light theme to match the screenshot and make sure dark theme is at least legible.
- Accessibility: contentDescription for icons, the ring announces the distance and range status, minimum 48dp touch targets, disabled state communicated to TalkBack, and text scaling up to 200 percent does not break the layout.
- Handle small screens and landscape by making the content scrollable.
- Add @Preview functions for: out of range, in range, weak signal, acquiring, no office set, permission denied, permanently denied, GPS off, already marked, and a large font scale.

5. Tests
- Compose UI tests (createComposeRule) against AttendanceContent: Mark Attendance is disabled when out of range and enabled only in range; the status chip text matches each state; the permission and GPS-off cards show the right buttons and call the right lambdas; Set Office Location is disabled while loading.

6. Verification
- Run ./gradlew assembleDebug, ./gradlew testDebugUnitTest and the compose UI tests if an emulator is available (otherwise tell me which ones I need to run on a device).
- Finish with a summary: files created, any ViewModel changes you had to make, assumptions, and a list of what still needs on-device visual comparison against the screenshot.

Rules: no secrets or API keys in code, BuildConfig or committed files; no new dependencies without asking me first; do not weaken tests to pass.
````

## Prompt 5 - Release hardening, audit and README

````text
Continue on the existing project. All features are implemented. In this final step: harden and verify the release build, audit the project against the assessment brief, and complete README.md. Do not add features. Only fix defects you find, and tell me about each one.

Before changing anything, review the whole project: build files, manifest, ProGuard rules, package structure, PROMPTS.md and the current README skeleton.

1. Release build and R8
- Confirm the release build type has minifyEnabled and shrinkResources on. Add the minimum keep rules needed (Hilt, DataStore, Maps/Play Services, kotlinx-serialization if used) and nothing broader than necessary. Do not use blanket "-keep class ** { *; }" rules or "-dontwarn" for everything.
- Signing: read the release keystore path, alias and passwords from a gitignored keystore.properties file or environment variables. Provide a committed keystore.properties.example with placeholder values only. If no signing config is available, the release build must still assemble (falling back to the debug signing config with a clear Gradle log warning) so reviewers can build it. Never write any real credential anywhere, and never commit a keystore.
- Set versionCode and versionName sensibly (1 and "1.0.0").
- Run ./gradlew clean assembleRelease. Fix any R8 or lint errors properly. Then review the generated mapping and the R8 output for warnings and tell me which ones are safe to ignore and why.
- Run ./gradlew lintRelease (or lint) and report errors and the important warnings. Fix real issues (missing permissions handling, hardcoded strings, missing contentDescription, API level problems).
- Verify the release APK path and size, and give me the exact path.
- List the behaviours I must verify by hand on a physical device with the RELEASE APK specifically, because minification can break them: location updates, DataStore persistence after app restart, permission flows, GPS-off flow, Maps (with and without key), Hilt injection.

2. Final audit against the brief (report as a checklist with PASS / FAIL / PARTIAL and the file that proves each item; fix any FAIL that is in scope)
- "Set Office Location" fetches the current GPS coordinates and saves them locally.
- "Mark Attendance" is only enabled and functional within 50 m of the saved coordinates, and is re-validated at tap time.
- A real-time distance indicator is shown (e.g. "You are 120m away from the office").
- Everything is on the same single AttendanceScreen, built with Jetpack Compose, matching the reference screenshot.
- State management with Kotlin Flow / StateFlow; layered MVVM architecture.
- Local persistence follows best practice (DataStore, full coordinate precision).
- Graceful handling of permission denial, permanent denial, approximate-only location, GPS off, no fix, weak accuracy, and location timeout.
- Location collection stops when the screen is not active.
- No secrets, API keys, keystores or local.properties are tracked by git (check git ls-files and the .gitignore).
- No leftover TODOs, debug logs containing coordinates, unused code, commented-out code or unused dependencies.
- Unit and UI tests exist and pass.
Also list any weaknesses a strict reviewer would flag, ranked by importance.

3. README.md (complete it using exactly these five numbered sections, in this order, written for a technical reviewer)
1. Project Title and Description: a creative project name (propose one that fits the app; use it as the title and also tell me where else it should be applied, e.g. app label), a short description, and a features list. State clearly that this repository is Task 1 (native Android) and that Task 2 (Flutter) is in a separate repository: leave the link as a TODO placeholder for me to fill.
2. Project Structure / Approaches: explain the layered MVVM architecture in a few sentences and show the package tree. Name the main classes (AttendanceViewModel, the use cases, the repositories, the stores) with one or two sentences each on their role. Explain the key design decisions: accuracy gating, tap-time re-validation, lifecycle-aware location collection, full-precision coordinate storage, and the Maps key fallback. List assumptions explicitly (including the 09:00 to 10:30 attendance window taken from the screenshot).
3. Generative AI Usage: briefly and honestly explain how AI tools were used in this project (scaffolding, boilerplate, tests, UI, review) and what I verified or changed myself. Include the essential prompts, taken from PROMPTS.md, as short quoted excerpts or condensed versions (do not paste thousands of words; keep each to the key instructions). Do not invent prompts that are not in PROMPTS.md, and do not claim I did anything I did not. Leave a clearly marked TODO line where I should add what I personally changed or verified.
4. How to Run: prerequisites (Android Studio version, JDK, minSdk and test device requirements), clone commands, how to add an optional Maps API key via local.properties (and that the app runs without it), how to run debug, how to build the release APK (including the signing fallback), how to run tests, and how to test the geofence without walking (mock location setup on emulator and on a device). Include a link placeholder for the release APK.
5. Screenshots: a section with image references pointing to a docs/screenshots/ folder, using descriptive file names and alt text for each state: out of range, in range, weak signal, permission denied, GPS off, attendance marked, no office set. Create the folder with a .gitkeep and tell me exactly which screenshots to capture. I will add the images myself; do not generate or fabricate images.

README rules: plain, specific language; no marketing wording; no emoji; Australian English spelling; do not claim anything the code does not do; do not include any key, password or personal data.

4. Verification and summary
- Finish with: the release APK path, the lint and R8 results, the audit checklist, the list of things I must do manually (screenshots, APK upload, repo link, TODOs in the README), and any unresolved risks.

Rules: no secrets in code or committed files; do not weaken or delete tests or lint checks to pass; ask me before adding any dependency or changing the architecture.
````
