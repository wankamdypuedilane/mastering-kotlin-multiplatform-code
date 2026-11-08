-- ch13-sql/queries/03-dml.sql
-- A new member. INTEGER PRIMARY KEY picks the next id.
INSERT INTO member (display_name, email, joined_on)
VALUES ('Iris Penhale', 'iris@larkmoor.example', '2026-11-01')
RETURNING id, display_name;

-- Lots harvested before 2025 and still on the shelf are retired.
UPDATE seed_lot SET status = 'RETIRED'
WHERE harvest_year < 2025 AND status = 'AVAILABLE';
SELECT changes() AS retired;

-- Bea leaves the library (she has no loans and donated nothing).
DELETE FROM member WHERE email = 'bea@larkmoor.example';

-- UPSERT: insert, or update the existing row with the same email.
INSERT INTO member (display_name, email, joined_on)
VALUES ('Tomas Reyes-Vidal', 'tomas@larkmoor.example', '2026-11-02')
ON CONFLICT (email) DO UPDATE SET display_name = excluded.display_name;

SELECT id, display_name, joined_on FROM member ORDER BY id;
