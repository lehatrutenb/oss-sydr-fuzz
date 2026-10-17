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

import ognl.Ognl;
import ognl.OgnlException;

public class ExpressionParseFuzzer {
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

    Object tree;
    try {
      tree = Ognl.parseExpression(data);
    } catch (OgnlException | IllegalArgumentException | IllegalStateException
             | UnsupportedOperationException | IndexOutOfBoundsException
             | NullPointerException e) {
      return;
    } catch (Error e) {
      // ognl.JavaCharStream throws a bare java.lang.Error on a malformed
      // unicode escape. Tolerate exactly that type; every Error subclass
      // (OutOfMemoryError, StackOverflowError) still propagates.
      if (e.getClass() != Error.class) {
        throw e;
      }
      return;
    }

    tree.toString();
    tree.hashCode();
  }
}
