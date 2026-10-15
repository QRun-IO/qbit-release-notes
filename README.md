# QBit Release Notes

Customer-facing release notes for every release of a QQQ application. When a new version starts
for the first time, the qbit records it once (across all pods), drafts plain-language notes from
the merged pull requests in the application's GitHub releases and from platform component
upgrades, and waits for an admin to review and publish them. Publishing sends one notification
to every user (through the core notification signal that qbit-alerts delivers to the bell and,
for users who opted in, by email) and adds the release to a "What's new" page. Design:
[docs/SPEC.md](docs/SPEC.md).

## Requirements

- Java 21, QQQ 4.1.0-RC.13 or newer.
- An RDBMS backend (PostgreSQL in production; H2 in tests).
- Optional: `qqq-middleware-javalin` for the UI API; a notification sink (qbit-alerts) to deliver
  notifications. Without one, publishing still works and notifications are only logged.

## Install

```xml
<dependency>
   <groupId>com.kingsrook.qbits</groupId>
   <artifactId>qbit-release-notes</artifactId>
   <version>0.1.0-RC.1</version>
</dependency>
```

Include `db/liquibase/qbit-release-notes/release_notes.changelog.sql` once in the host's Liquibase
changelog. It creates the four tables (no foreign keys) and seeds the settings row.

```java
ReleaseNotesQBitConfig config = new ReleaseNotesQBitConfig()
   .withBackendName("websiteDatabase")
   .withApplicationName("kof22").withApplicationLabel("kof22")
   .withAppVersion(currentReleaseVersion)          // X.Y.Z; anything else is ignored
   .withAppCommit(commitShaShort)
   .withSource(new GitHubReleaseSource("Org", "App-Backend", GitHubReleaseSource.Mode.TAG).withTokenEnvVar("RELEASE_NOTES_GITHUB_TOKEN"))
   .withSource(new GitHubReleaseSource("Org", "App-Frontend", GitHubReleaseSource.Mode.WINDOW).withTokenEnvVar("RELEASE_NOTES_GITHUB_TOKEN"))
   .withSource(new ComponentVersionsSource());
new ReleaseNotesQBitProducer().withQBitConfig(config).produce(qInstance);

// after the server has started (tables migrated):
ReleaseNotesRuntime.of(qInstance).detectInBackground();
```

Grant `release_notes.manage` (edit drafts, items, settings) and `release_notes.publish` (Publish,
Skip) to admins: `config.getAdminPermissionNames()`.

## Tables

| Table | Holds |
|---|---|
| `release_notes_release` | One row per version (unique app + version): status Draft / Published / Skipped, title, summary, audience; internal commit, source log and component versions. |
| `release_notes_item` | Customer text by category (New, Improved, Fixed, Security, Removed), optional link and screenshot URL, included flag; internal source reference and original title. |
| `release_notes_settings` | Publish mode (require approval / automatic), default audience, notify on publish, drafting on/off, GitHub ingestion, platform updates item, title template, style guide. |
| `release_notes_user_state` | When each user last opened What's new. |

## Actions

Record actions on Releases (standard review and result): **Publish**, **Skip**, **Redraft**; table
action **Check for new release**. None are in the nav.

## Content rules

The rule-based drafter keeps user-visible changes only and never publishes. A PR body line
`Release notes: <text>` (or a `## Release notes` section) is used as written; `Release notes: none`
excludes the PR. Conventional Commit types `chore`, `ci`, `build`, `test`, `refactor`, `docs`, `style`,
labels such as `internal`, `dependencies`, `skip-release-notes`, and infrastructure titles are
excluded. A sanitizer refuses PR/issue numbers, GitHub links, commit hashes, ticket keys, e-mail and
IP addresses, internal host names and secret-looking text, and blocks publishing while any
included text fails.

## UI API

Extension `release-notes` 1.0.0 (committed spec `src/main/resources/openapi/qqq-v1/release-notes.yaml`):
`GET /qqq/v1/qbit/release-notes/releases`, `GET /qqq/v1/qbit/release-notes/summary`,
`POST /qqq/v1/qbit/release-notes/seen`. Only customer fields are returned.

## Adopting in another app

Configure the sources for your repositories (a read-only, fine-grained GitHub token with
Contents and Pull requests read access on those repositories), include the changelog, register
the producer, call `detectInBackground()` after startup, grant the two permissions, and render
What's new from the UI API (Next UI does this when the extension is installed).

## Build

```bash
./mvnw verify
```

## License

Apache License 2.0. See [LICENSE](LICENSE).
