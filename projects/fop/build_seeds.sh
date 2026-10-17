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

FOP=/fop

MAX_SEED_SIZE=65536c

PAGE='<fo:layout-master-set><fo:simple-page-master master-name="p" page-height="200pt" page-width="200pt"><fo:region-body/></fo:simple-page-master></fo:layout-master-set>'

mkdir -p $OUT/corpus-FoDocumentFuzzer
n=0
while IFS= read -r f; do
  n=$((n + 1))
  cp "$f" "$OUT/corpus-FoDocumentFuzzer/seed-$n"
done < <(find $FOP -type f \( -name '*.fo' -o -name '*.xconf' -prune -o -name '*.fo' \) \
         -size -$MAX_SEED_SIZE 2>/dev/null | head -300)

printf '<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">%s<fo:page-sequence master-reference="p"><fo:flow flow-name="xsl-region-body"><fo:block font-size="12pt">hello</fo:block></fo:flow></fo:page-sequence></fo:root>' "$PAGE" \
  > $OUT/corpus-FoDocumentFuzzer/seed-block
printf '<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">%s<fo:page-sequence master-reference="p"><fo:flow flow-name="xsl-region-body"><fo:table table-layout="fixed" width="100%%"><fo:table-column column-width="50%%"/><fo:table-column column-width="50%%"/><fo:table-body><fo:table-row><fo:table-cell><fo:block>a</fo:block></fo:table-cell><fo:table-cell><fo:block>b</fo:block></fo:table-cell></fo:table-row></fo:table-body></fo:table></fo:flow></fo:page-sequence></fo:root>' "$PAGE" \
  > $OUT/corpus-FoDocumentFuzzer/seed-table
printf '<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">%s<fo:page-sequence master-reference="p"><fo:flow flow-name="xsl-region-body"><fo:list-block><fo:list-item><fo:list-item-label><fo:block>1.</fo:block></fo:list-item-label><fo:list-item-body><fo:block>x</fo:block></fo:list-item-body></fo:list-item></fo:list-block></fo:flow></fo:page-sequence></fo:root>' "$PAGE" \
  > $OUT/corpus-FoDocumentFuzzer/seed-list
printf '<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">%s<fo:page-sequence master-reference="p"><fo:flow flow-name="xsl-region-body"><fo:block><fo:inline font-weight="bold">b</fo:inline><fo:basic-link internal-destination="x">l</fo:basic-link><fo:block id="x">t</fo:block></fo:block></fo:flow></fo:page-sequence></fo:root>' "$PAGE" \
  > $OUT/corpus-FoDocumentFuzzer/seed-inline
printf '<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">%s<fo:page-sequence master-reference="p"><fo:static-content flow-name="xsl-region-before"><fo:block>hdr</fo:block></fo:static-content><fo:flow flow-name="xsl-region-body"><fo:block>body</fo:block></fo:flow></fo:page-sequence></fo:root>' "$PAGE" \
  > $OUT/corpus-FoDocumentFuzzer/seed-static
printf '<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">%s<fo:page-sequence master-reference="p"><fo:flow flow-name="xsl-region-body"><fo:block><fo:external-graphic src="url(http://127.0.0.1:1/x.png)"/></fo:block></fo:flow></fo:page-sequence></fo:root>' "$PAGE" \
  > $OUT/corpus-FoDocumentFuzzer/seed-graphic-denied
echo "corpus-FoDocumentFuzzer: $(find $OUT/corpus-FoDocumentFuzzer -type f | wc -l) seeds"

# PropertyValueFuzzer: charAt(0) % 20 picks the property, the rest is the value
mkdir -p $OUT/corpus-PropertyValueFuzzer
printf '\x0012pt' > $OUT/corpus-PropertyValueFuzzer/seed-font-size
printf '\x00150%%' > $OUT/corpus-PropertyValueFuzzer/seed-font-size-pct
printf '\x01serif, Helvetica, monospace' > $OUT/corpus-PropertyValueFuzzer/seed-font-family
printf '\x02italic bold 12pt/150%% serif' > $OUT/corpus-PropertyValueFuzzer/seed-font-shorthand
printf '\x03rgb(10, 20, 30)' > $OUT/corpus-PropertyValueFuzzer/seed-color
printf '\x03#ff0000' > $OUT/corpus-PropertyValueFuzzer/seed-color-hex
printf '\x05center top' > $OUT/corpus-PropertyValueFuzzer/seed-bg-position
printf '\x061pt 2pt 3pt 4pt' > $OUT/corpus-PropertyValueFuzzer/seed-margin
printf '\x082pt solid black' > $OUT/corpus-PropertyValueFuzzer/seed-border
printf '\x09retain' > $OUT/corpus-PropertyValueFuzzer/seed-conditionality
printf '\x0a1.2em' > $OUT/corpus-PropertyValueFuzzer/seed-space-before
printf '\x0bnormal' > $OUT/corpus-PropertyValueFuzzer/seed-line-height
printf '\x0cjustify' > $OUT/corpus-PropertyValueFuzzer/seed-text-align
printf '\x0dlr-tb' > $OUT/corpus-PropertyValueFuzzer/seed-writing-mode
printf '\x0eproportional-column-width(2)' > $OUT/corpus-PropertyValueFuzzer/seed-width-func
printf '\x0falways' > $OUT/corpus-PropertyValueFuzzer/seed-keep
printf '\x100.1em' > $OUT/corpus-PropertyValueFuzzer/seed-letter-spacing
printf '\x11super' > $OUT/corpus-PropertyValueFuzzer/seed-baseline
printf '\x12no-wrap' > $OUT/corpus-PropertyValueFuzzer/seed-wrap
printf '\x13preserve' > $OUT/corpus-PropertyValueFuzzer/seed-white-space
echo "corpus-PropertyValueFuzzer: 20 seeds"

# OutputFormatFuzzer: charAt(0) % 7 -> PS, PCL, AFP, plain text, RTF, area tree, intermediate format
mkdir -p $OUT/corpus-OutputFormatFuzzer
i=0
for f in $OUT/corpus-FoDocumentFuzzer/*; do
  [ -f "$f" ] || continue
  for sel in 0 1 2 3 4 5 6; do
    i=$((i + 1))
    { printf "\\$(printf '%03o' $sel)"; cat "$f"; } > "$OUT/corpus-OutputFormatFuzzer/seed-$i"
  done
done
echo "corpus-OutputFormatFuzzer: $(find $OUT/corpus-OutputFormatFuzzer -type f | wc -l) seeds"

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
