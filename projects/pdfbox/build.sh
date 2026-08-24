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

MAVEN_VERSION=$(wget -qO- https://dlcdn.apache.org/maven/maven-3/ | grep -oP 'href="\K3\.[0-9]+\.[0-9]+(?=/)' | sort -uV | tail -1)
wget https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz
tar -xvf apache-maven-*-bin.tar.gz
rm apache-maven-*-bin.tar.gz
mv apache-maven-* /opt/

MVN=$(ls -d /opt/apache-maven-*/bin/mvn)
MAVEN_ARGS="-DskipTests -Dmaven.javadoc.skip=true -Drat.skip=true -am -pl :pdfbox"
$MVN --batch-mode install $MAVEN_ARGS
CURRENT_VERSION=$($MVN org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate \
 -Dexpression=project.version -q -DforceStdout)

cp "pdfbox/target/pdfbox-$CURRENT_VERSION.jar" $OUT/pdfbox.jar
cp "fontbox/target/fontbox-$CURRENT_VERSION.jar" $OUT/fontbox.jar
cp "io/target/pdfbox-io-$CURRENT_VERSION.jar" $OUT/pdfbox-io.jar

$MVN dependency:copy -Dartifact=org.apache.logging.log4j:log4j-api:2.24.3 -DoutputDirectory=$OUT/
$MVN dependency:copy -Dartifact=org.apache.logging.log4j:log4j-core:2.24.3 -DoutputDirectory=$OUT/
mv $OUT/log4j-api-2.24.3.jar $OUT/log4j-api.jar
mv $OUT/log4j-core-2.24.3.jar $OUT/log4j-core.jar
# JaCoCo chokes on Multi-Release JARs (duplicate classes under META-INF/versions).
tmpdir=$(mktemp -d)
(cd "$tmpdir" && jar xf $OUT/log4j-core.jar && rm -rf META-INF/versions && jar cf $OUT/log4j-core.jar .)
rm -rf "$tmpdir"
cp $SRC/log4j2.xml $OUT/

JAZZER_API_PATH=/usr/local/lib/jazzer_standalone_deploy.jar
ALL_JARS="pdfbox.jar fontbox.jar pdfbox-io.jar log4j-api.jar log4j-core.jar"
BUILD_CLASSPATH=$(echo $ALL_JARS | xargs printf -- "$OUT/%s:"):$JAZZER_API_PATH

javac -encoding UTF-8 -cp $BUILD_CLASSPATH $(find $SRC -maxdepth 1 -name '*.java')
cp $SRC/*.class $OUT/

MAX_SEED_SIZE=2097152c

collect() {
  local target=$1
  shift
  local dir=/corpus-$target
  mkdir -p $dir
  local n=0
  for pattern in "$@"; do
    while IFS= read -r f; do
      n=$((n + 1))
      cp "$f" "$dir/seed-$n"
    done < <(find /pdfbox -name "$pattern" -type f -size -$MAX_SEED_SIZE)
  done
  echo "corpus-$target: $n seeds"
}

collect PDFExtractTextFuzzer '*.pdf'
collect PDFStreamParserFuzzer '*.pdf'
collect TTFParserFuzzer '*.ttf'
collect OTFParserFuzzer '*.otf' '*.ttf'
collect CFFParserFuzzer '*.otf'
collect CMapParserFuzzer '*.otf' '*.ttf'
collect PFAParserFuzzer '*.pfa' '*.pfb' '*.ttf'

mkdir -p /corpus-PDFWriteReadFuzzer
printf 'AHello PDFBox write-read' > /corpus-PDFWriteReadFuzzer/seed-1
printf 'BPage two with numbers 12345' > /corpus-PDFWriteReadFuzzer/seed-2
printf 'C' > /corpus-PDFWriteReadFuzzer/seed-3
echo "corpus-PDFWriteReadFuzzer: 3 seeds"

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
