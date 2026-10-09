# AGENTS.md

Rules for AI agents (Claude Code, Codex, Copilot, etc.) working in this repo.
The same rules apply to humans, see [CONTRIBUTING.md](CONTRIBUTING.md).

## The project in one sentence

OpenWrist is an unofficial third-party client for Wear OS that uses F-Droid
repositories (not affiliated with F-Droid), written in Kotlin with Compose for
Wear OS. State and plan: [README](README.md),
[architecture](docs/architecture.md), [roadmap](docs/roadmap.md).

## Top rule: stay understandable

The code is written with AI but read and maintained by humans. Readability
beats brevity and cleverness.

## You work for a human

Every contribution is submitted by a human who is responsible for it (see the
[AI policy](CONTRIBUTING.md#ai-assisted-contributions)). So:

- Never open pull requests, issues or comments on your own.
- Do not write issue, pull request or review text for your human to post.
  Translating text they wrote themselves is fine.
- Write code your human can follow: small steps, clear names, explain
  non-obvious decisions in the conversation and in comments.
- When in doubt, ask instead of guessing.

## How to work

- **Small commits with one intent.** Format and rules in
  [CONTRIBUTING.md](CONTRIBUTING.md#commits). Three clear commits are better
  than one that mixes everything.
- **Setup:** [docs/development.md](docs/development.md).
- **Before committing:** `./gradlew ktlintCheck detekt test` (once the Gradle
  project exists). Do not create a commit that does not build.
- **Build only what the task asks for.** No modules, interfaces,
  configuration or features in advance.
- **Docs in the same commit as the code.** If the architecture changes, update
  `docs/architecture.md`. For a larger decision, add an ADR in
  `docs/decisions/` (template `0000-template.md`).
- **Be open about uncertainty.** Mark anything not verified (library version,
  Wear OS API behavior, device behavior) as an assumption.

## Architecture rules

- Only the `app` module may depend on Compose, Wear OS UI or other
  Android UI libraries. Repository, index and install logic stay free of UI.
- Do not present OpenWrist as official or as part of F-Droid: no F-Droid
  logo or branding, and wording such as "unofficial client" where users see it.
- Never install an app without verifying its signature and checksum first.
- Every user-visible string is an Android string resource
  (`strings.xml`), never hard-coded. Only `app` produces user-visible text;
  other modules return typed errors and sealed classes.
- OpenWrist is `GPL-3.0-or-later`. Only add dependencies or copy code under
  GPL-3.0-compatible licenses, and keep copyright notices of copied code.

## Language

English for code, comments, commits and documentation. User-facing text is
written in English in the source and translated via resource files.
