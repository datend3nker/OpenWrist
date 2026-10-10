# Security Policy

## Supported versions

OpenWrist has no releases yet. Security fixes go into the `main` branch.

## Reporting a vulnerability

**Please do not report security problems in public issues.**

Use GitHub's private reporting instead: open the
[Security tab](https://github.com/datend3nker/OpenWrist/security) of the
repository and click **Report a vulnerability**. Only the maintainers can see
the report.

Please include:

- what the problem is and what an attacker could do with it,
- steps or a file to reproduce it,
- the OpenWrist version or commit you tested.

You will get an answer within two weeks. OpenWrist is a hobby project, so please
be patient. Once a fix is available, the problem will be disclosed together
with credit to you, unless you prefer to stay anonymous.

## What is in scope

OpenWrist downloads and installs apps on a watch, so the most relevant
problems are in code that fetches, verifies or installs them:

- repository index parsing and signature verification
- APK download, integrity checks and installation
- communication between watch and phone
- stored repository settings and credentials

Installing an app that was not verified, or a crash, hang or excessive memory
use caused by a crafted index or APK, counts as a security problem.
