#!/bin/bash -eu
# Copyright 2025 Google LLC
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
MAVEN_ARGS="-DskipTests -Dcheckstyle.skip -Dossindex.skip -Drat.skip=true \
  -Dmaven.javadoc.skip=true --no-transfer-progress -am -pl :tika-app"
$MVN --batch-mode install $MAVEN_ARGS
CURRENT_VERSION=$($MVN org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate \
 -Dexpression=project.version -q -DforceStdout)

# tika-app's main artifact is thin (Class-Path: lib/). Parsers and deps are
# copied to target/lib by maven-dependency-plugin during package.
cp "tika-app/target/tika-app-$CURRENT_VERSION.jar" $OUT/tika.jar
cp tika-app/target/lib/*.jar $OUT/
cp $SRC/tika-config.xml $SRC/log4j2.xml $OUT/

JAZZER_API_PATH=/usr/local/lib/jazzer_standalone_deploy.jar
BUILD_CLASSPATH=$(printf '%s:' $OUT/*.jar)$JAZZER_API_PATH

javac -encoding UTF-8 -cp $BUILD_CLASSPATH $(find $SRC -maxdepth 1 -name '*.java')
cp $SRC/*.class $OUT/

echo "==> class files in $OUT:"
ls -la "$OUT"/*.class
