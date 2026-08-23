// Copyright 2023 Google LLC
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
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.time.Duration;
import org.apache.commons.io.ByteOrderParser;
import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.EndianUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.ThreadUtils;

/** This fuzzer targets the static methods of the Utils classes in the base package. */
public class GeneralUtilsFuzzer {
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
    int selector = data.charAt(0);
    String payload = data.substring(1);
    byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
    try {
      byte[] outArray = new byte[payload.length()];
      ByteArrayOutputStream baos = new ByteArrayOutputStream(payload.length());
      switch (Math.floorMod(selector, 24) + 1) {
        case 1:
          ByteOrderParser.parseByteOrder(payload);
          break;
        case 2:
          CopyUtils.copy(
              payload, baos, Charset.defaultCharset().displayName());
          break;
        case 3:
          EndianUtils.readSwappedDouble(bytes, 0);
          break;
        case 4:
          EndianUtils.readSwappedFloat(bytes, 0);
          break;
        case 5:
          EndianUtils.readSwappedShort(bytes, 0);
          break;
        case 6:
          IOUtils.consume(new ByteArrayInputStream(bytes));
          break;
        case 7:
          byte[] equals = bytes;
          IOUtils.contentEquals(new ByteArrayInputStream(equals), new ByteArrayInputStream(equals));
          break;
        case 8:
          IOUtils.copy(new ByteArrayInputStream(bytes), baos);
          break;
        case 9:
          IOUtils.read(
              new ByteArrayInputStream(bytes),
              outArray,
              0,
              outArray.length);
          break;
        case 10:
          IOUtils.readFully(new ByteArrayInputStream(bytes), outArray);
          break;
        case 11:
          IOUtils.resourceToByteArray(payload);
          break;
        case 12:
          IOUtils.resourceToString(payload, Charset.defaultCharset());
          break;
        case 13:
          Long skip = StringInputs.longAt(payload, 0);
          IOUtils.skip(new ByteArrayInputStream(bytes), skip);
          break;
        case 14:
          IOUtils.toByteArray(new ByteArrayInputStream(bytes));
          break;
        case 15:
          byte[] case15 = bytes;
          IOUtils.contentEquals(
              new InputStreamReader(new ByteArrayInputStream(case15)),
              new InputStreamReader(new ByteArrayInputStream(case15)));
          break;
        case 16:
          byte[] case16 = bytes;
          IOUtils.contentEqualsIgnoreEOL(
              new InputStreamReader(new ByteArrayInputStream(case16)),
              new InputStreamReader(new ByteArrayInputStream(case16)));
          break;
        case 17:
          IOUtils.copy(
              new ByteArrayInputStream(bytes),
              new OutputStreamWriter(baos),
              Charset.defaultCharset().name());
          break;
        case 18:
          IOUtils.copy(
              new InputStreamReader(new ByteArrayInputStream(bytes)),
              new OutputStreamWriter(baos));
          break;
        case 19:
          IOUtils.copyLarge(
              new ByteArrayInputStream(bytes), baos, 0, outArray.length);
          break;
        case 20:
          IOUtils.copyLarge(
              new InputStreamReader(new ByteArrayInputStream(bytes)),
              new OutputStreamWriter(baos),
              0,
              outArray.length);
          break;
        case 21:
          IOUtils.readFully(
              new ByteArrayInputStream(bytes), outArray.length);
          break;
        case 22:
          IOUtils.readLines(
              new ByteArrayInputStream(bytes), Charset.defaultCharset());
          break;
        case 23:
          Long skip23 = StringInputs.longAt(payload, 1);
          IOUtils.skip(
              new InputStreamReader(new ByteArrayInputStream(bytes)),
              skip23);
          break;
        case 24:
          ThreadUtils.sleep(Duration.ofSeconds(Math.floorMod(payload.hashCode(), 11) - 5));
          break;
      }
    } catch (IOException | IllegalArgumentException | InterruptedException e) {
      // Known exception
    }
  }

}
