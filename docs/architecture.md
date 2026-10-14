# Architecture

This document describes the structure of OpenWrist. Modules are created once
there is code for them.

## Ground rules

1. **Only the app module is UI-specific.** Only `app` depends on Compose for
   Wear OS and other UI libraries. Everything else is testable without a
   device or emulator.
2. **Dependencies point inward.** Domain modules depend on nothing but the
   Kotlin standard library and the F-Droid libraries; `app` depends on all of
   them.
3. **Only watch builds are offered.** A version counts as compatible only if
   its manifest declares `android.hardware.type.watch`. Otherwise a phone build
   with the same `applicationId` and a higher `versionCode` would win.
4. **Installs are verified.** Index signatures and APK checksums and
   signatures are checked before anything is installed.
5. **Every user-visible string is translatable.** Only `app` produces text for
   the user, always from string resources. Domain modules return typed errors
   and sealed classes, never display strings. See
   [ADR 0004](decisions/0004-translations-with-android-resources.md).

## Modules

All modules live in one Gradle project.

| Module | Responsibility |
|---|---|
| `repo` | Repository index: download, verify and store it with the F-Droid libraries, answer which watch apps exist. Android library without UI |
| `install` | APK download, verification and installation. Android library without UI |
| `app` | Wear OS user interface (Compose for Wear OS), navigation, translations |

### How the app list is filled

1. `AppContainer` (in `app`) creates the F-Droid database. On first start,
   `DefaultRepositories` adds f-droid.org (enabled) and its archive
   (disabled), each with the certificate the index must be signed with.
2. `RepoRefresher` runs the F-Droid library's `RepoUpdater` for every enabled
   repository. It downloads the index, checks its signature and stores it.
   This happens on start when an index is missing or older than a day, and
   when the user taps "Refresh".
3. While storing, every version is checked with `WatchCompatibilityChecker`.
   It accepts a version only if it declares `android.hardware.type.watch` and
   the F-Droid library's checker accepts it (SDK version, ABI of native code,
   required features). The result is stored as `isCompatible`.
4. `WatchApps` lists only apps with at least one compatible version. The
   version it suggests is the newest compatible one.

### How an app is installed

1. The details screen shows the version `WatchApps` suggests. For a new
   install the repository's preferred signer is tried first, then any
   signer, because the preferred signer may belong to the phone builds only.
   For installed apps only versions with the installed signer count.
2. `InstallManager` downloads the APK. The F-Droid downloader tries the
   mirrors and checks the SHA-256 hash from the signed index.
3. `ApkVerifier` checks the file again before anything is installed: SHA-256
   hash, package name, version code and signing certificates, all compared
   with the signed index. Any mismatch stops the installation.
4. `ApkInstaller` hands the APK to `PackageInstaller`. Android shows its own
   confirmation; nothing is installed without the user agreeing.

If another app store installed the app (Android records the installer),
the details screen asks before updating it, because the update takes the
app away from that store: after it, Android records OpenWrist as installer.
Apps installed over ADB or by OpenWrist are updated without that question.

The user has to allow OpenWrist to install apps once. The details screen
opens the system setting for it, or shows the ADB command if the watch has
no such setting. The Updates screen lists installed apps with a newer watch
build.

### How app icons are loaded

`AppIcons` (in `repo`) downloads an app's icon from its repository with the
same downloader as APKs, so the SHA-256 hash from the signed index is checked.
Icons are cached in `cacheDir/icons`, named after their hash; Android may
delete them when storage runs low, and they are downloaded again when needed.
Without network no icon is shown.

The libraries come from Maven Central, see
[ADR 0003](decisions/0003-build-on-fdroid-libraries.md).

The application ID and root package is `io.github.datend3nker.openwrist`, see
[ADR 0006](decisions/0006-application-id.md). The app is standalone: it needs
no companion app on the phone and no Google Play services.

## Open questions

- Installs go through `PackageInstaller` sessions with
  `REQUEST_INSTALL_PACKAGES` and show the system confirmation dialog. This
  works on the Wear OS emulator (API 37). How it behaves on real watches is
  not verified yet (E1).
- `ApkVerifier` reads signing certificates with
  `getPackageArchiveInfo(GET_SIGNING_CERTIFICATES)`. Tested on API 37 only;
  that it works the same on API 30 to 32 is an assumption.
