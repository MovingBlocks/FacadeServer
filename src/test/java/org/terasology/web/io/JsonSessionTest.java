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
import com.google.gson.JsonNull;
import org.junit.Test;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.identity.storageServiceClient.BigIntegerBase64Serializer;
import org.terasology.network.ClientComponent;
import org.terasology.network.ClientInfoComponent;
import org.terasology.web.EngineRunner;
import org.terasology.web.authentication.AuthenticationFailedException;
import org.terasology.web.authentication.AuthenticationHandshakeHandler;
import org.terasology.web.authentication.ClientAuthenticationMessage;
import org.terasology.web.authentication.HandshakeHello;
import org.terasology.web.client.AnonymousHeadlessClient;
import org.terasology.web.client.AuthenticatedHeadlessClient;
import org.terasology.web.client.HeadlessClientFactory;
import org.terasology.web.io.gsonUtils.ByteArrayBase64Serializer;
import org.terasology.web.resources.ResourceManager;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceMethodName;


import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsonSessionTest {

    private static final BigInteger ZERO = BigInteger.ZERO;
    private static final byte[] EMPTY = new byte[]{};
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(BigInteger.class, BigIntegerBase64Serializer.getInstance())
            .registerTypeAdapter(byte[].class, ByteArrayBase64Serializer.getInstance())
            .create();

    @Test
    public void testAuthentication() {
        AuthenticationHandshakeHandlerMock authHandlerMock = new AuthenticationHandshakeHandlerMock();
        HeadlessClientFactory headlessClientFactoryMock = mock(HeadlessClientFactory.class);
        when(headlessClientFactoryMock.connectNewHeadlessClient("testId")).thenReturn(new AuthenticatedHeadlessClient("testId"));
        when(headlessClientFactoryMock.connectNewAnonymousHeadlessClient()).thenReturn(new AnonymousHeadlessClient());
        ResourceManager resourceManagerMock = mock(ResourceManager.class);
        JsonSession session = new JsonSession(authHandlerMock, headlessClientFactoryMock, resourceManagerMock);
        JsonElement dummyClientMessage = GSON.toJsonTree(new ClientAuthenticationMessage(
                new HandshakeHello(EMPTY, new PublicIdentityCertificate("testId", ZERO, ZERO, ZERO), 0),
                EMPTY));

        assertFalse(session.isAuthenticated());
        assertEquals(ActionResult.Status.OK, session.initAuthentication().getStatus());
        assertFalse(session.isAuthenticated());
        assertEquals(ActionResult.Status.BAD_REQUEST, session.finishAuthentication(JsonNull.INSTANCE).getStatus());
        assertFalse(session.isAuthenticated());
        assertEquals(ActionResult.Status.FORBIDDEN, session.finishAuthentication(dummyClientMessage).getStatus());
        assertFalse(session.isAuthenticated());
        authHandlerMock.nextResult = true;
        ActionResult result = session.finishAuthentication(dummyClientMessage);
        assertEquals(ActionResult.Status.OK, result.getStatus());
        assertEquals("AAAA", result.getData().getAsString()); //"AAAA" is the base64 of three 0 bytes, returned by the mock authenticate method
        assertTrue(session.isAuthenticated());
        assertEquals(ActionResult.Status.FORBIDDEN, session.finishAuthentication(dummyClientMessage).getStatus()); //already authenticated
    }

    private JsonSession setupAlwaysAccepting(String playerId, HeadlessClientFactory clientFactory, ResourceManager resourceManager) {
        AuthenticationHandshakeHandler authHandlerMock = mock(AuthenticationHandshakeHandler.class); //always accept, don't check for nulls
        JsonSession session = new JsonSession(authHandlerMock, clientFactory, resourceManager);
        PublicIdentityCertificate cert = new PublicIdentityCertificate(playerId, ZERO, ZERO, ZERO);
        ClientAuthenticationMessage message = new ClientAuthenticationMessage(new HandshakeHello(EMPTY, cert, 0), EMPTY);
        session.initAuthentication();
        assertEquals(ActionResult.Status.OK, session.finishAuthentication(GSON.toJsonTree(message)).getStatus());
        return session;
    }

    private JsonSession setupAlwaysAccepting(String playerId, EntityManager entityManager, ResourceManager resourceManager) {
        EngineRunner engineRunnerMock = mock(EngineRunner.class);
        when(engineRunnerMock.isRunningGame()).thenReturn(true);
        return setupAlwaysAccepting(playerId, new HeadlessClientFactory(entityManager, engineRunnerMock), resourceManager);
    }

    @Test
    public void testClientRegistration() {
        EntityManager entityManagerMock = mock(EntityManager.class);
        EntityRef clientEntityRefMock = mock(EntityRef.class);
        when(entityManagerMock.create("engine:client")).thenReturn(clientEntityRefMock);
        when(clientEntityRefMock.getComponent(ClientComponent.class)).thenReturn(new ClientComponent());

        EntityRef clientInfoEntityRefMock = mock(EntityRef.class);
        ClientInfoComponent clientInfo = new ClientInfoComponent();
        clientInfo.playerId = "testUserId";
        when(clientInfoEntityRefMock.exists()).thenReturn(true);
        when(clientInfoEntityRefMock.getComponent(ClientInfoComponent.class)).thenReturn(clientInfo);
        when(entityManagerMock.getEntitiesWith(ClientInfoComponent.class)).thenReturn(Collections.singletonList(clientInfoEntityRefMock));
        ResourceManager resourceManagerMock = mock(ResourceManager.class);

        JsonSession session = setupAlwaysAccepting("testUserId", entityManagerMock, resourceManagerMock);
        assertTrue(session.isAuthenticated());
        verify(entityManagerMock, times(2)).create("engine:client"); //2 because one is for the anonymous client
        verify(resourceManagerMock, times(2)).addClient(any(), any(), any());
        session.disconnect();
        assertFalse(session.isAuthenticated());
    }

    @Test
    public void testResourceRequest() throws ResourceAccessException {
        ResourceManager resourceManagerMock = mock(ResourceManager.class);
        when(resourceManagerMock.performAction(any(), any(), any(), any(), any())).thenReturn("someStringResult");

        JsonSession session = new JsonSession(null, mock(HeadlessClientFactory.class), resourceManagerMock);
        ActionResult result = session.accessResource(Arrays.asList("testResources", "someTestResource"), ResourceMethodName.GET, null);
        assertEquals(ActionResult.Status.OK, result.getStatus());
        assertEquals("someStringResult", result.getData().getAsString());
        assertNull(result.getMessage());
    }

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
}
