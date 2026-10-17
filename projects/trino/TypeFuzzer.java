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

import io.trino.sql.parser.ParsingException;
import io.trino.sql.parser.SqlParser;
import io.trino.sql.tree.Node;

public class TypeFuzzer {
  private static final SqlParser PARSER = new SqlParser();
  private static final int PRODUCTIONS = 4;

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

    int production = data.charAt(0) % PRODUCTIONS;
    String payload = data.substring(1);

    Node node = parse(production, payload);
    if (node == null) {
      return;
    }

    node.toString();
    node.getChildren();
    node.hashCode();
    node.getLocation();

    Node reparsed = parse(production, payload);
    if (reparsed != null) {
      node.equals(reparsed);
      reparsed.hashCode();
    }
  }

  private static Node parse(int production, String payload) {
    try {
      switch (production) {
        case 0:
          return PARSER.createType(payload);
        case 1:
          return PARSER.createPathSpecification(payload);
        case 2:
          return PARSER.createRowPattern(payload);
        default:
          return PARSER.createFunctionSpecification(payload);
      }
    } catch (ParsingException | IllegalArgumentException | UnsupportedOperationException
             | IndexOutOfBoundsException e) {
      return null;
    }
  }
}
