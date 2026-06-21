# I18n Policy

Put user-facing text in Android `res/values*/strings.xml` resources and access it through `T(...)` for resolved text.

Use `UiText` with `TR(...)` only when text must flow through state or models before resolution. Keep resource names short, grouped by screen or concept, and use placeholders instead of string concatenation.

Missing resources are bugs.
