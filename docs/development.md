# Development setup

How to get a machine ready to build OpenWrist.

## Tools

- JDK 17 or newer to start Gradle. Gradle runs its daemon on JDK 21 and
  downloads it if it is missing (`gradle/gradle-daemon-jvm.properties`).
  Not newer, because detekt 1.23 fails on JDK 25.
- Android Studio (current stable), with the Android SDK and a Wear OS
  emulator image
- `adb` for installing builds on a real watch

Gradle has to find the Android SDK. Android Studio writes its path to
`local.properties` when it opens the project. On the command line, set
`ANDROID_HOME` (for example `export ANDROID_HOME=~/Android/Sdk`) or create
`local.properties` with `sdk.dir=/path/to/sdk`. The file is not committed.

## Wear OS devices

Install and update behavior can differ between the emulator and real watches
and between Wear OS versions. *Not verified yet.* For work on installation,
test on a real watch.

## Everyday commands

```sh
./gradlew build                # build everything
./gradlew ktlintCheck detekt   # lint
./gradlew test                 # unit tests
```

Run them before every commit, see [CONTRIBUTING.md](../CONTRIBUTING.md).
CI runs the same checks on every pull request
(`.github/workflows/ci.yml`).
`./gradlew ktlintFormat` fixes most formatting problems on its own. Lint
settings live in `.editorconfig` (ktlint) and `config/detekt/detekt.yml`
(only the differences from the detekt defaults).
