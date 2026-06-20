# Device Compatibility

Battery Lumen reads Android battery broadcasts and `BatteryManager` properties.

Battery level, charging state, plug type, and timestamps are available on most devices. Temperature, voltage, current, and charge counter depend on the device.

If a device hides current or charge counter, the app still works with level and temperature. Health waits for 5 useful sessions.
