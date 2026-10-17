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
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;

import de.uzl.its.swat.templates.PersistentMode;

public class FuzzMergedPersistent {
  public static String[] args;

  private static final int TARGETS = 8;

  private static final Gson GSON = new Gson();

  private static final FieldNamingPolicy[] NAMING = FieldNamingPolicy.values();

  private static final LongSerializationPolicy[] LONGS = LongSerializationPolicy.values();

  private static final String[] DATE_FORMATS = {
    "yyyy-MM-dd", "MMM d, yyyy", "EEE, d MMM yyyy HH:mm:ss Z", "yyyy-MM-dd'T'HH:mm:ss",
  };

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
        FuzzParse.fuzzerTestOneInput(payload);
        break;
      case 1:
        FuzzReader.fuzzerTestOneInput(payload);
        break;
      case 2:
        FuzzStreamParser.fuzzerTestOneInput(payload);
        break;
      case 3:
        bean(payload);
        break;
      case 4:
        containers(payload);
        break;
      case 5:
        configured(payload);
        break;
      case 6:
        tree(payload);
        break;
      case 7:
        builtins(payload);
        break;
      default:
        break;
    }
  }

  /** Reflective deserialization: ConstructorConstructor plus the bound-field machinery. */
  private static void bean(String data) {
    FuzzBean parsed;
    try {
      parsed = GSON.fromJson(data, FuzzBean.class);
    } catch (JsonParseException | NumberFormatException | IllegalStateException e) {
      return;
    }

    if (parsed == null) {
      return;
    }

    try {
      GSON.toJson(parsed);
      GSON.toJsonTree(parsed);
    } catch (JsonParseException | IllegalStateException e) {
      return;
    }
  }

  /**
   * Generic containers, built with TypeToken.getParameterized rather than an
   * anonymous TypeToken subclass: build.sh copies only {@code <Fuzzer>.class}, so
   * a synthetic inner class would never reach /out.
   */
  private static void containers(String data) {
    Type[] types = {
      TypeToken.getParameterized(List.class, FuzzBean.class).getType(),
      TypeToken.getParameterized(Map.class, String.class, FuzzBean.class).getType(),
      TypeToken.getParameterized(Map.class, String.class,
          TypeToken.getParameterized(List.class, Integer.class).getType()).getType(),
      TypeToken.getArray(FuzzBean.class).getType(),
      TypeToken.getArray(int.class).getType(),
      TypeToken.get(Object.class).getType(),
      TypeToken.getParameterized(List.class, Object.class).getType(),
    };

    for (Type type : types) {
      Object parsed;
      try {
        parsed = GSON.fromJson(data, type);
      } catch (JsonParseException | NumberFormatException | IllegalStateException e) {
        continue;
      }
      if (parsed == null) {
        continue;
      }
      try {
        GSON.toJson(parsed, type);
      } catch (JsonParseException | IllegalStateException e) {
        continue;
      }
    }
  }

  /** The GsonBuilder surface: each knob changes which adapters get installed. */
  private static void configured(String data) {
    if (data.length() < 2) {
      return;
    }

    int knobs = data.charAt(0);
    String payload = data.substring(1);

    GsonBuilder builder = new GsonBuilder();
    builder.setFieldNamingPolicy(NAMING[knobs % NAMING.length]);
    builder.setLongSerializationPolicy(LONGS[(knobs >> 1) % LONGS.length]);
    if ((knobs & 1) != 0) {
      builder.serializeNulls();
    }
    if ((knobs & 2) != 0) {
      builder.setPrettyPrinting();
    }
    if ((knobs & 4) != 0) {
      builder.disableHtmlEscaping();
    }
    if ((knobs & 8) != 0) {
      builder.serializeSpecialFloatingPointValues();
    }
    if ((knobs & 16) != 0) {
      builder.excludeFieldsWithoutExposeAnnotation();
    }
    if ((knobs & 32) != 0) {
      builder.generateNonExecutableJson();
    }
    if ((knobs & 64) != 0) {
      builder.enableComplexMapKeySerialization();
    }
    if ((knobs & 128) != 0) {
      builder.disableInnerClassSerialization();
    }
    try {
      builder.setDateFormat(DATE_FORMATS[(knobs >> 2) % DATE_FORMATS.length]);
    } catch (IllegalArgumentException e) {
      return;
    }

    Gson gson = builder.create();

    FuzzBean parsed;
    try {
      parsed = gson.fromJson(payload, FuzzBean.class);
    } catch (JsonParseException | NumberFormatException | IllegalStateException e) {
      return;
    }

    if (parsed == null) {
      return;
    }

    try {
      String written = gson.toJson(parsed);
      gson.fromJson(written, FuzzBean.class);
    } catch (JsonParseException | NumberFormatException | IllegalStateException e) {
      return;
    }
  }

  /** The tree path: JsonTreeReader and JsonTreeWriter, plus a print/re-parse round trip. */
  private static void tree(String data) {
    JsonElement element;
    try {
      element = JsonParser.parseString(data);
    } catch (JsonParseException | IllegalStateException e) {
      return;
    }

    String written;
    try {
      written = GSON.toJson(element);
    } catch (JsonParseException | IllegalStateException e) {
      return;
    }

    JsonElement reparsed;
    try {
      reparsed = JsonParser.parseString(written);
    } catch (JsonParseException | IllegalStateException e) {
      return;
    }

    if (!element.equals(reparsed)) {
      return;
    }

    try {
      GSON.fromJson(element, FuzzBean.class);
      GSON.fromJson(element, Object.class);
      element.deepCopy();
      element.hashCode();
    } catch (JsonParseException | NumberFormatException | IllegalStateException e) {
      return;
    }
  }

  /** One document against every built-in adapter at once. */
  private static void builtins(String data) {
    FuzzTypes parsed;
    try {
      parsed = GSON.fromJson(data, FuzzTypes.class);
    } catch (JsonParseException | IllegalStateException | IllegalArgumentException e) {
      return;
    }

    if (parsed == null) {
      return;
    }

    try {
      GSON.toJson(parsed);
    } catch (JsonParseException | IllegalStateException
             | IllegalArgumentException | UnsupportedOperationException e) {
      return;
    }
  }
}
