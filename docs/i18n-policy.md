# I18n Policy

Put user-facing text in `app/src/main/assets/i18n/en.json` and access it through `T("...")`.

Use `strings.xml` only for `app_name`. Keep keys short, grouped by screen or concept, and use placeholders instead of string concatenation.

Missing keys are bugs.
