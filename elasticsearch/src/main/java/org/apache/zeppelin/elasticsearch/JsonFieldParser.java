/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zeppelin.elasticsearch;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * Json field parser, used in elastic search interpreter
 */
public class JsonFieldParser {
    
  static Set<String> parseJson(String json) throws IOException {
    Set<String> fields = new HashSet<String>();
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    while (true) {
      JsonToken token = reader.peek();
      switch (token) {
          case BEGIN_ARRAY:
            reader.beginArray();
            break;
          case END_ARRAY:
            reader.endArray();
            break;
          case BEGIN_OBJECT:
            reader.beginObject();
            break;
          case END_OBJECT:
            reader.endObject();
            break;
          case NAME:
            reader.nextName();
            break;
          case STRING:
            String s = reader.nextString();
            //fields.add(getPath(reader.getPath()));
            //print(reader.getPath(), quote(s));
            break;
          case NUMBER:
            String n = reader.nextString();
            //fields.add(getPath(reader.getPath()));
            //print(reader.getPath(), n);
            break;
          case BOOLEAN:
            boolean b = reader.nextBoolean();
            //fields.add(getPath(reader.getPath()));
            //print(reader.getPath(), b);
            break;
          case NULL:
            reader.nextNull();
            break;
          case END_DOCUMENT:
            reader.close();
            return fields;
      }
    }
  }

  static void print(String path, Object value) {
    path = path.substring(2);
    path = PATTERN.matcher(path).replaceAll("");
    System.out.println(path + ": " + value);
  }
  
  static String getPath(String path) {
    path = path.substring(2);
    path = PATTERN.matcher(path).replaceAll("");
    return path;
  }

  static String quote(String s) {
    return new StringBuilder().append('"').append(s).append('"').toString();
  }

  static final String REGEX = "\\[[0-9]+\\]";
  static final Pattern PATTERN = Pattern.compile(REGEX);
    
    
  public static void main(String[] args) throws IOException {
    String json = "{\"metadata\": {\"timestamp\": [\"val1\",\"val2\"], \"level\": \"val2\", "
      + "\"serviceName\": \"val3\", \"region\": \"val4\", "
      + "\"zone\": \"val5\", \"userId\": \"val6\" }}";
    System.out.println(parseJson(json));
  }

}

