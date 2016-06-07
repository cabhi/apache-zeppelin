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

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import org.apache.commons.lang.StringUtils;
import org.apache.zeppelin.display.Input;
import org.apache.zeppelin.interpreter.Interpreter;
import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.interpreter.InterpreterPropertyBuilder;
import org.apache.zeppelin.interpreter.InterpreterResult;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.InternalMultiBucketAggregation;
import org.elasticsearch.search.aggregations.bucket.InternalSingleBucketAggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.metrics.InternalMetricsAggregation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Elasticsearch Interpreter for Zeppelin.
 */
public class ElasticsearchInterpreter extends Interpreter {

  private static Logger logger = LoggerFactory.getLogger(ElasticsearchInterpreter.class);

  private static final String HELP = "Elasticsearch interpreter:\n"
    + "General format: <command> /<indices>/<types>/<id> <option> <JSON>\n"
    + "  - indices: list of indices separated by commas (depends on the command)\n"
    + "  - types: list of document types separated by commas (depends on the command)\n"
    + "Commands:\n"
    + "  - search /indices/types <query>\n"
    + "    . indices and types can be omitted (at least, you have to provide '/')\n"
    + "    . a query is either a JSON-formatted query, nor a lucene query\n"
    + "  - size <value>\n"
    + "    . defines the size of the result set (default value is in the config)\n"
    + "    . if used, this command must be declared before a search command\n"
    + "  - count /indices/types <query>\n"
    + "    . same comments as for the search\n"
    + "  - get /index/type/id\n"
    + "  - delete /index/type/id\n"
    + "  - index /ndex/type/id <json-formatted document>\n"
    + "    . the id can be omitted, elasticsearch will generate one";

  private static final List<String> COMMANDS = Arrays.asList(
    "count", "delete", "get", "help", "index", "search");
    

  public static final String ELASTICSEARCH_HOST = "elasticsearch.host";
  public static final String ELASTICSEARCH_PORT = "elasticsearch.port";
  public static final String ELASTICSEARCH_CLUSTER_NAME = "elasticsearch.cluster.name";
  public static final String ELASTICSEARCH_RESULT_SIZE = "elasticsearch.result.size";
  public static final String ELASTICSEARCH_RESULT_FROM = "elasticsearch.result.from";

  static {
    Interpreter.register(
      "elasticsearch",
      "elasticsearch",
      ElasticsearchInterpreter.class.getName(),
        new InterpreterPropertyBuilder()
          .add(ELASTICSEARCH_HOST, "localhost", "The host for Elasticsearch")
          .add(ELASTICSEARCH_PORT, "9300", "The port for Elasticsearch")
          .add(ELASTICSEARCH_CLUSTER_NAME, "elasticsearch", "The cluster name for Elasticsearch")
          .add(ELASTICSEARCH_RESULT_SIZE, "10", "The size of the result set of a search query")
          .add(ELASTICSEARCH_RESULT_FROM, "0", "The offset into the search query results")
          .build());
  }

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private Client client;
  private String host = "localhost";
  private int port = 9300;
  private String clusterName = "elasticsearch";
  private int resultSize = 10;
  private int resultFrom = 0;
  
  private Gson gsonParser = new Gson();
  
  LoadingCache<Request, InterpreterResult> elasticCache;

  public ElasticsearchInterpreter(Properties property) {
    super(property);
    this.host = getProperty(ELASTICSEARCH_HOST);
    this.port = Integer.parseInt(getProperty(ELASTICSEARCH_PORT));
    this.clusterName = getProperty(ELASTICSEARCH_CLUSTER_NAME);
    this.resultSize = Integer.parseInt(getProperty(ELASTICSEARCH_RESULT_SIZE));
    this.resultFrom = Integer.parseInt(getProperty(ELASTICSEARCH_RESULT_FROM));
    
    elasticCache = CacheBuilder.newBuilder().maximumSize(0)
            .expireAfterAccess(3, TimeUnit.DAYS)
            .build(new CacheLoader<Request, InterpreterResult>(){
              @Override
              public InterpreterResult load(Request req) throws Exception {
                return processSearchInternal(req);
              } 
            });
  }

