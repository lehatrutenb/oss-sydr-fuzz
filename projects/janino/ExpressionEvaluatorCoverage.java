// Copyright 2022 Google LLC
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
import java.io.StringReader;
import java.io.IOException;
import java.lang.AssertionError;
import java.nio.file.Files;
import java.nio.file.Path;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.ScriptEvaluator;

public class ExpressionEvaluatorCoverage {
  public static void main(String[] args) throws Exception {
      if (Path.of(args[0]).toFile().isFile()) {
        try {
            fuzz(Files.readAllBytes(Path.of(args[0])));
        } catch (Throwable ignored) {
            // One crash should not stop the rest of the corpus.
        }
        return;
      }
      try (var files = Files.list(Path.of(args[0]))) {
          files.filter(Files::isRegularFile).forEach(path -> {
              try {
                  fuzz(Files.readAllBytes(path));
              } catch (Throwable ignored) {
                  // One crash should not stop the rest of the corpus,
              }
          });
      }
  }
  public static void fuzz(byte[] input) {
    if (input == null || input.length == 0) {
      return;
    }
    try {
      ExpressionEvaluator.guessParameterNames(new Scanner(null, new StringReader(input.toString())));
    } catch(IOException | CompileException | AssertionError e) {
      return;
    }
  }
}
