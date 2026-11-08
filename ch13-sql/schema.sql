-- ch13-sql/schema.sql
-- Larkmoor Commons Seed Library, schema version 1.
PRAGMA foreign_keys = ON;

CREATE TABLE member (
  id           INTEGER PRIMARY KEY,
  display_name TEXT    NOT NULL,
  email        TEXT    NOT NULL UNIQUE,
  joined_on    TEXT    NOT NULL DEFAULT (date('now'))
) STRICT;

CREATE TABLE variety (
  id               INTEGER PRIMARY KEY,
  common_name      TEXT    NOT NULL UNIQUE,
  latin_name       TEXT    NOT NULL,
  family           TEXT    NOT NULL,
  days_to_maturity INTEGER NOT NULL CHECK (days_to_maturity > 0),
  difficulty       TEXT    NOT NULL DEFAULT 'EASY'
                   CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD'))
) STRICT;

CREATE TABLE seed_lot (
  id               INTEGER PRIMARY KEY,
  variety_id       INTEGER NOT NULL REFERENCES variety (id),
  harvest_year     INTEGER NOT NULL,
  grams            INTEGER NOT NULL CHECK (grams >= 0),
  germination_rate INTEGER NOT NULL
                   CHECK (germination_rate BETWEEN 0 AND 100),
  donor_member_id  INTEGER REFERENCES member (id),
  status           TEXT    NOT NULL DEFAULT 'AVAILABLE'
                   CHECK (status IN ('AVAILABLE', 'ON_LOAN', 'RETIRED'))
) STRICT;

CREATE TABLE loan (
  id          INTEGER PRIMARY KEY,
  lot_id      INTEGER NOT NULL REFERENCES seed_lot (id),
  member_id   INTEGER NOT NULL REFERENCES member (id),
  borrowed_on TEXT    NOT NULL,
  due_season  INTEGER NOT NULL,
  grams       INTEGER NOT NULL CHECK (grams > 0)
) STRICT;

CREATE TABLE harvest_return (
  id               INTEGER PRIMARY KEY,
  loan_id          INTEGER NOT NULL UNIQUE
                   REFERENCES loan (id) ON DELETE CASCADE,
  returned_on      TEXT    NOT NULL,
  grams            INTEGER NOT NULL CHECK (grams > 0),
  germination_rate INTEGER CHECK (germination_rate BETWEEN 0 AND 100)
) STRICT;

PRAGMA user_version = 1;
