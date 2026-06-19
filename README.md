# Open Battery

Open Battery is an Android charging decision assistant focused on evidence quality, local-first storage, and honest battery guidance.

## Current scope

- Native Android only
- Kotlin + Compose + Room + DataStore + WorkManager + Hilt
- Charge-session ledger from day one
- Now screen first, Health as an empty state until enough useful data exists

## Product stance

- Measured, estimated, inferred, and experimental values are labeled separately
- No backend
- No telemetry by default
- No fake precision
- Lucide is the only icon set
- UI assets stay vector-only

## Asset policy

- App icons use Lucide-derived vector drawables
- Bitmap UI assets are not allowed
- See [`docs/icon-policy.md`](docs/icon-policy.md) for the enforcement rules

## Repo layout

- `app/` Android application
- `docs/backlog/` tracked future work
- `battery srs.md` and `ui-ux specs.md` source requirements
