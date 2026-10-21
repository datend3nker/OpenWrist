# 0007: One signing key for all stores

- **Status:** proposed
- **Date:** 2026-10-21

## Context

Android accepts an update only if it is signed with the same key as the
installed app. OpenWrist is planned for IzzyOnDroid, f-droid.org and Google
Play. Each store can sign the app itself:

- f-droid.org builds from source and signs with its own key, unless the build
  is reproducible and F-Droid publishes the developer's signature instead.
- Google Play signs with a key it holds (Play App Signing). A developer can
  upload an existing key when the app is created.
- IzzyOnDroid publishes the developer's own signed APK.

If the stores use different keys, a user cannot update an app installed from
one store with a version from another. They must uninstall first and lose the
app's data. The application ID is already fixed, see
[ADR 0006](0006-application-id.md).

## Decision

OpenWrist uses **one app signing key** in every store, and a **second,
separate upload key** for Google Play.

- The app signing key is created once, kept offline and backed up, see
  [development.md](../development.md#release-builds).
- IzzyOnDroid gets APKs signed with the app signing key.
- For f-droid.org the build is kept reproducible, so F-Droid can publish the
  APK with this signature.
- For Google Play the app signing key is imported when the app is created
  (Play App Signing, with Google's PEPK tool). Play cannot be switched to this
  key afterwards. Google then signs the APKs it delivers with this key, so
  Play and the other stores give users the same signature.
- The upload key only signs the app bundle that is uploaded to Play. Google
  recommends that it differs from the app signing key. If it is lost or
  leaked, Play can reset it (an "upload key reset" request in the Play
  Console), which is not possible for the app signing key. The upload key
  therefore never needs to leave the build machine and the app signing key
  stays offline.

This is an assumption that still has to be verified for each store: that
F-Droid accepts the build as reproducible, and that Play accepts the key
import.

Checked on 2026-10-21: two clean clones in different directories produce the
same unsigned release APK (same SHA-256), see
[publishing.md](../publishing.md#2-reproducible-build). F-Droid's own build
server and the Play key import are still not verified. A local run of
`fdroid build` gave the same APK apart from the commit hash, with one open
risk: Gradle 9.8.1 may be unknown to F-Droid's build server.

## Alternatives

- **The same key for signing and upload:** one key less, but then a leak of
  the upload key is a leak of the key every store depends on. Whether Play
  allows it was not found in its documentation.
- **A separate app signing key per store:** simpler to start, but users cannot move
  between stores without losing data. Fixing it later is not possible.
- **Let each store sign with its own key:** the same problem, and the
  developer's key would not vouch for the app anywhere.
- **Skip Google Play:** avoids the second key, but loses the audience that only
  uses Play. Whether Play accepts an app that installs other apps is not
  verified yet and may decide this.

## Consequences

- Losing the app signing key means no store can ship updates. Backup is
  mandatory. Losing the upload key is recoverable.
- Play can later upgrade the app signing key (annual, enforced from Android 17
  / API 37 on). F-Droid's `AllowedAPKSigningKeys` would have to follow.
  *Assumption:* not tested, rotation is beyond this decision.
- Builds must stay reproducible: no timestamps, no absolute paths and no
  unpinned inputs in the release build.
- The Play decision has to be made before the first Play upload, because the
  key import cannot be done later.
