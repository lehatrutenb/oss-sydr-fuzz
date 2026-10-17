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

OGNL=/ognl

generic=$(mktemp -d)
printf 'name\n' > "$generic/seed-property"
printf 's.length()\n' > "$generic/seed-method"
printf 'n + 1 * 2 - 3 / 4 %% 5\n' > "$generic/seed-arith"
printf 'n > 1 ? "big" : "small"\n' > "$generic/seed-ternary"
printf 'map["k"].list[0]\n' > "$generic/seed-index"
printf '{1, 2, 3}\n' > "$generic/seed-list-literal"
printf '#{ "a" : 1, "b" : 2 }\n' > "$generic/seed-map-literal"
printf 'list.{ #this * 2 }\n' > "$generic/seed-projection"
printf 'list.{? #this > 1 }\n' > "$generic/seed-selection"
printf 'list.{^ #this > 1 }\n' > "$generic/seed-selection-first"
printf 'n instanceof java.lang.Integer\n' > "$generic/seed-instanceof"
printf '(n = 5), n\n' > "$generic/seed-sequence"
printf 'n in {1, 2, 3} and not (n not in {4})\n' > "$generic/seed-in"
printf 'n << 2 | 1 & 3 ^ 4\n' > "$generic/seed-bitwise"
printf '@java.lang.Math@max(1, 2)\n' > "$generic/seed-static"
printf 'new java.lang.String("x")\n' > "$generic/seed-constructor"
printf '#root.s\n' > "$generic/seed-context"
printf 's?.length()\n' > "$generic/seed-nullsafe"

for target in ExpressionParseFuzzer RestrictedEvalFuzzer ExpressionRoundTripFuzzer; do
  mkdir -p $OUT/corpus-$target
  cp $generic/* $OUT/corpus-$target/
  echo "corpus-$target: $(find $OUT/corpus-$target -type f | wc -l) seeds"
done
rm -rf $generic

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
