# Device Compatibility

Battery Lumen relies on Android battery broadcasts and BatteryManager properties.

## Best experience

- A device that exposes temperature
- A device that exposes current or charge counter values
- The app being opened while charging

## Fallbacks

- Unsupported readings are labeled unavailable
- Health remains empty until enough useful sessions exist
