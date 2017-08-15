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

package org.terasology.web.webSocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.io.ActionResult;
import org.terasology.web.io.JsonSession;
import org.terasology.web.io.gsonUtils.ValidatorTypeAdapterFactory;

/**
 * Manages one websocket session
 */
public class WebSocketHandler extends WebSocketAdapter {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private static final ErrorReportingWriteCallback ERROR_REPORTING_WRITE_CALLBACK = new ErrorReportingWriteCallback(logger);
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(ValidatorTypeAdapterFactory.getInstance())
            .disableHtmlEscaping()
            .create();
    private JsonSession jsonSession;

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        logger.info("Connected: " + session.getRemoteAddress());
        jsonSession = new JsonSession();
        jsonSession.setEventResourceObserver((resourceName, eventData) ->
                send(new ServerToClientMessage(ServerToClientMessage.MessageType.RESOURCE_EVENT, resourceName, eventData)));
        jsonSession.setReadableResourceObserver((resourceName, newData) ->
                send(new ServerToClientMessage(ServerToClientMessage.MessageType.RESOURCE_CHANGED, resourceName, newData)));
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        try {
            ClientToServerMessage deserializedMessage = GSON.fromJson(message, ClientToServerMessage.class);
            handleClientMessage(deserializedMessage);
        } catch (JsonSyntaxException ex) {
            trySendResult(new ActionResult(ex));
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        jsonSession.disconnect();
        jsonSession = null;
        logger.info("Socket Closed: [" + statusCode + "] " + reason);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        logger.error("Error", cause);
    }

    private void handleClientMessage(ClientToServerMessage clientMessage) {
        switch(clientMessage.getMessageType()) {
            case AUTHENTICATION_REQUEST:
                trySendResult(jsonSession.initAuthentication()); //send server handshake hello
                break;
            case AUTHENTICATION_DATA:
                trySendResult(jsonSession.finishAuthentication(clientMessage.getData())); //process client handshake hello
                break;
            case RESOURCE_REQUEST:
                parseAndHandleResourceRequest(clientMessage.getData());
        }
    }

    private void send(ServerToClientMessage message) {
        getSession().getRemote().sendString(GSON.toJson(message), ERROR_REPORTING_WRITE_CALLBACK);
    }

    private void trySendResult(ActionResult result, String resourceName) {
        send(new ServerToClientMessage(ServerToClientMessage.MessageType.ACTION_RESULT, resourceName, result.toJsonTree(GSON)));
    }

    private void trySendResult(ActionResult result) {
        send(new ServerToClientMessage(ServerToClientMessage.MessageType.ACTION_RESULT, result.toJsonTree(GSON)));
    }

    private void parseAndHandleResourceRequest(JsonElement requestMessage) {
        try {
            ResourceRequestClientMessage deserializedMessage = GSON.fromJson(requestMessage, ResourceRequestClientMessage.class);
            handleResourceRequest(deserializedMessage);
        } catch (JsonSyntaxException ex) {
            trySendResult(new ActionResult(ex));
        }
    }

    private void handleResourceRequest(ResourceRequestClientMessage deserializedMessage) {
        String resourceName = deserializedMessage.getResourceName();
        switch (deserializedMessage.getAction()) {
            case READ:
                trySendResult(jsonSession.readResource(resourceName), resourceName);
                break;
            case WRITE:
                trySendResult(jsonSession.writeResource(resourceName, deserializedMessage.getData()), resourceName);
        }
    }
}
