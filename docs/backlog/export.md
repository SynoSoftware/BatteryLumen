# Export

Export sessions as CSV and JSON with evidence and confidence labels intact.

CSV should carry session rows. JSON should carry session rows, schema version, model version, and settings like `target_charge_percent` and `design_capacity_mah`.

- CSV has stable column names.
- JSON includes schema, model version, and settings like target charge and design capacity.
- Export works offline.
