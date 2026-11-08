#!/usr/bin/env bash
# ch13-sql/check.sh
# Builds a fresh database for every script and compares its output
# with expected/<name>.txt.  ./check.sh --update rewrites the files.
set -euo pipefail
cd "$(dirname "$0")"
SQLITE="${SQLITE3:-sqlite3}"
echo "sqlite3 $("$SQLITE" --version | cut -d' ' -f1)"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
failed=0
for script in queries/*.sql exercises/solutions/*.sql; do
  name=$(basename "$script" .sql)
  db="$work/$name.db"
  "$SQLITE" "$db" < schema.sql
  "$SQLITE" -cmd "PRAGMA foreign_keys = ON" "$db" < seed.sql
  "$SQLITE" -header -column -nullvalue NULL \
    -cmd "PRAGMA foreign_keys = ON" "$db" \
    < "$script" > "$work/$name.txt" 2>&1 || true
  if [ "${1:-}" = "--update" ]; then
    cp "$work/$name.txt" "expected/$name.txt"
    echo "updated $name"
  elif diff -u "expected/$name.txt" "$work/$name.txt"; then
    echo "ok      $name"
  else
    echo "FAILED  $name"
    failed=1
  fi
done
exit "$failed"
