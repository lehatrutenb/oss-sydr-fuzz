// Copyright 2025 ISP RAS
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


/**
 * Every Commons IO fuzz target behind a single entry point.
 *
 * <p>Takes the same plain String the individual targets take. The first
 * character selects the target and the rest is the fuzz input, so a seed for any
 * one target still works here once a selector character is prepended.
 */

public class FuzzMerged {
  public static void main(String[] args) {
      try {
        fuzzerTestOneInput(Files.readString(Path.of(args[0])));
      } catch (IOException e) {
        return;
      }
  }
  private static final int TARGETS = 10;

  /**
   * Jazzer only calls fuzzerInitialize on the class named by --target_class, so
   * the per-target hooks have to be chained from here. Five of these targets set
   * up temporary files there and dereference them unguarded, so without this
   * they throw NullPointerException on their first input.
   */
  public static void fuzzerInitialize() {
    FileComparatorFuzzer.fuzzerInitialize();
    FileFilterFuzzer.fuzzerInitialize();
    FileUtilsFuzzer.fuzzerInitialize();
    PathUtilsFuzzer.fuzzerInitialize();
    WriterFuzzer.fuzzerInitialize();
  }

  public static void fuzzerTestOneInput(String data) {
    if (data == null || data.length() < 1) {
      return;
    }

    String payload = data.substring(1);

    switch (data.charAt(0) % TARGETS) {
      case 0:
        InputStreamFuzzer.fuzzerTestOneInput(payload);
        break;
      case 1:
        OutputStreamFuzzer.fuzzerTestOneInput(payload);
        break;
      case 2:
        ReaderFuzzer.fuzzerTestOneInput(payload);
        break;
      case 3:
        WriterFuzzer.fuzzerTestOneInput(payload);
        break;
      case 4:
        InputXmlFuzzer.fuzzerTestOneInput(payload);
        break;
      case 5:
        FileUtilsFuzzer.fuzzerTestOneInput(payload);
        break;
      case 6:
        PathUtilsFuzzer.fuzzerTestOneInput(payload);
        break;
      case 7:
        FileFilterFuzzer.fuzzerTestOneInput(payload);
        break;
      case 8:
        FileComparatorFuzzer.fuzzerTestOneInput(payload);
        break;
      case 9:
        GeneralUtilsFuzzer.fuzzerTestOneInput(payload);
        break;
      default:
        break;
    }
  }
}
