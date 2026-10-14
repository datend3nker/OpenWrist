# 0003: Build on the F-Droid libraries

- **Status:** accepted
- **Date:** 2026-10-09, accepted 2026-10-14

## Context

Parsing, verifying and updating F-Droid repositories is security-sensitive and
already solved by the F-Droid project. Writing it again would be costly and
risky. A proof of concept in a fork of the F-Droid client reuses `libs/index`,
`libs/database` and `libs/download` unchanged in a Wear OS module. It ran on
the Wear OS emulator only, not on real watches.

### Are the libraries published? (E3.1, checked 2026-10-13)

Yes. The F-Droid client repository
([`libs/`](https://gitlab.com/fdroid/fdroidclient/-/tree/master/libs))
publishes them to Maven Central:

| Artifact | Latest | Published | License |
|---|---|---|---|
| `org.fdroid:index` | 0.2.0 | 2025-11-03 | Apache-2.0 |
| `org.fdroid:download` | 0.2.0 | 2025-11-03 | Apache-2.0 |
| `org.fdroid:database` | 0.2.0 | 2025-11-03 | Apache-2.0 |
| `org.fdroid:core` (pulled in by the others) | 0.0.1 | | Apache-2.0 |

- `index` and `download` are Kotlin Multiplatform libraries; Gradle picks the
  Android variants (`index-android`, `download-android`) on its own.
  `database` is an Android library built on Room.
- They bring Room, Ktor with OkHttp and kotlinx-serialization as transitive
  dependencies.
- A trial build of `app` with all three as dependencies succeeded for debug
  and release (AGP 9.4.1, Kotlin 2.4.21, minSdk 30). No code used them, so
  this only shows that they resolve and compile, not that they work at
  runtime. The change was not committed.
- Apache-2.0 code may be used in a GPL-3.0 project
  ([ADR 0005](0005-license-gpl-3.0.md)).

What this means for updates:

- Releases are rare. 0.2.0 is the latest release, and the `libs/` folder
  has had 87 commits on `master` since then that are not published. Fixes
  reach OpenWrist only when the F-Droid project publishes a new version.
- The versions are `0.x`, so the API may change between releases.
- A fix we need before the next release would have to wait, be sent
  upstream, or be vendored (copied in with its copyright notices) for a
  while.

This answers whether the libraries can come as dependencies. Whether they
should is decided in E3.2.

## Decision

OpenWrist is an independent third-party client. It uses F-Droid's open source
libraries and public repositories but is not affiliated with the F-Droid
project.

Reuse the F-Droid libraries for index handling, verification and download,
and write only the Wear OS specific parts ourselves: the watch UI and a
compatibility checker that accepts watch builds only.

The code lives in this repository, not in the F-Droid client fork (E3.2).
The libraries come as normal dependencies from Maven Central, with the
versions in `gradle/libs.versions.toml`. Dependabot proposes new versions.

If OpenWrist needs a fix that is not released yet, we first ask the F-Droid
project for a release. Only if that takes too long, we build the libraries
from a pinned commit of the F-Droid client for a while (Git submodule and
Gradle composite build). That route was not tried yet.

## Alternatives

- **Own implementation of the index format:** full control, but a lot of work
  and a new source of security bugs.
- **Fork the F-Droid client:** inherits a large phone-oriented UI and code
  base that is hard to follow. The proof of concept took this route.
- **Git submodule of the F-Droid client, built from source:** always the
  newest code, but every clone needs the whole client repository, our build
  has to match its Gradle and Kotlin setup, and a submodule is pinned to a
  commit too, so it is not more up to date without manual updates.
- **Copy the library code into this repository:** we would maintain
  security-sensitive code that is not ours.

## Consequences

- Less code to write and audit, and compatibility with existing repositories.
- OpenWrist depends on the F-Droid libraries' API and release cycle. The
  libraries are at `0.x`, so updates may need code changes, and fixes on
  `master` reach OpenWrist only with the next release.
- The license of each library must be compatible with
  [ADR 0005](0005-license-gpl-3.0.md).
