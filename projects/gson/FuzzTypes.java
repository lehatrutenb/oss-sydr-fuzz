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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/** One field per built-in TypeAdapter, so a single document reaches the whole bank. */
public class FuzzTypes {
  Date date;
  java.sql.Date sqlDate;
  java.sql.Time sqlTime;
  java.sql.Timestamp sqlTimestamp;
  Calendar calendar;
  GregorianCalendar gregorian;
  BigDecimal bigDecimal;
  BigInteger bigInteger;
  UUID uuid;
  URI uri;
  URL url;
  Currency currency;
  Locale locale;
  BitSet bits;
  StringBuilder builder;
  StringBuffer buffer;
  AtomicInteger atomicInt;
  AtomicLong atomicLong;
  AtomicIntegerArray atomicIntArray;
  AtomicLongArray atomicLongArray;
  Number number;
  Character character;
  Class<?> type;
}
