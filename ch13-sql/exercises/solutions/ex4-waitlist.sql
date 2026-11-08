-- ch13-sql/exercises/solutions/ex4-waitlist.sql
-- Migration 1 -> 3 in one transaction: version 2 exactly as in
-- 09-migration.sql, then version 3. The database ends at 3 or stays at 1.
BEGIN;
ALTER TABLE seed_lot ADD COLUMN storage_box TEXT;          -- 1 -> 2
UPDATE seed_lot SET storage_box = 'B-' || harvest_year
WHERE status <> 'RETIRED';
CREATE INDEX seed_lot_variety_idx ON seed_lot (variety_id);
PRAGMA user_version = 2;
CREATE TABLE waitlist (                                    -- 2 -> 3
  member_id    INTEGER NOT NULL REFERENCES member (id),
  variety_id   INTEGER NOT NULL REFERENCES variety (id),
  requested_on TEXT    NOT NULL,
  PRIMARY KEY (member_id, variety_id)
) STRICT;
CREATE INDEX waitlist_variety_idx ON waitlist (variety_id, requested_on);
PRAGMA user_version = 3;
COMMIT;

-- A second request for the same variety keeps the first date.
INSERT INTO waitlist VALUES
  (4, 4, '2026-10-02'), (5, 4, '2026-09-21'), (1, 10, '2026-10-11'),
  (2, 4, '2026-10-30'), (5, 4, '2026-11-05')
ON CONFLICT (member_id, variety_id) DO NOTHING;

-- Varieties with more requests than available lots.
SELECT v.common_name, COUNT(*) AS waiting,
       (SELECT COUNT(*) FROM seed_lot AS s
        WHERE s.variety_id = v.id AND s.status = 'AVAILABLE') AS available
FROM waitlist AS w
JOIN variety AS v ON v.id = w.variety_id
GROUP BY v.id, v.common_name
HAVING waiting > available
ORDER BY waiting DESC;

PRAGMA user_version;
