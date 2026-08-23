// Copyright 2025 Google LLC
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
import org.jsoup.helper.ValidationException;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

/**
 * Parses a fuzzed HTML fragment in the context of a fuzzed element name.
 *
 * <p>Adapted from the OSS-Fuzz {@code FuzzedDataProvider} harness: the first
 * character picks how many of the following characters are the context tag name
 * (0-20), and the remainder is the fragment.
 */
public class FragmentHtmlFuzzer {
  public static void main(String[] args) {
      try {
        fuzzerTestOneInput(Files.readString(Path.of(args[0])));
      } catch (IOException e) {
        return;
      }
  }
  public static void fuzzerTestOneInput(String data) {
    if (data == null || data.isEmpty()) {
      return;
    }
    int nameLen = Math.min(Math.floorMod(data.charAt(0), 21), data.length() - 1);
    String contextElName = data.substring(1, 1 + nameLen);
    String html = data.substring(1 + nameLen);
    try {
      Element contextEl = new Element(contextElName);
      Parser.htmlParser().parseFragmentInput(html, contextEl, "https://example.com/");
    } catch (ValidationException ignored) {
    }
  }
}
