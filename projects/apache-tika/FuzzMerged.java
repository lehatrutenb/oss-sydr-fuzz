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
 * Every Tika fuzz target behind a single entry point.
 *
 * <p>Takes a String: the first character selects the target and the rest is the
 * payload passed through to the selected harness.
 */
public class FuzzMerged {
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
  private static final int TARGETS = 15;

  public static void fuzzerTestOneInput(String data) {
    if (data == null || data.length() < 1) {
      return;
    }

    String payload = data.substring(1);

    switch (data.charAt(0) % TARGETS) {
      case 0:
        //AutoDetectParserFuzzer.fuzzerTestOneInput(payload);
        break;
      case 1:
        //AudioVideoParsersFuzzer.fuzzerTestOneInput(payload);
        break;
      case 2:
        CompressorParserFuzzer.fuzzerTestOneInput(payload);
        break;
      case 3:
        HtmlParserFuzzer.fuzzerTestOneInput(payload);
        break;
      case 4:
        ImageParsersFuzzer.fuzzerTestOneInput(payload);
        break;
      case 5:
        JackcessParserFuzzer.fuzzerTestOneInput(payload);
        break;
      case 6:
        OOXMLParserFuzzer.fuzzerTestOneInput(payload);
        break;
      case 7:
        OfficeParserFuzzer.fuzzerTestOneInput(payload);
        break;
      case 8:
        OneNoteParserFuzzer.fuzzerTestOneInput(payload);
        break;
      case 9:
        PDFParserFuzzer.fuzzerTestOneInput(payload);
        break;
      case 10:
        PackageParserFuzzer.fuzzerTestOneInput(payload);
        break;
      case 11:
        RFC822ParserFuzzer.fuzzerTestOneInput(payload);
        break;
      case 12:
        RTFParserFuzzer.fuzzerTestOneInput(payload);
        break;
      case 13:
        TextAndCSVParserFuzzer.fuzzerTestOneInput(payload);
        break;
      case 14:
        XMLReaderUtilsFuzzer.fuzzerTestOneInput(payload);
        break;
      default:
        break;
    }
  }
}
