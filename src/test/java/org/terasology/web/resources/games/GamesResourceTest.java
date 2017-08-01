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
package org.terasology.web.resources.games;

import org.junit.Before;
import org.junit.Test;
import org.terasology.context.Context;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.network.Client;
import org.terasology.registry.InjectionHelper;
import org.terasology.web.ServerAdminsManager;
import org.terasology.web.resources.ResourceAccessException;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GamesResourceTest {

    private ModuleManager moduleManagerMock;
    private GameAction actionMock;
    private GamesResource gamesResource;

    @Before
    public void setUp() {
        ServerAdminsManager.setAdminList(Arrays.asList("admin1", "admin2"));
        moduleManagerMock = mock(ModuleManager.class);
        actionMock = mock(GameAction.class);
        Context contextMock = mock(Context.class);
        when(contextMock.get(ModuleManager.class)).thenReturn(moduleManagerMock);
        gamesResource = new GamesResource();
        InjectionHelper.inject(gamesResource, contextMock);
    }

    @Test(expected = ResourceAccessException.class)
    public void testWriteNotAllowed() throws ResourceAccessException {
        gamesResource.write(mockClient("user"), actionMock);
    }

    @Test
    public void testWriteOk() throws ResourceAccessException {
        gamesResource.write(mockClient("admin2"), actionMock);
        verify(actionMock, times(1)).perform(PathManager.getInstance(), moduleManagerMock);
    }

    private Client mockClient(String clientId) {
        Client result = mock(Client.class);
        when(result.getId()).thenReturn(clientId);
        return result;
    }
}
