# Prompts

## Prompt 1 - Project setup and scaffolding (13 October 2026)

A screenshot of the target Attendance screen was attached to this prompt (not committed).

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
