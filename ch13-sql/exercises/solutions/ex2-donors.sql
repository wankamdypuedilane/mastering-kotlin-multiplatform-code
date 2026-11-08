-- ch13-sql/exercises/solutions/ex2-donors.sql
-- Donors of at least two lots, biggest givers first.
SELECT m.display_name AS donor, COUNT(*) AS lots, SUM(s.grams) AS grams
FROM seed_lot AS s
JOIN member AS m ON m.id = s.donor_member_id
GROUP BY m.id, m.display_name
HAVING COUNT(*) >= 2
ORDER BY grams DESC;
