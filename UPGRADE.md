# Upgrading QBit Release Notes

## First release

There is no previous release. See [README.md](README.md) for installation.

Use QQQ 4.1.0-RC.13 or newer and Java 21. Include
`db/liquibase/qbit-release-notes/release_notes.changelog.sql` once in the host's Liquibase changelog
and run migrations before the application starts; the qbit does not apply them. Seeding is
insert-only, so edited settings are kept.

Register the producer with the running release version, grant `release_notes.manage` and
`release_notes.publish`, and call `ReleaseNotesRuntime.of(qInstance).detectInBackground()` after the
server starts. The first release records its own GitHub release only (there is no previous version
to compare against).
