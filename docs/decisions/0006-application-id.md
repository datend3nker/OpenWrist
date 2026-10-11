# 0006: Application ID io.github.datend3nker.openwrist

- **Status:** accepted
- **Date:** 2026-10-11

## Context

Android identifies an app by its application ID. Updates are only accepted
when the application ID and the signing key match the installed app, so the
ID cannot change after the first release without users losing updates.

The ID should be a reverse domain the project controls, and it must not
suggest that OpenWrist is part of F-Droid.

## Decision

The application ID and the Kotlin root package are
`io.github.datend3nker.openwrist`. The project is hosted at
`github.com/datend3nker`, so `datend3nker.github.io` belongs to it.

## Alternatives

- `org.fdroid.*`: would look official and is not our domain.
- An own domain such as `org.openwrist`: needs a domain the project does not
  own yet.
- `com.example.*`: placeholder, rejected by many tools and stores.

## Consequences

- The ID is fixed from the first release on.
- If the repository moves to another account or organization, the ID stays
  as it is.
