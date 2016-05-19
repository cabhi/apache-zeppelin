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



package com.wizni;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.google.gson.Gson;


public class DataGeneratorHelper {

    static String[] processIds = {};
    static String[] dates = {"2016-04-20T02:02:40", "2016-04-20T06:45:40", "2016-04-20T09:32:10",
	    "2016-04-20T15:34:40", "2016-04-20T18:19:40",
	    "2016-04-20T21:09:30", "2016-04-19T04:02:20", "2016-04-19T09:26:12",
	    "2016-04-19T11:52:40", "2016-04-19T14:45:56", "2016-04-19T18:32:51",
	    "2016-04-19T23:21:44", "2016-04-18T04:29:28", "2016-04-18T11:26:42",
	    "2016-04-17T07:43:21", "2016-04-17T09:43:19", "2016-04-17T14:33:43",
	    "2016-04-17T18:38:21", "2016-04-16T07:38:42", "2016-04-16T13:34:56"};
    static String[] levels = {"TRACE", "DEBUG", "INFO"};
    static String[] appIds = {"tapps", "appmap"};
    static String[] userIds =  {"himanshu@wizni.com","ankita@wizni.com","hemant@wizni.com","harish@wizni.com","harish@wizni.com", "shiv@wizni.com", "shiv@wizni.com"};
    static String[] httpMethods = {"GET", "GET", "GET", "GET", "POST", "POST", "OPTIONS", "DELETE"};
     //generate unique request Ids "requestId":"asdas-sadasd-sad",
    
    static String[][] requests = {{"/tapps/foundationalservice","/tapps/portfolio","/tapps/strategictheme", "/tapps/techproject"}, 
	    {"/appmap/ace_devicedetails", "/appmap/feedback", "/appmap/application", "/appmap/survey"}};
    static String[][] responses = {{"{identifier: 2, name: IT Asset Mgmt and Tracking, level: 1}",
	    "{planYear: 2016, portfolioLevel1: ELECTRIC BUSINESS, proposalStage: CONTINUING}",
    	    "{identifier: 2,type: FOUNDATION,level: 1}", 
    	    "{group: New,lineOfBusiness: Corporate Affairs, projectId: IT101230,user: sga5}"},
    	   {"{column: ManufacturerName,rawValue: Dell,source: ATRIUM,tag: computer}",
    		   "{EMAIL:hemant@wizni.com,WORK_ORDER: WO0000000421560,STATUS:submitted}",
    		   "{DESIGN_TIER: TIER 3,IT_CORE_SERVICE: 5.32,APP_ID: 159}",
    		   "{Context: APPLICATION,Identifier: 139,UpdateDate: 2016-02-25}"}};
    
    
    static String[] requestsTapps = {"/rest-api/tapps/foundationalservice","/rest-api/tapps/portfolio",
	    "/rest-api/tapps/strategictheme", "/rest-api/tapps/techproject"};
    static String[] responsesTapps = {"{identifier: 2, name: IT Asset Mgmt and Tracking, level: 1}",
	    "{planYear: 2016, portfolioLevel1: ELECTRIC BUSINESS, proposalStage: CONTINUING}",
    	    "{identifier: 2,type: FOUNDATION,level: 1}", 
    	    "{group: New,lineOfBusiness: Corporate Affairs, projectId: IT101230,user: sga5}"};
    
