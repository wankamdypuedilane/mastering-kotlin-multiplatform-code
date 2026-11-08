-- ch13-sql/queries/06-subqueries.sql
-- Correlated subquery: varieties the library has no seed for.
SELECT common_name
FROM variety AS v
WHERE NOT EXISTS (SELECT 1 FROM seed_lot AS s WHERE s.variety_id = v.id);

-- CTE: what each member borrowed and brought back for season 2025.
WITH season AS (
  SELECT l.member_id, l.grams AS lent, r.grams AS returned
  FROM loan AS l
  LEFT JOIN harvest_return AS r ON r.loan_id = l.id
  WHERE l.due_season = 2025
)
SELECT m.display_name, SUM(lent) AS lent,
       COALESCE(SUM(returned), 0) AS back,
       COUNT(*) - COUNT(returned) AS missing
FROM season
JOIN member AS m ON m.id = season.member_id
GROUP BY m.id, m.display_name
ORDER BY m.display_name;
