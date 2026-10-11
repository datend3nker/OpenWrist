# Development setup

How to get a machine ready to build OpenWrist.

## Tools

- JDK 17 or newer to start Gradle. Gradle runs its daemon on JDK 25 and
  downloads it if it is missing (`gradle/gradle-daemon-jvm.properties`).
- Android Studio (current stable), with the Android SDK and a Wear OS
  emulator image
- `adb` for installing builds on a real watch

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

These tasks are planned. Run them before every commit once they exist, see
[CONTRIBUTING.md](../CONTRIBUTING.md).
