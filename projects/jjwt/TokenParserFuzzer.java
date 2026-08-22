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

import java.io.StringReader;
import java.io.IOException;
import java.lang.AssertionError;
import java.nio.file.Files;
import java.nio.file.Path;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

public class TokenParserFuzzer {
    public static void main(String[] args) {
        try {
            fuzzerTestOneInput(Files.readString(Path.of(args[0])));
        } catch (IOException e) {
            return;
        }
    }

    public static void fuzzerTestOneInput(String input) {
        if (input == null || input.isEmpty() || input.isBlank()) {
            return;
        }
        Jwt<?,?> jwt;
        try {
            jwt = Jwts.parser().build().parse(input);
        } catch (JwtException ex) {}
    }
}