# AGENTS.md

## North Star

Write code like humans do: readable, easy to write, and easy to understand.

When tradeoffs are unclear, choose the option that makes the next person faster.

## Naming

Names are part of the design. They should say what something is, not how it works.

- Use the shortest clear name.
- Prefer domain language over implementation language.
- Prefer domain nouns over architectural nouns unless the architectural term is the domain term.
- Do not repeat context already provided by the type, file, namespace, or property path.
- Do not leak storage, transport, UI mechanics, or control flow into names.
- Do not use generic suffixes like `Helper`, `Manager`, or `Util` unless they are the clearest domain name.
- Boolean names should read as facts.
- Async methods must end with `Async`.
- If a touched name violates these rules, fix it in the same edit.
- Names longer than 3 words are a warning sign.
- Names longer than 4 words are not allowed unless no shorter clear name exists.

### Naming Pass

- Shorten any name that can be shortened without losing purpose.
- Rename anything that says how it works instead of what it means.
- Prefer one concept as one word where possible.
- Remove redundant prefixes and suffixes already implied by context.
- Keep async method names purpose-based and end them with `Async`.
- Run a naming pass over every touched name before finishing.

## Core Principles

- Correctness first. UI behavior must always reflect real state.
- Clarity over cleverness. If it is hard to read, it is wrong.
- Minimal, intentional changes. One clear purpose per edit.
- Explicit over implicit. Ownership, lifetimes, and data flow must be obvious.
- No blind trust. Existing code may be wrong, so verify it.

## Working Method

Before changing code:

- Identify the source of truth.
- Map entry points.
- Map outputs.
- Consider 2 to 3 approaches and pick the simplest valid one.
- Read surrounding code and reuse existing patterns.

After changing code:

- Remove dead code and unused imports.
- Validate the actual workflow, not just compilation.
- Keep changes narrow and reviewable.
- Re-check against the original task.
- Run a naming pass over every touched name.

## Change Safety

- Do not change multiple responsibilities in one edit.
- If a refactor is required, separate it from behavior changes.
- Prefer the smallest code path that proves the fix.
- Prefer the most direct implementation that works; do not add wrappers, coordinators, adapters, or mappers unless they remove duplication or make ownership clearer.
- Do not extract a helper or abstraction unless it removes duplication, reduces coupling, or marks a real boundary.
- If unsure about impact, isolate the change behind a small, testable boundary.
- Do not restore old behavior unless the task explicitly requires it.
- Prefer adding a narrow adapter over spreading special cases across call sites.

## Rethink Conditions

Stop and reconsider if you encounter:

- The same data stored in more than one place.
- The same rule implemented in multiple layers.
- UI behavior depending on timing or side effects.
- Multiple layers making the same decision.
- A command that updates one source of truth while pretending to update another.
- A fix that requires copying logic from another area.
- A change that cannot be explained in 2 to 3 sentences.

## State Management

- Owns source of truth, mutation, and derived state.
- There must be one authoritative source of truth for each piece of state.
- Derived UI state must be computed, not stored redundantly.
- Do not mirror the same state across View, ViewModel, and Service.
- If two layers are applying the same rule, the design is wrong until one owner is removed.
- Do not create a second write path just to refresh derived state.
- If a setting changes, update the setting; do not replay unrelated source events to force recomputation.
- All state transitions must be explicit and traceable in code.
- Avoid event cascades where multiple handlers mutate the same state indirectly.
- If state can change concurrently, define ordering or cancellation behavior explicitly.

## Ownership and Authority

- Owns where rules and decisions live.
- Each behavior, rule, and state transition must have one authoritative owner.
- Do not re-implement the same workflow in another layer for convenience.
- If a ViewModel needs logic owned by a service, move or expose that logic from the service instead of duplicating it.
- During migration, do not keep parallel implementations unless one is explicitly temporary and the removal path is clear.
- A layer may adapt data for its purpose, but it must not redefine the underlying rule.
- A layer must not reinterpret or override decisions made by its upstream owner.
- If a command changes preferences, it should not also persist the same domain event unless that event is the actual intent.

## Commands and Events

- Owns user intent entry points.
- UI events should delegate to one command or method with a clear name.
- Do not split one logical action across multiple handlers.
- Commands must represent user intent, not UI mechanics.
- Avoid side effects inside property setters.
- If a command triggers multiple operations, sequence them explicitly in one place.

