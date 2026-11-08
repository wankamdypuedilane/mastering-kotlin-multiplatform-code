-- ch13-sql/seed.sql
-- Fictional sample data for the Larkmoor Commons Seed Library.
INSERT INTO member (id, display_name, email, joined_on) VALUES
  (1, 'Ada Fenwick', 'ada@larkmoor.example',   '2023-03-04'),
  (2, 'Tomas Reyes', 'tomas@larkmoor.example', '2023-05-19'),
  (3, 'Priya Lal',   'priya@larkmoor.example', '2024-02-11'),
  (4, 'Owen Marsh',  'owen@larkmoor.example',  '2024-09-30'),
  (5, 'Hana Ito',    'hana@larkmoor.example',  '2025-04-02'),
  (6, 'Bea Quill',   'bea@larkmoor.example',   '2026-01-15');

INSERT INTO variety
  (id, common_name, latin_name, family, days_to_maturity, difficulty)
VALUES
  (1,  'Scarlet Runner',     'Phaseolus coccineus', 'Fabaceae',      90,
       'EASY'),
  (2,  'Moorland Sugar Pea', 'Pisum sativum',       'Fabaceae',      65,
       'EASY'),
  (3,  'Gold Drop',          'Solanum lycopersicum', 'Solanaceae',   75,
       'MEDIUM'),
  (4,  'Ember Pepper',       'Capsicum annuum',     'Solanaceae',    85,
       'HARD'),
  (5,  'Winter Kale',        'Brassica oleracea',   'Brassicaceae',  60,
       'EASY'),
  (6,  'Brindle Lettuce',    'Lactuca sativa',      'Asteraceae',    50,
       'EASY'),
  (7,  'Lantern Squash',     'Cucurbita pepo',      'Cucurbitaceae', 95,
       'MEDIUM'),
  (8,  'Fenland Carrot',     'Daucus carota',       'Apiaceae',      75,
       'MEDIUM'),
  (9,  'Ruby Chard',         'Beta vulgaris',       'Amaranthaceae', 55,
       'EASY'),
  (10, 'Heron Melon',        'Cucumis melo',        'Cucurbitaceae', 100,
       'HARD');

INSERT INTO seed_lot
  (id, variety_id, harvest_year, grams, germination_rate, donor_member_id,
   status)
VALUES
  (1,  1, 2024, 120, 92, 1,    'AVAILABLE'),
  (2,  1, 2025,  80, 88, 2,    'ON_LOAN'),
  (3,  2, 2025, 150, 95, 1,    'AVAILABLE'),
  (4,  3, 2024,   6, 81, 3,    'ON_LOAN'),
  (5,  3, 2025,   9, 90, NULL, 'AVAILABLE'),
  (6,  4, 2023,   4, 62, 4,    'RETIRED'),
  (7,  4, 2025,   5, 78, 3,    'AVAILABLE'),
  (8,  5, 2025,  12, 85, 2,    'ON_LOAN'),
  (9,  6, 2025,  10, 93, NULL, 'AVAILABLE'),
  (10, 7, 2024,  60, 74, 5,    'AVAILABLE'),
  (11, 8, 2024,  14, 69, 1,    'RETIRED'),
  (12, 8, 2025,  18, 84, 4,    'ON_LOAN'),
  (13, 9, 2025,  22, 88, NULL, 'AVAILABLE');

INSERT INTO loan (id, lot_id, member_id, borrowed_on, due_season, grams)
VALUES
  (1,  1,  2, '2025-03-10', 2025, 20),
  (2,  3,  3, '2025-03-22', 2025, 30),
  (3,  4,  1, '2025-04-05', 2025,  2),
  (4,  9,  4, '2025-04-12', 2025,  2),
  (5,  10, 2, '2025-05-01', 2025, 10),
  (6,  13, 5, '2025-05-03', 2025,  4),
  (7,  7,  3, '2025-02-20', 2025,  1),
  (8,  2,  1, '2026-03-08', 2026, 15),
  (9,  4,  4, '2026-03-15', 2026,  2),
  (10, 8,  5, '2026-03-30', 2026,  3),
  (11, 12, 3, '2026-04-02', 2026,  4);

INSERT INTO harvest_return (id, loan_id, returned_on, grams, germination_rate)
VALUES
  (1, 1, '2025-10-02', 200, 90),
  (2, 2, '2025-09-14', 340, 94),
  (3, 3, '2025-09-28',   7, NULL),
  (4, 5, '2025-10-20',  85, 71),
  (5, 6, '2025-09-05',  30, 86);
