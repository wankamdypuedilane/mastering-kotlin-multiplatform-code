-- ch13-sql/queries/07-indexes.sql
-- The loans screen of one member, newest first.
EXPLAIN QUERY PLAN
SELECT id, borrowed_on FROM loan
WHERE member_id = 3 ORDER BY borrowed_on DESC;

CREATE INDEX loan_member_idx ON loan (member_id, borrowed_on);

EXPLAIN QUERY PLAN
SELECT id, borrowed_on FROM loan
WHERE member_id = 3 ORDER BY borrowed_on DESC;
