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

ICU=/icu4j

# LocaleFormatFuzzer: charAt(0) % 6
#   0 ULocale  1 MessageFormat  2 PluralRules  3 DecimalFormat  4 SimpleDateFormat  5 DateTimePatternGenerator
mkdir -p $OUT/corpus-LocaleFormatFuzzer
printf '\x00zh-Hant-TW-u-ca-roc-nu-hanidec' > $OUT/corpus-LocaleFormatFuzzer/seed-locale
printf '\x00en-US-x-private' > $OUT/corpus-LocaleFormatFuzzer/seed-locale-private
printf '\x01{0, plural, one {# file} other {# files}} at {2, date, short}' > $OUT/corpus-LocaleFormatFuzzer/seed-message
printf '\x01{1, select, a {A} other {O}} and {0, number, percent}' > $OUT/corpus-LocaleFormatFuzzer/seed-message-select
printf '\x02one: n is 1; few: n in 2..4; other:' > $OUT/corpus-LocaleFormatFuzzer/seed-plural
printf '\x02one: i = 1 and v = 0 @integer 1' > $OUT/corpus-LocaleFormatFuzzer/seed-plural-samples
printf '\x03#,##0.00;(#,##0.00)' > $OUT/corpus-LocaleFormatFuzzer/seed-decimal
printf '\x03@@@E0' > $OUT/corpus-LocaleFormatFuzzer/seed-decimal-sci
printf "\x04yyyy-MM-dd'T'HH:mm:ss.SSSXXX" > $OUT/corpus-LocaleFormatFuzzer/seed-datefmt
printf '\x04GGGG yyyy QQQ MMMM EEEE' > $OUT/corpus-LocaleFormatFuzzer/seed-datefmt-wide
printf '\x05yMMMdjmm' > $OUT/corpus-LocaleFormatFuzzer/seed-skeleton
echo "corpus-LocaleFormatFuzzer: 11 seeds"

# NormalizeFuzzer: charAt(0) % 5
#   0 Normalizer2  1 UnicodeSet  2 Transliterator rules  3 IDNA/UTS46  4 StringPrep
mkdir -p $OUT/corpus-NormalizeFuzzer
printf '\x00A\xcc\x8angstro\xcc\x88m \xef\xac\x81 \xe2\x91\xa0 \xef\xbd\xb1' > $OUT/corpus-NormalizeFuzzer/seed-normalize
printf '\x00\xe1\xbe\xbc\xcd\x85 \xd5\xa5\xd6\x82' > $OUT/corpus-NormalizeFuzzer/seed-normalize-casefold
printf '\x01[[:L:][:N:]-[a-f]&[\\u0000-\\u00FF]]' > $OUT/corpus-NormalizeFuzzer/seed-unicodeset
printf '\x01[\\p{Script=Greek}\\p{Mn}]' > $OUT/corpus-NormalizeFuzzer/seed-unicodeset-prop
printf '\x02a > b; ::NFD; [:Mn:] > ; ::NFC;' > $OUT/corpus-NormalizeFuzzer/seed-translit
printf '\x02$x = [aeiou]; $x > V; . > .;' > $OUT/corpus-NormalizeFuzzer/seed-translit-var
printf '\x03\xd0\xbf\xd1\x80\xd0\xb8\xd0\xbc\xd0\xb5\xd1\x80.\xd1\x80\xd1\x84' > $OUT/corpus-NormalizeFuzzer/seed-idna
printf '\x03xn--80ak6aa92e.com' > $OUT/corpus-NormalizeFuzzer/seed-idna-punycode
printf '\x04Example\xc2\xa0Name' > $OUT/corpus-NormalizeFuzzer/seed-stringprep
echo "corpus-NormalizeFuzzer: 9 seeds"

# BreakCollateFuzzer: charAt(0) % 4
#   0 BreakIterator  1 RuleBasedCollator rules  2 RuleBasedNumberFormat rules  3 RuleBasedBreakIterator rules
mkdir -p $OUT/corpus-BreakCollateFuzzer
printf '\x00The quick brown fox. Jumps over! \xe4\xb8\xad\xe6\x96\x87\xe6\x96\xad\xe8\xa1\x8c \xd8\xa7\xd9\x84\xd8\xb9\xd8\xb1\xd8\xa8\xd9\x8a\xd8\xa9' > $OUT/corpus-BreakCollateFuzzer/seed-break
printf '\x00a\xe2\x80\x8bb\xc2\xadc\xe2\x80\xa8d' > $OUT/corpus-BreakCollateFuzzer/seed-break-controls
printf '\x01& a < b < c & B << q <<< Q' > $OUT/corpus-BreakCollateFuzzer/seed-collator
printf '\x01[strength 3] & x = y' > $OUT/corpus-BreakCollateFuzzer/seed-collator-opts
printf '\x02%%spellout-numbering:\n0: zero;\n1: one;\n2: two;\n' > $OUT/corpus-BreakCollateFuzzer/seed-rbnf
printf '\x03!!forward;\n$word = [[:L:]]+;\n$word.*;\n' > $OUT/corpus-BreakCollateFuzzer/seed-rbbi
echo "corpus-BreakCollateFuzzer: 6 seeds"

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
