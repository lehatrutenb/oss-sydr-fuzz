// Copyright 2026 ISP RAS
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

import de.uzl.its.swat.templates.PersistentMode;

public class FuzzMergedPersistent {
  public static String[] args;

  private static final int TARGETS = 3;

  public static void main(String[] args) {
    FuzzMergedPersistent.args = args;
    fuzzerTestOneInput("");
  }

  public static void fuzzerTestOneInput(String data) {
    if (FuzzMergedPersistent.args == null) {
      dispatch(data);
      return;
    }

    for (String path : FuzzMergedPersistent.args) {
      String input;
      try {
        input = PersistentMode.readString(path, true);
      } catch (IOException e) {
        continue;
      }
      PersistentMode.MakeSymbolic(input);
      if (input == null || input.isEmpty()) {
        continue;
      }
      try {
        dispatch(input);
      } finally {
        PersistentMode.dumpAndCleanSymbolicState();
      }
    }
  }

  private static void dispatch(String data) {
    if (data == null || data.length() < 1) {
      return;
    }

    String payload = data.substring(1);

    switch (data.charAt(0) % TARGETS) {
      case 0:
        FoDocumentFuzzer.fuzzerTestOneInput(payload);
        break;
      case 1:
        PropertyValueFuzzer.fuzzerTestOneInput(payload);
        break;
      case 2:
        OutputFormatFuzzer.fuzzerTestOneInput(payload);
        break;
      default:
        break;
    }
  }
}
