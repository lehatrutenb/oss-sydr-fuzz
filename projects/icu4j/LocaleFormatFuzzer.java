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
import java.util.Date;

import com.ibm.icu.text.DateTimePatternGenerator;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.text.PluralRules;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.ULocale;

public class LocaleFormatFuzzer {
  private static final int APIS = 6;

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
          ULocale locale = ULocale.forLanguageTag(payload);
          locale.toLanguageTag();
          locale.getDisplayName();
          locale.getBaseName();
          ULocale.createCanonical(payload);
          break;
        }
        case 1: {
          MessageFormat format = new MessageFormat(payload, LOCALE);
          format.format(new Object[] {Integer.valueOf(1), "x", new Date(0L)});
          format.toPattern();
          break;
        }
        case 2: {
          PluralRules rules = PluralRules.createRules(payload);
          if (rules != null) {
            rules.getKeywords();
            rules.select(1.0d);
            rules.toString();
          }
          PluralRules.parseDescription(payload);
          break;
        }
        case 3: {
          DecimalFormat format = new DecimalFormat(payload);
          format.format(1234.5678d);
          format.toPattern();
          format.parse("1,234.57");
          break;
        }
        case 4: {
          SimpleDateFormat format = new SimpleDateFormat(payload, LOCALE);
          format.format(new Date(0L));
          format.toPattern();
          format.parse("2026-01-01");
          break;
        }
        default: {
          DateTimePatternGenerator generator = DateTimePatternGenerator.getInstance(LOCALE);
          generator.getBestPattern(payload);
          generator.addPattern(payload, false, null);
          break;
        }
      }
    } catch (ParseException | IllegalArgumentException | IllegalStateException
             | UnsupportedOperationException | IndexOutOfBoundsException
             | NullPointerException e) {
      return;
    } catch (AssertionError e) {
      // ICU's AffixPatternMatcher.consumeToken has an explicit
      // `throw new AssertionError()` default branch that a fuzzed
      // DecimalFormat pattern reaches. Tolerate exactly that type.
      if (e.getClass() != AssertionError.class) {
        throw e;
      }
      return;
    }
  }
}
