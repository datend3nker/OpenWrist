# OpenWrist

An unofficial, free and open source third-party client for Wear OS that uses
F-Droid repositories.

OpenWrist lets you browse, install and update free apps directly on your
watch, without Google Play and without a phone companion app. It builds on
the F-Droid libraries and only shows apps built for watches.

> **Not affiliated with F-Droid.** OpenWrist is an independent third-party
> project. It is not made, endorsed or supported by the F-Droid project or
> F-Droid Limited. It only reads F-Droid's public repositories (f-droid.org and
> others) and reuses the open source F-Droid libraries. "F-Droid" is a name
> of its respective owners. Problems with OpenWrist go to this project, not to
> F-Droid.

> **Status:** early development, not released yet. The app browses,
> installs and updates watch apps from f-droid.org, tested on the Wear OS
> emulator only. Tests on real watches are still missing, see the
> [roadmap](docs/roadmap.md).

## Goals

- **Usable on a small round screen.** Browsing and installing must work with a
  thumb and a few taps.
- **Only free software.** Apps come from F-Droid compatible repositories.
- **Readable code.** The project is built with AI assistance but is meant for
  humans. Every commit should make sense on its own.
- **Easy to translate.** Every user-visible string is a resource from day one.

## Features

| Feature | State |
|---|---|
| Browse and search apps | works on the emulator |
| Install apps on the watch | works on the emulator |
| Update installed apps | works on the emulator |
| Uninstall apps | works on the emulator |
| Notify about updates once a day | works on the emulator |
| Verify index and APK signatures | works on the emulator |
| Add and manage repositories | planned, f-droid.org only for now |

## Publishing your watch app for OpenWrist

OpenWrist has no store of its own. It reads existing F-Droid repositories
(f-droid.org, IzzyOnDroid or your own), so any app in one of them that is
built for watches shows up. Publishing happens with the repository, not with
OpenWrist; OpenWrist cannot add apps to f-droid.org. How it works is based on the proof of concept, see
[Roadmap](docs/roadmap.md).

**What your app has to do**

- Declare the watch feature in the manifest of the watch build:
  ```xml
  <uses-feature android:name="android.hardware.type.watch" android:required="true" />
  ```
  OpenWrist offers a version only if the index lists this feature. Without it,
  the version is treated as a phone build and ignored.
- Be free software that the repository accepts, built from source.
- Ship native code for the ABI of the watch (for example `arm64-v8a`). In the
  proof of concept, an app with only `armeabi-v7a` code was not shown on an
  `arm64-v8a` or `x86_64` emulator.

**Companion app (phone and watch build)**

Both ways work:

- *Same `applicationId` as the phone app* (what Google recommends): one
  F-Droid entry with phone and watch versions. OpenWrist picks only watch
  versions, the phone client skips them.
  Give the watch build its own `versionCode` range, for example 200 to 299
  for the watch and 1000 and up for the phone. Catima does it like this and
  is the only example found in the f-droid.org index (2026-10-07).
- *Separate `applicationId`* (for example `com.example.wear`): its own entry
  that contains only watch versions.

**Standalone watch app**

Same as above with a separate `applicationId`. No phone app and no phone
companion is needed to install it with OpenWrist.

**How to get into a repository**

1. Publish your source and add a build recipe for each variant. For
   f-droid.org this is a metadata file in
   [fdroiddata](https://gitlab.com/fdroid/fdroiddata), see the
   [F-Droid contributor documentation](https://f-droid.org/docs/Submitting_to_F-Droid_Quick_Start_Guide/).
   For IzzyOnDroid or your own repository, follow their rules.
2. Sign every release with the same key. Android rejects updates signed with
   a different key.
3. Check the result with the index: the watch build must carry the
   `android.hardware.type.watch` feature.

**Known limits** (not all verified on real watches)

- With a shared `applicationId`, f-droid.org archives old builds by
  `versionCode` and does not know about variants. The watch build can end up
  in the archive repository, which OpenWrist has disabled by default.
- For new installs, the F-Droid index names one preferred signer per app. If
  the watch build is signed only by you and the preferred signer is F-Droid's
  key, OpenWrist falls back to any signer. Use the same signing setup as your
  phone app to avoid surprises.
- The steps for f-droid.org are the general F-Droid process. The proof of
  concept did not submit an app, so build recipes for watch variants are
  untested.

## Documentation

- [Development setup](docs/development.md): get a machine ready to build OpenWrist
- [Architecture](docs/architecture.md): module layout and ground rules
- [Roadmap](docs/roadmap.md): order of work
- [Decisions](docs/decisions/): why things are built the way they are (ADRs)
- [Contributing](CONTRIBUTING.md): commits, code style, documentation
- [AGENTS.md](AGENTS.md): rules for AI agents working in this repo

## Contributing

Contributions are welcome, and so are questions: ask them in
[Discussions](https://github.com/datend3nker/OpenWrist/discussions). New here?
Look for issues labeled
[`good first issue`](https://github.com/datend3nker/OpenWrist/labels/good%20first%20issue).

OpenWrist is built **with AI, for people**: using AI tools is fine, but you
are responsible for what you submit.

- You must understand every line you contribute and be able to explain it.
- Fully automated or agentic contributions (PRs, issues or comments from bots
  or agents without a human in the loop) are not accepted.
- Vibe-coded changes you have not reviewed yourself are not accepted.
- Issues, pull requests and comments are written by you. Translation tools
  are fine; nobody is judged for grammar, spelling or language.

Details on commits, code style and the AI policy are in
[CONTRIBUTING.md](CONTRIBUTING.md). Everyone taking part follows the
[Code of Conduct](CODE_OF_CONDUCT.md). Security problems are reported
privately, see [SECURITY.md](SECURITY.md).

## License

OpenWrist is free software under the
[GNU General Public License v3.0 or later](LICENSE). Anyone may use, change
and share it. Modified versions must stay open under the same license. See
[ADR 0005](docs/decisions/0005-license-gpl-3.0.md) for the reasoning.
