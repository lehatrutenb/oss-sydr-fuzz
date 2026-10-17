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

import org.apache.fop.apps.MimeConstants;

public class OutputFormatFuzzer {
  private static final String[] FORMATS = {
      MimeConstants.MIME_POSTSCRIPT,
      MimeConstants.MIME_PCL,
      MimeConstants.MIME_AFP,
      MimeConstants.MIME_PLAIN_TEXT,
      MimeConstants.MIME_RTF,
      MimeConstants.MIME_FOP_AREA_TREE,
      MimeConstants.MIME_FOP_IF,
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

    String format = FORMATS[data.charAt(0) % FORMATS.length];
    String fo = data.substring(1);

    try {
      FopSupport.render(fo, format);
    } catch (Exception e) {
      return;
    }
  }
}
