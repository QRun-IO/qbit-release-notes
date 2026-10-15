# QBit Release Notes: specification

Status: approved for build (owner request 2026-10-15). Version 0.1.

## Goal

Every release of a QQQ application produces one customer-facing release note that is shared
with all users through the notifications system and kept on a "What's new" page. Internal
release emails to the owner are separate and unchanged.

## Shape

| Piece | Where |
|---|---|
| Release records, items, settings, per-user read state | Standard QQQ tables (`release_notes_*`) in the host database |
| Detection (once per version, cluster-wide) | `ReleaseNotesRuntime.detectInBackground()`, called by the host after startup |
| Content sources | `ReleaseSource` SPI: `GitHubReleaseSource` (GitHub releases + merged PRs), `ComponentVersionsSource` (QQQ/qbit versions that changed) |
| Drafting | `ReleaseNotesDrafter` SPI; default `RuleBasedDrafter` (deterministic, no LLM) |
| Publishing | `Publish` record action (standard ETL review/result) or auto-publish when settings allow |
| Notification | `QAlerts.notify(NotificationSignal)` with category `RELEASE`, severity INFO |
| User-facing page | Next UI `/app/whats-new` via the `release-notes` UI API extension (`/qqq/v1/qbit/release-notes/*`) |

The qbit does **not** depend on qbit-alerts. It emits a core `NotificationSignal`; when
qbit-alerts is the instance's sink, the signal becomes a bell notification resolved to its
audience. Each user's existing per-category notification preferences apply: in-app can be
muted for `RELEASE`, and email is sent only when the user opted in for that category (through
qbit-email templates and brand). No separate alert type is needed: release notes are
notifications, not alerts.

## Tables

- `release_notes_settings` (one seeded row): publish mode (`REQUIRE_APPROVAL` default /
  `AUTO_PUBLISH`), audience (`ALL_USERS` or `PERMISSION` + value), notify on publish, drafting
  on/off, include platform updates, ingest GitHub on/off, title template, style guide (for
  editors and future drafters).
- `release_notes_release`: app name, version, commit, previous version, detected at, status
  (`DRAFT` / `PUBLISHED` / `SKIPPED`), title, summary, audience, published at/by,
  notified at; internal: drafted by, component versions (JSON), source log. Unique
  `(app_name, version)`.
- `release_notes_item`: release, category (`NEW` / `IMPROVED` / `FIXED` / `SECURITY` /
  `REMOVED`), customer-facing text, optional link and screenshot URL (an object-storage or
  https URL), sort order, included flag; internal: source type, source ref (e.g.
  `Website-Backend#262`), source title. Internal fields never leave the admin tables.
- `release_notes_user_state`: user id, last seen at. Drives the per-user "new" indicator.

No foreign keys (QQQ joins only). All DDL in the qbit's Liquibase changelog, runnable by the
application database user.

## Detection

On startup the host calls `detectInBackground()`. If the running version is a release version
(`X.Y.Z`), the runtime inserts the release row; the unique key makes this happen exactly once
across pods and restarts (the loser of a concurrent insert re-reads and stops). Only the
inserting pod gathers sources and drafts. Development versions (SNAPSHOT, commit hashes,
`unknown`) are ignored. A release row can be re-drafted with the `Redraft` action.

## Content and drafting

Sources return `ReleaseChange`s (title, body, labels, internal ref). The GitHub source reads
the releases of a repository between the previous and the current version (`TAG` mode, for the
application repository) or published in the window since the previous release (`WINDOW` mode,
for a separately-released frontend), collects the PR numbers from their bodies and reads each
PR's title, body and labels. It needs a read-only token for private repositories; without one
it records a warning and contributes nothing.

The rule-based drafter keeps user-visible changes only: it skips internal labels and
Conventional Commit types (`chore`, `ci`, `build`, `test`, `refactor`, `docs`, `style`),
dependency bumps and infrastructure work. A PR body section `Release notes:` / `## Release
notes` is used verbatim when present (authors write the customer text); otherwise the title is
cleaned (prefix, scope, PR and issue numbers, URLs, code spans removed). Category comes from
the type or labels. Changed platform components become one optional "Platform and
reliability improvements" item. `ReleaseNotesSanitizer` refuses PR/issue numbers, GitHub
URLs, commit hashes, ticket keys, e-mail and IP addresses and secret-looking text: drafted
items that fail are excluded, and publishing is refused while any included text fails.

Drafts are never published automatically unless settings say `AUTO_PUBLISH`. LLM drafting
(qbit-assistant) is a future drafter behind the same SPI; the assistant needs a user session,
so admins polish drafts with it interactively instead.

## Publishing

`Publish` (record action, permission `release_notes.publish`): review lists the selected
drafts and any sanitizer problems, then marks them `PUBLISHED`, stamps who/when and sends one
notification each (title "What's new: ...", body = summary, link
`/app/whats-new?release=<id>`, source key `release-notes:<app>:<version>` so a resend
updates rather than duplicates). `Skip` marks drafts `SKIPPED`. `Redraft` regathers.

## UI API (`release-notes` 1.0.0)

- `GET /qqq/v1/qbit/release-notes/releases?limit&offset`: published releases visible to the
  caller, newest first, with their included items and an `isNew` flag; `unreadCount`.
- `GET /qqq/v1/qbit/release-notes/summary`: `unreadCount` and the latest release, for the
  user-menu badge.
- `POST /qqq/v1/qbit/release-notes/seen`: marks everything as seen for the caller.

Any signed-in user may call them; audience is enforced per release. Only customer fields are
returned.

## Admin

App `releaseNotes` ("Release Notes"), placed by the host (kof22: Admin > System). Tables:
Releases, Items, Settings, Read State. Processes are record actions, never in the nav.

## Tests

Once-per-version under concurrency; approval flow; auto-publish; audience; notification
signal (category, link, source key); sanitizer refuses internal refs; drafter skip/categorize
rules; GitHub source parsing against recorded payloads; changelog runs twice; help coverage;
UI API contract.