    static String[] requestsAppmap = {"/appmap/ace_devicedetails", "/appmap/feedback", 
	    "/appmap/application", "/appmap/survey"};
    static String[] responsesAppmap = {"{column: ManufacturerName,rawValue: Dell,source: ATRIUM,tag: computer}",
	   "{EMAIL:hemant@wizni.com,WORK_ORDER: WO0000000421560,STATUS:submitted}",
	   "{DESIGN_TIER: TIER 3,IT_CORE_SERVICE: 5.32,APP_ID: 159}",
	   "{Context: APPLICATION,Identifier: 139,UpdateDate: 2016-02-25}"};
    static int[] statusCodes = {200, 201, 400, 401, 500};
    static String[] statusMessages = {"OK", "Created", "Bad Request", "Unauthorized", "Internal Server Error"};
    static int[] responseTimes = {1, 2, 3, 4, 5, 6, 8, 12, 13, 14, 15, 16, 18, 22, 25, 27, 18, 
	    33, 35, 38, 39, 44, 46, 47, 48, 49, 50, 51, 57, 59, 61, 68, 70, 75, 77, 79, 
	    83, 85, 89, 94, 98, 101, 104, 107, 118, 145, 172, 198, 206, 234};
    static int[] requestSizes = {35, 38, 39, 44, 46, 47, 48, 49, 50, 51, 57, 59};
    static int[] responseSizes = {101, 104, 107, 118, 145, 172, 198, 206, 234, 543, 871, 1415, 1567, 1982};
    static String[] names = {"MongoDB Error", "FailedLoadingCustomApi", "FailedLoadingModule", "ErrorFetchingMiddlewares"};
    
    static String[] userAgents = {"Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko",
	"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)", 
	"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"};
    static String[] regions = {"Eastern US", "Central US"};
    static String[][] zones = {{"us-east1-b", "us-east1-c", "us-east1-d"},
	{"us-central1-a", "us-central1-b", "us-central1-c"}};
    static String[] logs = {"wize.log"};
    static String[] urlMapEntries = {"app.js"};
    static String[][] instanceIds = {{"tapps-nodeA", "tapps-nodeB"},
	{ "appmap-nodeA", "appmap-nodeB", "appmap-nodeC"}};
    static String[] httpVersions = {"HTTP/1.1", "HTTP/2.0"};
    static String[] ips = {"128.192.130.21", "51.189.72.89", "51.123.130.21", "94.65.30.76", "56.78.167.21", "75.121.89.21"};
    static String[] moduleIds = {"api", "middleware", "third-party-module"};
    static String[] versionIds = {"v1.1", "v1.4", "v1.9"};
    static String[] serviceNames = {"com.wizni.application"};
    static String[] logMessages = {"Started loading project config environments", 
	"Started loading project configurations for environment  DEVELOPMENT",
	"Started creating mongodb connection",
	"Started loading middlewares",
	"No Cron Job Found!",
	"Connected with MongoDB Server",
	"All project related data are loaded!",
	"Mongoose Connection Open with MongoDB Server"
	};
    static SourceLocation[] sourceLocations = {
	new SourceLocation("inversion-of-controller.js", "31"),
	new SourceLocation("inversion-of-controller.js", "53"),
	new SourceLocation("inversion-of-controller.js", "76"),
	new SourceLocation("inversion-of-controller.js", "171"),
	new SourceLocation("inversion-of-controller.js", "76"),
	new SourceLocation("cron-controller.js", "284"),
	new SourceLocation("app.js", "104"),
	new SourceLocation("inversion-of-controller.js", "240"),
	new SourceLocation("app.js", "118")
    };
    
    static SourceReference[][] sourceReferences = {{
	new SourceReference("github.com/tapps", "5160c20c6be2c880c98066ad16ae9ef2f3679321"),
	new SourceReference("github.com/tapps", "ff61adf86da4a496f6bcf05de83b1ba9111af4dd")},{
	new SourceReference("github.com/appmap", "be2e773f38f8902e6b2b684e39485bde2fdd3cdc"),
	new SourceReference("github.com/appmap", "e2ea134776273e38387b33fd842ed45c6c859159"),
	new SourceReference("github.com/appmap", "a5b7a260a5c045deae1b678781111ac7d5ca926c")}};
    
    
    
    
    public static String generateInfoAppMap () {
	int tuples = 1;
	List<String> tupleList = new ArrayList<>();
	for(int i=0; i<tuples; i++){
	    int index = generateRandom(requestsAppmap.length);
	    tupleList.add("{\"date\": \"" + dates[generateRandom(dates.length)] + "\",\"logLevel\": \"INFO\"," + 
	    "\"appId\":\"appmap\",\"userId\": \"" + userIds[generateRandom(userIds.length)] + "\","+
	    "\"httpMethod\": \"" + httpMethods[generateRandom(httpMethods.length)] + "\",\"reqId\":\"" + generateUUID() + "\","+
	    "\"request\":\"" + requestsAppmap[index] + "\",\"response\":\""+responsesAppmap[index]+"\", \"statusCode\":200,"+
	    "\"statusMessage\":\"OK\",\"responseTime\":"+responseTimes[generateRandom(responseTimes.length)]+"}");
	}
	System.out.println(tupleList);
	
	return tupleList.get(0);

    }
    
