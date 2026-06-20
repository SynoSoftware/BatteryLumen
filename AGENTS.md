# AGENTS.md

## North Star

Write code like humans do: readable, easy to write, and easy to understand.

When tradeoffs are unclear, choose the option that makes the next person faster.

## Naming

Names are part of the design. They should say what something is, not how it works.

- Use the shortest clear name.
- Prefer domain language over implementation language.
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

## State Management

- There must be one authoritative source of truth for each piece of state.
- Derived UI state must be computed, not stored redundantly.
- Do not mirror the same state across View, ViewModel, and Service.
- If two layers are applying the same rule, the design is wrong until one owner is removed.
- Do not create a second write path just to refresh derived state.
- If a setting changes, update the setting; do not replay unrelated source events to force recomputation.
- All state transitions must be explicit and traceable in code.
- Avoid event cascades where multiple handlers mutate the same state indirectly.
- If state can change concurrently, define ordering or cancellation behavior explicitly.

## Commands and Events

- UI events should delegate to one command or method with a clear name.
- Do not split one logical action across multiple handlers.
- Commands must represent user intent, not UI mechanics.
- Avoid side effects inside property setters.
- If a command triggers multiple operations, sequence them explicitly in one place.

## Data Flow

- Prefer Service -> ViewModel -> View.
- Avoid back-and-forth flow between layers.
- Do not pass UI types into services.
- Transform data once at boundaries, not repeatedly.
- Mapping, formatting, validation, and filtering must each live in one predictable place.
- Keep one canonical mapper per entity or domain pair.
- Do not duplicate an extension or helper in a second file if the first one already owns the transformation.
- Prefer replacing a complete state instead of mutating shared objects in place.

## Error Handling

- Every failure path must result in user-visible feedback or a clearly propagated exception.
- Do not swallow exceptions without a reason documented in code.
- Do not mix success and error states in the same return shape without clear signaling.
- Handle external/API failures at the boundary, not deep inside UI code.

## Ownership and Authority

- Each behavior, rule, and state transition must have one authoritative owner.
- Do not re-implement the same workflow in another layer for convenience.
- If a ViewModel needs logic owned by a service, move or expose that logic from the service instead of duplicating it.
- During migration, do not keep parallel implementations unless one is explicitly temporary and the removal path is clear.
- A layer may adapt data for its purpose, but it must not redefine the underlying rule.
- A layer must not reinterpret or override decisions made by its upstream owner.
- If a command changes preferences, it should not also persist the same domain event unless that event is the actual intent.

## Layer Boundaries

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

## Rethink Conditions

Stop and reconsider if you encounter:

- The same data stored in more than one place.
- The same rule implemented in multiple layers.
- UI behavior depending on timing or side effects.
- Multiple layers making the same decision.
- A command that updates one source of truth while pretending to update another.
- A fix that requires copying logic from another area.
- A change that cannot be explained in 2 to 3 sentences.

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
- Use `T("...")` for user-facing text keys.
- Keep keys short and tree-shaped.
- Treat missing keys as bugs.
- Use `app/src/main/assets/i18n/en.json` as the localization catalog.
- Use `strings.xml` only for `app_name`.
- Prefer placeholders over string concatenation when a message needs values.

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
