/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.web.servlet;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.SecurityConfig;
import org.terasology.web.EngineRunner;
import org.terasology.web.authentication.AuthenticationFailedException;
import org.terasology.web.authentication.AuthenticationHandshakeHandler;
import org.terasology.web.authentication.ClientAuthenticationMessage;
import org.terasology.web.authentication.HandshakeHello;

/**
 * Manages one websocket session
 */
public class WsHandler extends WebSocketAdapter {

    private static final Logger logger = LoggerFactory.getLogger(WsHandler.class);
    private static final Gson GSON = new GsonBuilder().create();

    private AuthenticationHandshakeHandler authHandler;
    private String clientId; //this is the same UUID used to identify players in the engine, and is only set when the client is correctly authenticated

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        logger.info("Connected: " + session.getRemoteAddress() + ", starting authentication handshake");
        SecurityConfig securityConfig = EngineRunner.getContext().get(Config.class).getSecurity();
        authHandler = new AuthenticationHandshakeHandler(securityConfig.getServerPublicCertificate());
        HandshakeHello serverHello = authHandler.initServerHello();
        trySend(GSON.toJson(serverHello));
        logger.info("Sent server handshake hello to " + session.getRemoteAddress());
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        logger.info("Received client handshake data from " + getSession().getRemoteAddress());
        if (clientId == null) {
            //then client message must be authentication handshake client hello
            try {
                ClientAuthenticationMessage clientAuthentication = GSON.fromJson(message, ClientAuthenticationMessage.class);
                authHandler.authenticate(clientAuthentication.getClientHello(), clientAuthentication.getSignature());
                clientId = clientAuthentication.getClientHello().getCertificate().getId();
                logger.info("Authentication successfully completed for client " + getSession().getRemoteAddress());
            } catch (NullPointerException | JsonSyntaxException | AuthenticationFailedException ex) {
                logger.info("Client authentication failed for " + getSession().getRemoteAddress(), ex);
                getSession().close();
            }
        } else {
            trySend(message);
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        logger.info("Socket Closed: [" + statusCode + "] " + reason);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        logger.error("Error", cause);
    }

    private void trySend(String message) {
        try {
            getSession().getRemote().sendString(message);
        } catch (IOException e) {
            logger.warn("Unable to send message!", e);
        }
    }
}
