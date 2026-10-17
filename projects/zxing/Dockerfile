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

RUN true && until git clone https://github.com/zxing/zxing zxing; do sleep 30; done

COPY build.sh build_seeds.sh /zxing
COPY *.java /src/

ENV CASR_SOURCE_DIRS /libsrc

WORKDIR /zxing
RUN git checkout zxing-3.5.3
RUN mkdir -p /root/.m2 && echo '<settings xmlns="http://apache.org" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://apache.org https://apache.org"><mirrors><mirror><id>central-mirror-fix</id><name>Redirect broken Sonatype Grid to Central</name><url>https://repo.maven.apache.org/maven2/</url><mirrorOf>rso-public-grid</mirrorOf></mirror></mirrors></settings>' > /root/.m2/settings.xml
RUN bash /zxing/build.sh
RUN bash /zxing/build_seeds.sh
