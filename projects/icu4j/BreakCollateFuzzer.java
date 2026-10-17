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
import java.text.ParseException;

import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.RuleBasedBreakIterator;
import com.ibm.icu.text.RuleBasedCollator;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.util.ULocale;

public class BreakCollateFuzzer {
  private static final int APIS = 4;

  private static final ULocale LOCALE = ULocale.US;

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

    int api = data.charAt(0) % APIS;
    String payload = data.substring(1);

    try {
      switch (api) {
        case 0: {
          BreakIterator[] iterators = {
              BreakIterator.getCharacterInstance(LOCALE),
              BreakIterator.getWordInstance(LOCALE),
              BreakIterator.getLineInstance(LOCALE),
              BreakIterator.getSentenceInstance(LOCALE),
          };
          for (BreakIterator iterator : iterators) {
            iterator.setText(payload);
            int count = 0;
            for (int at = iterator.first(); at != BreakIterator.DONE; at = iterator.next()) {
              count++;
              if (count > 100000) {
                break;
              }
            }
            iterator.last();
            iterator.previous();
          }
          break;
        }
        case 1: {
          RuleBasedCollator collator = new RuleBasedCollator(payload);
          collator.getRules();
          collator.compare("a", "b");
          collator.getCollationKey("abc");
          break;
        }
        case 2: {
          RuleBasedNumberFormat format = new RuleBasedNumberFormat(payload);
          format.format(1234L);
          format.parse("one");
          break;
        }
        default: {
          RuleBasedBreakIterator iterator = new RuleBasedBreakIterator(payload);
          iterator.setText("abc def. ghi");
          for (int at = iterator.first(); at != BreakIterator.DONE; at = iterator.next()) {
            iterator.getRuleStatus();
          }
          break;
        }
      }
    } catch (ParseException | IllegalArgumentException | IllegalStateException
             | UnsupportedOperationException | IndexOutOfBoundsException
             | NullPointerException e) {
      return;
    } catch (RuntimeException e) {
      if (e.getClass() != RuntimeException.class) {
        throw e;
      }
      return;
    } catch (Exception e) {
      // RuleBasedCollator(String) declares a bare `throws Exception`, so the
      // checked surface has to be caught; unchecked types are handled above.
      return;
    }
  }
}
