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

The first start downloads the whole f-droid.org index (about 63 MB). On the
`WearOS-GMS` emulator image (API 37, x86_64) download and import took about
two minutes (2026-10-14). Later refreshes only download changes. Not measured
on a real watch yet (E2).

On the same emulator, installing Catima's watch build (versionCode 201) and
updating from build 200 to 201 worked, including the system confirmation
(2026-10-14). The "Install unknown apps" setting exists there and can be
switched on from OpenWrist. On watches without it:

```sh
adb shell appops set io.github.datend3nker.openwrist REQUEST_INSTALL_PACKAGES allow
```

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

## Release builds

`./gradlew assembleRelease` builds an unsigned release APK. To sign it, set
these environment variables first:

| Variable | Content |
|---|---|
| `OPENWRIST_KEYSTORE` | Path to the keystore file |
| `OPENWRIST_KEYSTORE_PASSWORD` | Password of the keystore |
| `OPENWRIST_KEY_ALIAS` | Alias of the key in the keystore |
| `OPENWRIST_KEY_PASSWORD` | Password of the key |

Keystores (`*.jks`, `*.keystore`) are ignored by Git. Keep the key and its
passwords outside the repository and back them up: Android installs updates
only if they are signed with the same key as the installed version.

A keystore can be created with `keytool` from the JDK:

```sh
keytool -genkeypair -keystore openwrist-release.jks -alias openwrist \
  -keyalg RSA -keysize 4096 -validity 10000
```

`apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk`
(from the Android SDK build tools) shows which key signed the APK.

The release build leaves out the dependency metadata block that the Android
Gradle plugin adds for Google Play. It is encrypted with a Google key, and
F-Droid compatible repositories reject APKs that contain it.

## Store metadata

Title, descriptions, icon and screenshots for F-Droid compatible
repositories live in `fastlane/metadata/android/<locale>/`, the layout that
f-droid.org and IzzyOnDroid read. English (`en-US`) and German (`de-DE`)
exist; images are shared from `en-US`.

- `short_description.txt` must stay below 80 characters.
- `images/icon.png` is a 512 × 512 rendering of the launcher icon
  (`app/src/main/res/drawable/ic_launcher_foreground.xml` on a round
  background). Render it again when the icon changes.
- The screenshots are taken on the Wear OS emulator with
  `adb exec-out screencap -p > 1.png`. They are in `phoneScreenshots`
  because that is the folder phone clients show. Assumption: whether the
  repositories would also show a `wearScreenshots` folder was not checked.

## Testing the daily update check

WorkManager does not start a periodic job before its time, also not with
`adb shell cmd jobscheduler run`. To test `UpdateCheckWorker`, enqueue it
once in a local build, for example in `UpdateCheckWorker.schedule()`:

```kotlin
WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<UpdateCheckWorker>().build())
```

Install an older version of a watch app first (on the emulator, Catima
1.0.0 from f-droid.org) so there is an update to report. Do not commit the
change.

## Testing repository links

A repository link can be sent to the emulator or a watch over ADB. It opens
the screen that adds the repository, with the address filled in:

```sh
adb shell am start -a android.intent.action.VIEW \
  -d "'fdroidrepos://apt.izzysoft.de/fdroid/repo?fingerprint=3BF0D6ABFEAE2F401707B6D966BE743BF0EEE49C2561B9BA39073711F628937A'"
```

Text typed with `adb shell input text` does not reach text fields through
the Wear OS keyboard, so this is also the easiest way to test adding a
repository.