  @Override
  public void open() {
    try {
      logger.info("prop={}", getProperty());
      final Settings settings = Settings.settingsBuilder()
        .put("cluster.name", clusterName)
        .put(getProperty())
        .build();
      client = TransportClient.builder().settings(settings).build()
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
    }
    catch (IOException e) {
      logger.error("Open connection with Elasticsearch", e);
    }
  }

  @Override
  public void close() {
    if (client != null) {
      client.close();
    }
  }
  
  @Override
  public InterpreterResult interpret(String cmd, InterpreterContext interpreterContext) {
    logger.info("Run Elasticsearch command '" + cmd + "'");
    
    if (StringUtils.isEmpty(cmd) || StringUtils.isEmpty(cmd.trim())) {
      return new InterpreterResult(InterpreterResult.Code.SUCCESS);
    }
    cmd = expandedCommand(cmd, interpreterContext.getConfig().get(Input.FILTER_TAB_KEY));
    cmd = enhanceCommand(cmd);
    logger.info("Run Enhanced Elasticsearch command '" + cmd + "'");

    int currentResultSize = resultSize;
    int currentResultFrom = resultFrom;

    if (client == null) {
      return new InterpreterResult(InterpreterResult.Code.ERROR,
        "Problem with the Elasticsearch client, please check your configuration (host, port,...)");
    }

    String[] items = StringUtils.split(cmd.trim(), " ", 3);

    // Process some specific commands (help, size, ...)
    if ("help".equalsIgnoreCase(items[0])) {
      return processHelp(InterpreterResult.Code.SUCCESS, null);
    }
    
    int searchLineIndex = 0;
    if ("from".equalsIgnoreCase(items[0])) {
      // In this case, the line with from must be followed by line with size or a search,
      // so we will continue with the next lines
      final String[] lines = StringUtils.split(cmd.trim(), "\n", 2);
      if (lines.length < 2) {
        return processHelp(InterpreterResult.Code.ERROR,
          "From cmd must be followed by either a Size cmd or a search");
      }
      
      final String[] fromLine = StringUtils.split(lines[0], " ", 2);
      if (fromLine.length != 2) {
        return processHelp(InterpreterResult.Code.ERROR, "Right format is : from <value>");
      }
      currentResultFrom = (int) Double.parseDouble(fromLine[1]);
      items = StringUtils.split(lines[1].trim(), " ", 3);
      
      //remove 'from' line from cmd
      lines[0] = null;
      cmd = Joiner.on("\n").skipNulls().join(lines);
    }

    if ("size".equalsIgnoreCase(items[0])) {
      // In this case, the line with size must be followed by a search,
      // so we will continue with the next lines
      final String[] lines = StringUtils.split(cmd.trim(), "\n", 2);

      if (lines.length < 2) {
        return processHelp(InterpreterResult.Code.ERROR,
                           "Size cmd must be followed by a search");
      }

      final String[] sizeLine = StringUtils.split(lines[0], " ", 2);
      if (sizeLine.length != 2) {
        return processHelp(InterpreterResult.Code.ERROR, "Right format is : size <value>");
      }
      currentResultSize = Integer.parseInt(sizeLine[1]);

      items = StringUtils.split(lines[1].trim(), " ", 3);
    }

    if (items.length < 2) {
      return processHelp(InterpreterResult.Code.ERROR, "Arguments missing");
    }

    final String method = items[0];
    final String url = items[1];
    final String data = items.length > 2 ? items[2].trim() : null;

    final String[] urlItems = StringUtils.split(url.trim(), "/");

    try {
      if ("get".equalsIgnoreCase(method)) {
        return processGet(urlItems);
      }
      else if ("count".equalsIgnoreCase(method)) {
        return processCount(urlItems, data);
      }
      else if ("search".equalsIgnoreCase(method)) {
        return processSearch(urlItems, data, currentResultFrom, currentResultSize);
      }
      else if ("index".equalsIgnoreCase(method)) {
        return processIndex(urlItems, data);
      }
      else if ("delete".equalsIgnoreCase(method)) {
        return processDelete(urlItems);
      }
      else if ("getFields".equalsIgnoreCase(method)) {
        return processGetFields(urlItems);
      }

      return processHelp(InterpreterResult.Code.ERROR, "Unknown command");
    }
    catch (Exception e) {
      logger.error("Error in processing query: " + e);
      return new InterpreterResult(InterpreterResult.Code.ERROR, "Error : " + e.getMessage());
    }
  }

