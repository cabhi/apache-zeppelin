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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * Json field parser, used in elastic search interpreter
 */
public class JsonFieldParser {
    
  
    static Set<String> getAllFields(String json) throws IOException {
	Set<String> fields = parseJson(json);
	Set<String> finalFields = new HashSet<String>();
	for(String field: fields){
	    if(!field.contains(".raw"))
		finalFields.add(field);
	}
	
	return finalFields;
    }
    
    static Set<String> getFieldsWithRaw(String json) throws IOException {
	Set<String> fields = parseJson(json);
	Set<String> finalFields = new HashSet<String>();
	for(String field: fields){
	    if(field.contains(".raw"))
		finalFields.add(field.replaceAll(".fields.raw", ""));
	}
	
	return finalFields;
    }
    
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
            fields.add(getPath(reader.getPath()));
//            print(reader.getPath(), quote(s));
            break;
          case NUMBER:
            String n = reader.nextString();
            fields.add(getPath(reader.getPath()));
//            print(reader.getPath(), n);
            break;
          case BOOLEAN:
            boolean b = reader.nextBoolean();
            fields.add(getPath(reader.getPath()));
//            print(reader.getPath(), b);
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
    path = path.replaceAll("properties.","");
    
    //remove type name
    int index = path.indexOf('.');
    path = path.substring(index + 1);
    
    //remove field property, to get field name
    int lastIndex = path.lastIndexOf('.');
    path = path.substring(0, lastIndex);
    
    return path;
  }

  static String quote(String s) {
    return new StringBuilder().append('"').append(s).append('"').toString();
  }

  static final String REGEX = "\\[[0-9]+\\]";
  static final Pattern PATTERN = Pattern.compile(REGEX);
    
    
  public static void main(String[] args) throws IOException {
    Scanner scanner = new Scanner(new File("/tmp/sample1.json"));
    String json = scanner.nextLine();
    System.out.println(parseJson(json));
    System.out.println(getAllFields(json));
    System.out.println(getFieldsWithRaw(json));
    
    Class klass = JsonReader.class;
    URL location = klass.getResource('/' + klass.getName().replace('.', '/') + ".class");
    System.out.println(location);
  }

}

