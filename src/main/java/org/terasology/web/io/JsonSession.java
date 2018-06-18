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
package org.terasology.web.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import org.terasology.config.Config;
import org.terasology.engine.modes.GameState;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.i18n.I18nMap;
import org.terasology.i18n.gson.I18nMapTypeAdapter;
import org.terasology.identity.storageServiceClient.BigIntegerBase64Serializer;
import org.terasology.naming.Name;
import org.terasology.naming.Version;
import org.terasology.naming.gson.NameTypeAdapter;
import org.terasology.naming.gson.VersionTypeAdapter;
import org.terasology.utilities.gson.UriTypeAdapterFactory;
import org.terasology.web.EngineRunner;
import org.terasology.web.resources.base.InputParser;
import org.terasology.web.resources.base.ResourceMethodName;
import org.terasology.web.resources.base.ResourcePath;
import org.terasology.web.authentication.AuthenticationFailedException;
import org.terasology.web.authentication.AuthenticationHandshakeHandler;
import org.terasology.web.authentication.AuthenticationHandshakeHandlerImpl;
import org.terasology.web.authentication.ClientAuthenticationMessage;
import org.terasology.web.authentication.HandshakeHello;
import org.terasology.web.client.HeadlessClient;
import org.terasology.web.client.HeadlessClientFactory;
import org.terasology.web.io.gsonUtils.ByteArrayBase64Serializer;
import org.terasology.web.io.gsonUtils.ValidatorTypeAdapterFactory;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.ResourceManager;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Handles the JSON data goes across the REST API.
 */
public class JsonSession {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(ValidatorTypeAdapterFactory.getInstance())
            .registerTypeAdapter(BigInteger.class, BigIntegerBase64Serializer.getInstance())
            .registerTypeAdapter(byte[].class, ByteArrayBase64Serializer.getInstance())
            .registerTypeAdapter(Name.class, new NameTypeAdapter())
            .registerTypeAdapter(Version.class, new VersionTypeAdapter())
            .registerTypeAdapter(I18nMap.class, new I18nMapTypeAdapter())
            .registerTypeAdapterFactory(new UriTypeAdapterFactory())
            .create();
    private static final InputParser<JsonElement> JSON_INPUT_PARSER = new InputParser<JsonElement>() {
        @Override
        public <T> T parse(JsonElement input, Class<T> outputType) throws ResourceAccessException {
            try {
                return GSON.fromJson(input, outputType);
            } catch (JsonSyntaxException ex) {
                throw new ResourceAccessException(new ActionResult(ex));
            }
        }
    };
    private static Set<JsonSession> allSessions = new HashSet<>();

    private final AuthenticationHandshakeHandler authHandler;
    private final ResourceManager resourceManager;

    private HeadlessClientFactory headlessClientFactory;
    private HeadlessClient client;
    private BiConsumer<Collection<String>, JsonElement> resourceChangeSubscriber = (path, data) -> { };
    private BiConsumer<Collection<String>, JsonElement> resourceEventListener = (path, data) -> { };

    JsonSession(AuthenticationHandshakeHandler authHandler, HeadlessClientFactory headlessClientFactory, ResourceManager resourceManager) {
        this.authHandler = authHandler;
        this.headlessClientFactory = headlessClientFactory;
        this.resourceManager = resourceManager;
        this.client = headlessClientFactory.connectNewAnonymousHeadlessClient();
        setResourceObservers(); //observe the notifications sent for the anonymous client
        allSessions.add(this);
    }

    public JsonSession() {
        this(new AuthenticationHandshakeHandlerImpl(EngineRunner.getInstance().getFromCurrentContext(Config.class).getSecurity()),
                new HeadlessClientFactory(EngineRunner.getInstance().getFromCurrentContext(EntityManager.class)), ResourceManager.getInstance());
    }

    public static void disconnectAllClients() {
        for (JsonSession session: allSessions) {
            session.client.disconnect();
        }
    }

    public static void handleEngineStateChanged(GameState newEngineState) {
        allSessions.forEach((instance) -> {
            instance.headlessClientFactory = new HeadlessClientFactory(newEngineState.getContext().get(EntityManager.class));
            instance.removeResourceObservers();
            instance.client.disconnect();
            if (instance.isAuthenticated()) {
                instance.client = instance.headlessClientFactory.connectNewHeadlessClient(instance.client.getId());
            } else {
                instance.client = instance.headlessClientFactory.connectNewAnonymousHeadlessClient();
            }
            instance.setResourceObservers();
        });
    }

    public void setResourceChangeSubscriber(BiConsumer<Collection<String>, JsonElement> resourceChangeSubscriber) {
        this.resourceChangeSubscriber = resourceChangeSubscriber;
    }

    public void setResourceEventListener(BiConsumer<Collection<String>, JsonElement> resourceEventListener) {
        this.resourceEventListener = resourceEventListener;
    }

    private void notifyResourceChanged(ResourcePath resourcePath, Object newData) {
        resourceChangeSubscriber.accept(Collections.unmodifiableCollection(resourcePath.getItemList()), GSON.toJsonTree(newData));
    }

    private void notifyResourceEvent(ResourcePath resourcePath, Object eventData) {
        resourceEventListener.accept(Collections.unmodifiableCollection(resourcePath.getItemList()), GSON.toJsonTree(eventData));
    }

    private void setResourceObservers() {
       resourceManager.addClient(client, this::notifyResourceChanged, this::notifyResourceEvent);
    }

    private void removeResourceObservers() {
        resourceManager.removeClient(client);
    }

    public boolean isAuthenticated() {
        return client != null && !client.isAnonymous();
    }

    public ActionResult initAuthentication() {
        if (isAuthenticated()) {
            return new ActionResult(ActionResult.Status.FORBIDDEN, "Already authenticated");
        }
        HandshakeHello serverHello = authHandler.initServerHello();
        return new ActionResult(GSON.toJsonTree(serverHello));
    }

    public ActionResult finishAuthentication(JsonElement clientMessage) {
        if (isAuthenticated()) {
            return new ActionResult(ActionResult.Status.FORBIDDEN, "Already authenticated");
        }
        try {
            ClientAuthenticationMessage clientAuthentication = GSON.fromJson(clientMessage, ClientAuthenticationMessage.class);
            byte[] serverVerification = authHandler.authenticate(clientAuthentication);
            removeResourceObservers(); //remove the observers for the anonymous client
            client.disconnect(); //disconnect the anonymous client
            String clientId = clientAuthentication.getClientHello().getCertificate().getId();
            client = headlessClientFactory.connectNewHeadlessClient(clientId);
            setResourceObservers(); //observe the notifications sent for the authenticated client
            return new ActionResult(GSON.toJsonTree(serverVerification));
        } catch (JsonSyntaxException ex) {
            return new ActionResult(ex);
        } catch (AuthenticationFailedException ex) {
            return new ActionResult(ActionResult.Status.FORBIDDEN);
        }
    }

    public void disconnect() {
        removeResourceObservers();
        client.disconnect();
        client = null;
        allSessions.remove(this);
    }

    public ActionResult accessResource(List<String> resourcePath, ResourceMethodName methodName, JsonElement inputData) {
        try {
            Object resultData = resourceManager.performAction(new ResourcePath(resourcePath), methodName, inputData, JSON_INPUT_PARSER, client);
            return new ActionResult(GSON.toJsonTree(resultData));
        } catch (ResourceAccessException ex) {
            return ex.getResultToSend();
        }
    }

}
