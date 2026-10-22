# Privacy policy

OpenWrist is an unofficial, free and open source client for Wear OS that
installs apps from F-Droid compatible repositories. It is not affiliated with
F-Droid. This page says what the app does with data. The source code is public
at <https://github.com/datend3nker/OpenWrist>, so every statement here can be
checked.

## In short

- OpenWrist has **no account, no analytics, no advertising and no tracking**.
- The developer **does not receive any data** from the app and runs no server
  for it.
- The app only talks to the app repositories that are enabled in it.

## What the app contacts

When it refreshes its app list, downloads an icon or downloads an app to
install it, OpenWrist connects to the **repositories enabled in the app**. By
default that is f-droid.org (`https://f-droid.org/repo`); its archive is
included but disabled. You can add other repositories, for example
IzzyOnDroid or your own, and turn any of them off.

These servers can see what every web server sees: your IP address, the time
of the request, which file was requested, and the user agent
`OpenWrist <version>`. Their own privacy policies apply, for example that of
f-droid.org. OpenWrist adds no identifier of its own: no device ID, no
advertising ID, no account.

## What stays on the watch

- The list of repositories, a copy of their app indexes (names, descriptions,
  versions) and the app's settings, in the app's private storage.
- App icons and downloaded app files, in the app's cache. A downloaded app is
  installed only after OpenWrist has verified the repository index signature,
  the file checksum and the app's signing certificate.

Uninstalling OpenWrist or clearing its data removes all of it.

## Permissions and why

| Permission | Reason |
|---|---|
| Internet | Download repository indexes, icons and apps. |
| Notifications | Tell you that updates are available (a daily check). |
| Install and uninstall apps | Install, update and remove apps when you ask for it. Android asks you to confirm. |
| See all installed apps | Compare installed versions with the repository to offer updates. The list stays on the watch and is not sent anywhere. |

## Backups

The app allows Android's backup. If you have a backup of the watch turned on,
the app's private data (see above) can be part of it, under the rules of your
Google account and device.

## Children

OpenWrist is not directed at children and collects no personal data from
anyone.

## Changes and contact

Changes to this page are visible in the repository history. Questions go to
the issue tracker at <https://github.com/datend3nker/OpenWrist/issues>. For
sensitive reports use the private reporting described in
[SECURITY.md](../SECURITY.md).
