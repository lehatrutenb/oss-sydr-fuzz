// Copyright 2026 ISP RAS
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

import java.nio.charset.StandardCharsets;

import dev.fuzzit.javafuzz.core.AbstractFuzzTarget;
import dev.fuzzit.javafuzz.core.Fuzzer;

public class TimeJavaFuzz extends AbstractFuzzTarget {
  public static void main(String[] args) throws Exception {
    String corpus = args.length > 0 ? args[0] : "corpus";
    new Fuzzer(new TimeJavaFuzz(), corpus).start();
  }

  public void fuzz(byte[] input) {
    if (input == null || input.length == 0) {
      return;
    }
    TimeFuzzer.fuzzerTestOneInput(new String(input, StandardCharsets.UTF_8));
  }
}
