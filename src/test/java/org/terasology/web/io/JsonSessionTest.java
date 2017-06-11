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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
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


import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
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
        public void authenticate(ClientAuthenticationMessage authenticationMessage) throws AuthenticationFailedException {
            Preconditions.checkNotNull(authenticationMessage.getClientHello());
            Preconditions.checkNotNull(authenticationMessage.getSignature());
            if (!nextResult) {
                throw new AuthenticationFailedException(true);
            }
        }
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(BigInteger.class, BigIntegerBase64Serializer.getInstance())
            .registerTypeAdapter(byte[].class, ByteArrayBase64Serializer.getInstance())
            .create();

    @Test
    public void testAuthentication() {
        AuthenticationHandshakeHandlerMock authHandlerMock = new AuthenticationHandshakeHandlerMock();
        HeadlessClientFactory headlessClientFactoryMock = mock(HeadlessClientFactory.class);
        when(headlessClientFactoryMock.connectNewHeadlessClient("testId")).thenReturn(new HeadlessClient("testId"));
        JsonSession session = new JsonSession(authHandlerMock, headlessClientFactoryMock);
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
        assertEquals(ActionResult.Status.OK, session.finishAuthentication(dummyClientMessage).getStatus());
        assertTrue(session.isAuthenticated());
        assertEquals(ActionResult.Status.UNAUTHORIZED, session.finishAuthentication(dummyClientMessage).getStatus()); //already authenticated
    }

    private JsonSession setupAlwaysAccepting(String playerId, EntityManager entityManager) {
        AuthenticationHandshakeHandler authHandlerMock = mock(AuthenticationHandshakeHandler.class); //always accept, don't check for nulls
        JsonSession session = new JsonSession(authHandlerMock, new HeadlessClientFactory(entityManager));
        PublicIdentityCertificate cert = new PublicIdentityCertificate(playerId, null, null, null);
        ClientAuthenticationMessage message = new ClientAuthenticationMessage(new HandshakeHello(null, cert, 0), null);
        session.initAuthentication();
        session.finishAuthentication(GSON.toJsonTree(message));
        return session;
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
}
