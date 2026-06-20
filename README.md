# Battery Lumen

Battery Lumen stores battery readings and charging sessions on device.

The app has four screens: Now, Health, Ledger, and Info.

- Now shows stress, action, time to target, and confidence.
- Health shows an estimated capacity range after 5 useful sessions.
- Ledger stores session history with temperature and charge exposure.
- Info explains the evidence labels and model thresholds.
- Values are labeled measured, estimated, inferred, or experimental.
- No account, cloud sync, ads, telemetry, or root.

Built with Kotlin, Jetpack Compose, Room, DataStore, WorkManager, and Material 3.

Build:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug --warning-mode all
```

Docs:

- [Product requirements](specs/battery%20srs.md)
- [UI requirements](specs/ui-ux%20specs.md)
- [Model notes](docs/model-documentation.md)
- [Stored data](docs/data-schema.md)
- [Backlog](docs/backlog/README.md)