  @Override
  public void cancel(InterpreterContext interpreterContext) {
    // Nothing to do
  }

  @Override
  public FormType getFormType() {
    return FormType.SIMPLE;
  }

  @Override
  public int getProgress(InterpreterContext interpreterContext) {
    return 0;
  }

  @Override
  public List<String> completion(String s, int i) {
    final List<String> suggestions = new ArrayList<>();

    if (StringUtils.isEmpty(s)) {
      suggestions.addAll(COMMANDS);
    }
    else {
      for (String cmd : COMMANDS) {
        if (cmd.toLowerCase().contains(s)) {
          suggestions.add(cmd);
        }
      }
    }

    return suggestions;
  }

  private InterpreterResult processHelp(InterpreterResult.Code code, String additionalMessage) {
    final StringBuffer buffer = new StringBuffer();

    if (additionalMessage != null) {
      buffer.append(additionalMessage).append("\n");
    }

    buffer.append(HELP).append("\n");

    return new InterpreterResult(code, InterpreterResult.Type.TEXT, buffer.toString());
  }

  /**
   * Processes a "get" request.
   * 
   * @param urlItems Items of the URL
   * @return Result of the get request, it contains a JSON-formatted string
   */
  private InterpreterResult processGet(String[] urlItems) {

    if (urlItems.length != 3 
        || StringUtils.isEmpty(urlItems[0]) 
        || StringUtils.isEmpty(urlItems[1]) 
        || StringUtils.isEmpty(urlItems[2])) {
      return new InterpreterResult(InterpreterResult.Code.ERROR,
                                   "Bad URL (it should be /index/type/id)");
    }

    final GetResponse response = client
      .prepareGet(urlItems[0], urlItems[1], urlItems[2])
      .get();
    if (response.isExists()) {
      final String json = gson.toJson(response.getSource());

      return new InterpreterResult(
                    InterpreterResult.Code.SUCCESS,
                    InterpreterResult.Type.TEXT,
                    json);
    }
        
    return new InterpreterResult(InterpreterResult.Code.ERROR, "Document not found");
  }

  /**
   * Processes a "count" request.
   * 
   * @param urlItems Items of the URL
   * @param data May contains the JSON of the request
   * @return Result of the count request, it contains the total hits
   */
  private InterpreterResult processCount(String[] urlItems, String data) {

    if (urlItems.length > 2) {
      return new InterpreterResult(InterpreterResult.Code.ERROR,
                                   "Bad URL (it should be /index1,index2,.../type1,type2,...)");
    }

    final SearchResponse response = searchData(urlItems, data, 0, 0);

    return new InterpreterResult(
      InterpreterResult.Code.SUCCESS,
      InterpreterResult.Type.TEXT,
      "" + response.getHits().getTotalHits());
  }
  
  static class Request{
    String[] urlItems;
    String data;
    int size;
    int from;
      
    Request(String[] urlItems, String data, int from, int size) {
      this.urlItems = urlItems;
      this.data = data;
      this.from = from;
      this.size = size;
    }
      
    public String[] getUrlItems() {
      return urlItems;
    }
    public void setUrlItems(String[] urlItems) {
      this.urlItems = urlItems;
    }
    public String getData() {
      return data;
    }
    public void setData(String data) {
      this.data = data;
    }
    public int getSize() {
      return size;
    }
    public void setSize(int size) {
      this.size = size;
    }
    public int getFrom() {
      return from;
    }
    public void setFrom(int from) {
      this.from = from;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((data == null) ? 0 : data.hashCode());
      result = prime * result + size;
      result = prime * result + from;
      result = prime * result + Arrays.hashCode(urlItems);
      return result;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Request other = (Request) obj;
      if (data == null) {
        if (other.data != null)
          return false;
      } else if (!data.equals(other.data))
        return false;
      if (size != other.size)
        return false;
      if (from != other.from)
        return false;
      if (!Arrays.equals(urlItems, other.urlItems))
        return false;
      return true;
    }
    