    public static String generateInfoTapps () {
	int tuples = 1;
	List<String> tupleList = new ArrayList<>();
	for(int i=0; i<tuples; i++){
	    int index = generateRandom(requestsTapps.length);
	    tupleList.add("{\"date\": \"" + dates[generateRandom(dates.length)] + "\",\"logLevel\": \"INFO\"," + 
	    "\"appId\":\"tapps\",\"userId\": \"" + userIds[generateRandom(userIds.length)] + "\","+
	    "\"httpMethod\": \"" + httpMethods[generateRandom(httpMethods.length)] + "\",\"reqId\":\"" + generateUUID() + "\","+
	    "\"request\":\"" + requestsTapps[index] + "\",\"response\":\""+responsesTapps[index]+"\", \"statusCode\":200,"+
	    "\"statusMessage\":\"OK\",\"responseTime\":"+responseTimes[generateRandom(responseTimes.length)]+"}");
	}
	System.out.println(tupleList);
	
	return tupleList.get(0);

    }
    
    public static String generateErrorAppmap () {
	int tuples = 1;
	List<String> tupleList = new ArrayList<>();
	for(int i=0; i<tuples; i++){
	    int index = generateRandom(requestsAppmap.length);
	    tupleList.add("{\"date\": \"" + dates[generateRandom(dates.length)] + "\",\"logLevel\": \"ERROR\"," + 
	    "\"appId\":\"appmap\",\"userId\": \"" + userIds[generateRandom(userIds.length)] + "\","+
	    "\"httpMethod\": \"" + httpMethods[generateRandom(httpMethods.length)] + "\",\"reqId\":\"" + generateUUID() + "\","+
	    "\"request\":\"" + requestsAppmap[index] + "\",\"error\":\""+names[generateRandom(names.length)]+"\", \"statusCode\":500,"+
	    "\"statusMessage\":\"Internal Server Error\",\"responseTime\":"+responseTimes[generateRandom(responseTimes.length)]+"}");
	}
	System.out.println(tupleList);
	
	return tupleList.get(0);

    }
    
    public static String generateErrorTapps () {
	int tuples = 1;
	List<String> tupleList = new ArrayList<>();
	for(int i=0; i<tuples; i++){
	    int index = generateRandom(requestsTapps.length);
	    tupleList.add("{\"date\": \"" + dates[generateRandom(dates.length)] + "\",\"logLevel\": \"ERROR\"," + 
	    "\"appId\":\"tapps\",\"userId\": \"" + userIds[generateRandom(userIds.length)] + "\","+
	    "\"httpMethod\": \"" + httpMethods[generateRandom(httpMethods.length)] + "\",\"reqId\":\"" + generateUUID() + "\","+
	    "\"request\":\"" + requestsTapps[index] + "\",\"error\":\""+names[generateRandom(names.length)]+"\", \"statusCode\":500,"+
	    "\"statusMessage\":\"Internal Server Error\",\"responseTime\":"+responseTimes[generateRandom(responseTimes.length)]+"}");
	}
	System.out.println(tupleList);
	
	return tupleList.get(0);
    }
    
    private static int generateRandom(int size){
	Random randomGenerator = new Random();
	return randomGenerator.nextInt(size);
    }
    
    private static String generateUUID(){
	return UUID.randomUUID().toString();
    }
    
