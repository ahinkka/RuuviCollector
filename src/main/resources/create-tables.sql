CREATE TABLE IF NOT EXISTS measurement (
  recorded_at INTEGER NOT NULL,
  sensor TEXT NOT NULL,
  temperature REAL,
  pressure REAL,
  acceleration_x REAL,
  acceleration_y REAL,
  acceleration_z REAL,
  battery_voltage REAL,
  tx_power REAL,
  movement_counter INTEGER,
  measurement_sequence_number INTEGER
);

CREATE INDEX IF NOT EXISTS recorded_at_idx ON measurement (recorded_at);
CREATE INDEX IF NOT EXISTS sensor_idx ON measurement (sensor);