    @Override
    public String toString() {
      return "Request [urlItems=" + Arrays.toString(urlItems) + ", data="
        + data + ", from=" + from + ", size=" + size + "]";
    }
      
  }
 

  /**
   * Processes a "search" request.
   * 
   * @param urlItems Items of the URL
   * @param data May contains the JSON of the request
   * @param size Limit of result set
   * @return Result of the search request, it contains a tab-formatted string of the matching hits
 * @throws ExecutionException 
   */
  private InterpreterResult processSearch(String[] urlItems, String data, int from, int size) 
      throws ExecutionException {

    if (urlItems.length > 2) {
      return new InterpreterResult(InterpreterResult.Code.ERROR,
                                   "Bad URL (it should be /index1,index2,.../type1,type2,...)");
    }
    
    
    
    Request req = new Request(urlItems, data, from, size);
    return elasticCache.get(req);
    
  }
  
  private InterpreterResult processSearchInternal(Request req) {
    final SearchResponse response = searchData(req.getUrlItems(), 
        req.getData(), req.getSize(), req.getFrom());
    return buildResponseMessage(response);
  }
  
  

  /**
   * Processes a "index" request.
   * 
   * @param urlItems Items of the URL
   * @param data JSON to be indexed
   * @return Result of the index request, it contains the id of the document
   */
  private InterpreterResult processIndex(String[] urlItems, String data) {
        
    if (urlItems.length < 2 || urlItems.length > 3) {
      return new InterpreterResult(InterpreterResult.Code.ERROR,
                                   "Bad URL (it should be /index/type or /index/type/id)");
    }
        
    final IndexResponse response = client
      .prepareIndex(urlItems[0], urlItems[1], urlItems.length == 2 ? null : urlItems[2])
      .setSource(data)
      .get();

    return new InterpreterResult(
      InterpreterResult.Code.SUCCESS,
      InterpreterResult.Type.TEXT,
      response.getId());
  }

  /**
   * Processes a "delete" request.
   * 
   * @param urlItems Items of the URL
   * @return Result of the delete request, it contains the id of the deleted document
   */
  private InterpreterResult processDelete(String[] urlItems) {

    if (urlItems.length != 3 
        || StringUtils.isEmpty(urlItems[0]) 
        || StringUtils.isEmpty(urlItems[1]) 
        || StringUtils.isEmpty(urlItems[2])) {
      return new InterpreterResult(InterpreterResult.Code.ERROR,
                                   "Bad URL (it should be /index/type/id)");
    }

    final DeleteResponse response = client
      .prepareDelete(urlItems[0], urlItems[1], urlItems[2])
      .get();
        
    if (response.isFound()) {
      return new InterpreterResult(
        InterpreterResult.Code.SUCCESS,
        InterpreterResult.Type.TEXT,
        response.getId());
    }
        
    return new InterpreterResult(InterpreterResult.Code.ERROR, "Document not found");
  }
  
  /**
   * Processes a "getFields" request.
   * 
   * @param urlItems index, type
   * @return fields of the given index and type
 * @throws ExecutionException 
 * @throws InterruptedException 
 * @throws IOException 
   */
  private InterpreterResult processGetFields(String[] urlItems) throws InterruptedException, ExecutionException, IOException {

    if (urlItems.length != 2 
        || StringUtils.isEmpty(urlItems[0]) 
        || StringUtils.isEmpty(urlItems[1])) {
      return new InterpreterResult(InterpreterResult.Code.ERROR,
                                   "Bad URL (it should be /index/type)");
    }
    
    IndicesAdminClient indicesAdminClient = client.admin().indices();
    
    GetMappingsRequest request = new GetMappingsRequest();
    ActionFuture<GetMappingsResponse> responseFuture = indicesAdminClient.getMappings(request.indices(urlItems[0]));
    
    GetMappingsResponse mappingResponse = responseFuture.get();
    
    ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = mappingResponse.getMappings();
    ImmutableOpenMap<String, MappingMetaData> indexMappings = mappings.get(urlItems[0]);
    MappingMetaData metaData = indexMappings.get(urlItems[1]);
    
    StringBuilder response = new StringBuilder().append("{\"fields\":[");
    String metadataJson = metaData.source().string();
    logger.info("Metadata Json: " + metadataJson);
    
    //parse fields from metadata
    Set<String> fields = JsonFieldParser.getAllFields(metadataJson);
    String prefix = "";
    
    //add fields to the response
    for(String field: fields) {
	response.append(prefix).append("\"").append(field).append("\"");
	prefix=",";
    }
    
    response.append("]}");
    logger.info("Returning Fields: " + response);
    return new InterpreterResult(InterpreterResult.Code.SUCCESS, InterpreterResult.Type.TEXT, response.toString());
  }
  
