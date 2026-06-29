# janino 

Janino is a super-small, super-fast Java compiler. 

## Build Docker

sudo docker build --build-arg BASE_IMAGE=swat-playground-22 -t oss-sydr-fuzz-janino 

    $ sudo docker build -t oss-sydr-fuzz-janino .

## Run Hybrid Fuzzing

Unzip Sydr (`sydr.zip`) in `projects/janino` directory:

    $ unzip sydr.zip

Run Docker:

    $ sudo docker run --cap-add=SYS_PTRACE  --security-opt seccomp=unconfined -v /etc/localtime:/etc/localtime:ro --rm -it -v $PWD:/fuzz oss-sydr-fuzz-janino /bin/bash

Change directory to `/fuzz`:

    # cd /fuzz

Run fuzzing:

    # sydr-fuzz -c ExpressionEvaluatorFuzzer.toml run

Minimize corpus:

    # sydr-fuzz -c ExpressionEvaluatorFuzzer.toml cmin 

Collect and report coverage:

    # sydr-fuzz -c ExpressionEvaluatorFuzzer.toml cov-html -s /janino/janino/src/main/java
