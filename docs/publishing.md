# Publishing

Steps and drafts for releasing OpenWrist. The order matters: the signing key
comes first, see [ADR 0007](decisions/0007-signing-key-and-stores.md).
Submissions to the stores are made by a human, this page only prepares them.

Anything marked *assumption* was not verified against the store.

## 1. Release checklist

1. Create the app signing key once and back it up, see
   [development.md](development.md#release-builds). For Play also create a
   separate upload key.
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
`metadata/io.github.datend3nker.openwrist.yml` and nothing else. The merge
request title is "New app: OpenWrist". The recipe below passed
`fdroid lint`, `fdroid rewritemeta` (no changes) and `fdroid checkupdates` on
2026-10-22. Rules from F-Droid's merge request template that matter here:
`commit` is the full hash, not the tag; no description, changelog or images in
the metadata (they come from `fastlane/` in this repository); the fork must be
public with an unprotected branch; one app per merge request.

```yaml
Categories:
  - System
License: GPL-3.0-or-later
AuthorName: datend3nker
SourceCode: https://github.com/datend3nker/OpenWrist
IssueTracker: https://github.com/datend3nker/OpenWrist/issues

AutoName: OpenWrist

RepoType: git
Repo: https://github.com/datend3nker/OpenWrist.git
Binaries: https://github.com/datend3nker/OpenWrist/releases/download/v%v/OpenWrist-%v.apk

Builds:
  - versionName: 0.1.0
    versionCode: 1
    commit: 218cfdd1001b1ad357cc217849d7b7abd2954e37
    subdir: app
    gradle:
      - yes
    prebuild: sed -i -e '/foojay/d' ../settings.gradle.kts

AllowedAPKSigningKeys: b713c36083293e7bb99af4e9bd88f4f31ab1951b69ecaffb4220feb306eac161

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 0.1.0
CurrentVersionCode: 1
```

Tested on 2026-10-21 with `fdroidserver` 2.4.5 (`fdroid scanner` and
`fdroid build -l`, without the buildserver VM, against commit `888337d`):

- **Scanner:** it rejects the `foojay-resolver` plugin in
  `settings.gradle.kts` ("usual suspect"). Gradle uses it to download the
  JDK 21 for the daemon. The `prebuild` line above removes it only in
  F-Droid's build; our own builds keep it. With it removed, F-Droid's build
  host must provide JDK 21 itself. After that line the scanner found nothing.
- **Build:** the APK F-Droid's process builds is byte-identical to a local
  `assembleRelease`, except `META-INF/version-control-info.textproto`, which
  contains the commit hash. Build and tag therefore have to be the same commit
  for F-Droid's APK to match the developer's.
- **Gradle 9.8.1:** `fdroidserver` 2.4.5 only knows Gradle hashes up to 8.14.2
  and stops with "No hash for gradle version 9.8.1". The test passed only after
  adding the official hash to a local copy of `gradlew-fdroid`. Whether
  F-Droid's current build server knows 9.8.1 is **not verified**. If not, the
  merge request or the Gradle version has to change.
- **Categories:** `fdroid lint` rejected `System` only because the local test
  directory had no `config/categories.yml`. Check the value against the real
  `fdroiddata` checkout.
- **Reproducible against the release:** on 2026-10-22 the tag `v0.1.0`
  (`218cfdd`) was built with this recipe. `apksigcopier compare` between the
  GitHub release APK and that build succeeded: same files, same CRCs. F-Droid
  can therefore publish the developer's signature. The certificate
  fingerprint in `AllowedAPKSigningKeys` is that of the app signing key, see
  [ADR 0007](decisions/0007-signing-key-and-stores.md).
- **Not tested:** the buildserver VM and its exact JDK and build-tools
  versions, and `fdroid lint` against the real `fdroiddata`.
- **Generative AI:** the F-Droid
  [inclusion policy](https://f-droid.org/docs/Inclusion_Policy/) does not
  mention it. As of 2026-10-22 two proposals are open and undecided: an
  [interim policy](https://gitlab.com/fdroid/admin/-/work_items/699) that is
  neutral and encourages disclosure (it covers contributions to F-Droid
  itself, not apps), and a
  [request](https://gitlab.com/fdroid/admin/-/work_items/641) to tag apps that
  use AI with an anti-feature. OpenWrist says in its README that it is built
  with AI; keep saying it in the merge request. Reviewers may ask about it.
- **Installer:** the policy does not mention `REQUEST_INSTALL_PACKAGES` or
  `QUERY_ALL_PACKAGES`. It forbids downloading executable files without the
  user's consent. OpenWrist downloads and installs an app only after the user
  asked for it and Android confirmed. Expect questions about this.

## 5. Google Play

Decide this **before** the first upload: the app signing key can be imported
only when the app is created (Play Console, *Play app signing*, encrypt and
upload the key with Google's PEPK tool). Create a separate upload key and sign
the bundle with it, see [ADR 0007](decisions/0007-signing-key-and-stores.md).

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
