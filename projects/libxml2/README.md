# libxml2

libxml2 is an XML toolkit implemented in C, originally developed for the GNOME Project.

## Build Docker

    $ sudo docker build -t oss-sydr-fuzz-libxml2 .

## Build LibAFL-DiFuzz Docker

Pass `sydr.zip` as an argument:

    $ sudo docker build --build-arg SYDR_ARCHIVE="sydr.zip" -t oss-sydr-fuzz-libafl-libxml2 -f ./Dockerfile_libafl .

## Run Hybrid Fuzzing

Unzip Sydr (`sydr.zip`) in `projects/libxml2` directory:

    $ unzip sydr.zip

Run docker:

    $ sudo docker run --cap-add=SYS_PTRACE  --security-opt seccomp=unconfined -v /etc/localtime:/etc/localtime:ro --rm -it -v $PWD:/fuzz oss-sydr-fuzz-libxml2 /bin/bash

Run docker for LibAFL-DiFuzz:

    $ sudo docker run --cap-add=SYS_PTRACE  --security-opt seccomp=unconfined -v /etc/localtime:/etc/localtime:ro --rm -it -v $PWD:/fuzz oss-sydr-fuzz-libafl-libxml2 /bin/bash

Change directory to `/fuzz`:

    # cd /fuzz

Run hybrid fuzzing with libfuzzer:

    # sydr-fuzz -c fuzz_xml.toml run

Run hybrid fuzzing with afl++:

    # sydr-fuzz -c fuzz_xml-afl++.toml run

Run hybrid fuzzing with LibAFL-DiFuzz:

    # sydr-fuzz -c libxml-libafl.toml run

Minimize corpus:

    # sydr-fuzz -c fuzz_xml.toml cmin

Collect coverage:

    # sydr-fuzz -c fuzz_xml.toml cov-export -- -format=lcov > xml.lcov
    # genhtml -o xml xml.lcov

Check security predicates:

    # sydr-fuzz -c fuzz_xml.toml security

## Supported Targets

    * xml
    * html
    * uri
    * regexp
    * schema
    * xinclude
    * xpath