# Roadmap

Ordered by risk: whatever could make the project fail comes first.

1. **Proof of concept (done, not part of this repository):** a `wear` module
   in a fork of the F-Droid client (`gitlab.com/datend3nker/fdroidclient`,
   branches `wear/01` to `wear/07`). It downloads the index, shows only watch
   builds, installs and updates them with `PackageInstaller` and has a small
   settings screen. Tested on the Wear OS emulator only.
2. **Decide the home of the code:** keep it as a module in the F-Droid client
   fork, or move it into this repository with the F-Droid libraries as
   dependencies. See [ADR 0003](decisions/0003-build-on-fdroid-libraries.md).
3. **Test on real watches**, including the install permission flow
4. **Index size:** the first import is slow, see risks
5. **Production quality:** tests, lint, release builds, translations
6. **Later:** arbitrary repositories, app icons, background updates

## Biggest risks

1. Install permission on real watches: the emulator has a settings screen for
   "Install unknown apps", other watches may need ADB
2. Index size and speed on a watch: the f-droid.org index is 63 MB and storing
   it took about 6 minutes on the emulator
3. Usability on a small screen
4. Reusing the F-Droid libraries outside the F-Droid client
