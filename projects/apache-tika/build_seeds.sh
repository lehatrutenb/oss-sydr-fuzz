#!/bin/bash -eu
# Copyright 2025 Google LLC
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
# Packages tika's own unit test files into a corpus directory per fuzz target,
# selected by file extension. Upstream builds zip archives here because OSS-Fuzz
# consumes <target>_seed_corpus.zip; sydr-fuzz takes a directory, so the seeds
# are laid out directly.
SRC=/src
OUT=/out

TIKA=/tika

# Seeds are capped in size: a handful of tika's test documents are large, and
# libFuzzer derives -max_len from the biggest unit in the corpus.
MAX_SEED_SIZE=2097152c  # 2 MiB; find -size units round up, so use bytes

collect() {
  local target=$1
  shift
  local dir=/corpus-$target
  mkdir -p $dir
  local n=0
  local pattern
  local f
  for pattern in "$@"; do
    while IFS= read -r f; do
      n=$((n + 1))
      cp "$f" "$dir/seed-$n"
    done < <(find $TIKA -name "$pattern" -type f -size -$MAX_SEED_SIZE)
  done
  echo "corpus-$target: $n seeds"
}

collect AudioVideoParsersFuzzer '*-webm.noext' '*-mkv.noext' '*.aif' '*.au' '*.flv' \
                                '*.m4a' '*.mkv' '*.mp3' '*.wav'
collect CompressorParserFuzzer  '*.Z' '*.bz2' '*.gz' '*.tbz2' '*.tgz' '*.zst'
collect HtmlParserFuzzer        '*.html'
collect ImageParsersFuzzer      '*.avif' '*.bmp' '*.bpg' '*.gif' '*.heic' '*.icns' '*.jp2' \
                                '*.jb2' '*.jpg' '*.jxl' '*.png' '*.psd' '*.tif' '*.webp'
collect JackcessParserFuzzer    '*.mdb' '*.accdb'
collect OOXMLParserFuzzer       '*.docm' '*.docx' '*.pptm' '*.pptx' '*.xlsm' '*.xlsx'
collect OfficeParserFuzzer      '*.msg' '*.doc' '*.ppt' '*.xls'
collect OneNoteParserFuzzer     '*.one'
collect PDFParserFuzzer         '*.pdf'
collect PackageParserFuzzer     '*.7z' '*.ar' '*.jar' '*.rar' '*.tar' '*.zip' '*.zlib'
collect RFC822ParserFuzzer      '*.eml'
collect RTFParserFuzzer         '*.rtf'
collect TextAndCSVParserFuzzer  '*.txt' '*.tsv' '*.csv'
collect XMLReaderUtilsFuzzer    '*.xml'

# AutoDetectParser dispatches on content, so it gets everything.
mkdir -p /corpus-AutoDetectParserFuzzer
n=0
while IFS= read -r f; do
  n=$((n + 1))
  cp "$f" /corpus-AutoDetectParserFuzzer/seed-$n
done < <(find $TIKA -path '*/test-documents/*' -type f -size -$MAX_SEED_SIZE)
echo "corpus-AutoDetectParserFuzzer: $n seeds"

# FuzzMerged takes a String rather than a byte[], so it is seeded from the
# textual formats only. Handing it the binary corpora would add well over a
# hundred megabytes of seeds that cannot survive the round trip through a
# String, and the mutator framework generates text for this parameter anyway.
mkdir -p /corpus-FuzzMerged
n=0
for f in /corpus-HtmlParserFuzzer/* /corpus-XMLReaderUtilsFuzzer/* \
         /corpus-TextAndCSVParserFuzzer/* /corpus-RFC822ParserFuzzer/*; do
  [ -f "$f" ] || continue
  n=$((n + 1))
  cp "$f" "/corpus-FuzzMerged/seed-$n"
done
echo "corpus-FuzzMerged: $n seeds"