## Data Flow

- Owns how data moves and gets translated between layers.
- Prefer Service -> ViewModel -> View.
- Avoid back-and-forth flow between layers.
- Do not pass UI types into services.
- Transform data once at boundaries, not repeatedly.
- Mapping, formatting, validation, and filtering must each live in one predictable place.
- Keep one canonical mapper per entity or domain pair.
- Do not duplicate an extension or helper in a second file if the first one already owns the transformation.
- Prefer replacing a complete state instead of mutating shared objects in place.

## API Boundaries

- Owns transport-to-app translation at the network edge.
- Keep network models at the network boundary.
- Do not let raw API response shapes leak into ViewModels, UI state, or domain code.
- Normalize API inconsistencies in one place, not per call site.
- Normalize boundary data once; do not repeat that cleanup in ViewModels, UI code, or other call sites.
- Services should expose app-friendly results, not transport details.
- Make retry, timeout, and cancellation behavior explicit where network calls are owned.
- Map external data into domain or UI state once at the boundary.

## External Data

- Owns validation of untrusted inputs before they become app state.
- Treat server responses, local storage records, intents, files, notifications, permissions, and system APIs as external data.
- Convert external data into app-owned models before it reaches UI workflow code.
- Keep parsing, validation, and fallback behavior near the boundary that receives the data.
- Do not let persistence, network, or Android framework shapes become the app’s source of truth unless they are explicitly the domain model.
- Validate external data before using it to update state, start work, navigate, persist, or render UI.

## Layer Boundaries

- Owns what each layer may decide and what it may only adapt.
- Views render and route intent; they do not decide workflow rules.
- ViewModels orchestrate UI behavior; they do not recreate service policy.
- Services define workflow and integration rules; they do not depend on UI concerns.
- Models and UI state are data carriers, not places for workflow logic.
- Keep shared rules in one place so breakpoints can follow the flow in a single pass.

## Debuggability

- Key workflows must be traceable with breakpoints in a single pass.
- Avoid indirection that hides call paths.
- Prefer explicit method calls over implicit side effects.
- Important state transitions should have a single obvious location.

## Navigation and Lifetime

- Make navigation state explicit.
- Avoid workflow logic in constructors or composable setup code.
- Understand lifetimes of Application, Activity, ViewModel, composables, workers, and services.
- Do not keep stale `Context`, `Activity`, `View`, or navigation references.
- Cancel work when the owning lifecycle ends.
- Use lifecycle-aware collection for flows observed by UI.
- Do not start background work from UI code unless the ownership and cancellation path are clear.

## Compose UI

- Composables render state and route intent; they do not own workflow rules.
- Keep expensive work out of composables.
- Avoid unnecessary recomposition.
- Prefer stable UI state shapes where it improves Compose behavior without adding complexity.
- Keep UI state small, explicit, and screen-focused.
- Do not pass mutable domain objects directly into composables when a stable UI model is clearer.
- Side effects must be intentional, keyed correctly, and easy to trace.

## Theming and Accessibility

- No hard-coded colors.
- Use Material theme values and design tokens.
- Ensure light and dark modes work without code changes.
- Keep UI responsive.
- Add meaningful content descriptions where needed.
- Support keyboard and screen reader behavior where applicable.
- Do not rely on color alone for state.

## Performance

- Minimize unnecessary recomposition.
- Keep expensive work out of UI paths.
- Avoid unnecessary allocations in hot paths.
- Use paging or lazy layouts for large datasets.
- Do not introduce caching unless ownership, invalidation, and lifetime are clear.

## Collections and Lists

- Use observable or mutable state collections only where UI change tracking needs them.
- Replace entire collections when the source changes significantly.
- Avoid mutating collections during enumeration.
- Batch updates when possible to avoid excessive recomposition or refresh.
- Large datasets must use paging, lazy lists, or virtualization patterns already in the app.

## Error Handling

- Every failure path must result in user-visible feedback or a clearly propagated exception.
- Do not swallow exceptions without a reason documented in code.
- Do not mix success and error states in the same return shape without clear signaling.
- Handle external/API failures at the boundary, not deep inside UI code.

## Security and Privacy

