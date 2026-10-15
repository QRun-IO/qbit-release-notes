# Changelog

All notable changes are documented here. This project follows Keep a Changelog and semantic versioning.

## [Unreleased]

### Added
- Release records, items, settings and per-user read state as standard QQQ tables (`release_notes_*`) with a Liquibase changelog (no foreign keys) and seeded settings (require approval, all users, notify).
- Once-per-version detection across pods and restarts (unique app + version), started by the host after startup.
- `ReleaseSource` SPI with `GitHubReleaseSource` (TAG and WINDOW modes; token read from the environment at call time) and `ComponentVersionsSource`.
- `ReleaseNotesDrafter` SPI with the deterministic `RuleBasedDrafter` and `ReleaseNotesSanitizer` (no internal references, personal data or secrets in published text).
- Publish, Skip and Redraft record actions and the Check for new release table action.
- One `RELEASE` notification per published release through `QAlerts.notify`, linking to What's new.
- UI API extension `release-notes` 1.0.0: releases, summary, seen.
- Help bundle covering the app, tables, fields and processes.
