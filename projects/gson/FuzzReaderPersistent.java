// Copyright 2021 Google LLC
// Modifications copyright (C) 2026 ISP RAS
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

public class FuzzReaderPersistent {
  public static String[] args;

  public static void main(String[] args) {
    FuzzReaderPersistent.args = args;
    fuzzerTestOneInput("");
  }

  public static void fuzzerTestOneInput(String _input) {
    for (String path : FuzzReaderPersistent.args) {
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
        FuzzReader.fuzzerTestOneInput(input);
      } finally {
        PersistentMode.dumpAndCleanSymbolicState();
      }
    }
  }
}
