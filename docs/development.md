# Development setup

How to get a machine ready to build OpenWrist. There is no Gradle project yet;
this page will be completed together with the first module.

## Tools

- JDK 17 or newer
- Android Studio (current stable), with the Android SDK and a Wear OS
  emulator image
- `adb` for installing builds on a real watch

## Wear OS devices

Install and update behavior can differ between the emulator and real watches
and between Wear OS versions. *Not verified yet.* For work on installation,
test on a real watch.

## Everyday commands

```sh
./gradlew ktlintCheck detekt   # lint
./gradlew test                 # unit tests
```

These tasks are planned. Run them before every commit once they exist, see
[CONTRIBUTING.md](../CONTRIBUTING.md).