  /**
   * Processes a "getFields" request.
   * 
   * @param urlItems index, type
   * @return fields of the given index and type
 * @throws ExecutionException 
 * @throws InterruptedException 
 * @throws IOException 
   */
  private Set<String> processGetFieldsWithRaw(String cmd) throws InterruptedException, ExecutionException, IOException {
    final String[] lines = StringUtils.split(cmd.trim(), "\n", 3);
    String searchLine = null;
    for(String line: lines){
      if(line.trim().startsWith("search"))
        searchLine = line;
    }
    
    String url = StringUtils.split(searchLine.trim(), " ", 3)[1];
    String[] urlItems = StringUtils.split(url.trim(), "/", 3);
    IndicesAdminClient indicesAdminClient = client.admin().indices();
    
    GetMappingsRequest request = new GetMappingsRequest();
    ActionFuture<GetMappingsResponse> responseFuture = indicesAdminClient.getMappings(request.indices(urlItems[0]));
    
    GetMappingsResponse mappingResponse = responseFuture.get();
    
    ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = mappingResponse.getMappings();
    ImmutableOpenMap<String, MappingMetaData> indexMappings = mappings.get(urlItems[0]);
    MappingMetaData metaData = indexMappings.get(urlItems[1]);
    
    StringBuilder response = new StringBuilder().append("{\"fields\":[");
    String metadataJson = metaData.source().string();
    logger.info("Metadata Json: " + metadataJson);
    
    return JsonFieldParser.getFieldsWithRaw(metadataJson);
  }
    
  private SearchResponse searchData(String[] urlItems, String query, int size, int from) {

    final SearchRequestBuilder reqBuilder = new SearchRequestBuilder(
      client, SearchAction.INSTANCE);
    reqBuilder.setIndices();
        
    if (urlItems.length >= 1) {
      reqBuilder.setIndices(StringUtils.split(urlItems[0], ","));
    }
    if (urlItems.length > 1) {
      reqBuilder.setTypes(StringUtils.split(urlItems[1], ","));
    }

    if (!StringUtils.isEmpty(query)) {
      // The query can be either JSON-formatted, nor a Lucene query
      // So, try to parse as a JSON => if there is an error, consider the query a Lucene one
      try {
        final Map source = gson.fromJson(query, Map.class);
        reqBuilder.setExtraSource(source);
      }
      catch (JsonParseException e) {
        // This is not a JSON (or maybe not well formatted...)
        reqBuilder.setQuery(QueryBuilders.queryStringQuery(query).analyzeWildcard(true));
      }
    }

    reqBuilder.setSize(size);
    reqBuilder.setFrom(from);

    final SearchResponse response = reqBuilder.get();

    return response;
  }

  private InterpreterResult buildAggResponseMessage(Aggregations aggregations) {

    // Only the result of the first aggregation is returned
    //
    final Aggregation agg = aggregations.asList().get(0);
    InterpreterResult.Type resType = InterpreterResult.Type.TEXT;
    String resMsg = "";

    if (agg instanceof InternalMetricsAggregation) {
      resMsg = XContentHelper.toString((InternalMetricsAggregation) agg).toString();
    }
    else if (agg instanceof InternalSingleBucketAggregation) {
      InternalSingleBucketAggregation singleBucketAggregation = 
              (InternalSingleBucketAggregation) agg;
      if (singleBucketAggregation.getAggregations().asList()
              .get(0) instanceof InternalMultiBucketAggregation) {
        InternalMultiBucketAggregation multiBucketAgg = 
              (InternalMultiBucketAggregation) singleBucketAggregation
              .getAggregations().asList().get(0);
        resMsg = buildMultiBucketResponse(multiBucketAgg).toString();
        resType = InterpreterResult.Type.TABLE;
      }
      else
        resMsg = XContentHelper.toString(singleBucketAggregation);
    }
    else if (agg instanceof InternalMultiBucketAggregation) {
      final InternalMultiBucketAggregation multiBucketAgg = (InternalMultiBucketAggregation) agg;
      resMsg = buildMultiBucketResponse(multiBucketAgg).toString();
      resType = InterpreterResult.Type.TABLE;
    }

    return new InterpreterResult(InterpreterResult.Code.SUCCESS, resType, resMsg);
  }
  
