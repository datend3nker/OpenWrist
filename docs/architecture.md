# Architecture

This document describes the planned structure. Only `app` exists so far.
Modules are created once there is code for them; until then the list below is
a plan, not a commitment.

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

## Planned modules

All modules live in one Gradle project. Names are a proposal.

| Module | Responsibility |
|---|---|
| `repo` | Repository index: download, parse, verify, local cache |
| `install` | APK download, verification, installation, updates |
| `app` | Wear OS user interface (Compose for Wear OS), navigation, translations. Exists, shows a placeholder screen |

The application ID and root package is `io.github.datend3nker.openwrist`, see
[ADR 0006](decisions/0006-application-id.md). The app is standalone: it needs
no companion app on the phone and no Google Play services.

## Open questions

- Installs go through `PackageInstaller` sessions with
  `REQUEST_INSTALL_PACKAGES` and show the system confirmation dialog. How this
  behaves on real watches is not verified yet.
