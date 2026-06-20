# I18n Policy

Battery Lumen keeps user-facing text in the JSON localization catalog at `app/src/main/assets/i18n/en.json`.

## Rules

- Use `T("...")` for user-facing text keys.
- Keep keys short and tree-shaped.
- Treat missing keys as bugs.
- Use `strings.xml` only for `app_name`.
- Prefer placeholders over string concatenation when a message needs values.

## Notes

- The resolver reads `en.json` from `app/src/main/assets/i18n/`.
- The catalog is structured as a tree so related labels stay grouped.
- `T()` returns a `UiText`, and `asString()` resolves it at the UI boundary.
