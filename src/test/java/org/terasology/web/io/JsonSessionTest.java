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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.identity.storageServiceClient.BigIntegerBase64Serializer;
import org.terasology.network.ClientComponent;
import org.terasology.network.ClientInfoComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.web.authentication.AuthenticationFailedException;
import org.terasology.web.authentication.AuthenticationHandshakeHandler;
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
import java.util.Arrays;
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
            Preconditions.checkNotNull(authenticationMessage.getClientHello());
            Preconditions.checkNotNull(authenticationMessage.getSignature());
            if (!nextResult) {
                throw new AuthenticationFailedException(AuthenticationFailedException.INVALID_CLIENT_CERT);
            }
            return new byte[]{0, 0, 0};
        }
    }

    private static class ObservableReadableResourceMock extends ObservableReadableResource<String> {
        @Override
        public String read(EntityRef clientEntity) {
            return "test";
        }
    }

    private static class EventEmittingResourceMock extends EventEmittingResource<String> {
    }

    private static class ClientEntityMockBundle {
        private EntityRef entity;
        private HeadlessClientFactory factoryMock;

        private ClientEntityMockBundle() {
            entity = mock(EntityRef.class);
            HeadlessClient client = mock(HeadlessClient.class);
            when(client.getEntity()).thenReturn(entity);
            factoryMock = mock(HeadlessClientFactory.class);
            when(factoryMock.connectNewHeadlessClient("testPlayerId")).thenReturn(client);
        }
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(BigInteger.class, BigIntegerBase64Serializer.getInstance())
            .registerTypeAdapter(byte[].class, ByteArrayBase64Serializer.getInstance())
            .create();

    private static ResourceManager resourceManagerMock;

    @BeforeClass
    public static void setUpResourceManagerMock() {
        resourceManagerMock = mock(ResourceManager.class);
        when(resourceManagerMock.getAllAs(any())).thenReturn(Sets.newHashSet());
    }

    private void assertResult(ActionResult.Status expectedStatus, JsonElement expectedData, ActionResult actual) {
        assertEquals(expectedStatus, actual.getStatus());
        assertEquals(expectedData, actual.getData());
    }

    @Test
    public void testAuthentication() {
        AuthenticationHandshakeHandlerMock authHandlerMock = new AuthenticationHandshakeHandlerMock();
        HeadlessClientFactory headlessClientFactoryMock = mock(HeadlessClientFactory.class);
        when(headlessClientFactoryMock.connectNewHeadlessClient("testId")).thenReturn(new HeadlessClient("testId"));
        JsonSession session = new JsonSession(authHandlerMock, headlessClientFactoryMock, resourceManagerMock);
        JsonElement dummyClientMessage = GSON.toJsonTree(new ClientAuthenticationMessage(
                new HandshakeHello(null, new PublicIdentityCertificate("testId", null, null, null), 0),
                new byte[2]));

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
        assertEquals(ActionResult.Status.UNAUTHORIZED, session.finishAuthentication(dummyClientMessage).getStatus()); //already authenticated
    }

    private JsonSession setupAlwaysAccepting(String playerId, HeadlessClientFactory clientFactory, ResourceManager resourceManager,
                                             BiConsumer<EntityRef, JsonElement> readableResourceObserver, BiConsumer<EntityRef, JsonElement> eventResourceObserver) {
        AuthenticationHandshakeHandler authHandlerMock = mock(AuthenticationHandshakeHandler.class); //always accept, don't check for nulls
        JsonSession session = new JsonSession(authHandlerMock, clientFactory, resourceManager);
        session.setReadableResourceObserver(readableResourceObserver);
        session.setEventResourceObserver(eventResourceObserver);
        PublicIdentityCertificate cert = new PublicIdentityCertificate(playerId, null, null, null);
        ClientAuthenticationMessage message = new ClientAuthenticationMessage(new HandshakeHello(null, cert, 0), null);
        session.initAuthentication();
        session.finishAuthentication(GSON.toJsonTree(message));
        return session;
    }

    private JsonSession setupAlwaysAccepting(String playerId, EntityManager entityManager) {
        return setupAlwaysAccepting(playerId, new HeadlessClientFactory(entityManager), resourceManagerMock, null, null);
    }

    @Test
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

        // TODO: this is a workaround to AbstractClient.findClientEntityRef taking the EntityManager from CoreRegistry
        // TODO: instead of getting it as an argument from the calling method (AbstractClient.createEntity)
        Context context = new ContextImpl();
        context.put(EntityManager.class, entityManagerMock);
        CoreRegistry.setContext(context);

        JsonSession session = setupAlwaysAccepting("testUserId", entityManagerMock);
        assertTrue(session.isAuthenticated());
        verify(entityManagerMock).create("engine:client");
        session.disconnect();
        assertFalse(session.isAuthenticated());

        session = setupAlwaysAccepting("nonExistingUserId", entityManagerMock);
        assertFalse(session.isAuthenticated());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReadResource() throws UnsupportedResourceTypeException {
        ClientEntityMockBundle clientMock = new ClientEntityMockBundle();

        ReadableResource<String> readableResource = mock(ReadableResource.class);
        when(readableResource.read(clientMock.entity)).thenReturn("testValue");
        ResourceManager resourceManager = mock(ResourceManager.class);
        when(resourceManager.getAs("testResource", ReadableResource.class)).thenReturn(readableResource);

        JsonSession session = setupAlwaysAccepting("testPlayerId", clientMock.factoryMock, resourceManager, null, null);
        assertResult(ActionResult.Status.OK, new JsonPrimitive("testValue"), session.readResource("testResource"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWriteResource() throws UnsupportedResourceTypeException {
        ClientEntityMockBundle clientMock = new ClientEntityMockBundle();

        WritableResource<String> writableResource = mock(WritableResource.class);
        when(writableResource.getDataType()).thenReturn(String.class);
        ResourceManager resourceManager = mock(ResourceManager.class);
        when(resourceManager.getAs("testResource", WritableResource.class)).thenReturn(writableResource);

        JsonSession session = setupAlwaysAccepting("testPlayerId", clientMock.factoryMock, resourceManager, null, null);
        session.writeResource("testResource", new JsonPrimitive("testValue"));
        verify(writableResource).write(clientMock.entity, "testValue");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testObservableReadableResource() throws UnsupportedResourceTypeException {
        ClientEntityMockBundle clientMock = new ClientEntityMockBundle();

        ObservableReadableResource<String> observableReadableResource = new ObservableReadableResourceMock();
        ResourceManager resourceManager = mock(ResourceManager.class);
        when(resourceManager.getAllAs(ObservableReadableResource.class)).thenReturn(Sets.newHashSet(observableReadableResource));

        BiConsumer<EntityRef, JsonElement> observer = mock(BiConsumer.class);

        setupAlwaysAccepting("testPlayerId", clientMock.factoryMock, resourceManager, observer, null);
        verify(observer, times(0)).accept(any(), any());
        observableReadableResource.notifyChanged(clientMock.entity);
        verify(observer, times(1)).accept(clientMock.entity, new JsonPrimitive("test"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEventEmittingResource() throws UnsupportedResourceTypeException {
        ClientEntityMockBundle clientMock = new ClientEntityMockBundle();

        EventEmittingResource<String> eventEmittingResource = new EventEmittingResourceMock();
        ResourceManager resourceManager = mock(ResourceManager.class);
        when(resourceManager.getAllAs(EventEmittingResource.class)).thenReturn(Sets.newHashSet(eventEmittingResource));

        BiConsumer<EntityRef, JsonElement> observer = mock(BiConsumer.class);

        setupAlwaysAccepting("testPlayerId", clientMock.factoryMock, resourceManager, null, observer);
        verify(observer, times(0)).accept(any(), any());
        eventEmittingResource.notifyEvent(clientMock.entity, "test");
        verify(observer, times(1)).accept(clientMock.entity, new JsonPrimitive("test"));
    }
}
