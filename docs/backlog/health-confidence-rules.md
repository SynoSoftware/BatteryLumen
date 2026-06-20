# Health Confidence Rules

Grade confidence from the same signals the engine already uses: temperature present, level present, charging-like state, reliable current, a session with at least 2 samples, and a useful session.

Low means too few signals. Medium means 3 or 4 signals. High means 5 or more.

- Tiny sample counts stay low.
- Conflicting sessions reduce confidence.
- Stable repeated sessions increase confidence.
