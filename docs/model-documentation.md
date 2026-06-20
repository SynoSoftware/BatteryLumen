# Model Documentation

Battery Lumen uses a state-time stress model.

Stress thresholds:

- Under 35 C: good
- 35 to 39.9 C: normal
- 40 to 44.9 C: high stress
- 45 C and above: severe stress
- While charging, 43 C and above escalates stress one step
- Under 80 percent while charging: good
- 80 to 89 percent while charging: normal
- 90 to 95 percent while charging: high stress
- 95 percent and above while charging: high stress

Useful session gate:

- Gain at least 30 percent
- Duration at least 10 minutes
- At least 2 samples
- Not wireless
- Max temperature below 45 C

Confidence is high at 5 signals or more, medium at 3 or 4, and low below that.

The engine reads battery level, charging state, plug type, temperature, and voltage/current/charge counter when available.
It outputs charging stress, thermal stress, charge-level stress, recommended action, session quality, capacity estimate, and health display when design capacity is known.

Actions are `continue charging`, `avoid charging to full`, `unplug now`, `unplug if you do not need a full charge`, `cool the device or unplug`, and `start charging to see guidance`.

Measured means device reading. Estimated means derived from measured values. Inferred means model judgment. Experimental stays out of primary advice.
