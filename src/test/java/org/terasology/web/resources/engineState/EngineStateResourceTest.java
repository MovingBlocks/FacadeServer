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
package org.terasology.web.resources.engineState;

import org.junit.Before;
import org.junit.Test;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.GameState;
import org.terasology.registry.InjectionHelper;
import org.terasology.web.StateEngineIdle;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourcePath;

import static org.junit.Assert.assertEquals;
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
        Context contextMock = mock(Context.class);
        when(contextMock.get(GameEngine.class)).thenReturn(engineMock);
        engineStateResource = new EngineStateResource();
        InjectionHelper.inject(engineStateResource, contextMock);
    }

    @Test
    public void testWriteOk() throws ResourceAccessException {
        EngineStateMetadata stateMetadataMock = mock(EngineStateMetadata.class);
        engineStateResource.getPutMethod(ResourcePath.EMPTY).perform(stateMetadataMock, null);
        verify(stateMetadataMock, times(1)).switchEngineToThisState(engineMock);
    }

    // TODO: add other write tests

    @Test
    public void testRead() throws ResourceAccessException {
        EngineStateMetadata result = engineStateResource.getGetMethod(ResourcePath.EMPTY).perform(null, null);
        assertEquals(EngineStateMetadata.State.IDLE, result.getState());
        assertEquals(null, result.getGameName());
    }
}
