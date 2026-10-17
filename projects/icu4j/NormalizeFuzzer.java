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

import com.ibm.icu.text.IDNA;
import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.StringPrep;
import com.ibm.icu.text.StringPrepParseException;
import com.ibm.icu.text.Transliterator;
import com.ibm.icu.text.UnicodeSet;

public class NormalizeFuzzer {
  private static final int APIS = 5;

  private static final Normalizer2[] NORMALIZERS = {
      Normalizer2.getNFCInstance(),
      Normalizer2.getNFDInstance(),
      Normalizer2.getNFKCInstance(),
      Normalizer2.getNFKDInstance(),
      Normalizer2.getNFKCCasefoldInstance(),
  };

  private static final IDNA UTS46 = IDNA.getUTS46Instance(IDNA.NONTRANSITIONAL_TO_ASCII);

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
          for (Normalizer2 normalizer : NORMALIZERS) {
            String normalized = normalizer.normalize(payload);
            normalizer.isNormalized(normalized);
            normalizer.hasBoundaryBefore(payload.charAt(0));
          }
          break;
        }
        case 1: {
          UnicodeSet set = new UnicodeSet(payload);
          set.size();
          set.toPattern(true);
          set.complement();
          break;
        }
        case 2: {
          Transliterator transliterator =
              Transliterator.createFromRules("fuzz", payload, Transliterator.FORWARD);
          transliterator.transliterate("abc");
          transliterator.toRules(false);
          break;
        }
        case 3: {
          IDNA.Info info = new IDNA.Info();
          UTS46.nameToASCII(payload, new StringBuilder(), info);
          info.hasErrors();
          UTS46.nameToUnicode(payload, new StringBuilder(), new IDNA.Info());
          UTS46.labelToASCII(payload, new StringBuilder(), new IDNA.Info());
          break;
        }
        default: {
          StringPrep prep = StringPrep.getInstance(StringPrep.RFC3491_NAMEPREP);
          prep.prepare(payload, StringPrep.DEFAULT);
          break;
        }
      }
    } catch (StringPrepParseException | IllegalArgumentException | IllegalStateException
             | UnsupportedOperationException | IndexOutOfBoundsException
             | NullPointerException e) {
      return;
    } catch (RuntimeException e) {
      // Transliterator.filteredTransliterate throws a bare
      // java.lang.RuntimeException ("Incomplete non-incremental
      // transliteration") on some fuzzed rule sets. Tolerate exactly that
      // type; every subclass still propagates.
      if (e.getClass() != RuntimeException.class) {
        throw e;
      }
      return;
    }
  }
}
