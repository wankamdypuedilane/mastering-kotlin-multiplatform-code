-- ch13-sql/queries/02-select.sql
-- Quick crops: everything that matures in under 70 days.
SELECT common_name, family, days_to_maturity AS days
FROM variety
WHERE days_to_maturity < 70
ORDER BY days_to_maturity;

-- The catalog in pages of four, sorted by name: page 2.
SELECT id, common_name, difficulty
FROM variety
ORDER BY common_name
LIMIT 4 OFFSET 4;
