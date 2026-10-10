# 0004: Translations with Android string resources

- **Status:** proposed
- **Date:** 2026-10-09

## Context

OpenWrist should be easy to translate. Adding translation support later means
hunting down every hard-coded string, so the mechanism has to be in place from
the first user-visible text.

## Decision

- All user-visible strings are Android string resources (`strings.xml`), one
  `values-xx` folder per language.
- **Only `app` produces user-visible text.** Domain modules return typed
  errors and sealed classes; `app` turns them into translated messages.

## Alternatives

- **Strings in code, translate later:** cheap now, expensive later.
- **A separate i18n library:** adds a dependency for something the platform
  already does.

## Consequences

- Translators can use standard tools (Weblate, Android Studio) without
  knowing Kotlin.
- Adding a language means adding a resource file; no code changes.
