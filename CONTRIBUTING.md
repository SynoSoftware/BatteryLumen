# Contributing

Keep changes small and aligned with the SRS and the code.

- User-facing text goes through Android string resources with `T(...)` for resolved text and `UiText`/`TR(...)` for deferred text.
- App icons use Lucide-derived vector drawables only.
- Measured, estimated, inferred, and experimental values must stay separate.
- Update docs when model rules, thresholds, confidence labels, or stored fields change.
- Run `.\gradlew.bat assembleDebug --warning-mode all`.
- Add tests for model, storage, or migration changes.
