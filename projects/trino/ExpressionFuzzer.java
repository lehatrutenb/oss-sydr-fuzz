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

import io.trino.sql.ExpressionFormatter;
import io.trino.sql.parser.ParsingException;
import io.trino.sql.parser.SqlParser;
import io.trino.sql.tree.Expression;

public class ExpressionFuzzer {
  private static final SqlParser PARSER = new SqlParser();

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

    Expression expression = parse(data);
    if (expression == null) {
      return;
    }

    expression.toString();
    expression.getChildren();
    String formatted;
    try {
      formatted = ExpressionFormatter.formatExpression(expression);
    } catch (UnsupportedOperationException e) {
      return;
    }
    Expression reparsed = parse(formatted);
    if (reparsed != null) {
      reparsed.hashCode();
      expression.equals(reparsed);
    }
  }

  private static Expression parse(String sql) {
    try {
      return PARSER.createExpression(sql);
    } catch (ParsingException | IllegalArgumentException | UnsupportedOperationException
             | IndexOutOfBoundsException e) {
      return null;
    }
  }
}
