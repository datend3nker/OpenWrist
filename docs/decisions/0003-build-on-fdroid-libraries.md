# 0003: Build on the F-Droid libraries

- **Status:** proposed
- **Date:** 2026-10-09

## Context

Parsing, verifying and updating F-Droid repositories is security-sensitive and
already solved by the F-Droid project. Writing it again would be costly and
risky. A proof of concept in a fork of the F-Droid client reuses `libs/index`,
`libs/database` and `libs/download` unchanged in a Wear OS module. It ran on
the Wear OS emulator only, not on real watches.

## Decision

OpenWrist is an independent third-party client. It uses F-Droid's open source
libraries and public repositories but is not affiliated with the F-Droid
project.

Reuse the F-Droid libraries for index handling, verification and download,
and write only the Wear OS specific parts ourselves: the watch UI and a
compatibility checker that accepts watch builds only.

Still open: whether the code stays a module in the F-Droid client fork (the
libraries are then used as they are) or moves into this repository (the
libraries then come as dependencies, if they are published for that). This
ADR stays `proposed` until that is decided.

## Alternatives

- **Own implementation of the index format:** full control, but a lot of work
  and a new source of security bugs.
- **Fork the F-Droid client:** inherits a large phone-oriented UI and code
  base that is hard to follow.

## Consequences

- Less code to write and audit, and compatibility with existing repositories.
- OpenWrist depends on the F-Droid libraries' API and release cycle.
- The license of each library must be compatible with
  [ADR 0005](0005-license-gpl-3.0.md).
