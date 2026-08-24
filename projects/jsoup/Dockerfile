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
RUN cat fuzzing/dictionaries/html.dict \
        fuzzing/dictionaries/svg.dict \
        fuzzing/dictionaries/mathml.dict \
      > /out/HtmlFuzzer.dict
RUN cat fuzzing/dictionaries/html.dict \
        fuzzing/dictionaries/svg.dict \
        fuzzing/dictionaries/css.dict \
      > /out/CleanFuzzer.dict
RUN cat fuzzing/dictionaries/html.dict \
        fuzzing/dictionaries/svg.dict \
        fuzzing/dictionaries/css.dict \
      > /out/CssHtmlFuzzer.dict
RUN cat fuzzing/dictionaries/xml.dict \
        fuzzing/dictionaries/xpath.dict \
      > /out/XmlFuzzer.dict
RUN cp fuzzing/dictionaries/css.dict /out/SelectorFuzzer.dict
RUN rm -rf fuzzing

RUN mkdir /corpus-xml
RUN git clone --depth 1 https://github.com/dvyukov/go-fuzz-corpus && \
    mv go-fuzz-corpus/xml/corpus/* /corpus-xml/ && rm -rf go-fuzz-corpus

RUN git clone https://github.com/jhy/jsoup jsoup

COPY build.sh /jsoup/build.sh
COPY HtmlFuzzer.java XmlFuzzer.java CleanFuzzer.java SelectorFuzzer.java \
     CssHtmlFuzzer.java FragmentHtmlFuzzer.java FuzzMerged.java /src/

ENV CASR_SOURCE_DIRS /jsoup/src/main/java/

WORKDIR /jsoup
RUN git checkout 19c758e2e4abc6a1f52193ceab09c22a7c010f8b
RUN mkdir -p /root/.m2 && echo '<settings xmlns="http://apache.org" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://apache.org https://apache.org"><mirrors><mirror><id>central-mirror-fix</id><name>Redirect broken Sonatype Grid to Central</name><url>https://repo.maven.apache.org/maven2/</url><mirrorOf>rso-public-grid</mirrorOf></mirror></mirrors></settings>' > /root/.m2/settings.xml
RUN bash /jsoup/build.sh
