#!/bin/bash -eu
# Copyright 2025 ISP RAS
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

GROUP_ID=com.google.zxing
ARTIFACT_ID=core
VERSION=3.5.3

MAVEN_VERSION=$(wget -qO- https://dlcdn.apache.org/maven/maven-3/ | grep -oP 'href="\K3\.[0-9]+\.[0-9]+(?=/)' | sort -uV | tail -1)
wget https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz
tar -xvf apache-maven-*-bin.tar.gz
rm apache-maven-*-bin.tar.gz
mv apache-maven-* /opt/

MVN=$(ls -d /opt/apache-maven-*/bin/mvn)

mkdir -p /deps
cat > /deps/pom.xml <<EOF
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>oss-sydr-fuzz</groupId>
  <artifactId>deps</artifactId>
  <version>1</version>
  <packaging>jar</packaging>
  <dependencies>
    <dependency>
      <groupId>$GROUP_ID</groupId>
      <artifactId>$ARTIFACT_ID</artifactId>
      <version>$VERSION</version>
    </dependency>
  </dependencies>
</project>
EOF

$MVN -B -f /deps/pom.xml dependency:copy-dependencies \
  -DoutputDirectory=$OUT -DincludeScope=runtime -Dmdep.stripVersion=true
$MVN -B -f /deps/pom.xml dependency:copy \
  -Dartifact=$GROUP_ID:$ARTIFACT_ID:$VERSION:jar:sources -DoutputDirectory=/deps
mkdir -p /libsrc
unzip -qo /deps/$ARTIFACT_ID-$VERSION-sources.jar -d /libsrc

# JaCoCo aborts the whole coverage report when two class files share a name
# but differ, which is exactly what a Multi-Release jar holds:
# META-INF/versions/N/<C>.class next to the base <C>.class. Drop the versioned
# copies. `zip -d` deletes entries in place so the survivors keep their bytes --
# repacking with `jar cf` regenerates the manifest and makes signed jars fail at
# class load with "SecurityException: Invalid signature file digest".
for jar in "$OUT"/*.jar; do
  [ -f "$jar" ] || continue
  if unzip -l "$jar" 'META-INF/versions/*' 2>/dev/null | grep '\.class$' \
       | grep -qv 'module-info\.class$'; then
    echo "dropping multi-release classes from ${jar##*/}"
    zip -qd "$jar" 'META-INF/versions/*'
  fi
done

JAZZER_API_PATH=/usr/local/lib/jazzer_standalone_deploy.jar
ALL_JARS=$(cd $OUT && ls *.jar | tr '\n' ' ')
BUILD_CLASSPATH=$(echo $ALL_JARS | xargs printf -- "$OUT/%s:"):$JAZZER_API_PATH:$SWAT_TEMPLATES

javac --release 14 -proc:none -encoding UTF-8 \
  -cp "$BUILD_CLASSPATH:$SRC:$OUT/javafuzz-core.jar" \
  $(find $SRC -maxdepth 1 -name '*.java')
cp $SRC/*.class $OUT/
