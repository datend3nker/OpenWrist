# R8 rules for apps that use this module. The app's release build shrinks
# and renames all code, including the F-Droid libraries.

# After the first full download, RepoUpdater updates a repository with index
# diffs. org.fdroid:database applies them with ReflectionDiffer, which uses
# kotlin-reflect: it reads the primary constructor and the property names of
# the database entities and of the index v2 classes, and matches them against
# the JSON keys of the diff. That needs the Kotlin metadata and the original
# class and member names. Without these rules the update fails with
# "no primary constructor class ...".
-keep class kotlin.Metadata { *; }
-keep class org.fdroid.database.** { *; }
-keep class org.fdroid.index.v2.** { *; }
