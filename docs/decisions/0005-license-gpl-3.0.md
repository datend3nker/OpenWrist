# 0005: License under GPL-3.0-or-later

- **Status:** accepted
- **Date:** 2026-10-09

## Context

OpenWrist should be free for everyone to use. At the same time, no company
should be able to take it and turn it into a closed product. The F-Droid client
is itself GPL-licensed. OpenWrist is made with AI, for people.

## Decision

OpenWrist is licensed under the **GNU General Public License v3.0 or later**
(SPDX: `GPL-3.0-or-later`). The full text is in [`LICENSE`](../../LICENSE).

## Alternatives

- **MIT / Apache-2.0:** anyone, including companies, could build a closed
  product on top of OpenWrist.
- **PolyForm Noncommercial:** forbids commercial use entirely, but is not open
  source. It would be incompatible with GPL-licensed F-Droid libraries, keep
  OpenWrist out of F-Droid itself and put off contributors.

## Consequences

- Anyone may use, study, change and share OpenWrist, including companies.
- Anyone who distributes a modified version must publish its source under the
  GPL. A closed fork is not allowed.
- GPL-licensed F-Droid code may be used with its copyright notices kept, see
  [ADR 0003](0003-build-on-fdroid-libraries.md).
- **Changing the license later needs the consent of every copyright holder.**
  While the project has a single author this is easy. Once outside
  contributions are accepted, it is practically impossible without a
  contributor agreement. Revisit this decision before going public.
- Purely AI-generated code may not be protected by copyright in some
  jurisdictions, which weakens any license. Human selection and revision of
  the code strengthen protection.
