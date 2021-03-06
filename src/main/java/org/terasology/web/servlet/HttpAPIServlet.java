/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.web.servlet;

import com.google.gson.JsonElement;
import org.terasology.web.io.ActionResult;
import org.terasology.web.io.JsonSession;
import org.terasology.web.resources.base.ResourceMethodName;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servlet used to expose the REST API over http.
 */
@Path("api")
public class HttpAPIServlet {

    private static final String SESSION_TOKEN_HEADER = "Session-Token";
    private static final String RESOURCE_PATH_MATCHER = "resources/{resourcePath: .+?}";
    private static final String RESOURCE_PATH_PARAM = "resourcePath";

    private final Map<String, JsonSessionWithEventQueue> sessions = new HashMap<>(); //maps session tokens with the active sessions
    private JsonSession anonymousSession;

    private JsonSessionWithEventQueue getSessionWithEventQueue(String token) {
        JsonSessionWithEventQueue session = sessions.get(token);
        if (session == null) {
            //non-existing token -> forbidden
            throw new JsonWebApplicationException("Invalid session token", Response.Status.FORBIDDEN);
        }
        return session;
    }

    private JsonSessionWithEventQueue getSessionWithEventQueue(HttpServletRequest request) {
        return getSessionWithEventQueue(request.getHeader(SESSION_TOKEN_HEADER));
    }

    private JsonSession getSession(HttpServletRequest request) {
        String token = request.getHeader(SESSION_TOKEN_HEADER);
        if (token == null) {
            if (anonymousSession == null) {
                anonymousSession = new JsonSession();
            }
            return anonymousSession;
        }
        return getSessionWithEventQueue(token).getSession();
    }

    @GET
    @Path("auth")
    @Produces(MediaType.APPLICATION_JSON)
    public ActionResult initAuthentication(@Context HttpServletResponse response) {
        //initialize new session
        String sessionId = UUID.randomUUID().toString();
        JsonSessionWithEventQueue session = new JsonSessionWithEventQueue();
        sessions.put(sessionId, session);
        response.setHeader(SESSION_TOKEN_HEADER, sessionId);
        return session.getSession().initAuthentication();
    }

    @POST
    @Path("auth")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ActionResult finishAuthentication(JsonElement data, @Context HttpServletRequest request) {
        JsonSession session = getSession(request);
        if (session == anonymousSession) {
            //session token header is mandatory for this endpoint
            throw new JsonWebApplicationException("Missing session token header", Response.Status.FORBIDDEN);
        }
        return session.finishAuthentication(data);
    }

    /**
     * End an http session by logging out of it.
     * @param request http request containing information on the session.
     * @return result that tells caller to end the session.
     */
    @DELETE
    @Path("auth")
    @Produces(MediaType.APPLICATION_JSON)
    public ActionResult logout(@Context HttpServletRequest request) {
        String token = request.getHeader(SESSION_TOKEN_HEADER);
        if (!sessions.containsKey(token)) {
            throw new JsonWebApplicationException("Invalid session token", Response.Status.NOT_FOUND);
        }
        sessions.get(token).getSession().disconnect();
        sessions.remove(token);
        return ActionResult.OK;
    }

    @GET
    @Path("events")
    @Produces(MediaType.APPLICATION_JSON)
    public List<JsonSessionWithEventQueue.ResourceEvent> getEvents(@Context HttpServletRequest request) {
        return getSessionWithEventQueue(request).drainEventQueue();
    }

    @GET
    @Path(RESOURCE_PATH_MATCHER)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ActionResult accessResourceGet(JsonElement data, @Context HttpServletRequest request, @PathParam(RESOURCE_PATH_PARAM) String resourcePath) {
        return accessResource(data, request, resourcePath);
    }

    @POST
    @Path(RESOURCE_PATH_MATCHER)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ActionResult accessResourcePost(JsonElement data, @Context HttpServletRequest request, @PathParam(RESOURCE_PATH_PARAM) String resourcePath) {
        return accessResource(data, request, resourcePath);
    }

    @PUT
    @Path(RESOURCE_PATH_MATCHER)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ActionResult accessResourcePut(JsonElement data, @Context HttpServletRequest request, @PathParam(RESOURCE_PATH_PARAM) String resourcePath) {
        return accessResource(data, request, resourcePath);
    }

    @DELETE
    @Path(RESOURCE_PATH_MATCHER)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ActionResult accessResourceDelete(JsonElement data, @Context HttpServletRequest request, @PathParam(RESOURCE_PATH_PARAM) String resourcePath) {
        return accessResource(data, request, resourcePath);
    }

    @PATCH
    @Path(RESOURCE_PATH_MATCHER)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ActionResult accessResourcePatch(JsonElement data, @Context HttpServletRequest request, @PathParam(RESOURCE_PATH_PARAM) String resourcePath) {
        return accessResource(data, request, resourcePath);
    }

    private ActionResult accessResource(JsonElement data, HttpServletRequest request, String resourcePath) {
        List<String> splitPath = Arrays.asList(resourcePath.split("/"));
        ResourceMethodName resourceMethodName = ResourceMethodName.valueOf(request.getMethod());
        return getSession(request).accessResource(splitPath, resourceMethodName, data);
    }

}