    public static String getNewEntry() {
	LogEntry entry = new LogEntry();
	
	int responseTime = responseTimes[generateRandom(responseTimes.length)];
	
	Calendar calendar = Calendar.getInstance(); 
	SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	String endTimestamp = isoFormat.format(calendar.getTime());
	
	Calendar startCalendar = Calendar.getInstance();
	startCalendar.setTimeInMillis(calendar.getTimeInMillis());
	startCalendar.add(Calendar.MILLISECOND, -responseTime);
	String startTimestamp = isoFormat.format(startCalendar.getTime());
	
	String userId = userIds[generateRandom(userIds.length)];
	String ip = ips[generateRandom(ips.length)];
	
	int index = generateRandom(appIds.length);
	String appId = appIds[index];
	
	int statusCode = statusCodes[generateRandom(statusCodes.length)];
	
	String url = requests[index][generateRandom(requests[index].length)];
	
	//App
	entry.appInfo.appId = appId;
	entry.appInfo.moduleId = moduleIds[generateRandom(moduleIds.length)];   
	entry.appInfo.versionId = versionIds[generateRandom(versionIds.length)];
	entry.appInfo.ip = ip;
	entry.appInfo.startTime = startTimestamp;   
	entry.appInfo.endTime = endTimestamp;
	entry.appInfo.method = httpMethods[generateRandom(httpMethods.length)];   
	entry.appInfo.resource = url;
	entry.appInfo.error = (statusCode == 500)?names[generateRandom(names.length)]:"NONE";
	entry.appInfo.httpVersion = httpVersions[generateRandom(httpVersions.length)];
	entry.appInfo.nickname = userId.split("@")[0];
	entry.appInfo.urlMapEntry = urlMapEntries[generateRandom(urlMapEntries.length)];
	entry.appInfo.instanceId = instanceIds[index][generateRandom(instanceIds[index].length)];
	entry.appInfo.lines = getLines(startTimestamp);
	entry.appInfo.sourceReferences = getSourceReferences(index);
	
	//HttpRequest
	entry.httpRequest.requestMethod = httpMethods[generateRandom(httpMethods.length)];;
	entry.httpRequest.requestUrl = url;
	entry.httpRequest.requestSize = requestSizes[generateRandom(requestSizes.length)];;
	//entry.httpRequest.request;
	entry.httpResponse.status = statusCode;
	entry.httpResponse.response = 
		(statusCode == 500)?"":responses[index][generateRandom(responses[index].length)];
	entry.httpResponse.responseSize = responseSizes[generateRandom(responseSizes.length)];
	entry.httpResponse.responseTime = responseTime;
	entry.httpRequest.userAgent = userAgents[generateRandom(userAgents.length)];
	entry.httpRequest.remoteIp = ip;
	
	//Metadata
	int regionId = generateRandom(regions.length);
	entry.metadata.timestamp = endTimestamp;
	entry.metadata.level = 
		(statusCode == 500)?"ERROR":levels[generateRandom(levels.length)];
	entry.metadata.serviceName = serviceNames[generateRandom(serviceNames.length)];
	entry.metadata.region = regions[regionId];
	entry.metadata.zone = zones[regionId][generateRandom(zones[regionId].length)];
	entry.metadata.userId = userId;
	
	//root
	entry.log = logs[generateRandom(logs.length)];
	entry.requestId = generateUUID();
	
	Gson gson = new Gson();
	return gson.toJson(entry);
    }
    
    public static SourceReference[] getSourceReferences(int index) {
	SourceReference[] srcReferences = new SourceReference[1];
	srcReferences[0] = sourceReferences[index][generateRandom(sourceReferences[index].length)];
	return srcReferences;
    }

    public static Line[] getLines (String time) {
      int numLines = 1+ generateRandom(3);
      Line[] lines = new Line[numLines];
      for (int i=0; i<numLines; i++) {
	  lines[i] = getLine(time);
      }
      return lines;
    }
    
    public static Line getLine(String time) {
	Line line = new Line();
	int index = generateRandom(logMessages.length);
	line.level = levels[generateRandom(levels.length)];
	line.logMessage = logMessages[index];
	line.sourceLocation = sourceLocations[index];
	line.time = time;
	
	return line;
    }
}
