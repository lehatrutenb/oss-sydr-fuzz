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

mkdir -p $OUT/corpus-MultiFormatEncodeFuzzer
printf 'mdd1234567890128' > $OUT/corpus-MultiFormatEncodeFuzzer/seed-ean13
printf 'ldd96385074' > $OUT/corpus-MultiFormatEncodeFuzzer/seed-ean8
printf 'qddZXING SYDR FUZZ' > $OUT/corpus-MultiFormatEncodeFuzzer/seed-qr
printf 'fddhttps://example.com/path?q=1' > $OUT/corpus-MultiFormatEncodeFuzzer/seed-aztec
printf 'kddZXINGSYDR' > $OUT/corpus-MultiFormatEncodeFuzzer/seed-datamatrix
printf 'gddA12345A' > $OUT/corpus-MultiFormatEncodeFuzzer/seed-codabar
printf 'ndd1234567890' > $OUT/corpus-MultiFormatEncodeFuzzer/seed-itf
printf 'pddZXING-417' > $OUT/corpus-MultiFormatEncodeFuzzer/seed-pdf417
printf 'jddZXING-128' > $OUT/corpus-MultiFormatEncodeFuzzer/seed-code128
printf 'hddZXING39' > $OUT/corpus-MultiFormatEncodeFuzzer/seed-code39
printf 'qdd\xc3\xa9\xd0\xb0\xe4\xb8\xad' > $OUT/corpus-MultiFormatEncodeFuzzer/seed-utf8
echo "corpus-MultiFormatEncodeFuzzer: 11 seeds"

mkdir -p $OUT/corpus-ResultParserFuzzer
printf 'ahttps://example.com/path?q=1#frag' > $OUT/corpus-ResultParserFuzzer/seed-uri
printf 'bmailto:user@example.com?subject=hi&body=text' > $OUT/corpus-ResultParserFuzzer/seed-email
printf 'cMATMSG:TO:user@example.com;SUB:hi;BODY:text;;' > $OUT/corpus-ResultParserFuzzer/seed-docomo-mail
printf 'dtel:+1-555-0100' > $OUT/corpus-ResultParserFuzzer/seed-tel
printf 'eSMSTO:+15550100:hello there' > $OUT/corpus-ResultParserFuzzer/seed-smsto
printf 'fsmsto:+15550100:hello' > $OUT/corpus-ResultParserFuzzer/seed-sms
printf 'ggeo:40.71,-74.01,12?q=Office' > $OUT/corpus-ResultParserFuzzer/seed-geo
printf 'hWIFI:S:MyNet;T:WPA;P:secret;H:true;;' > $OUT/corpus-ResultParserFuzzer/seed-wifi
printf 'iBEGIN:VCARD\r\nVERSION:3.0\r\nN:Doe;John\r\nFN:John Doe\r\nTEL;TYPE=CELL:+15550100\r\nEMAIL:john@example.com\r\nADR:;;1 Main St;Town;ST;12345;US\r\nORG:Example\r\nURL:https://example.com\r\nEND:VCARD\r\n' > $OUT/corpus-ResultParserFuzzer/seed-vcard
printf 'jBEGIN:VEVENT\r\nSUMMARY:Standup\r\nDTSTART:20260101T090000Z\r\nDTEND:20260101T093000Z\r\nLOCATION:Room 1\r\nEND:VEVENT\r\n' > $OUT/corpus-ResultParserFuzzer/seed-vevent
printf 'kMECARD:N:Doe,John;TEL:+15550100;EMAIL:john@example.com;;' > $OUT/corpus-ResultParserFuzzer/seed-mecard
printf 'lBIZCARD:N:John;X:Doe;T:Dev;C:Example;A:1 Main St;B:+15550100;E:john@example.com;;' > $OUT/corpus-ResultParserFuzzer/seed-bizcard
printf 'mMEBKM:TITLE:Example;URL:https\\://example.com;;' > $OUT/corpus-ResultParserFuzzer/seed-bookmark
printf 'nurlto:Example:https://example.com' > $OUT/corpus-ResultParserFuzzer/seed-urlto
printf 'm9780201896831' > $OUT/corpus-ResultParserFuzzer/seed-isbn
printf 'c123456789012' > $OUT/corpus-ResultParserFuzzer/seed-product
printf 'h1M8GDM9AXKP042788' > $OUT/corpus-ResultParserFuzzer/seed-vin
printf 'b(01)90012345678908(3103)001750' > $OUT/corpus-ResultParserFuzzer/seed-rss-expanded
printf 'splain text contents' > $OUT/corpus-ResultParserFuzzer/seed-text
echo "corpus-ResultParserFuzzer: 19 seeds"

# HighLevelEncodeFuzzer: charAt(0) % 6 picks the encoder family
#   0 QR  1 DataMatrix  2 Aztec  3 PDF417  4,5 1D writers (charAt(1) % 9 picks which)
mkdir -p $OUT/corpus-HighLevelEncodeFuzzer
printf 'f\x01ZXING SYDR FUZZ' > $OUT/corpus-HighLevelEncodeFuzzer/seed-qr
printf 'g\x02ZXINGSYDR' > $OUT/corpus-HighLevelEncodeFuzzer/seed-datamatrix
printf 'h\x03ZXING SYDR FUZZ' > $OUT/corpus-HighLevelEncodeFuzzer/seed-aztec
printf 'i\x04ZXING-417-PAYLOAD' > $OUT/corpus-HighLevelEncodeFuzzer/seed-pdf417
printf 'j\x00ZXING-128' > $OUT/corpus-HighLevelEncodeFuzzer/seed-code128
printf 'j\x01ZXING39' > $OUT/corpus-HighLevelEncodeFuzzer/seed-code39
printf 'j\x02ZXING93' > $OUT/corpus-HighLevelEncodeFuzzer/seed-code93
printf 'j\x03A12345A' > $OUT/corpus-HighLevelEncodeFuzzer/seed-codabar
printf 'j\x041234567890' > $OUT/corpus-HighLevelEncodeFuzzer/seed-itf
printf 'j\x05123456789012' > $OUT/corpus-HighLevelEncodeFuzzer/seed-ean13
printf 'j\x069638507' > $OUT/corpus-HighLevelEncodeFuzzer/seed-ean8
printf 'f\x00\xc3\xa9\xd0\xb0\xe4\xb8\xad' > $OUT/corpus-HighLevelEncodeFuzzer/seed-qr-utf8
echo "corpus-HighLevelEncodeFuzzer: 12 seeds"

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
