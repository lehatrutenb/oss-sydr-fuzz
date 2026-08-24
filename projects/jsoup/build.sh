#!/bin/bash -eu
# Copyright 2021 Google LLC
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

echo "==> sources in $SRC:"
ls -la "$SRC"

MAVEN_VERSION=$(wget -qO- https://dlcdn.apache.org/maven/maven-3/ | grep -oP 'href="\K3\.[0-9]+\.[0-9]+(?=/)' | sort -uV | tail -1)
wget https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz
tar -xvf apache-maven-*-bin.tar.gz
rm apache-maven-*-bin.tar.gz
mv apache-maven-* /opt/

MVN=$(ls -d /opt/apache-maven-*/bin/mvn)
MAVEN_ARGS="-Dmaven.test.skip=true -Dmaven.javadoc.skip=true -Danimal.sniffer.skip=true -Djapicmp.skip=true"
$MVN --batch-mode package ${MAVEN_ARGS}
CURRENT_VERSION=$($MVN org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate \
 -Dexpression=project.version -q -DforceStdout)
cp "target/jsoup-$CURRENT_VERSION.jar" $OUT/jsoup.jar

# re2j is an optional dependency jsoup detects at runtime.
$MVN dependency:copy -Dartifact=com.google.re2j:re2j:1.8 -DoutputDirectory=$OUT/
mv $OUT/re2j-1.8.jar $OUT/re2j.jar

JAZZER_API_PATH=/usr/local/lib/jazzer_standalone_deploy.jar
if [ ! -f "$JAZZER_API_PATH" ]; then
  echo "ERROR: jazzer not found at $JAZZER_API_PATH" >&2
  ls -la /usr/local/lib/ >&2 || true
  exit 1
fi

ALL_JARS="jsoup.jar re2j.jar"
BUILD_CLASSPATH=$(echo $ALL_JARS | xargs printf -- "$OUT/%s:"):$JAZZER_API_PATH

# Same pattern as gson/janino: compile each source, copy .class into $OUT.
# FuzzMerged last so the per-target classes already exist beside the sources.
for fuzzer in $(find "$SRC" -maxdepth 1 -name '*Fuzzer.java' | sort); do
  fuzzer_basename=$(basename -s .java "$fuzzer")
  echo "==> javac $fuzzer_basename"
  javac -encoding UTF-8 -cp "$BUILD_CLASSPATH:$SRC" "$fuzzer"
  cp "$SRC/$fuzzer_basename.class" "$OUT/"
done

echo "==> javac FuzzMerged"
javac -encoding UTF-8 -cp "$BUILD_CLASSPATH:$SRC" "$SRC/FuzzMerged.java"
cp "$SRC/FuzzMerged.class" "$OUT/"

echo "==> class files in $OUT:"
ls -la "$OUT"/*.class

for required in HtmlFuzzer XmlFuzzer CleanFuzzer SelectorFuzzer CssHtmlFuzzer FragmentHtmlFuzzer FuzzMerged; do
  if [ ! -f "$OUT/$required.class" ]; then
    echo "ERROR: missing $OUT/$required.class" >&2
    exit 1
  fi
done

MAX_SEED_SIZE=65536
mkdir -p /corpus-html
seed=0
for f in src/test/resources/htmltests/* src/test/resources/fuzztests/*; do
  [ -f "$f" ] || continue
  seed=$((seed + 1))
  unit=/corpus-html/seed-$seed
  if ! gzip -dc "$f" > "$unit" 2>/dev/null; then
    cp "$f" "$unit"
  fi
  if [ "$(stat -c%s "$unit")" -gt $MAX_SEED_SIZE ]; then
    rm "$unit"
  fi
done

mkdir -p /corpus-clean
cp /corpus-html/* /corpus-clean/

mkdir -p /corpus-xml
cp src/test/resources/htmltests/*.xml src/test/resources/htmltests/*.xhtml \
   src/test/resources/htmltests/*.svg /corpus-xml/ 2>/dev/null || true

mkdir -p /corpus-selector
echo 'div > p.note[href^=http]' > /corpus-selector/seed1
echo 'a[href], img[src$=.png]' > /corpus-selector/seed2
echo ':matches(^foo$):not(.bar):nth-child(2n+1)' > /corpus-selector/seed3
echo 'html body div#id .cls * :root:has(> span)' > /corpus-selector/seed4

mkdir -p /corpus-csshtml
printf 'Adiv.note > p a[href]\n<html><body><div class=note><p><a href=x>l</a></p></div></body></html>' \
  > /corpus-csshtml/seed1
printf 'B*\n<div><span class=x>y</span></div>' > /corpus-csshtml/seed2
cp /corpus-selector/seed1 /corpus-csshtml/seed3
cp /corpus-html/seed-1 /corpus-csshtml/seed4 2>/dev/null || true

mkdir -p /corpus-fragment
printf 'Adiv\n<p>hello <b>world</b></p>' > /corpus-fragment/seed1
printf 'Btd\n<a href=x>cell</a>' > /corpus-fragment/seed2
cp /corpus-html/* /corpus-fragment/ 2>/dev/null || true

mkdir -p /corpus-merged
n=0
for f in /corpus-html/* /corpus-xml/* /corpus-selector/* \
         /corpus-csshtml/* /corpus-fragment/*; do
  [ -f "$f" ] || continue
  n=$((n + 1))
  cp "$f" "/corpus-merged/seed-$n"
done
echo "corpus-merged: $n seeds"
