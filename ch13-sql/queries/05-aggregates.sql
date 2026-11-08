-- ch13-sql/queries/05-aggregates.sql
-- Stock per plant family (retired lots excluded).
SELECT v.family, COUNT(*) AS lots, SUM(s.grams) AS grams,
       MAX(s.germination_rate) AS best
FROM seed_lot AS s
JOIN variety AS v ON v.id = s.variety_id
WHERE s.status <> 'RETIRED'
GROUP BY v.family
ORDER BY grams DESC;

-- Members who borrowed at least 10 g in total.
SELECT m.display_name, COUNT(*) AS loans, SUM(l.grams) AS grams
FROM loan AS l
JOIN member AS m ON m.id = l.member_id
GROUP BY m.id, m.display_name
HAVING SUM(l.grams) >= 10
ORDER BY grams DESC;
