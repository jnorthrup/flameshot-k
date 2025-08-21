# Kotlin Multiplatform (KMP) Release Deliverable — flameshot-k kotlin-port

Purpose

- Provide a compact, actionable release deliverable for the Kotlin Multiplatform port that aims to faithfully emulate the existing Qt UI/UX using Compose Multiplatform (Compose for Desktop/Compose Multiplatform).

Contract (inputs / outputs / success criteria)

- Inputs: `kotlin-port` Gradle project (source), supported targets (JVM/Desktop, macOS/native, Android if present).
- Outputs: versioned artifacts (desktop JAR or native app bundle, optional Android AAR/APK), a Git-tagged release, and a short release notes summary.
- Success: reproducible build on CI for desktop targets; Compose UI behaviors match primary Flameshot flows (screenshot, annotate, save/share) and automated smoke tests pass.

Assumptions

- Compose Multiplatform is used to emulate Qt widgets/behavior.
- Existing project already contains `kotlin-port` Gradle files (`build.gradle.kts`, `gradlew`).
- Native packaging (macOS, Linux) is optional for initial release — we start with JVM/Compose for Desktop and add native later.

Release checklist

1. Update `gradle.properties` version and `settings.gradle.kts` as needed.
2. Ensure `build.gradle.kts` has multiplatform targets for `jvm` (desktop) and any other required targets.
3. Add a smoke UI test target (Unit + integration smoke that exercises capture → annotate → save).
4. Run the build locally: `./gradlew clean build` (Desktop target).
5. Produce artifacts: `./gradlew :kotlin-port:package` or `:kotlin-port:packageDistribution` depending on the gradle setup.
6. Create release tag: `git tag -a vX.Y.Z -m "Kotlin port release vX.Y.Z"` and push.
7. Publish artifacts to GitHub Releases (or chosen artifact host).

Quality gates

- Build: Gradle builds must succeed on macOS and Linux (CI matrix).
- Lint/Format: Ensure Kotlin linting (ktlint/detekt) passes.
- Tests: At least one smoke UI test and unit tests for core logic.

Edge cases to consider

- Headless CI environments: ensure `org.jetbrains.compose.desktop.application` packaging can run headless (use Xvfb on Linux CI for UI tests if needed).
- Native binary signing on macOS: plan for code signing in follow-up.
- Large assets: avoid bundling unnecessary images; reference `img/` in repo.

Compose/Qt emulation notes (practical guidance)

- Map key Qt widgets to Compose equivalents: QMainWindow → `Window`/`Application`; QPainter-based annotate → Canvas/DrawScope; QMenus → `MenuBar` and `DropdownMenu`.
- Keep business logic (screenshot capture, file handling, color utils) in shared Kotlin code and only implement UI adapters in Compose desktop module.
- For screenshot capture and low-level OS hooks, keep existing native (C++) implementation or expose them via a small JNI/FFI shim if necessary; prefer `Desktop` subsystem wrappers in Kotlin where possible.

CI and publishing (suggestion)

- Create a GitHub Actions workflow that checks out code, sets up JDK 17, runs `./gradlew -Pdesktop build`, runs smoke tests, and uses the `actions/upload-artifact` + `ncipollo/release-action` to attach artifacts to a release.

Next steps I can do for you

- Add a sample GitHub Actions workflow for building and publishing a KMP Compose desktop artifact.
- Add a minimal smoke UI test harness and Gradle task for packaging the desktop artifact.

Notes

- I made minimal assumptions about native distribution to keep the first deliverable focused and reproducible.

