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

package org.apache.zeppelin.display;

import org.apache.commons.lang.StringUtils;

import com.google.gson.internal.StringMap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.stream.FileImageInputStream;

/**
 * Input type.
 */
public class Input implements Serializable {
  /**
   * Parameters option.
   */
  public static class ParamOption {
    Object value;
    String displayName;

    public ParamOption(Object value, String displayName) {
      super();
      this.value = value;
      this.displayName = displayName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ParamOption that = (ParamOption) o;

      if (value != null ? !value.equals(that.value) : that.value != null) return false;
      return displayName != null ? displayName.equals(that.displayName) : that.displayName == null;

    }

    @Override
    public int hashCode() {
      int result = value != null ? value.hashCode() : 0;
      result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
      return result;
    }

    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }

    public String getDisplayName() {
      return displayName;
    }

    public void setDisplayName(String displayName) {
      this.displayName = displayName;
    }

  }

  String name;
  String displayName;
  String type;
  String argument;
  Object defaultValue;
  ParamOption[] options;
  boolean hidden;

  public Input(String name, Object defaultValue, String type) {
    this.name = name;
    this.displayName = name;
    this.defaultValue = defaultValue;
    this.type = type;
  }

  public Input(String name, Object defaultValue, String type, ParamOption[] options) {
    this.name = name;
    this.displayName = name;
    this.defaultValue = defaultValue;
    this.type = type;
    this.options = options;
  }

  public Input(String name, String displayName, String type, String argument, Object defaultValue,
      ParamOption[] options, boolean hidden) {
    super();
    this.name = name;
    this.displayName = displayName;
    this.argument = argument;
    this.type = type;
    this.defaultValue = defaultValue;
    this.options = options;
    this.hidden = hidden;
  }

  @Override
  public boolean equals(Object o) {
    return name.equals(((Input) o).getName());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  public ParamOption[] getOptions() {
    return options;
  }

  public void setOptions(ParamOption[] options) {
    this.options = options;
  }

  public boolean isHidden() {
    return hidden;
  }

  // Syntax of variables: ${TYPE:NAME=DEFAULT_VALUE1|DEFAULT_VALUE2|...,VALUE1|VALUE2|...}
  // Type is optional. Type may contain an optional argument with syntax: TYPE(ARG)
  // NAME and VALUEs may contain an optional display name with syntax: NAME(DISPLAY_NAME)
  // DEFAULT_VALUEs may not contain display name
  // Examples:  ${age}                              input form without default value
  //            ${age=3}                            input form with default value
  //            ${age(Age)=3}                       input form with display name and default value
  //            ${country=US(United States)|UK|JP}  select form with
  //            ${checkbox( or ):country(Country)=US|JP,US(United States)|UK|JP}
  //                                                checkbox form with " or " as delimiter: will be
  //                                                expanded to "US or JP"
  private static final Pattern VAR_PTN = Pattern.compile("([_])?[$][{]([^=}]*([=][^}]*)?)[}]");
  private static final Pattern DEPENDENT_VAR_PTN = 
    Pattern.compile("([_])?[@][{]([^=}]*([=][^}]*)?)[}]");

  private static String[] getNameAndDisplayName(String str) {
    Pattern p = Pattern.compile("([^(]*)\\s*[(]([^)]*)[)]");
    Matcher m = p.matcher(str.trim());
    if (m == null || m.find() == false) {
      return null;
    }
    String[] ret = new String[2];
    ret[0] = m.group(1);
    ret[1] = m.group(2);
    return ret;
  }

  private static String[] getType(String str) {
    Pattern p = Pattern.compile("([^:()]*)\\s*([(][^()]*[)])?\\s*:(.*)");
    Matcher m = p.matcher(str.trim());
    if (m == null || m.find() == false) {
      return null;
    }
    String[] ret = new String[3];
    ret[0] = m.group(1).trim();
    if (m.group(2) != null) {
      ret[1] = m.group(2).trim().replaceAll("[()]", "");
    }
    ret[2] = m.group(3).trim();
    return ret;
  }

  private static Input getInputForm(Matcher match) {
    String hiddenPart = match.group(1);
    boolean hidden = false;
    if ("_".equals(hiddenPart)) {
      hidden = true;
    }
    String m = match.group(2);

    String namePart;
    String valuePart;

    int p = m.indexOf('=');
    if (p > 0) {
      namePart = m.substring(0, p);
      valuePart = m.substring(p + 1);
    } else {
      namePart = m;
      valuePart = null;
    }


    String varName;
    String displayName = null;
    String type = null;
    String arg = null;
    Object defaultValue = "";
    ParamOption[] paramOptions = null;

    // get var name type
    String varNamePart;
    String[] typeArray = getType(namePart);
    if (typeArray != null) {
      type = typeArray[0];
      arg = typeArray[1];
      varNamePart = typeArray[2];
    } else {
      varNamePart = namePart;
    }

    // get var name and displayname
    String[] varNameArray = getNameAndDisplayName(varNamePart);
    if (varNameArray != null) {
      varName = varNameArray[0];
      displayName = varNameArray[1];
    } else {
      varName = varNamePart.trim();
    }

    // get defaultValue
    if (valuePart != null) {
      // find default value
      int optionP = valuePart.indexOf(",");
      if (optionP >= 0) { // option available
        defaultValue = valuePart.substring(0, optionP);
        if (type != null && type.equals("checkbox")) {
          // checkbox may contain multiple default checks
          defaultValue = Input.splitPipe((String) defaultValue);
        }
        String optionPart = valuePart.substring(optionP + 1);
        String[] options = Input.splitPipe(optionPart);

        paramOptions = new ParamOption[options.length];

        for (int i = 0; i < options.length; i++) {

          String[] optNameArray = getNameAndDisplayName(options[i]);
          if (optNameArray != null) {
            paramOptions[i] = new ParamOption(optNameArray[0], optNameArray[1]);
          } else {
            paramOptions[i] = new ParamOption(options[i], null);
          }
        }


      } else { // no option
        defaultValue = valuePart;
      }

    }

    return new Input(varName, displayName, type, arg, defaultValue, paramOptions, hidden);
  }

  public static Map<String, Input> extractSimpleQueryParam(String script) {
    Map<String, Input> params = new HashMap<String, Input>();
    if (script == null) {
      return params;
    }
    String replaced = script;

    Matcher match = VAR_PTN.matcher(replaced);
    while (match.find()) {
      Input param = getInputForm(match);
      params.put(param.name, param);
    }

    params.remove("pql");
    return params;
  }

  private static final String DEFAULT_DELIMITER = ",";

  public static String resolveDependentParams(Map<String, Object> params, String script) {
    String replaced = script;
      
    Matcher match = DEPENDENT_VAR_PTN.matcher(replaced);
    while (match.find()) {
      Input input = getInputForm(match);
      Object value;
      if (params.containsKey(input.name)) {
        value = params.get(input.name);
      } else {
        value = input.defaultValue;
      }
      replaced = match.replaceFirst(value.toString());
      match = VAR_PTN.matcher(replaced);
    }
      
    return replaced;
  }
  /*Formatting the date and returning the required format*/
  public static String getFormatedDate(Date date){
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  	return sdf.format(date);
  	}
  public static String getSimpleQuery(Map<String, Object> params, String script) throws IOException {
    String replaced = resolveDependentParams(params, script);
    
    Matcher match = VAR_PTN.matcher(replaced);
    while (match.find()) {
      Input input = getInputForm(match);
      Object value;
      if (params.containsKey(input.name)) {
        value = params.get(input.name);
      } else {
        value = input.defaultValue;
      }
      String expanded;      
      if (input.name.contains("FilterTab")) {
    	  String fileName=getFileName(script);
    		List<String> properitiesFileValues=null; 		
    	    Map<String,Object>initialCheckMap= new HashMap<>();
    	    /*if(initialCheckMap.get(fileName)==null){
    	       	properitiesFileValues=getPropValues(fileName);
    	    	initialCheckMap.put(fileName, getFormatedDate(new Date()));
    	    	initialCheckMap.put("values", properitiesFileValues);
    	    }else{
    	    	String lastModifiedDate=(String)initialCheckMap.get(fileName);
    	    	String currentDate=getFormatedDate(new Date());	
    	    	if(lastModifiedDate.compareTo(currentDate)!=0){
    	    		properitiesFileValues=getPropValues(fileName);
    	        	initialCheckMap.put("values", properitiesFileValues);
    	        	initialCheckMap.put(fileName,currentDate);
    	        	}
    	    }*/
        expanded = getFilterTabQuery(value,(List<String>)initialCheckMap.get("values"));
      } else if (value instanceof Object[] || value instanceof Collection) {  // multi-selection
        String delimiter = input.argument;
        if (delimiter == null) {
          delimiter = DEFAULT_DELIMITER;
        }
        Collection<Object> checked = value instanceof Collection ? (Collection<Object>) value
                : Arrays.asList((Object[]) value);
        List<Object> validChecked = new LinkedList<Object>();
        for (Object o : checked) {  // filter out obsolete checked values
          for (ParamOption option : input.getOptions()) {
            if (option.getValue().equals(o)) {
              validChecked.add(o);
              break;
            }
          }
        }
        params.put(input.name, validChecked);
        expanded = StringUtils.join(validChecked, delimiter);
      } else {  // single-selection
        expanded = value.toString();
      }
      replaced = match.replaceFirst(expanded);
      match = VAR_PTN.matcher(replaced);
    }

    return replaced;
  }
  /*Getting index name out of script */
  public static String getFileName(String script){
	  String str=script.substring(script.indexOf("/"), script.indexOf("{")).trim();
	  String[] strArray=str.split("/");
	  return strArray[1];
  }
  /*loading properties file  and reading the key and adding to list*/
  public static List<String> getPropValues(String propertyFileName) throws IOException {
	  InputStream inputStream=null;
	  List<String> valuesList=  new ArrayList<String>();
		try {
			Properties prop = new Properties();
			String path = String.format("%s/%s", System.getProperty("user.dir"), "elasticsearch/_templates/"+propertyFileName+".properties");
			path=path.replace("zeppelin-server", "conf");
			 inputStream =new FileInputStream(path);
			if (inputStream != null) {
				prop.load(inputStream);
				 Enumeration<Object> em = prop.keys();
				 while(em.hasMoreElements()){
					 String key = (String)em.nextElement();
					 valuesList.add(key); 
				 }
			} 
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
		return valuesList;
	}
  /*Preparing query for FilterTab*/
  public static String getFilterTabQuery(Object filterTabValue,List<String> rawFields) {
    StringBuffer query = new StringBuffer();
    List<StringMap<String>> values = (List<StringMap<String>>) filterTabValue;
    for (StringMap<String> value: values) {
      String field = value.get("column");
      String operator = value.get("operator");
      String operand = value.get("operand"); 
      switch (operator) {
          case "=": 
        	  /*if(rawFields.contains(field)){
            query.append("{\"term\":").append("{").append(field).append(".raw:")
              .append(operand.toLowerCase()).append("}}");
        	  }else*/{
        		  query.append("{\"term\":").append("{").append(field).append(":")
                  .append(operand.toLowerCase()).append("}}");
        	  }
            break;
          case "!=": 
            query.append("{\"filter\":").append("{\"not\":").append("{\"term\":")
              .append("{").append(field).append(":").append(operand).append("}}}}");
            break;
          case "<": 
            query.append("{\"range\":").append("{").append(field).append(":")
              .append("{\"lt\":").append(operand).append("}}}");
            break;
          case "<=": 
            query.append("{\"range\":").append("{").append(field).append(":")
              .append("{\"lte\":").append(operand).append("}}}");
            break;
          case ">": 
            query.append("{\"range\":").append("{").append(field).append(":")
              .append("{\"gt\":").append(operand).append("}}}");
            break;
          case ">=": 
            query.append("{\"range\":").append("{").append(field).append(":")
              .append("{\"gte\":").append(operand).append("}}}");
            break;
          case "contains": 
        	  if(isValidWord(operand)){
        		  query.append("{\"wildcard\":").append("{").append(field).append(":*")
                  .append(operand).append("*}}"); 
        	  }else{
            query.append("{\"match_phrase\":").append("{").append(field).append(":")
              .append(operand).append("}}");
        	  }
            break; 
          case "starts with":
            query.append("{\"prefix\":").append("{ \"").append(field).append("\":\"")
              .append(operand).append("\"}}");
      }
      query.append(",");
    } 
    return query.toString();
  }
  public static boolean isValidWord(String inputString) {
	    return inputString.matches("[A-Za-z]*");
	}

  public static String[] split(String str) {
    return str.split(";(?=([^\"']*\"[^\"']*\")*[^\"']*$)");

  }

  /*
   * public static String [] splitPipe(String str){ //return
   * str.split("\\|(?=([^\"']*\"[^\"']*\")*[^\"']*$)"); return
   * str.split("\\|(?=([^\"']*\"[^\"']*\")*[^\"']*$)"); }
   */


  public static String[] splitPipe(String str) {
    return split(str, '|');
  }

  public static String[] split(String str, char split) {
    return split(str, new String[] {String.valueOf(split)}, false);
  }

  public static String[] split(String str, String[] splitters, boolean includeSplitter) {
    String escapeSeq = "\"',;${}";
    char escapeChar = '\\';

    String[] blockStart = new String[] {"\"", "'", "${", "N_(", "N_<"};
    String[] blockEnd = new String[] {"\"", "'", "}", "N_)", "N_>"};

    return split(str, escapeSeq, escapeChar, blockStart, blockEnd, splitters, includeSplitter);

  }

  public static String[] split(String str, String escapeSeq, char escapeChar, String[] blockStart,
      String[] blockEnd, String[] splitters, boolean includeSplitter) {

    List<String> splits = new ArrayList<String>();

    String curString = "";

    boolean escape = false; // true when escape char is found
    int lastEscapeOffset = -1;
    int blockStartPos = -1;
    List<Integer> blockStack = new LinkedList<Integer>();

    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);

      // escape char detected
      if (c == escapeChar && escape == false) {
        escape = true;
        continue;
      }

      // escaped char comes
      if (escape == true) {
        if (escapeSeq.indexOf(c) < 0) {
          curString += escapeChar;
        }
        curString += c;
        escape = false;
        lastEscapeOffset = curString.length();
        continue;
      }

      if (blockStack.size() > 0) { // inside of block
        curString += c;
        // check multichar block
        boolean multicharBlockDetected = false;
        for (int b = 0; b < blockStart.length; b++) {
          if (blockStartPos >= 0
              && getBlockStr(blockStart[b]).compareTo(str.substring(blockStartPos, i)) == 0) {
            blockStack.remove(0);
            blockStack.add(0, b);
            multicharBlockDetected = true;
            break;
          }
        }

        if (multicharBlockDetected == true) {
          continue;
        }

        // check if current block is nestable
        if (isNestedBlock(blockStart[blockStack.get(0)]) == true) {
          // try to find nested block start

          if (curString.substring(lastEscapeOffset + 1).endsWith(
              getBlockStr(blockStart[blockStack.get(0)])) == true) {
            blockStack.add(0, blockStack.get(0)); // block is started
            blockStartPos = i;
            continue;
          }
        }

        // check if block is finishing
        if (curString.substring(lastEscapeOffset + 1).endsWith(
            getBlockStr(blockEnd[blockStack.get(0)]))) {
          // the block closer is one of the splitters (and not nested block)
          if (isNestedBlock(blockEnd[blockStack.get(0)]) == false) {
            for (String splitter : splitters) {
              if (splitter.compareTo(getBlockStr(blockEnd[blockStack.get(0)])) == 0) {
                splits.add(curString);
                if (includeSplitter == true) {
                  splits.add(splitter);
                }
                curString = "";
                lastEscapeOffset = -1;

                break;
              }
            }
          }
          blockStartPos = -1;
          blockStack.remove(0);
          continue;
        }

      } else { // not in the block
        boolean splitted = false;
        for (String splitter : splitters) {
          // forward check for splitter
          int curentLenght = i + splitter.length();
          if (splitter.compareTo(str.substring(i, Math.min(curentLenght, str.length()))) == 0) {
            splits.add(curString);
            if (includeSplitter == true) {
              splits.add(splitter);
            }
            curString = "";
            lastEscapeOffset = -1;
            i += splitter.length() - 1;
            splitted = true;
            break;
          }
        }
        if (splitted == true) {
          continue;
        }

        // add char to current string
        curString += c;

        // check if block is started
        for (int b = 0; b < blockStart.length; b++) {
          if (curString.substring(lastEscapeOffset + 1)
                       .endsWith(getBlockStr(blockStart[b])) == true) {
            blockStack.add(0, b); // block is started
            blockStartPos = i;
            break;
          }
        }
      }
    }
    if (curString.length() > 0) {
      splits.add(curString.trim());
    }
    return splits.toArray(new String[] {});

  }

  private static String getBlockStr(String blockDef) {
    if (blockDef.startsWith("N_")) {
      return blockDef.substring("N_".length());
    } else {
      return blockDef;
    }
  }

  private static boolean isNestedBlock(String blockDef) {
    if (blockDef.startsWith("N_")) {
      return true;
    } else {
      return false;
    }
  }
}
