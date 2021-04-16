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
package org.terasology.web.resources.worldGenerators;

import org.junit.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.engine.world.generator.internal.WorldGeneratorInfo;
import org.terasology.engine.world.generator.internal.WorldGeneratorManager;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourcePath;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AvailableWorldGeneratorsResourceTest {

    @Test
    public void testRead() throws ResourceAccessException {
        WorldGeneratorManager managerMock = mock(WorldGeneratorManager.class);
        WorldGeneratorInfo worldGen1 = new WorldGeneratorInfo(new SimpleUri("module1:worldgen1"), "", "");
        WorldGeneratorInfo worldGen2 = new WorldGeneratorInfo(new SimpleUri("module1:worldgen2"), "", "");
        when(managerMock.getWorldGenerators()).thenReturn(Arrays.asList(worldGen1, worldGen2));
        Context contextMock = mock(Context.class);
        when(contextMock.get(WorldGeneratorManager.class)).thenReturn(managerMock);
        AvailableWorldGeneratorsResource resource = new AvailableWorldGeneratorsResource();
        InjectionHelper.inject(resource, contextMock);

        List<WorldGeneratorInfo> result = resource.getGetMethod(ResourcePath.createEmpty()).perform(null, null);
        assertEquals(2, result.size());
        assertTrue(result.contains(worldGen1));
        assertTrue(result.contains(worldGen2));
    }
}
