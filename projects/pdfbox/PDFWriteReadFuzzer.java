// Copyright 2023 Google LLC
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Builds a PDF from fuzzed text, saves it, reloads it and strips the text.
 *
 * <p>Adapted from the OSS-Fuzz {@code @FuzzTest}/{@code FuzzedDataProvider}
 * harness to a plain {@code String} entry point: the first character picks the
 * page count (1-10) and a Standard 14 font; the rest is drawn as page text.
 * Round-trip goes through an in-memory buffer instead of a temp file.
 */
public class PDFWriteReadFuzzer {
  public static void main(String[] args) {
      try {
        fuzzerTestOneInput(Files.readString(Path.of(args[0])));
      } catch (IOException e) {
        return;
      }
  }
  private static final FontName[] FONTS = FontName.values();

  public static void fuzzerTestOneInput(String data) {
    if (data == null || data.isEmpty()) {
      return;
    }
    int control = data.charAt(0);
    int pages = Math.floorMod(control, 10) + 1;
    FontName font = FONTS[Math.floorMod(control, FONTS.length)];
    String line = data.substring(1);
    if (line.length() > 10000) {
      line = line.substring(0, 10000);
    }

    try (PDDocument doc = new PDDocument()) {
      for (int i = 0; i < pages; ++i) {
        PDPage myPage = new PDPage();
        doc.addPage(myPage);
        try (PDPageContentStream cont = new PDPageContentStream(doc, myPage)) {
          cont.beginText();
          cont.setFont(new PDType1Font(font), 12);
          cont.setLeading(14f);
          cont.newLineAtOffset(50, 700);
          cont.showText(line);
          cont.newLine();
          cont.endText();
        }
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      doc.save(baos);

      try (PDDocument loaded = Loader.loadPDF(baos.toByteArray())) {
        new PDFTextStripper().getText(loaded);
      }
    } catch (IOException | IllegalArgumentException e) {
    }
  }
}
