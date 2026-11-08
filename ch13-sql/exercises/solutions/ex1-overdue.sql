-- ch13-sql/exercises/solutions/ex1-overdue.sql
-- Loans from past seasons that never came back.
SELECT l.id AS loan, m.display_name AS member,
       v.common_name AS variety, l.due_season AS due
FROM loan AS l
JOIN member   AS m ON m.id = l.member_id
JOIN seed_lot AS s ON s.id = l.lot_id
JOIN variety  AS v ON v.id = s.variety_id
LEFT JOIN harvest_return AS r ON r.loan_id = l.id
WHERE r.id IS NULL AND l.due_season < 2026
ORDER BY l.id;
