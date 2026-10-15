# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Overview

**QBit Release Notes**: customer-facing release notes for every release of a QQQ app. Detects a new version once across pods, drafts notes from GitHub releases/PRs and platform upgrades (rule-based, no LLM), waits for admin approval by default, publishes one `RELEASE` notification through `QAlerts.notify`, and serves What's new through the `release-notes` UI API extension. Design: docs/SPEC.md.

## Build Commands

```bash
./mvnw compile          # Compile (always use the wrapper)
./mvnw test             # Unit tests (H2)
./mvnw verify           # Tests + checkstyle + JaCoCo gate (70% instructions, 90% classes)
./mvnw -Dtest=ReleaseNotesMetaDataTest -Dreleasenotes.openapi.update=true test   # regenerate the committed OpenAPI
```

## Architecture

- Entry point: `ReleaseNotesQBitProducer implements QBitMetaDataProducer<ReleaseNotesQBitConfig>`.
- `ReleaseNotesRuntime` (supplemental instance metadata): config, drafter, clock, `detectInBackground()`.
- `ReleaseNotesService`: detect (unique insert), draft, problems, publish, skip, What's new.
- `sources/`: `ReleaseSource` SPI, `GitHubReleaseSource`, `ComponentVersionsSource`.
- `drafting/`: `ReleaseNotesDrafter` SPI, `RuleBasedDrafter`, `ReleaseNotesSanitizer`.
- `processes/`: Publish / Skip / Redraft record actions (standard ETL), Check for new release.
- `api/`: UI API extension specs and versioned schemas.

## Invariants

- Internal fields (source refs, titles, commit, source log, component versions) never leave the admin tables or appear in the UI API.
- Nothing an automated drafter wrote is published unless settings say AUTO_PUBLISH; the sanitizer gates every publish.
- No secrets in metadata: GitHub tokens are read from the environment at call time.
- DB changes only in the Liquibase changelog; the only foreign key is item -> release.

## Naming (owner rule)

- Tables are lowercase snake_case prefixed `release_notes_`; backend name equals the table name.
- Permissions: `release_notes.manage`, `release_notes.publish`.
- Processes run as table or record actions, never in the nav.

## Code Style

- 3-space indentation, opening braces on the next line, `return (x);`.
- Flower-box comments on every class and method.
- Fluent setters (`withX()`), wrapper types in config and entities.

## Dependencies

- Java 21; parent `com.kingsrook:qbit-build-parent:2.1.1` with guardrails; the default-active `qqq-snapshot` profile imports `qqq-bom-pom:4.1.0-RC.13`.
- Tests: JUnit 5, AssertJ, H2, Liquibase.

## Release

- CircleCI qqq-orb; `develop` publishes SNAPSHOTs; a `release/*` branch publishes RCs (merge a develop PR labelled `release:rc`); a `v*` tag publishes the release.
