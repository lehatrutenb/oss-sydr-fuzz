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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;

public class ResultParserFuzzer {
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

    BarcodeFormat[] formats = BarcodeFormat.values();
    BarcodeFormat format = formats[data.charAt(0) % formats.length];
    String contents = data.substring(1);

    try {
      ParsedResult parsed = ResultParser.parseResult(new Result(contents, null, null, format));
      parsed.getType();
      parsed.getDisplayResult();
      parsed.toString();
    } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
      return;
    }
  }
}
