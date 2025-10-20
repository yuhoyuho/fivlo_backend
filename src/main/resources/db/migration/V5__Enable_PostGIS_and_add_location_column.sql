-- Enable PostGIS extension and convert coordinates to geometry point
CREATE EXTENSION IF NOT EXISTS postgis;

-- Add geometry Point column to forgetting_prevention_reminders
ALTER TABLE forgetting_prevention_reminders
ADD COLUMN location geometry(Point, 4326);

-- Convert existing latitude and longitude values into geometry points
UPDATE forgetting_prevention_reminders
SET location = ST_SetSRID(
    ST_MakePoint(
        location_longitude::double precision,
        location_latitude::double precision
    ), 4326)
WHERE location_latitude IS NOT NULL
  AND location_longitude IS NOT NULL;

-- Create GIST index on the new location column for spatial queries
CREATE INDEX idx_forgetting_prevention_reminders_location_gist
ON forgetting_prevention_reminders USING GIST(location);