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

/**
 * Scalars derived from the payload String.
 *
 * <p>These targets take a plain String: the first character selects the branch
 * and the rest is the fuzz input. The flags, lengths and counts they used to
 * pull from a FuzzedDataProvider come from here instead, so they still vary with
 * the input without a provider. Call sites pass an index so that successive uses
 * within one input differ from each other.
 *
 * <p>This is not a fuzzer-driven source: the fuzzer controls these values only
 * indirectly, through the payload they are derived from.
 */
final class StringInputs {
  private StringInputs() {}

  /** A flag derived from the payload character at the given index. */
  static boolean flagAt(String s, int i) {
    return !s.isEmpty() && (s.charAt(Math.floorMod(i, s.length())) & 1) != 0;
  }

  /** A long derived from the payload character at the given index. */
  static long longAt(String s, int i) {
    return s.isEmpty() ? 0L : (long) s.charAt(Math.floorMod(i, s.length())) * (i + 1L);
  }
}
