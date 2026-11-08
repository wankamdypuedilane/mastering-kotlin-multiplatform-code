-- ch13-sql/exercises/solutions/ex3-yield.sql
-- Grams returned per gram lent, by variety, for season 2025.
WITH returned AS (
  SELECT s.variety_id, l.grams AS lent, r.grams AS back
  FROM loan AS l
  JOIN harvest_return AS r ON r.loan_id = l.id
  JOIN seed_lot AS s ON s.id = l.lot_id
  WHERE l.due_season = 2025
)
SELECT v.common_name, SUM(lent) AS lent, SUM(back) AS back,
       ROUND(1.0 * SUM(back) / SUM(lent), 1) AS yield
FROM returned
JOIN variety AS v ON v.id = returned.variety_id
GROUP BY v.id, v.common_name
ORDER BY yield DESC;
