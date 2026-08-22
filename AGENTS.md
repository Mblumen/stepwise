# AGENTS.md

## Project

This repository contains an existing Android application written in Java.

Stepwise turns steps recorded by the phone or Fitbit into distance along a
selected virtual track. Track routes and milestones are visualized with
OpenStreetMap tiles.

The application is being incrementally improved. The existing codebase is the
source of truth for architecture, conventions, and behavior.

Before making non-trivial changes, inspect the relevant existing code and
understand how the application currently solves the problem.

## General rules

* Prefer small, focused changes.
* Follow existing project conventions.
* Reuse existing abstractions and infrastructure.
* Preserve existing behavior unless the task explicitly requires a change.
* Avoid unrelated refactoring.
* Do not introduce new frameworks or dependencies when existing project
  infrastructure already provides the required functionality.
* Do not introduce Kotlin unless explicitly requested.
* Do not modify unrelated files.
* Do not create commits unless explicitly requested.
* Never claim that something was tested or verified if it was not.

## Android and Java

Use the Java, Android, Gradle, and SDK versions configured by the project.

The repository has one `app` module. It currently uses Java 21, minSdk 30,
targetSdk 35, and compileSdk 35, built with Kotlin DSL, Gradle 8.13, and Android
Gradle Plugin 8.13.2. Application code belongs under the `de.hd.stepwise`
package. The single existing Kotlin event-bus file is an exception; keep new
application code in Java unless Kotlin is explicitly requested.

The UI is Views-based: XML layouts, view binding, one main activity, fragments,
RecyclerView adapters, ViewModels, and LiveData. Use the existing Navigation
Component graph and Safe Args for in-app navigation. Do not introduce Compose.

The application follows a pragmatic UI -> ViewModel -> repository -> Room DAO
flow. Hilt supplies application-scoped services, repositories, ViewModels, and
WorkManager workers. Extend those seams rather than accessing persistence or
constructing shared services directly from new UI code.

Follow the existing project architecture and Android lifecycle patterns. Do not
introduce a new architectural pattern merely because it is considered modern.

Avoid blocking I/O, database operations, or network requests on the main thread.
Existing repositories and helpers use executors, Room LiveData, and `postValue`;
WorkManager performs periodic step synchronization. Preserve lifecycle-aware
observation and the established executor-based threading style.

## Persistence and integrations

Room is the source of truth for tracks, milestones, progress, settings, step
events, daily steps, achievements, and records. Schema exports are committed in
`app/schemas`. Increase the database version, add an explicit forward migration,
and update the exported schema for every schema change; do not use destructive
migration as a shortcut.

Track, achievement, image, and GeoJSON catalog data is downloaded and cached by
the existing initialization helpers. Route rendering uses osmdroid with Mapnik
OpenStreetMap tiles; GeoJSON routes are converted to osmdroid `GeoPoint`s and the
virtual position is interpolated from distance walked. Reuse this route pipeline
instead of adding another map abstraction. MapLibre and Google Maps dependencies
exist, but they are not the primary embedded map implementation.

Step input is selected through `StepSource`: the Android
`TYPE_STEP_COUNTER` sensor or the Fitbit Web API using AppAuth. Both sources
produce persisted `StepEvent`s, and the Hilt-enabled `StepSyncWorker` consumes
unhandled events, advances progress, and dispatches milestone, achievement, and
track-finished notifications. Keep sensor baselines, source switching, event
handling, boot behavior, runtime permissions, and notification behavior in the
existing `progresstracking` components.

Network access currently uses `HttpURLConnection` in download/Fitbit helpers and
OkHttp in routing code. Fitbit authentication state is stored in encrypted
SharedPreferences. A GitHub token may be supplied as `GITHUB_TOKEN` in local
Gradle properties; never commit credentials or log tokens.

## Domain model

* A `Track` is reusable catalog data with start/end metadata and an optional
  cached route. Its ordered `Milestone`s store the distance from the previous
  milestone; the `MilestoneWithTotalDistance` Room view derives cumulative
  distance along the track.
* A `UserProgress` is one user's progress instance for a track. It stores steps,
  distance in meters, timing information, and an `ACTIVE`, `PAUSED`, or
  `COMPLETED` status. Only one progress is intended to be active; starting or
  resuming another pauses the current one.
* Step deltas are converted to meters with the user's configured step length and
  applied only to active progress. The cumulative distance drives both the
  virtual map position and milestone reach checks.
* Reaching a milestone creates a per-progress `ReachedMilestone` fact,
  preserving the steps at which it was reached and preventing duplicate
  notifications. The milestone's catalog-level
  `unlocked` flag is also updated for display.
* Passing the final milestone produces a track-finished result and notification;
  completion is persisted when the existing finish-progress flow changes the
  progress status to `COMPLETED`. Preserve this distinction when changing
  completion behavior.

Keep entities in `entities`, Room access in `daos`, data orchestration in
`repositories`, step acquisition/sync in `progresstracking`, route calculations
in `routing`, remote JSON shapes in `dtos`, and screen code below `ui`.

## Testing

Changes that affect behavior should include appropriate tests where practical.

Use the testing frameworks and patterns already established by the project.

Before completing a task, run the most relevant available tests and build
verification when practical.

The current test suites contain only Android template smoke tests, so add focused
tests for changed behavior rather than treating existing coverage as sufficient.
From Windows, use the Gradle wrapper:

* `./gradlew.bat testDebugUnitTest` for local unit tests.
* `./gradlew.bat connectedDebugAndroidTest` for instrumentation tests when a
  device or emulator is available.
* `./gradlew.bat lintDebug` for Android lint.
* `./gradlew.bat assembleDebug` for debug build verification.

## Skills

Project-specific skills are located under `.agents/skills/`.

Skills provide specialized workflows and should be used when they materially
help with the task.

If you are unsure which skill or workflow is appropriate, use `ask-matt`.

Read the relevant skill's `SKILL.md` before using that skill.

Prefer the smallest set of skills that adequately addresses the task.

## Agent skills

### Issue tracker

Issues and specs are tracked in this repository's GitHub Issues. See
`docs/agents/issue-tracker.md`.

### Domain docs

This is a single-context repository with domain terminology in root
`CONTEXT.md` and architectural decisions under `docs/adr/`. See
`docs/agents/domain.md`.

## Working process

For substantial changes:

1. Understand the task and relevant existing code.
2. Clarify important ambiguities.
3. Choose an approach consistent with the existing architecture.
4. Implement incrementally.
5. Test the change.
6. Review the final diff.

For large or unfamiliar areas, use an appropriate investigation, planning, or
architecture skill before implementation.

## Completion

Before considering a task complete:

* The requested behavior is implemented.
* The change fits the existing architecture.
* Relevant tests are updated or added where appropriate.
* Relevant verification has been performed.
* The final diff contains only relevant changes.
* No temporary files, debugging code, or secrets were added.

If something could not be verified, state that explicitly.

## Permissions

For requested implementation work, agents may edit files within this repository
and run non-destructive Gradle builds, tests, lint, Git inspection, and Android
emulator verification without asking first. Continue to request confirmation for
destructive operations, external writes not explicitly requested, or changes
outside this repository.
