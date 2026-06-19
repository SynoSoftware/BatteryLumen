# Icon Policy

Open Battery uses Lucide as the only icon set in the app UI.

## Rules

- Use `LucideIcon` for app icons.
- Use Lucide-derived vector drawables only.
- Do not add bitmap UI assets.
- Do not use Compose Material icon packs.
- Do not use `android.R.drawable.*` for app UI or notifications.

## Current asset format

- Android uses vector drawable XML at runtime.
- The Lucide icons in `app/src/main/res/drawable/` are vector drawables derived from Lucide SVG paths.
- This keeps the app vector-only while staying native to Android resources.

## Enforcement

- Gradle fails the build if bitmap files are added under `src/main/res`.
- Gradle fails the build if bitmap drawable XML is added.
- Gradle fails the build if Material icon APIs, `android.R.drawable.*`, or bitmap/image decode APIs appear in app source.
