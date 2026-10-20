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

OpenWrist uses **one signing key** in every store.

- The key is created once, kept offline and backed up, see
  [development.md](../development.md#release-builds).
- IzzyOnDroid gets APKs signed with this key.
- For f-droid.org the build is kept reproducible, so F-Droid can publish the
  APK with this signature.
- For Google Play the same key is uploaded as the app signing key when the app
  is created. Play cannot be switched to this key afterwards.

This is an assumption that still has to be verified for each store: that
F-Droid accepts the build as reproducible, and that Play accepts the key
import.

## Alternatives

- **A separate key per store:** simpler to start, but users cannot move
  between stores without losing data. Fixing it later is not possible.
- **Let each store sign with its own key:** the same problem, and the
  developer's key would not vouch for the app anywhere.
- **Skip Google Play:** avoids the second key, but loses the audience that only
  uses Play. Whether Play accepts an app that installs other apps is not
  verified yet and may decide this.

## Consequences

- Losing the key means no store can ship updates. Backup is mandatory.
- Builds must stay reproducible: no timestamps, no absolute paths and no
  unpinned inputs in the release build.
- The Play decision has to be made before the first Play upload, because the
  key import cannot be done later.
