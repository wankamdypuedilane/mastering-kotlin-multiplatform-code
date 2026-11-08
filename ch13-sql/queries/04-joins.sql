-- ch13-sql/queries/04-joins.sql
-- INNER JOIN: this season's loans with member and variety names.
SELECT l.id, m.display_name AS member, v.common_name AS variety,
       l.grams
FROM loan AS l
JOIN member   AS m ON m.id = l.member_id
JOIN seed_lot AS s ON s.id = l.lot_id
JOIN variety  AS v ON v.id = s.variety_id
WHERE l.due_season = 2026
ORDER BY l.id;

-- LEFT JOIN: last season's loans, with their harvest return if any.
SELECT l.id AS loan, l.grams AS lent, r.grams AS returned
FROM loan AS l
LEFT JOIN harvest_return AS r ON r.loan_id = l.id
WHERE l.due_season = 2025
ORDER BY l.id;
