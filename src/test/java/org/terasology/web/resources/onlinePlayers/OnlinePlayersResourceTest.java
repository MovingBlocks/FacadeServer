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
package org.terasology.web.resources.onlinePlayers;

import org.junit.Test;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.network.Client;
import org.terasology.network.NetworkSystem;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.registry.InjectionHelper;
import org.terasology.rendering.nui.Color;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceObserver;
import org.terasology.web.resources.base.ResourcePath;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OnlinePlayersResourceTest {

    private Client mockClient(String id, String name, Color color) {
        Client result = mock(Client.class);
        when(result.getId()).thenReturn(id);
        when(result.getName()).thenReturn(name);
        when(result.getColor()).thenReturn(color);
        return result;
    }

    @Test
    public void testRead() throws ResourceAccessException {
        NetworkSystem networkSystemMock = mock(NetworkSystem.class);
        List<Client> clientMocks = Arrays.asList(
                mockClient("id1", "name1", new Color(255, 0, 0, 255)),
                mockClient("id2", "name2", new Color(0, 255, 0, 255))
        );
        when(networkSystemMock.getPlayers()).thenReturn(clientMocks);

        Context context = new ContextImpl();
        context.put(NetworkSystem.class, networkSystemMock);
        OnlinePlayersResource onlinePlayersResource = new OnlinePlayersResource();
        InjectionHelper.inject(onlinePlayersResource, context);
        List<OnlinePlayerMetadata> expectedResult = Arrays.asList(
                new OnlinePlayerMetadata("id1", "name1", new Color(255, 0, 0, 255)),
                new OnlinePlayerMetadata("id2", "name2", new Color(0, 255, 0, 255))
        );
        assertEquals(expectedResult, onlinePlayersResource.getGetMethod(ResourcePath.createEmpty()).perform(null, null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNotification() {
        ResourceObserver resourceObserverMock = mock(ResourceObserver.class);

        Context context = new ContextImpl();
        context.put(NetworkSystem.class, mock(NetworkSystem.class));
        OnlinePlayersResource onlinePlayersResource = new OnlinePlayersResource();
        InjectionHelper.inject(onlinePlayersResource, context);
        onlinePlayersResource.setObserver(resourceObserverMock);
        onlinePlayersResource.onConnected(mock(ConnectedEvent.class), EntityRef.NULL);
        onlinePlayersResource.onDisconnected(mock(DisconnectedEvent.class), EntityRef.NULL);

        //2 times because one for Connected and one for Disconnected events
        verify(resourceObserverMock, times(2)).onChangedForAllClients(ResourcePath.createEmpty(), onlinePlayersResource);
    }
}
