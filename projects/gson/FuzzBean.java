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

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FuzzBean {
  @SerializedName("renamed")
  String named;

  @SerializedName(value = "primary", alternate = {"secondary", "tertiary"})
  String alternates;

  @Expose
  String exposed;

  @Expose(serialize = false, deserialize = true)
  String deserializeOnly;

  transient String skipped;

  static String statik;

  private String privateField;

  boolean flag;
  Boolean boxedFlag;
  byte b;
  short s;
  int i;
  long l;
  float f;
  double d;
  char c;
  Integer boxedInt;
  Double boxedDouble;
  String text;
  int[] ints;
  String[] strings;
  List<String> list;
  List<FuzzInner> beans;
  Map<String, Integer> map;
  Map<String, List<String>> nestedMap;
  FuzzInner inner;
  FuzzColor color;
  Object any;

  String getPrivateField() {
    return privateField;
  }
}
