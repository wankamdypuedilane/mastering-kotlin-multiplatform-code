-- ch13-sql/queries/08-transactions.sql
-- Lending is two writes: mark the lot, record the loan.
BEGIN;
UPDATE seed_lot SET status = 'ON_LOAN' WHERE id = 3;
INSERT INTO loan (lot_id, member_id, borrowed_on, due_season, grams)
VALUES (3, 42, '2026-11-08', 2027, 25);   -- member 42 does not exist
ROLLBACK;
SELECT id, status FROM seed_lot WHERE id = 3;

BEGIN;
UPDATE seed_lot SET status = 'ON_LOAN' WHERE id = 3;
INSERT INTO loan (lot_id, member_id, borrowed_on, due_season, grams)
VALUES (3, 5, '2026-11-08', 2027, 25);
COMMIT;
SELECT id, status FROM seed_lot WHERE id = 3;
