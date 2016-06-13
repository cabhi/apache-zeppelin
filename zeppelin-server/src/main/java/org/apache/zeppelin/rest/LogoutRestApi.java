package org.apache.zeppelin.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.shiro.subject.Subject;
import org.apache.zeppelin.server.JsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/logout")
@Produces("application/json")
public class LogoutRestApi {
private static final Logger LOG = LoggerFactory.getLogger(LogoutRestApi.class);

/**
 * Required by Swagger.
 */
public LogoutRestApi() {
 super();
}


@POST
public Response logout() {
  JsonResponse response;
  
  Subject currentUser = org.apache.shiro.SecurityUtils.getSubject();
  currentUser.logout();

  Map<String, String> data = new HashMap<>();
  data.put("principal", "anonymous");
  data.put("roles", "");
  data.put("ticket", "anonymous");
 
  response = new JsonResponse(Response.Status.OK, "", data);
  LOG.warn(response.toString());
  return response.build();
}

}
