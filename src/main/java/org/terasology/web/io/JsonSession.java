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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.identity.storageServiceClient.BigIntegerBase64Serializer;
import org.terasology.network.Client;
import org.terasology.web.EngineRunner;
import org.terasology.web.authentication.AuthenticationFailedException;
import org.terasology.web.authentication.AuthenticationHandshakeHandler;
import org.terasology.web.authentication.AuthenticationHandshakeHandlerImpl;
import org.terasology.web.authentication.ClientAuthenticationMessage;
import org.terasology.web.authentication.HandshakeHello;
import org.terasology.web.client.AnonymousHeadlessClient;
import org.terasology.web.client.HeadlessClientFactory;
import org.terasology.web.io.gsonUtils.ByteArrayBase64Serializer;
import org.terasology.web.io.gsonUtils.ValidatorTypeAdapterFactory;
import org.terasology.web.resources.EventEmittingResource;
import org.terasology.web.resources.ObservableReadableResource;
import org.terasology.web.resources.ReadableResource;
import org.terasology.web.resources.ResourceAccessException;
import org.terasology.web.resources.ResourceManager;
import org.terasology.web.resources.WritableResource;

import java.math.BigInteger;
import java.util.function.BiConsumer;

public class JsonSession {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(ValidatorTypeAdapterFactory.getInstance())
            .registerTypeAdapter(BigInteger.class, BigIntegerBase64Serializer.getInstance())
            .registerTypeAdapter(byte[].class, ByteArrayBase64Serializer.getInstance())
            .create();

    private final AuthenticationHandshakeHandler authHandler;
    private final HeadlessClientFactory headlessClientFactory;
    private final ResourceManager resourceManager;
    private final JsonSessionResourceObserver resourceObserver;

    private Client client;

    JsonSession(AuthenticationHandshakeHandler authHandler, HeadlessClientFactory headlessClientFactory, ResourceManager resourceManager) {
        this.authHandler = authHandler;
        this.headlessClientFactory = headlessClientFactory;
        this.resourceManager = resourceManager;
        this.resourceObserver = new JsonSessionResourceObserver(this);
        this.client = headlessClientFactory.connectNewAnonymousHeadlessClient();
        setResourceObservers(); //observe the notifications sent for the anonymous client
    }

    public JsonSession() {
        this(new AuthenticationHandshakeHandlerImpl(EngineRunner.getContext().get(Config.class).getSecurity()),
                new HeadlessClientFactory(EngineRunner.getContext().get(EntityManager.class)), ResourceManager.getInstance());
    }

    private void setResourceObservers() {
        for (ObservableReadableResource observableResource: resourceManager.getAll(ObservableReadableResource.class)) {
            observableResource.setObserver(client.getEntity(), resourceObserver);
        }
        for (EventEmittingResource eventResource: resourceManager.getAll(EventEmittingResource.class)) {
            eventResource.setObserver(client.getEntity(), resourceObserver);
        }
    }

    private void removeResourceObservers() {
        for (ObservableReadableResource observableResource: resourceManager.getAll(ObservableReadableResource.class)) {
            observableResource.removeObserver(client.getEntity());
        }
        for (EventEmittingResource eventResource: resourceManager.getAll(EventEmittingResource.class)) {
            eventResource.removeObserver(client.getEntity());
        }
    }

    public void setReadableResourceObserver(BiConsumer<String, JsonElement> observer) {
        resourceObserver.setReadableResourceObserver(observer);
    }

    public void setEventResourceObserver(BiConsumer<String, JsonElement> observer) {
        resourceObserver.setEventResourceObserver(observer);
    }

    public boolean isAuthenticated() {
        return client != null && !(client instanceof AnonymousHeadlessClient);
    }

    public ActionResult initAuthentication() {
        if (isAuthenticated()) {
            return new ActionResult(ActionResult.Status.UNAUTHORIZED, "Already authenticated");
        }
        HandshakeHello serverHello = authHandler.initServerHello();
        return new ActionResult(GSON.toJsonTree(serverHello));
    }

    public ActionResult finishAuthentication(JsonElement clientMessage) {
        if (isAuthenticated()) {
            return new ActionResult(ActionResult.Status.UNAUTHORIZED, "Already authenticated");
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
            return new ActionResult(ActionResult.Status.UNAUTHORIZED);
        }
    }

    public void disconnect() {
        removeResourceObservers();
        client.disconnect();
        client = null;
    }

    JsonElement serializeEvent(Object eventData) {
        return GSON.toJsonTree(eventData);
    }

    JsonElement readResource(ReadableResource resource) {
        return GSON.toJsonTree(resource.read(client));
    }

    public ActionResult readResource(String resourceName) {
        ReadableResource resource;
        try {
            resource = resourceManager.getAs(resourceName, ReadableResource.class);
        } catch (ResourceAccessException ex) {
            return ex.getResultToSend();
        }
        return new ActionResult(readResource(resource));
    }

    public ActionResult writeResource(String resourceName, JsonElement data) {
        if (!isAuthenticated()) {
            return new ActionResult(ActionResult.Status.UNAUTHORIZED, "Only authenticated clients can write to resources.");
        }
        WritableResource resource;
        try {
            resource = resourceManager.getAs(resourceName, WritableResource.class);
        } catch (ResourceAccessException ex) {
            return ex.getResultToSend();
        }
        resource.write(client, GSON.fromJson(data, resource.getDataType()));
        return ActionResult.OK;
    }

}
