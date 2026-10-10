# 0002: Kotlin and Compose for Wear OS

- **Status:** proposed (the proof of concept already uses it)
- **Date:** 2026-10-09

## Context

OpenWrist runs on Wear OS and needs a UI that works on small round screens.

## Decision

Kotlin with Compose for Wear OS, built with Gradle.

## Alternatives

- **Views / XML layouts:** older, more code for lists and navigation, and
  Wear OS guidance favors Compose.
- **Flutter or other cross-platform toolkits:** no benefit for a Wear OS only
  app and weaker access to Wear OS APIs.

## Consequences

- Standard tooling and documentation for Wear OS apply.
- The app only runs on Wear OS, which is the goal.
