/*
 * Copyright 2017 MovingBlocks
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

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import org.terasology.web.io.ActionResult;
import org.terasology.web.io.JsonSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

@Path("http")
public class HttpAPIServlet {

    private static final String SESSION_TOKEN_HEADER = "Session-Token";
    private final Map<String, JsonSession> sessions = Maps.newHashMap(); //maps session tokens with the active sessions
    private JsonSession anonymousSession;

    // TODO: call this after engine initialization
    public void initAnonymourSession() {
         anonymousSession = new JsonSession();
    }

    private Map.Entry<String, JsonSession> newSession() {
        Map.Entry<String, JsonSession> entry = new AbstractMap.SimpleEntry<>(UUID.randomUUID().toString(), new JsonSession());
        sessions.put(entry.getKey(), entry.getValue());
        return entry;
    }

    private JsonSession getSession(HttpServletRequest request) {
        String token = request.getHeader(SESSION_TOKEN_HEADER);
        if (token == null) {
            return anonymousSession;
        }
        JsonSession session = sessions.get(token);
        if (session == null) {
            throw new JsonWebApplicationException("Invalid session token", Response.Status.FORBIDDEN); //non-existing token token -> forbidden
        }
        return session;
    }

    @GET
    @Path("auth")
    @Produces(MediaType.APPLICATION_JSON)
    public ActionResult initAuthentication(@Context HttpServletResponse response) {
        Map.Entry<String, JsonSession> session = newSession();
        response.setHeader(SESSION_TOKEN_HEADER, session.getKey());
        return session.getValue().initAuthentication();
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

    @DELETE
    @Path("auth")
    @Produces(MediaType.APPLICATION_JSON)
    public ActionResult logout(@Context HttpServletRequest request) {
        String token = request.getHeader(SESSION_TOKEN_HEADER);
        if (!sessions.containsKey(token)) {
            throw new JsonWebApplicationException("Invalid session token", Response.Status.FORBIDDEN);
        }
        sessions.remove(token);
        return ActionResult.OK;
    }

}
