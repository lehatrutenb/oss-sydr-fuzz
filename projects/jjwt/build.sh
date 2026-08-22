#!/bin/bash -eu
# Copyright 2021 Google LLC
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

sed -i 's/1.5</1.7</g' pom.xml

MVN=$(ls -d /opt/apache-maven-*/bin/mvn)
MAVEN_ARGS="-Djavac.src.version=17 -Djavac.target.version=17 -DskipTests"
$MVN package $MAVEN_ARGS
CURRENT_VERSION=$($MVN org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate \
 -Dexpression=project.version -q -DforceStdout)

cp "api/target/jjwt-api-$CURRENT_VERSION.jar" $OUT/jjwt-api.jar
cp "impl/target/jjwt-impl-$CURRENT_VERSION.jar" $OUT/jjwt-impl.jar
cp "extensions/gson/target/jjwt-gson-$CURRENT_VERSION.jar" $OUT/jjwt-gson.jar

# jjwt-gson needs Gson on the classpath
$MVN -pl extensions/gson dependency:copy-dependencies \
  -DincludeGroupIds=com.google.code.gson \
  -DoutputDirectory=$OUT \
  -DstripVersion=true \
  $MAVEN_ARGS

cp $OUT/gson-2.11.0.jar $OUT/gson.jar

JAZZER_API_PATH=/usr/local/lib/jazzer_standalone_deploy.jar
ALL_JARS="jjwt-api.jar jjwt-impl.jar jjwt-gson.jar gson.jar"
BUILD_CLASSPATH=$(echo $ALL_JARS | xargs printf -- "$OUT/%s:"):$JAZZER_API_PATH
RUNTIME_CLASSPATH=$(echo $ALL_JARS | xargs printf -- "\$this_dir/%s:"):\$this_dir

for fuzzer in $(find $SRC -name '*Fuzzer.java'); do
  fuzzer_basename=$(basename -s .java $fuzzer)
  javac -cp $BUILD_CLASSPATH $fuzzer
  cp $SRC/$fuzzer_basename.class $OUT/
done

mkdir $OUT/corpus
