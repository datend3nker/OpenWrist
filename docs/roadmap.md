# Roadmap

Ordered by risk: whatever could make the project fail comes first.

The work is split into **epics**, and each epic into **tickets**. Both are
written so they can be copied into GitHub as they are:

- An epic becomes a tracking issue with the label `epic`. Its goal and
  acceptance criteria go into the issue description.
- Each ticket becomes an issue (or sub-issue of the epic). The ticket line is
  the issue title, the indented line is its acceptance criterion.
- Epics are grouped into **milestones**, which become GitHub milestones. Each
  milestone ends with something a person can actually use or evaluate.

Tickets in later epics are rough on purpose. They get sharper when the epic
starts, based on what earlier epics taught us.

## Starting point: proof of concept

A proof of concept exists outside this repository: a `wear` module in a fork
of the F-Droid client (`gitlab.com/datend3nker/fdroidclient`, branches
`wear/01` to `wear/07`). It downloads the index, shows only watch builds,
installs and updates them with `PackageInstaller` and has a small settings
screen. It was tested on the Wear OS emulator only.

The first milestone uses it to answer the open questions before new code is
written.

## Labels

| Label | Meaning |
|---|---|
| `epic` | Tracking issue for an epic |
| `feature` | New behavior for users |
| `bug` | Something does not work as intended |
| `task` | Build, CI, packaging, infrastructure |
| `research` | Investigate options and record the result as an ADR |
| `test` | Tests |
| `docs` | Documentation only |
| `hardware` | Report about a watch or Wear OS version |
| `compatibility` | A watch app that OpenWrist does not show or install correctly |
| `good first issue` | Small, well-described ticket for a first contribution |
| `help wanted` | Maintainers would welcome help with this |
| `accessibility` | Barrier affecting people with disabilities |
| `duplicate` | Already reported elsewhere (triage) |
| `invalid` | Not a valid report (triage) |
| `wontfix` | Will not be worked on (triage) |

Area labels name the module or topic: `repo`, `install`, `app`, `i18n`, `ci`,
`packaging`.

## Overview

| Milestone | Epics | Result |
|---|---|---|
| **M0: Foundation** | E1, E2, E3, E4 | Proof of concept tested on real watches, home of the code decided, project builds |
| **M1: First release** (v0.1) | E5, E6, E7 | Browse, install and update watch apps on a real watch, published in a repository |
| **Later** | E8, E9, E10 | Own repositories, app icons, background updates |

---

## M0: Foundation

### E1: Test the proof of concept on real watches

`epic` · `research` · `install`

**Goal:** find out whether installing apps works on real watches. This is the
biggest risk: the emulator has a settings screen for "Install unknown apps",
other watches may need ADB.

**Acceptance criteria:**
- The proof of concept was tested on at least one real watch.
- The results per watch and Wear OS version are documented in
  [development.md](development.md).

**Tickets:**

- [ ] **E1.1** Install the proof of concept on a real watch via ADB · `research`
  - The app starts and loads the index on the watch.
- [ ] **E1.2** Test the "Install unknown apps" permission flow on real watches · `research` `install`
  - It is clear whether users can grant the permission on the watch or need ADB.
- [ ] **E1.3** Install and update an app end to end on a real watch · `research` `install`
  - One install and one update succeed, including the system confirmation.
- [ ] **E1.4** Document results per watch and Wear OS version · `docs`

### E2: Index size and speed

`epic` · `research` · `repo`

**Goal:** a first start that does not take minutes. The f-droid.org index is
63 MB, and storing it took about 6 minutes on the emulator.

**Acceptance criteria:**
- Load time, storage and memory use on a real watch are measured.
- An ADR describes how OpenWrist handles the index.

**Tickets:**

- [ ] **E2.1** Measure index download, parse and store time on a real watch · `research` `repo`
- [ ] **E2.2** Measure storage and memory use of the index · `research` `repo`
- [ ] **E2.3** Evaluate ways to speed up the first import · `research` `repo`
  - For example index diffs, dropping non-watch apps early or importing in
    the background.
- [ ] **E2.4** Decide how OpenWrist handles the index · `research` `docs`
  - ADR accepted.

### E3: Decide where the code lives

`epic` · `research`

**Goal:** decide whether OpenWrist stays a module in the F-Droid client fork
or moves into this repository with the F-Droid libraries as dependencies.

**Acceptance criteria:**
- [ADR 0003](decisions/0003-build-on-fdroid-libraries.md) is accepted or
  superseded.
- [ADR 0002](decisions/0002-kotlin-and-compose-for-wear-os.md) is accepted or
  superseded.

**Tickets:**

- [x] **E3.1** Check how the F-Droid libraries can be used outside the client · `research`
  - It is clear whether they are published as artifacts or have to be
    vendored, and what that means for updates.
- [x] **E3.2** Decide where the code lives · `research` `docs`
  - ADR 0003 is accepted or superseded.
- [ ] **E3.3** Confirm or revise Kotlin and Compose for Wear OS · `research` `docs`
  - ADR 0002 is accepted or superseded.

