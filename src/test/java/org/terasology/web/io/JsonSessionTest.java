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

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.identity.storageServiceClient.BigIntegerBase64Serializer;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.ClientInfoComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.web.authentication.AuthenticationFailedException;
import org.terasology.web.authentication.AuthenticationHandshakeHandler;
import org.terasology.web.authentication.ClientAuthenticationMessage;
import org.terasology.web.authentication.HandshakeHello;
import org.terasology.web.client.AnonymousHeadlessClient;
import org.terasology.web.client.AuthenticatedHeadlessClient;
import org.terasology.web.client.HeadlessClientFactory;
import org.terasology.web.io.gsonUtils.ByteArrayBase64Serializer;
import org.terasology.web.resources.EventEmittingResource;
import org.terasology.web.resources.ObservableReadableResource;
import org.terasology.web.resources.ReadableResource;
import org.terasology.web.resources.ResourceAccessException;
import org.terasology.web.resources.ResourceManager;
import org.terasology.web.resources.WritableResource;


import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsonSessionTest {

    private static class AuthenticationHandshakeHandlerMock implements AuthenticationHandshakeHandler {
        private boolean nextResult;

        @Override
        public HandshakeHello initServerHello() {
            return null;
        }

        @Override
        public byte[] authenticate(ClientAuthenticationMessage authenticationMessage) throws AuthenticationFailedException {
            if (!nextResult) {
                throw new AuthenticationFailedException(AuthenticationFailedException.INVALID_CLIENT_CERT);
            }
            return new byte[]{0, 0, 0};
        }
    }

    private static class ObservableReadableResourceMock extends ObservableReadableResource<String> {
        @Override
        public String getName() {
            return "testResource";
        }
        @Override
        public String read(Client requestingClient) {
            return "test";
        }
    }

    private static class EventEmittingResourceMock extends EventEmittingResource<String> {
        @Override
        public String getName() {
            return "testResource";
        }
    }

    private static class ClientEntityMockBundle {
        private AuthenticatedHeadlessClient client;
        private HeadlessClientFactory factoryMock;

        private ClientEntityMockBundle() {
            client = mock(AuthenticatedHeadlessClient.class);
            when(client.isAnonymous()).thenCallRealMethod();
            factoryMock = mock(HeadlessClientFactory.class);
            when(factoryMock.connectNewHeadlessClient("testPlayerId")).thenReturn(client);
            when(factoryMock.connectNewAnonymousHeadlessClient()).thenReturn(mock(AnonymousHeadlessClient.class));
        }
    }

    private static class AnonymousClientEntityMockBundle {
        private AnonymousHeadlessClient client;
        private HeadlessClientFactory factoryMock;

        private AnonymousClientEntityMockBundle() {
            client = mock(AnonymousHeadlessClient.class);
            when(client.isAnonymous()).thenCallRealMethod();
            factoryMock = mock(HeadlessClientFactory.class);
            when(factoryMock.connectNewAnonymousHeadlessClient()).thenReturn(client);
        }
    }

    private static final BigInteger ZERO = BigInteger.ZERO;
    private static final byte[] EMPTY = new byte[]{};
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(BigInteger.class, BigIntegerBase64Serializer.getInstance())
            .registerTypeAdapter(byte[].class, ByteArrayBase64Serializer.getInstance())
            .create();
    private static ResourceManager resourceManagerMock;

    @BeforeClass
    public static void setUpResourceManagerMock() {
        resourceManagerMock = mock(ResourceManager.class);
        when(resourceManagerMock.getAll(any())).thenReturn(new HashSet<>());
    }

    private void assertResult(ActionResult.Status expectedStatus, JsonElement expectedData, ActionResult actual) {
        assertEquals(expectedStatus, actual.getStatus());
        assertEquals(expectedData, actual.getData());
    }

    @Test
    public void testAuthentication() {
        AuthenticationHandshakeHandlerMock authHandlerMock = new AuthenticationHandshakeHandlerMock();
        HeadlessClientFactory headlessClientFactoryMock = mock(HeadlessClientFactory.class);
        when(headlessClientFactoryMock.connectNewHeadlessClient("testId")).thenReturn(new AuthenticatedHeadlessClient("testId"));
        when(headlessClientFactoryMock.connectNewAnonymousHeadlessClient()).thenReturn(new AnonymousHeadlessClient());
        JsonSession session = new JsonSession(authHandlerMock, headlessClientFactoryMock, resourceManagerMock);
        JsonElement dummyClientMessage = GSON.toJsonTree(new ClientAuthenticationMessage(
                new HandshakeHello(EMPTY, new PublicIdentityCertificate("testId", ZERO, ZERO, ZERO), 0),
                EMPTY));

        assertFalse(session.isAuthenticated());
        assertEquals(ActionResult.Status.OK, session.initAuthentication().getStatus());
        assertFalse(session.isAuthenticated());
        assertEquals(ActionResult.Status.BAD_REQUEST, session.finishAuthentication(JsonNull.INSTANCE).getStatus());
        assertFalse(session.isAuthenticated());
        assertEquals(ActionResult.Status.UNAUTHORIZED, session.finishAuthentication(dummyClientMessage).getStatus());
        assertFalse(session.isAuthenticated());
        authHandlerMock.nextResult = true;
        ActionResult result = session.finishAuthentication(dummyClientMessage);
        assertEquals(ActionResult.Status.OK, result.getStatus());
        assertEquals("AAAA", result.getData().getAsString()); //"AAAA" is the base64 of three 0 bytes, returned by the mock authenticate method
        assertTrue(session.isAuthenticated());
        //assertEquals(ActionResult.Status.UNAUTHORIZED, session.finishAuthentication(dummyClientMessage).getStatus()); //already authenticated
    }

    private JsonSession setupAlwaysAccepting(String playerId, HeadlessClientFactory clientFactory, ResourceManager resourceManager,
                                             BiConsumer<String, JsonElement> readableResourceObserver, BiConsumer<String, JsonElement> eventResourceObserver) {
        AuthenticationHandshakeHandler authHandlerMock = mock(AuthenticationHandshakeHandler.class); //always accept, don't check for nulls
        JsonSession session = new JsonSession(authHandlerMock, clientFactory, resourceManager);
        session.setReadableResourceObserver(readableResourceObserver);
        session.setEventResourceObserver(eventResourceObserver);
        PublicIdentityCertificate cert = new PublicIdentityCertificate(playerId, ZERO, ZERO, ZERO);
        ClientAuthenticationMessage message = new ClientAuthenticationMessage(new HandshakeHello(EMPTY, cert, 0), EMPTY);
        session.initAuthentication();
        assertEquals(ActionResult.Status.OK, session.finishAuthentication(GSON.toJsonTree(message)).getStatus());
        return session;
    }

    private JsonSession setupAlwaysAccepting(String playerId, EntityManager entityManager) {
        return setupAlwaysAccepting(playerId, new HeadlessClientFactory(entityManager), resourceManagerMock, null, null);
    }

    @Test
    @Ignore //TODO: either remove this test or refactor EngineRunner to be a singleton that can be mocked instead of a class with static methods
    public void testEntityRegistration() {
        EntityManager entityManagerMock = mock(EntityManager.class);
        EntityRef clientEntityRefMock = mock(EntityRef.class);
        when(entityManagerMock.create("engine:client")).thenReturn(clientEntityRefMock);
        when(clientEntityRefMock.getComponent(ClientComponent.class)).thenReturn(new ClientComponent());

        EntityRef clientInfoEntityRefMock = mock(EntityRef.class);
        ClientInfoComponent clientInfo = new ClientInfoComponent();
        clientInfo.playerId = "testUserId";
        when(clientInfoEntityRefMock.exists()).thenReturn(true);
        when(clientInfoEntityRefMock.getComponent(ClientInfoComponent.class)).thenReturn(clientInfo);
        when(entityManagerMock.getEntitiesWith(ClientInfoComponent.class)).thenReturn(Arrays.asList(clientInfoEntityRefMock));

        JsonSession session = setupAlwaysAccepting("testUserId", entityManagerMock);
        assertTrue(session.isAuthenticated());
        verify(entityManagerMock, times(2)).create("engine:client"); //2 because one is for the anonymous client
        session.disconnect();
        assertFalse(session.isAuthenticated());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReadResource() throws ResourceAccessException {
        ClientEntityMockBundle clientMock = new ClientEntityMockBundle();

        ReadableResource<String> readableResource = mock(ReadableResource.class);
        when(readableResource.read(clientMock.client)).thenReturn("testValue");
        ResourceManager resourceManager = mock(ResourceManager.class);
        when(resourceManager.getAs("testResource", ReadableResource.class)).thenReturn(readableResource);

        JsonSession session = setupAlwaysAccepting("testPlayerId", clientMock.factoryMock, resourceManager, null, null);
        assertResult(ActionResult.Status.OK, new JsonPrimitive("testValue"), session.readResource("testResource"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWriteResource() throws ResourceAccessException {
        ClientEntityMockBundle clientMock = new ClientEntityMockBundle();

        WritableResource<String> writableResource = mock(WritableResource.class);
        when(writableResource.getDataType()).thenReturn(String.class);
        ResourceManager resourceManager = mock(ResourceManager.class);
        when(resourceManager.getAs("testResource", WritableResource.class)).thenReturn(writableResource);

        JsonSession session = setupAlwaysAccepting("testPlayerId", clientMock.factoryMock, resourceManager, null, null);
        assertEquals(ActionResult.Status.OK, session.writeResource("testResource", new JsonPrimitive("testValue")).getStatus());
        verify(writableResource).write(clientMock.client, "testValue");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testObservableReadableResource() throws ResourceAccessException {
        ClientEntityMockBundle clientMock = new ClientEntityMockBundle();

        ObservableReadableResource<String> observableReadableResource = new ObservableReadableResourceMock();
        ResourceManager resourceManager = mock(ResourceManager.class);
        when(resourceManager.getAll(ObservableReadableResource.class)).thenReturn(Sets.newHashSet(observableReadableResource));

        BiConsumer<String, JsonElement> observer = mock(BiConsumer.class);

        setupAlwaysAccepting("testPlayerId", clientMock.factoryMock, resourceManager, observer, null);
        verify(observer, times(0)).accept(any(), any());
        observableReadableResource.notifyChanged(clientMock.client.getEntity());
        verify(observer, times(1)).accept(observableReadableResource.getName(), new JsonPrimitive("test"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEventEmittingResource() throws ResourceAccessException {
        ClientEntityMockBundle clientMock = new ClientEntityMockBundle();

        EventEmittingResource<String> eventEmittingResource = new EventEmittingResourceMock();
        ResourceManager resourceManager = mock(ResourceManager.class);
        when(resourceManager.getAll(EventEmittingResource.class)).thenReturn(Sets.newHashSet(eventEmittingResource));

        BiConsumer<String, JsonElement> observer = mock(BiConsumer.class);

        setupAlwaysAccepting("testPlayerId", clientMock.factoryMock, resourceManager, null, observer);
        verify(observer, times(0)).accept(any(), any());
        eventEmittingResource.notifyEvent(clientMock.client.getEntity(), "test");
        verify(observer, times(1)).accept(eventEmittingResource.getName(), new JsonPrimitive("test"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAnonymousReadResource() throws ResourceAccessException {
        AnonymousClientEntityMockBundle clientMock = new AnonymousClientEntityMockBundle();

        ReadableResource<String> readableResource = mock(ReadableResource.class);
        when(readableResource.read(clientMock.client)).thenReturn("testValue");
        ResourceManager resourceManager = mock(ResourceManager.class);
        when(resourceManager.getAs("testResource", ReadableResource.class)).thenReturn(readableResource);

        JsonSession session = new JsonSession(null, clientMock.factoryMock, resourceManager);
        assertResult(ActionResult.Status.OK, new JsonPrimitive("testValue"), session.readResource("testResource"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAnonymousCantWriteResource() throws ResourceAccessException {
        AnonymousClientEntityMockBundle clientMock = new AnonymousClientEntityMockBundle();

        WritableResource<String> writableResource = mock(WritableResource.class);
        when(writableResource.getDataType()).thenReturn(String.class);
        ResourceManager resourceManager = mock(ResourceManager.class);
        when(resourceManager.getAs("testResource", WritableResource.class)).thenReturn(writableResource);

        JsonSession session = new JsonSession(null, clientMock.factoryMock, resourceManager);
        assertEquals(ActionResult.Status.UNAUTHORIZED, session.writeResource("testResource", new JsonPrimitive("testValue")).getStatus());
        verify(writableResource, times(0)).write(clientMock.client, "testValue");
    }
}
