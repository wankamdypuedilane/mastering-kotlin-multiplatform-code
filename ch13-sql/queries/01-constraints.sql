-- ch13-sql/queries/01-constraints.sql
-- Each INSERT breaks one rule of the schema; SQLite refuses all four.
INSERT INTO seed_lot (variety_id, harvest_year, grams, germination_rate)
VALUES (1, 2026, 'a handful', 90);
INSERT INTO seed_lot (variety_id, harvest_year, grams, germination_rate)
VALUES (1, 2026, 40, 140);
INSERT INTO seed_lot (variety_id, harvest_year, grams, germination_rate)
VALUES (77, 2026, 40, 90);
INSERT INTO member (display_name, email)
VALUES ('Ada Twin', 'ada@larkmoor.example');
-- A STRICT INTEGER column converts '40' losslessly; DEFAULT fills status.
INSERT INTO seed_lot (variety_id, harvest_year, grams, germination_rate)
VALUES (1, 2026, '40', 90);
SELECT id, grams, typeof(grams) AS type, status
FROM seed_lot WHERE harvest_year = 2026;
