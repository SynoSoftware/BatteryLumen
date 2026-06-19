# AGENTS.md

## North Star

Write code like humans do: readable, easy to write, and easy to understand.

When tradeoffs are unclear, choose the option that makes the next person faster.

## Naming

Names are part of the design.

- Use the shortest clear name.
- Prefer domain language over implementation language.
- Do not leak storage, transport, UI mechanics, or control flow into names.
- Boolean names should read as facts.
- Async methods must end with `Async`.
- If a touched name violates these rules, fix it in the same edit.
- Names longer than 3 words are a warning sign.
- Names longer than 4 words are not allowed unless no shorter clear name exists.

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
- Avoid partial mutations of shared objects; prefer replacing with a complete state.

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

## Layer Boundaries

- Views render and route intent; they do not decide workflow rules.
- ViewModels orchestrate UI behavior; they do not recreate service policy.
- Services define workflow and integration rules; they do not depend on UI concerns.
- Mapping, formatting, validation, and filtering must each live in one predictable place.

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
- A fix that requires copying logic from another area.
- A change that cannot be explained in 2 to 3 sentences.

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
