-- ch13-sql/queries/09-migration.sql
-- Migration 1 -> 2: lots get the label of the storage box they sit in.
PRAGMA user_version;
BEGIN;
ALTER TABLE seed_lot ADD COLUMN storage_box TEXT;
UPDATE seed_lot SET storage_box = 'B-' || harvest_year
WHERE status <> 'RETIRED';
CREATE INDEX seed_lot_variety_idx ON seed_lot (variety_id);
PRAGMA user_version = 2;
COMMIT;
PRAGMA user_version;
SELECT id, status, storage_box FROM seed_lot WHERE variety_id = 4;