  private String buildMultiBucketResponse(InternalMultiBucketAggregation multiBucketAgg) {
    StringBuffer buffer = null;
    if (multiBucketAgg.getBuckets().size() > 0 && multiBucketAgg.getBuckets()
             .get(0).getAggregations().asList().size() > 0)
      buffer = buildBucketResponse(multiBucketAgg);
    else {
      buffer = new StringBuffer("key\tcount");
      for (MultiBucketsAggregation.Bucket bucket : multiBucketAgg.getBuckets()) {
        buffer.append("\n")
          .append(enhance(bucket.getKeyAsString()))
          .append("\t")
          .append(bucket.getDocCount());
      }
    }
      
    return buffer.toString();
  }
  
  private StringBuffer buildBucketResponse(InternalMultiBucketAggregation multiBucketAgg){
    StringBuffer buffer = new StringBuffer("key1\tkey2\tcount");
    for (MultiBucketsAggregation.Bucket bucket : multiBucketAgg.getBuckets()) {
      String key1Val = enhance(bucket.getKeyAsString());
      final Aggregation agg = bucket.getAggregations().asList().get(0);
      final InternalMultiBucketAggregation inMultiBucketAgg = (InternalMultiBucketAggregation) agg;
      for (MultiBucketsAggregation.Bucket inBucket : inMultiBucketAgg.getBuckets()) {
        buffer.append("\n")
          .append(key1Val)
          .append("\t")
          .append(enhance(inBucket.getKeyAsString()))
          .append("\t")
          .append(inBucket.getDocCount());
      }
    }
      
    return buffer;
  }
  
  private String buildSearchHitsWithFieldsResponseMessage(SearchHit[] hits) {
      // First : get all the keys in order to build an ordered list of the
      // values for each hit
      //
    logger.info("buildSearchHitsWithFieldsResponseMessage: Got {} hits", hits.length);
    final List<Map<String, SearchHitField>> flattenHits = new LinkedList<>();
    final Set<String> keys = new TreeSet<>();
    for (SearchHit hit : hits) {
      final Map<String, SearchHitField> flattenMap = hit.getFields();
      flattenHits.add(flattenMap);

      for (String key : flattenMap.keySet()) {
        keys.add(key);
      }
    }
    // Next : build the header of the table
    //
    final StringBuffer buffer = new StringBuffer();
    for (String key : keys) {
      buffer.append(enhanceHeaders(key)).append('\t');
    }
    buffer.replace(buffer.lastIndexOf("\t"), buffer.lastIndexOf("\t") + 1,
            "\n");

    // Finally : build the result by using the key set
    //
    for (Map<String, SearchHitField> hit : flattenHits) {
      for (String key : keys) {
        final SearchHitField val = hit.get(key);
        if (val != null) {
          String str = val.getValue().toString().replaceAll("\n", "\\\\n");
          buffer.append(enhance(str));
        }
        buffer.append('\t');
      }
      buffer.replace(buffer.lastIndexOf("\t"),
        buffer.lastIndexOf("\t") + 1, "\n");
    }

    return buffer.toString();
  }
  