- Treat all inputs as untrusted.
- Validate file paths, URIs, external data, and incoming intents.
- No secrets in source, logs, screenshots, generated artifacts, or test fixtures.
- No sensitive user data in logs by default.
- Keep trust-preserving behavior explicit, especially around local data, sync, export, backups, and notifications.
- Prefer least privilege for permissions.
- Ask for permissions only when the feature actually needs them.

## Practical Defaults

- Prefer simple classes over frameworks.
- Prefer explicit naming over generic terms.
- Prefer fewer moving parts.
- Prefer debuggable solutions over clever ones.

If multiple solutions work, choose the one that feels most natural to read and modify.

## Android Architecture

- Use native Android only.
- Prefer Kotlin, Compose, Material 3, Room, DataStore, and WorkManager.
- Keep the first module simple unless coupling proves otherwise.
- Keep model logic pure Kotlin where possible so it can be tested without Android.
- Keep the app local-first and trust-preserving.

## Localization

- No hard-coded user-facing strings.
- Use Android `res/values*/strings.xml` as the single source of truth for app text.
- Keep resource names semantic, short, readable, and grouped by concept, for example `daily_summary_good`, `decision_reason_hot_charging`, `settings_theme_system`.
- Prefer `T(daily_summary_good)` for normal Compose string usage; `T(...)` should return a resolved `String` in composable scope.
- Use `Context.T(...)` for Android APIs outside Compose, such as notifications, services, or system labels.
- Use `@string/...` in XML when XML is the call site.
- Use direct `stringResource(...)` / `getString(...)` only when it is clearer than the wrapper.
- Use `UiText` with `TR(...)` only when text must pass through state/models before resolution.
- Do not use `T(...).asString()` at normal UI call sites.
- Do not push `Context` into domain code.
- Treat missing resources as bugs.
- Prefer placeholders over string concatenation when a message needs values.
- Use positional placeholders when argument order matters.
- Use `<plurals>` for real singular/plural variants.
- Do not reintroduce JSON localization assets, loaders, parsers, fallback catalogs, string-key lookup, or runtime localization helpers.

## Icons and Assets

- Use Lucide as the only icon set.
- Use `LucideIcon` for app icons.
- Use Lucide-derived vector drawables only.
- Do not add bitmap UI assets.
- Do not use Compose Material icon packs.
- Do not use `android.R.drawable.*` for app UI or notifications.

## Async and Threading

- No blocking work on the UI thread.
- Use cancellation tokens or coroutine cancellation where appropriate.
- Keep background work off the UI thread.
- Use explicit lifecycle-aware collection where the UI observes flows.

## Editing Standards

- Make cohesive, minimal changes.
- Do not expand scope unnecessarily.
- Avoid introducing new dependencies without strong justification.
- No magic numbers. Centralize constants when reused or business-relevant.
- Keep error handling explicit and meaningful.

## Build and Verification

- Maintain build stability.
- Validate locally with the project build.
- Use default Gradle output locations.
- Do not write generated output into alternate folders.
- If blocked, clearly state the reason.
- Focus testing on changed workflows.

## Bug Fix Testing

Do not add or modify automated tests by default when fixing bugs.

First fix the smallest code path that explains the bug, then verify the changed workflow with the existing build and focused manual/runtime checks.

Only add or change a test when it materially helps prove the fix and at least one of these is true:

- The first fix attempt fails or the bug is difficult to verify manually.
- The bug is a regression in shared business logic.
- The bug has already happened before.
- There is an existing nearby test harness that can cover the behavior with a small, focused test.
- The user explicitly asks for a test.

If a test is added, it must be narrow, use existing test patterns, and must not require new infrastructure, broad rewrites, or repeated test-harness debugging.

Do not spend more time repairing the test than fixing the bug. If the test becomes noisy, brittle, or requires broad mocking changes, stop and verify manually instead.

When no test is added, state the manual verification performed.

### Local Tooling

- Java is installed at `C:\Program Files\Android\Android Studio\jbr`.
- Use that JDK for Gradle and Android builds.
- Everything is installed at `C:\Program Files\Everything\`.
- `es.exe` is available there and should be used for fast file searches when present.

## Generated Artifacts

- Do not read or modify `bin/`, `obj/`, or other generated files.
- Debug using source only.

## Absolute Rule

Never run destructive git commands without explicit confirmation:

- `git restore`
- `git reset`
- `git clean`
- `git checkout --`

Preserve all local changes.
