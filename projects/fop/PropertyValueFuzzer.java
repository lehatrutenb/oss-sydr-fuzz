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

import org.apache.xmlgraphics.util.MimeConstants;

public class PropertyValueFuzzer {
  private static final String[] PROPERTIES = {
      "font-size",
      "font-family",
      "font",
      "color",
      "background-color",
      "background-position",
      "margin",
      "padding-top",
      "border",
      "border-before-width.conditionality",
      "space-before.optimum",
      "line-height",
      "text-align",
      "writing-mode",
      "width",
      "keep-together.within-page",
      "letter-spacing",
      "baseline-shift",
      "wrap-option",
      "white-space-treatment",
  };

  public static void main(String[] args) {
      try {
        fuzzerTestOneInput(Files.readString(Path.of(args[0])));
      } catch (IOException e) {
        return;
      }
  }

  public static void fuzzerTestOneInput(String data) {
    if (data == null || data.length() < 2) {
      return;
    }

    String property = PROPERTIES[data.charAt(0) % PROPERTIES.length];
    String value = data.substring(1);

    // An XML attribute value cannot contain a raw quote, angle bracket or
    // ampersand; escaping them here keeps the document well formed so the
    // fuzzer reaches the property parser rather than the XML parser.
    String escaped = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");

    String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"
        + "<fo:layout-master-set>"
        + "<fo:simple-page-master master-name=\"p\" page-height=\"200pt\" page-width=\"200pt\">"
        + "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
        + "<fo:page-sequence master-reference=\"p\"><fo:flow flow-name=\"xsl-region-body\">"
        + "<fo:block " + property + "=\"" + escaped + "\">text</fo:block>"
        + "</fo:flow></fo:page-sequence></fo:root>";

    try {
      FopSupport.render(fo, MimeConstants.MIME_PDF);
    } catch (Exception e) {
      return;
    }
  }
}
