#!/bin/bash -eu
# Copyright 2023 Google LLC
# Modifications copyright (C) 2025 ISP RAS
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
################################################################################
SRC=/src
OUT=/out

TRINO=/trino

MAX_SEED_SIZE=65536c

mkdir -p $OUT/corpus-StatementFuzzer
n=0
while IFS= read -r f; do
  n=$((n + 1))
  cp "$f" "$OUT/corpus-StatementFuzzer/seed-$n"
done < <(find $TRINO/testing $TRINO/core -type f -name '*.sql' -size -$MAX_SEED_SIZE 2>/dev/null)
printf 'SELECT a, b FROM t WHERE a > 1 GROUP BY b HAVING count(*) > 2 ORDER BY b LIMIT 10\n' \
  > $OUT/corpus-StatementFuzzer/seed-select
printf 'WITH x AS (SELECT 1 AS c) SELECT * FROM x JOIN y ON x.c = y.c\n' \
  > $OUT/corpus-StatementFuzzer/seed-cte
printf 'CREATE TABLE t (a bigint, b varchar(10), c array(row(x double))) WITH (format = %s)\n' "'ORC'" \
  > $OUT/corpus-StatementFuzzer/seed-create
printf 'SELECT sum(x) OVER (PARTITION BY y ORDER BY z ROWS BETWEEN 1 PRECEDING AND CURRENT ROW) FROM t\n' \
  > $OUT/corpus-StatementFuzzer/seed-window
printf 'SELECT * FROM t MATCH_RECOGNIZE (PARTITION BY a ORDER BY b MEASURES A.x AS m PATTERN (A B+) DEFINE B AS B.y > 0)\n' \
  > $OUT/corpus-StatementFuzzer/seed-match-recognize
printf 'INSERT INTO t SELECT 1 UNION ALL SELECT 2 INTERSECT SELECT 3 EXCEPT SELECT 4\n' \
  > $OUT/corpus-StatementFuzzer/seed-setops
printf 'EXPLAIN ANALYZE SELECT json_query(c, %s) FROM t\n' "'lax \$.a'" \
  > $OUT/corpus-StatementFuzzer/seed-explain
echo "corpus-StatementFuzzer: $(find $OUT/corpus-StatementFuzzer -type f | wc -l) seeds"

mkdir -p $OUT/corpus-ExpressionFuzzer
printf 'a + b * 2 - (c / 4) %% 5\n' > $OUT/corpus-ExpressionFuzzer/seed-arith
printf "CASE WHEN a IS NULL THEN 'x' WHEN a BETWEEN 1 AND 2 THEN 'y' ELSE 'z' END\n" \
  > $OUT/corpus-ExpressionFuzzer/seed-case
printf "CAST(row(1, 'a') AS row(x bigint, y varchar))\n" > $OUT/corpus-ExpressionFuzzer/seed-cast
printf "a IN (1, 2, 3) AND b LIKE '%%x%%' ESCAPE '!' OR NOT c\n" \
  > $OUT/corpus-ExpressionFuzzer/seed-predicates
printf "INTERVAL '3' DAY + TIMESTAMP '2026-01-01 00:00:00 UTC'\n" \
  > $OUT/corpus-ExpressionFuzzer/seed-interval
printf 'TRY(array_agg(x) FILTER (WHERE x > 0))[1]\n' > $OUT/corpus-ExpressionFuzzer/seed-call
printf 'EXISTS (SELECT 1) AND a = ALL (SELECT b FROM t)\n' > $OUT/corpus-ExpressionFuzzer/seed-subquery
echo "corpus-ExpressionFuzzer: 7 seeds"

mkdir -p $OUT/corpus-TypeFuzzer
printf 'darray(row(x bigint, y varchar(10)))' > $OUT/corpus-TypeFuzzer/seed-type-array
printf 'dmap(varchar, array(decimal(10, 2)))' > $OUT/corpus-TypeFuzzer/seed-type-map
printf 'dtimestamp(6) with time zone' > $OUT/corpus-TypeFuzzer/seed-type-timestamp
printf 'acatalog.schema, other.schema' > $OUT/corpus-TypeFuzzer/seed-path
printf 'b(A B+ C* | D?)' > $OUT/corpus-TypeFuzzer/seed-pattern
printf 'bPERMUTE(A, B){2,5}' > $OUT/corpus-TypeFuzzer/seed-pattern-permute
printf 'cFUNCTION f(x bigint) RETURNS bigint RETURN x + 1' > $OUT/corpus-TypeFuzzer/seed-function
echo "corpus-TypeFuzzer: 7 seeds"

mkdir -p $OUT/corpus-FuzzMergedPersistent
n=0
for dir in $OUT/corpus-*; do
  case "$dir" in
    */corpus-FuzzMergedPersistent) continue ;;
  esac
  for f in "$dir"/*; do
    [ -f "$f" ] || continue
    n=$((n + 1))
    cp "$f" "$OUT/corpus-FuzzMergedPersistent/seed-$n"
  done
done
echo "corpus-FuzzMergedPersistent: $n seeds"
