# Publishing

Steps and drafts for releasing OpenWrist. The order matters: the signing key
comes first, see [ADR 0007](decisions/0007-signing-key-and-stores.md).
Submissions to the stores are made by a human, this page only prepares them.

Anything marked *assumption* was not verified against the store.

## 1. Release checklist

1. Create the signing key once and back it up, see
   [development.md](development.md#release-builds).
2. Make the repository public, see E7.5 in the [roadmap](roadmap.md).
3. Check that `versionCode` and `versionName` in `app/build.gradle.kts` match
   `fastlane/metadata/android/*/changelogs/<versionCode>.txt`.
4. Run `./gradlew ktlintCheck detekt test`.
5. Tag the commit (`git tag -s v0.1.0`) and push the tag.
6. Build the signed APK with `./gradlew assembleRelease` and check it with
   `apksigner verify --print-certs`. Note the SHA-256 of the certificate: the
   stores need it.
7. Attach the APK to a GitHub release with the release notes.

## 2. Reproducible build

F-Droid can only publish the developer's signature if its own build produces
the same APK. Checked on 2026-10-21: two clean clones in different
directories built `assembleRelease` and produced the same unsigned APK
(identical SHA-256). Repeat this check before each release:

```sh
git clone . /tmp/a/openwrist && git clone . /tmp/b/other-path/openwrist
(cd /tmp/a/openwrist && ./gradlew --no-daemon assembleRelease)
(cd /tmp/b/other-path/openwrist && ./gradlew --no-daemon assembleRelease)
sha256sum /tmp/*/openwrist/app/build/outputs/apk/release/*.apk \
  /tmp/b/other-path/openwrist/app/build/outputs/apk/release/*.apk
```

Not covered by this check: another JDK or Android SDK build-tools version.
F-Droid's build server may differ here. *Assumption.*

## 3. IzzyOnDroid

IzzyOnDroid reads the signed APK from the GitHub release and the metadata
from `fastlane/metadata/android/`. The request is an issue in the IzzyOnDroid
inventory repository, see its
[inclusion guide](https://gitlab.com/IzzyOnDroid/repo/-/blob/master/README.md).
Needed: repository URL, the signed APK in a release, license
(GPL-3.0-or-later).

## 4. f-droid.org

A merge request to
[fdroiddata](https://gitlab.com/fdroid/fdroiddata) adds
`metadata/io.github.datend3nker.openwrist.yml`. Draft, to be checked against
the current [metadata reference](https://f-droid.org/docs/Build_Metadata_Reference/):

```yaml
Categories:
  - System
License: GPL-3.0-or-later
SourceCode: https://github.com/datend3nker/OpenWrist
IssueTracker: https://github.com/datend3nker/OpenWrist/issues

AutoName: OpenWrist

RepoType: git
Repo: https://github.com/datend3nker/OpenWrist.git

Builds:
  - versionName: 0.1.0
    versionCode: 1
    commit: v0.1.0
    subdir: app
    gradle:
      - yes

# Publish the developer's signature (needs the reproducible build above).
# Replace with the SHA-256 of the release certificate.
AllowedAPKSigningKeys: <sha256 of the release certificate>

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 0.1.0
CurrentVersionCode: 1
```

Open points, *assumptions*:

- The `Categories` value is a guess, pick one from the F-Droid list.
- F-Droid scans the source for non-free dependencies. The
  [architecture](architecture.md) only uses open source libraries, but the
  scanner result is not known yet.
- The build has to run on F-Droid's build server with its JDK and SDK.

## 5. Google Play

Decide this **before** the first upload: the signing key can be imported only
when the app is created.

Prepare:

- A Play developer account (one-time fee) and identity verification.
- An Android App Bundle: `./gradlew bundleRelease`. Play accepts bundles, not
  APKs, for new apps. The `dependenciesInfo` block is switched off for F-Droid
  and stays off, so Play shows no dependency metadata. *Assumption: Play does
  not require it.*
- Store listing from `fastlane/metadata/android/` (texts, icon, screenshots).
  Play wants Wear OS screenshots in its own format. *Assumption.*
- A privacy policy URL. OpenWrist has no account and no analytics. It only
  contacts the repositories the user enables, which should be stated.
- The Data safety form: nothing is collected or shared by the developer.
- A declaration for `REQUEST_INSTALL_PACKAGES` and `REQUEST_DELETE_PACKAGES`.
  Play allows these only when package installation is the core function.
  An app store client may qualify, but this is **not verified** and is the
  main risk: the app can be rejected. Ask Play support or read the current
  policy before paying for the account and importing the key.

If Play rejects the app, the other stores are not affected, since all use the
same key.
