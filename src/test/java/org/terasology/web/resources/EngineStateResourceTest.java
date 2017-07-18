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
package org.terasology.web.resources;

import org.junit.Before;
import org.junit.Test;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.GameState;
import org.terasology.network.Client;
import org.terasology.web.ServerAdminsManager;
import org.terasology.web.StateEngineIdle;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EngineStateResourceTest {

    private GameEngine engineMock;
    private EngineStateResource engineStateResource;

    @Before
    public void setUp() {
        engineMock = mock(GameEngine.class);
        GameState state = new StateEngineIdle();
        when(engineMock.getState()).thenReturn(state);
        ServerAdminsManager.setAdminList(Arrays.asList("serverAdm1", "admin"));
        engineStateResource = new EngineStateResource(engineMock);
    }

    @Test(expected = ResourceAccessException.class)
    public void testWriteFail() throws ResourceAccessException {
        engineStateResource.write(mockClient("someUser"), "");
    }

    @Test
    public void testWriteOk() throws ResourceAccessException {
        engineStateResource.write(mockClient("serverAdm1"), "");
        verify(engineMock, times(1)).changeState(isA(StateEngineIdle.class));
    }

    private Client mockClient(String clientId) {
        Client result = mock(Client.class);
        when(result.getId()).thenReturn(clientId);
        return result;
    }
}