  private InterpreterResult buildSearchHitsResponseMessage(SearchHit[] hits) {
    if (hits == null || hits.length == 0) {
      logger.info("buildSearchHitsResponseMessage: Got {} hits", hits.length);
      return new InterpreterResult(
        InterpreterResult.Code.SUCCESS,
        InterpreterResult.Type.TEXT,
        "");
    }        
    if (hits[0].getSourceAsString() == null) {
      return new InterpreterResult(
        InterpreterResult.Code.SUCCESS,
        InterpreterResult.Type.TABLE,
        buildSearchHitsWithFieldsResponseMessage(hits));
    }
    
    StringBuffer jsonResponse = new StringBuffer("{\"rows\":[");
    String prefix = "";
    final Map<String, Object> hitFields = new HashMap<>();
    Gson gson = new Gson();
    for (SearchHit hit : hits) {
      String json = hit.getSourceAsString();
//      if(json == null) {
//        hitFields.clear();
//        for (SearchHitField hitField : hit.getFields().values()) {
//          hitFields.put(hitField.getName(), hitField.getValues());
//        }
//        json = gson.toJson(hitFields);
//      }
      
      jsonResponse.append(prefix);
      prefix = ",";
      jsonResponse.append(json);
    } 
    jsonResponse.append("]}");
    
    return new InterpreterResult(
      InterpreterResult.Code.SUCCESS,
      InterpreterResult.Type.TEXT,
      jsonResponse.toString());
  }

  private String buildSearchHitsResponseMessageOriginal (SearchHit[] hits) {
        
    if (hits == null || hits.length == 0) {
      return "";
    }
    
    if (hits[0].getSourceAsString() == null)
      return buildSearchHitsWithFieldsResponseMessage(hits);

    //First : get all the keys in order to build an ordered list of the values for each hit
    //
    final List<Map<String, Object>> flattenHits = new LinkedList<>();
    final Set<String> keys = new TreeSet<>();
    for (SearchHit hit : hits) {
      final String json = hit.getSourceAsString();
      final Map<String, Object> flattenMap = JsonFlattener.flattenAsMap(json);
      flattenHits.add(flattenMap);

      for (String key : flattenMap.keySet()) {
        keys.add(key);
      }
    }

    // Next : build the header of the table
    //
    final StringBuffer buffer = new StringBuffer();
    for (String key : keys) {
      buffer.append(enhanceHeaders(key)).append('\t');
    }
    buffer.replace(buffer.lastIndexOf("\t"), buffer.lastIndexOf("\t") + 1, "\n");

    // Finally : build the result by using the key set
    //
    for (Map<String, Object> hit : flattenHits) {
      for (String key : keys) {
        final Object val = hit.get(key);
        if (val != null) {
          String str = val.toString().replaceAll("\n", "\\\\n");
          buffer.append(enhance(str));
        }
        buffer.append('\t');
      }
      buffer.replace(buffer.lastIndexOf("\t"), buffer.lastIndexOf("\t") + 1, "\n");
    }

    return buffer.toString();
  }
  
  private static final Pattern EMPTY_1 = Pattern.
    compile("\\{[\\s]*\"[^\"]+\"[\\s]*:[\\s]*\\{[\\s]*\"[^\"]+\"[\\s]*:[\\s]*}[\\s]*}[\\s]*,");
  private static final Pattern EMPTY_2 = Pattern.
    compile(",\\{[\\s]*\"[^\"]+\"[\\s]*:[\\s]*\\{[\\s]*\"[^\"]+\"[\\s]*:[\\s]*}[\\s]*}[\\s]*");
  private static final Pattern EMPTY_3 = Pattern.
    compile("\\{[\\s]*\"[^\"]+\"[\\s]*:[\\s]*\\{[\\s]*\"[^\"]+\"[\\s]*:[\\s]*}[\\s]*}[\\s]*");
   
  
  private String expandedCommand(String cmd, Object params) {
      String expanded;
    try {
	expanded = getFilterTabQuery(params, processGetFieldsWithRaw(cmd));
    } catch (Exception e) {
	return cmd;
    }
      return cmd.replaceFirst(Input.FILTER_TAB_KEY, expanded);
  }
  
