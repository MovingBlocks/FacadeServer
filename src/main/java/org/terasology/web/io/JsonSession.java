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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.identity.storageServiceClient.BigIntegerBase64Serializer;
import org.terasology.web.EngineRunner;
import org.terasology.web.authentication.AuthenticationFailedException;
import org.terasology.web.authentication.AuthenticationHandshakeHandler;
import org.terasology.web.authentication.AuthenticationHandshakeHandlerImpl;
import org.terasology.web.authentication.ClientAuthenticationMessage;
import org.terasology.web.authentication.HandshakeHello;
import org.terasology.web.client.HeadlessClient;
import org.terasology.web.client.HeadlessClientFactory;
import org.terasology.web.resources.EventEmittingResource;
import org.terasology.web.resources.ObservableReadableResource;
import org.terasology.web.resources.ReadableResource;
import org.terasology.web.resources.ResourceManager;
import org.terasology.web.resources.UnsupportedResourceTypeException;
import org.terasology.web.resources.WritableResource;

import java.math.BigInteger;
import java.util.function.BiConsumer;

public class JsonSession {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BigInteger.class, BigIntegerBase64Serializer.getInstance())
            .registerTypeAdapter(byte[].class, ByteArrayBase64Serializer.getInstance())
            .create();

    private final AuthenticationHandshakeHandler authHandler;
    private final HeadlessClientFactory headlessClientFactory;
    private final ResourceManager resourceManager;
    private final JsonSessionResourceObserver resourceObserver;

    private HeadlessClient client;

    JsonSession(AuthenticationHandshakeHandler authHandler, HeadlessClientFactory headlessClientFactory, ResourceManager resourceManager) {
        this.authHandler = authHandler;
        this.headlessClientFactory = headlessClientFactory;
        this.resourceManager = resourceManager;
        this.resourceObserver = new JsonSessionResourceObserver(this);
        for (ObservableReadableResource observableResource: resourceManager.getAllAs(ObservableReadableResource.class)) {
            observableResource.addObserver(resourceObserver);
        }
        for (EventEmittingResource eventResource: resourceManager.getAllAs(EventEmittingResource.class)) {
            eventResource.addObserver(resourceObserver);
        }
    }

    public JsonSession() {
        this(new AuthenticationHandshakeHandlerImpl(EngineRunner.getContext().get(Config.class).getSecurity()),
                new HeadlessClientFactory(EngineRunner.getContext().get(EntityManager.class)), ResourceManager.getInstance());
    }

    public void setReadableResourceObserver(BiConsumer<EntityRef, JsonElement> observer) {
        resourceObserver.setReadableResourceObserver(observer);
    }

    public void setEventResourceObserver(BiConsumer<EntityRef, JsonElement> observer) {
        resourceObserver.setEventResourceObserver(observer);
    }

    public boolean isAuthenticated() {
        return client != null;
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
            String clientId = clientAuthentication.getClientHello().getCertificate().getId();
            client = headlessClientFactory.connectNewHeadlessClient(clientId);
            return new ActionResult(GSON.toJsonTree(serverVerification));
        } catch (NullPointerException | JsonSyntaxException ex) {
            return new ActionResult(ActionResult.Status.BAD_REQUEST);
        } catch (AuthenticationFailedException ex) {
            return new ActionResult(ActionResult.Status.UNAUTHORIZED);
        }
    }

    public void disconnect() {
        for (ObservableReadableResource observableResource: resourceManager.getAllAs(ObservableReadableResource.class)) {
            observableResource.removeObserver(resourceObserver);
        }
        for (EventEmittingResource eventResource: resourceManager.getAllAs(EventEmittingResource.class)) {
            eventResource.removeObserver(resourceObserver);
        }
        client.disconnect();
        client = null;
    }

    <T> JsonElement serializeEvent(EventEmittingResource<T> eventResource, T eventData) {
        return GSON.toJsonTree(eventData);
    }

    JsonElement readResource(ReadableResource resource) {
        return GSON.toJsonTree(resource.read(client.getEntity()));
    }

    public ActionResult readResource(String resourceName) {
        ReadableResource resource;
        try {
            resource = resourceManager.getAs(resourceName, ReadableResource.class);
        } catch (UnsupportedResourceTypeException ex) {
            return new ActionResult(ActionResult.Status.BAD_REQUEST, ex.getMessage());
        }
        return new ActionResult(readResource(resource));
    }

    public ActionResult writeResource(String resourceName, JsonElement data) {
        WritableResource resource;
        try {
            resource = resourceManager.getAs(resourceName, WritableResource.class);
        } catch (UnsupportedResourceTypeException ex) {
            return new ActionResult(ActionResult.Status.BAD_REQUEST, ex.getMessage());
        }
        resource.write(client.getEntity(), GSON.fromJson(data, resource.getDataType()));
        return ActionResult.OK;
    }

}
