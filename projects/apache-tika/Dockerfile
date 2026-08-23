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

ARG BASE_IMAGE="sydr/ubuntu20.04-sydr-fuzz"
FROM $BASE_IMAGE

RUN mkdir /src && mkdir /out

RUN git clone --depth 1 https://github.com/google/fuzzing
RUN cp fuzzing/dictionaries/pdf.dict /out/pdf.dict
RUN cp fuzzing/dictionaries/html.dict /out/html.dict
RUN cp fuzzing/dictionaries/xml.dict /out/xml.dict
RUN rm -rf fuzzing

RUN git clone https://github.com/apache/tika tika

COPY build.sh build_seeds.sh /tika
COPY *.java tika-config.xml log4j2.xml /src/

ENV CASR_SOURCE_DIRS /tika

WORKDIR /tika
RUN git checkout 2b70202aaec3db48fd5ff4d996d974444a750a11
RUN mkdir -p /root/.m2 && echo '<settings xmlns="http://apache.org" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://apache.org https://apache.org"><mirrors><mirror><id>central-mirror-fix</id><name>Redirect broken Sonatype Grid to Central</name><url>https://repo.maven.apache.org/maven2/</url><mirrorOf>rso-public-grid</mirrorOf></mirror></mirrors></settings>' > /root/.m2/settings.xml
RUN bash /tika/build.sh
RUN bash /tika/build_seeds.sh