  /*Preparing query for FilterTab*/
  public static String getFilterTabQuery(Object filterTabValue,Set<String> rawFields) {
	  logger.info("Entering getFilterTabQuery method with filter tab value: "+filterTabValue);
	  if(filterTabValue==null){
		  filterTabValue="";
		  return filterTabValue.toString();
	  }
	  
	  logger.info("++++++FilterTab vlaue in starting of getFilterTabQuery ++++++++  "+filterTabValue);
    StringBuffer query = new StringBuffer();
    List<Map<String, String>> values = (List<Map<String, String>>) filterTabValue;
    for (Map<String, String> value: values) {
    	 logger.info("++++++FilterTab vlaue in starting of getFilterTabQuery  foreach loop ++++++++  "+value);
      String field = value.get("column");
      String operator = value.get("operator");
      String operand = value.get("operand"); 
      switch (operator) {
          case "=": 
        	  if(rawFields.contains(field)){
            query.append("{\"term\":").append("{").append(field).append(".raw:")
              .append(operand.toLowerCase()).append("}}");
        	  }else{
        		  query.append("{\"term\":").append("{").append(field).append(":").append("\"")
                  .append(operand.toLowerCase()).append("\"").append("}}");
        	  }
            break;
          case "!=": 
            query.append("{\"filter\":").append("{\"not\":").append("{\"term\":")
              .append("{").append(field).append(":").append("\"").append(operand).append("\"").append("}}}}");
            break;
          case "<": 
            query.append("{\"range\":").append("{").append(field).append(":")
              .append("{\"lt\":").append("\"").append(operand).append("\"").append("}}}");
            break;
          case "<=": 
            query.append("{\"range\":").append("{").append(field).append(":")
              .append("{\"lte\":").append("\"").append(operand).append("\"").append("}}}");
            break;
          case ">": 
            query.append("{\"range\":").append("{").append(field).append(":")
              .append("{\"gt\":").append("\"").append(operand).append("\"").append("}}}");
            break;
          case ">=": 
            query.append("{\"range\":").append("{").append(field).append(":")
              .append("{\"gte\":").append("\"").append(operand).append("\"").append("}}}");
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
  
  private static String enhanceCommand(String cmd){
    
    Matcher match = EMPTY_1.matcher(cmd);
    cmd = match.replaceAll("");
    
    match = EMPTY_2.matcher(cmd);
    cmd = match.replaceAll("");
    
    match = EMPTY_3.matcher(cmd);
    cmd = match.replaceAll("");
        
    return cmd;
  }
  
  private static String enhanceHeaders(String val){
    return val.substring(0, 1).toUpperCase() + val.substring(1);
  }
  
  private static String enhance(Object val){
    return val.toString();
//    String res = parseDate(val.toString());
//    return removeUrlHeader(res);
  }
  
  private static String removeUrlHeader(String val){
    return val.replaceAll("/rest-api", "");
  }
  
  private static String parseDate(String val){     
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    SimpleDateFormat outputFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    try {
      Date date = formatter.parse(val);
      return outputFormatter.format(date).toString();
    } catch (ParseException e){
      return val;
    }
  }
  
  public static void main(String[] args) {
//    System.out.println(enhanceHeaders("reqId"));
//    System.out.println(enhanceHeaders("logLevel"));
//    System.out.println(enhanceHeaders("request"));
//    System.out.println(enhanceHeaders("Response"));
      
    System.out.println(enhanceCommand("{\"wildcard\":{\"logLevel\": \"more\"}},{\"wildcard\":"
      + "{\"userId\":}}" +
      ",{\"wildcard\":{\"statusMessage\":}},{\"wildcard\":{\"request\":some}},{\"term\""
      + ":{\"statusCode\":}},{\"wildcard\":{\"httpMethod\":something}}"));
//    System.out.println(parseDate("2016-04-20T00:00:00"));
      
//    System.out.println(removeUrlHeader("/rest-api/tapps/foundationalservice"));
  }

  private InterpreterResult buildResponseMessage(SearchResponse response) {

    final Aggregations aggregations = response.getAggregations();

    if (aggregations != null && aggregations.asList().size() > 0) {
      return buildAggResponseMessage(aggregations);
    }
    
    return buildSearchHitsResponseMessage(response.getHits().getHits());

//    return new InterpreterResult(
//      InterpreterResult.Code.SUCCESS,
//      InterpreterResult.Type.TABLE,
//      buildSearchHitsResponseMessage(response.getHits().getHits()));
  }
  

}
