// Copyright 2021 Google LLC
// Modifications copyright (C) 2025 ISP RAS
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.json.JsonSanitizer;

public class IdempotenceFuzzer {
  public static void main(String[] args) {
      try {
        fuzzerTestOneInput(Files.readString(Path.of(args[0])));
      } catch (IOException e) {
        return;
      }
  }
  public static void fuzzerTestOneInput(String input) {
    String output;
    try {
      output = JsonSanitizer.sanitize(input, 10);
    } catch (ArrayIndexOutOfBoundsException e) {
      // ArrayIndexOutOfBoundsException is expected if nesting depth is
      // exceeded.
      return;
    }

    // Ensure that sanitizing twice does not give different output
    // (idempotence). Since failure to be idempotent is not a security issue in
    // itself, fail with a regular AssertionError.
    assert JsonSanitizer.sanitize(output).equals(output) : "Not idempotent";
  }
}
