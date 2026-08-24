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
import java.nio.charset.StandardCharsets;


/**
 * Every Commons Compress fuzz target behind a single entry point.
 *
 * <p>Takes the same plain String the individual targets take. The first
 * character selects the target and the rest is the fuzz input, so a seed for any
 * one target still works here once a selector character is prepended.
 */

public class FuzzMerged extends BaseTests {
  public static String normString(String input) {
      if (input == null) return null;
      return input.codePoints()
          .filter(cp -> cp <= 0x2FFFF)
          .collect(StringBuilder::new, 
              StringBuilder::appendCodePoint, 
              StringBuilder::append)
          .toString();
  }

  public static void main(String[] args) {
      try {
        fuzzerTestOneInput(normString(new String(Files.readAllBytes(Path.of(args[0])), StandardCharsets.UTF_8)));
      } catch (IOException e) {
        return;
      }
  }
  private static final int TARGETS = 16;

  public static void fuzzerTestOneInput(String data) {
    if (data == null || data.length() < 1) {
      return;
    }

    String payload = data.substring(1);

    switch (data.charAt(0) % TARGETS) {
      case 0:
        ArchiverArFuzzer.fuzzerTestOneInput(payload);
        break;
      case 1:
        //ArchiverArjFuzzer.fuzzerTestOneInput(payload);
        break;
      case 2:
        ArchiverCpioFuzzer.fuzzerTestOneInput(payload);
        break;
      case 3:
        ArchiverDumpFuzzer.fuzzerTestOneInput(payload);
        break;
      case 4:
        ArchiverTarStreamFuzzer.fuzzerTestOneInput(payload);
        break;
      case 5:
        ArchiverZipStreamFuzzer.fuzzerTestOneInput(payload);
        break;
      case 6:
        CompressSevenZFuzzer.fuzzerTestOneInput(payload);
        break;
      case 7:
        CompressTarFuzzer.fuzzerTestOneInput(payload);
        break;
      case 8:
        CompressZipFuzzer.fuzzerTestOneInput(payload);
        break;
      case 9:
        CompressorBZip2Fuzzer.fuzzerTestOneInput(payload);
        break;
      case 10:
        CompressorDeflate64Fuzzer.fuzzerTestOneInput(payload);
        break;
      case 11:
        CompressorGzipFuzzer.fuzzerTestOneInput(payload);
        break;
      case 12:
        CompressorLZ4Fuzzer.fuzzerTestOneInput(payload);
        break;
      case 13:
        CompressorPack200Fuzzer.fuzzerTestOneInput(payload);
        break;
      case 14:
        CompressorSnappyFuzzer.fuzzerTestOneInput(payload);
        break;
      case 15:
        CompressorZFuzzer.fuzzerTestOneInput(payload);
        break;
      default:
        break;
    }
  }
}
