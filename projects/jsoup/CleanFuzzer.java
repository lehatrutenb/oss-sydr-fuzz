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
import com.code_intelligence.jazzer.api.FuzzerSecurityIssueHigh;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

public class CleanFuzzer {
  public static void main(String[] args) {
      try {
        fuzzerTestOneInput(Files.readString(Path.of(args[0])));
      } catch (IOException e) {
        return;
      }
  }
  private static final Safelist SAFELIST = Safelist.relaxed();

  // Tags that can execute script or pull in external content, none of which are
  // on the relaxed safelist. Surviving one is a sanitizer bypass.
  private static final Set<String> FORBIDDEN_TAGS =
      new HashSet<>(
          Arrays.asList(
              "script", "style", "iframe", "object", "embed", "base", "link", "meta", "form",
              "frame", "frameset"));

  public static void fuzzerTestOneInput(String data) {
    if (data == null) {
      return;
    }
    String output = Jsoup.clean(data, SAFELIST);

    // Re-parse the sanitized markup and inspect the resulting tree rather than
    // the string: text nodes and attribute values may legitimately contain
    // things like "javascript:" or "onload=", so a substring denylist over the
    // serialized output would report those as bypasses.
    Document cleaned = Jsoup.parseBodyFragment(output);
    for (Element el : cleaned.body().getAllElements()) {
      assert !FORBIDDEN_TAGS.contains(el.normalName())
          : new FuzzerSecurityIssueHigh("Sanitizer emitted <" + el.normalName() + ">");
      for (Attribute attr : el.attributes()) {
        assert !attr.getKey().toLowerCase(Locale.ROOT).startsWith("on")
            : new FuzzerSecurityIssueHigh("Sanitizer emitted event handler " + attr.getKey());
      }
    }
  }
}