### E4: Set up the build foundation

`epic` · `task` · `ci` · `i18n`

**Goal:** a project every contributor can build and that CI checks. The
details depend on E3; if the code stays in the F-Droid client fork, this epic
is adapted.

**Acceptance criteria:**
- A fresh clone builds with the commands from [development.md](development.md).
- CI is green on `main`.
- The placeholder screen shows up translated with a test translation.

**Tickets:**

- [x] **E4.1** Set up the Gradle project with wrapper and version catalog · `task`
  - `./gradlew build` works on a fresh clone.
- [x] **E4.2** Add the `app` module with an empty Compose for Wear OS screen · `feature` `app`
  - The app starts on the Wear OS emulator; the application ID is chosen.
- [x] **E4.3** Set up ktlint and detekt · `task`
  - `./gradlew ktlintCheck detekt` runs and fails on violations.
- [x] **E4.4** Add CI workflow for lint, detekt and unit tests · `task` `ci`
  - Pull requests show a failing check on lint, detekt or test errors.
- [x] **E4.5** Add string resources with a test translation · `task` `i18n`
  - The placeholder text comes from `strings.xml`
    ([ADR 0004](decisions/0004-translations-with-android-resources.md)).
- [x] **E4.6** Keep dependencies up to date with Dependabot · `task` `ci`
  - Dependabot opens pull requests for new versions in
    `gradle/libs.versions.toml`, the Gradle wrapper and the GitHub Actions
    in `.github/workflows/`.

---

## M1: First release (v0.1)

### E5: Browse and search apps

`epic` · `feature` · `repo` · `app`

**Goal:** find watch apps on the watch.

**Acceptance criteria:**
- The app list shows only watch builds that match the watch's ABI.
- Apps can be searched and their details opened on a round screen.

**Tickets:**

- [x] **E5.1** Download and cache the repository index · `feature` `repo`
- [x] **E5.2** Offer only versions that declare `android.hardware.type.watch` · `feature` `repo`
- [x] **E5.3** Offer only versions with native code for the watch's ABI · `feature` `repo`
- [x] **E5.4** Show the app list on a round screen · `feature` `app`
- [x] **E5.5** Search apps · `feature` `app`
- [x] **E5.6** Show app details · `feature` `app`

### E6: Install and update apps

`epic` · `feature` · `install`

**Goal:** install and update apps safely.

**Acceptance criteria:**
- Nothing is installed without a verified index signature, APK checksum and
  APK signature.
- Installed apps are updated when a newer watch build exists.

**Tickets:**

- [x] **E6.1** Verify the index signature · `feature` `repo`
- [x] **E6.2** Download APKs and verify checksum and signature · `feature` `install`
- [x] **E6.3** Install through `PackageInstaller` with the system confirmation · `feature` `install`
- [x] **E6.4** Guide users through the install permission · `feature` `app`
  - Based on the results of E1.
- [x] **E6.5** Detect and install updates · `feature` `install`
- [x] **E6.6** Test that tampered APKs and indexes are rejected · `test` `install`

### E7: First release

`epic` · `task` · `packaging`

**Goal:** people can install OpenWrist on their watch.

**Acceptance criteria:**
- v0.1.0 is available in an F-Droid compatible repository.

**Tickets:**

- [ ] **E7.1** Set up signed release builds · `task` `packaging`
- [ ] **E7.2** Add German translation · `feature` `i18n`
- [ ] **E7.3** Add store metadata (description, screenshots) · `task` `packaging`
- [ ] **E7.4** Submit OpenWrist to f-droid.org or IzzyOnDroid · `task` `packaging`
- [ ] **E7.5** Prepare the repository for going public · `docs`
  - [ADR 0005](decisions/0005-license-gpl-3.0.md) reviewed, code-of-conduct
    contact filled in, private vulnerability reporting enabled (GitHub offers
    it only for public repositories), `main` protected, and the
    "not affiliated with F-Droid" wording checked.
- [ ] **E7.6** Release v0.1.0 with release notes · `task`

---

## Later

### E8: Manage repositories

`epic` · `feature` · `repo`

- [ ] **E8.1** Show and enable or disable repositories · `feature` `repo` `app`
- [ ] **E8.2** Add a repository by URL and fingerprint · `feature` `repo` `app`
  - Typing on a watch is hard; consider other ways, such as a QR code.

### E9: App icons

`epic` · `feature` · `app`

- [ ] **E9.1** Show app icons in the list and details · `feature` `app`

### E10: Background updates

`epic` · `feature` · `install`

- [ ] **E10.1** Check for updates in the background · `feature` `install`
- [ ] **E10.2** Notify about available updates · `feature` `app`

---

## Biggest risks

1. Install permission on real watches: the emulator has a settings screen for
   "Install unknown apps", other watches may need ADB (E1)
2. Index size and speed on a watch: the f-droid.org index is 63 MB and storing
   it took about 6 minutes on the emulator (E2)
3. Usability on a small screen (E5)
4. Reusing the F-Droid libraries outside the F-Droid client (E3)
