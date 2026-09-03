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

IO=/commons-io
GOFUZZ=$SRC/go-fuzz-corpus

MAX_SEED_SIZE=1048576c

TARGETS="FileComparatorFuzzer FileFilterFuzzer FileUtilsFuzzer GeneralUtilsFuzzer \
         InputStreamFuzzer InputXmlFuzzer OutputStreamFuzzer PathUtilsFuzzer \
         ReaderFuzzer WriterFuzzer"

generic=$(mktemp -d)
n=0
while IFS= read -r f; do
  n=$((n + 1))
  cp "$f" "$generic/seed-$n"
done < <(find $IO/src/test/resources -type f -size -$MAX_SEED_SIZE)
printf 'hello world\n' > "$generic/seed-text"
printf '\x00\x01\x02\x03\xff\xfe\xfd' > "$generic/seed-binary"
printf '\xef\xbb\xbfwith bom\n' > "$generic/seed-bom"

for target in $TARGETS; do
  mkdir -p $OUT/corpus-$target
  cp $generic/* $OUT/corpus-$target/
  echo "corpus-$target: $(find $OUT/corpus-$target -type f | wc -l) seeds"
done
rm -rf $generic

n=$(find $OUT/corpus-InputXmlFuzzer -type f | wc -l)
while IFS= read -r f; do
  n=$((n + 1))
  cp "$f" "$OUT/corpus-InputXmlFuzzer/xml-$n"
done < <(find $GOFUZZ/xml/corpus -type f -size -$MAX_SEED_SIZE)
echo "corpus-InputXmlFuzzer: $n seeds"

mkdir -p $OUT/corpus-FuzzMerged
n=0
for dir in $OUT/corpus-*; do
  case "$dir" in
    */corpus-FuzzMerged) continue ;;
  esac
  for f in "$dir"/*; do
    [ -f "$f" ] || continue
    n=$((n + 1))
    cp "$f" "$OUT/corpus-FuzzMerged/seed-$n"
  done
done
echo "corpus-FuzzMerged: $n seeds"
