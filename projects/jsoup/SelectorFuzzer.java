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
import org.jsoup.Jsoup;
import org.jsoup.helper.ValidationException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Selector;

public class SelectorFuzzer {
  public static void main(String[] args) {
      try {
        fuzzerTestOneInput(Files.readString(Path.of(args[0])));
      } catch (IOException e) {
        return;
      }
  }
  // A fixed document to evaluate the fuzzed queries against, so that the CSS
  // query parser and the evaluators are what actually varies.
  private static final Document DOC =
      Jsoup.parse(
          "<html><head><title>t</title><link rel=stylesheet href=/s.css></head>"
              + "<body id=main class='a b'><div class=note data-x=1><p>one<span>two</span></p>"
              + "<a href='https://dummy.example/p?q=1' rel=nofollow>link</a>"
              + "<img src=i.png alt=''><ul><li>x<li>y<li>z</ul>"
              + "<table><tr><td>c1<td>c2</table><!-- c --></div>"
              + "<svg><circle r=1/></svg><form><input name=n value=v></form></body></html>");

  public static void fuzzerTestOneInput(String data) {
    if (data == null) {
      return;
    }
    try {
      DOC.select(data);
    } catch (Selector.SelectorParseException | ValidationException expected) {
      // Malformed queries are rejected by design.
    }
  }
}
