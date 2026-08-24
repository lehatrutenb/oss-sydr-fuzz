#!/bin/bash -eu
# Copyright 2023 Google LLC
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
# Builds a corpus directory per fuzz target from commons-compress' own test
# archives, plus the matching go-fuzz-corpus sets. Upstream produces
# <target>_seed_corpus.zip archives here because that is what OSS-Fuzz consumes;
# sydr-fuzz takes a directory, so the seeds are laid out directly.
SRC=/src
OUT=/out

COMPRESS=/commons-compress
GOFUZZ=$SRC/go-fuzz-corpus

MAX_SEED_SIZE=2097152c  # 2 MiB; find -size units round up, so use bytes

# collect <target> <pattern>...  -- patterns are matched under the checkout
collect() {
  local target=$1
  shift
  local dir=/corpus-$target
  mkdir -p $dir
  local n
  n=$(find $dir -type f | wc -l)
  local pattern
  local f
  for pattern in "$@"; do
    while IFS= read -r f; do
      n=$((n + 1))
      cp "$f" "$dir/seed-$n"
    done < <(find $COMPRESS -name "$pattern" -type f -size -$MAX_SEED_SIZE)
  done
  echo "corpus-$target: $n seeds"
}

# gofuzz <target> <go-fuzz-corpus subdir>
gofuzz() {
  local target=$1
  local sub=$2
  local dir=/corpus-$target
  mkdir -p $dir
  local n
  n=$(find $dir -type f | wc -l)
  local f
  while IFS= read -r f; do
    n=$((n + 1))
    cp "$f" "$dir/seed-$n"
  done < <(find $GOFUZZ/$sub/corpus -type f -size -$MAX_SEED_SIZE)
}

# Seed from go-fuzz-corpus first, then add the project's own test archives.
gofuzz  ArchiverTarStreamFuzzer tar
collect ArchiverTarStreamFuzzer '*.tar'
gofuzz  ArchiverZipStreamFuzzer zip
collect ArchiverZipStreamFuzzer '*.zip'
gofuzz  CompressTarFuzzer tar
collect CompressTarFuzzer '*.tar'
gofuzz  CompressZipFuzzer zip
collect CompressZipFuzzer '*.zip'
gofuzz  CompressorBZip2Fuzzer bzip2
collect CompressorBZip2Fuzzer '*.bz2'
gofuzz  CompressorGzipFuzzer gzip
collect CompressorGzipFuzzer '*.gz'
gofuzz  CompressorSnappyFuzzer snappy
collect CompressorSnappyFuzzer '*.sz'

collect ArchiverArFuzzer '*.ar'
collect ArchiverArjFuzzer '*.arj'
collect ArchiverCpioFuzzer '*.cpio'
collect ArchiverDumpFuzzer '*.dump'
collect CompressSevenZFuzzer '*.7z'
collect CompressorDeflate64Fuzzer '*.deflate'
collect CompressorLZ4Fuzzer '*lz4'
collect CompressorPack200Fuzzer '*.pack'
collect CompressorZFuzzer '*.Z'

# FuzzMerged reaches every target from one config, so give it the union of the
# per-target corpora.
mkdir -p /corpus-FuzzMerged
n=0
for dir in /corpus-*; do
  case "$dir" in
    */corpus-FuzzMerged) continue ;;
  esac
  for f in "$dir"/*; do
    [ -f "$f" ] || continue
    n=$((n + 1))
    cp "$f" "/corpus-FuzzMerged/seed-$n"
  done
done
echo "corpus-FuzzMerged: $n seeds"
