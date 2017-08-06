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
package org.terasology.web.resources.modules;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.i18n.I18nMap;
import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleRegistry;
import org.terasology.naming.Name;
import org.terasology.naming.Version;
import org.terasology.registry.InjectionHelper;
import org.terasology.web.resources.ResourceAccessException;
import org.terasology.world.generator.internal.WorldGeneratorInfo;
import org.terasology.world.generator.internal.WorldGeneratorManager;

import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AvailableModulesResourceTest {

    @Test
    public void testRead() throws ResourceAccessException {
        Module moduleMock2 = mockModule(new Name("module2"), new Version("1.1.0"), "Module 2");
        Module moduleMock1 = mockModule(new Name("module1"), new Version("1.0.0"), "Module 1");
        Set<Module> modules = ImmutableSet.of(moduleMock1, moduleMock2);

        ModuleRegistry moduleRegistryMock = mock(ModuleRegistry.class);
        when(moduleRegistryMock.stream()).thenReturn(modules.stream());
        ModuleManager moduleManagerMock = mock(ModuleManager.class);
        when(moduleManagerMock.getRegistry()).thenReturn(moduleRegistryMock);

        WorldGeneratorManager worldGeneratorManagerMock = mock(WorldGeneratorManager.class);
        WorldGeneratorInfo worldGen1 = new WorldGeneratorInfo(new SimpleUri("module1:worldgen1"), "", "");
        WorldGeneratorInfo worldGen2 = new WorldGeneratorInfo(new SimpleUri("module1:worldgen2"), "", "");
        when(worldGeneratorManagerMock.getWorldGenerators()).thenReturn(Arrays.asList(worldGen1, worldGen2));

        Context contextMock = mock(Context.class);
        when(contextMock.get(ModuleManager.class)).thenReturn(moduleManagerMock);
        when(contextMock.get(WorldGeneratorManager.class)).thenReturn(worldGeneratorManagerMock);

        AvailableModulesResource availableModulesResource = new AvailableModulesResource();
        InjectionHelper.inject(availableModulesResource, contextMock);

        AvailableModulesData result = availableModulesResource.read(null);
        assertEquals(2, result.getModules().size());
        assertEquals(moduleMock1.getMetadata(), result.getModules().get(0));
        assertEquals(moduleMock2.getMetadata(), result.getModules().get(1));
        assertEquals(2, result.getWorldGenerators().size());
        assertTrue(result.getWorldGenerators().contains(worldGen1));
        assertTrue(result.getWorldGenerators().contains(worldGen2));
    }

    private Module mockModule(Name id, Version version, String displayName) {
        ModuleMetadata metadata = mock(ModuleMetadata.class);
        when(metadata.getId()).thenReturn(id);
        when(metadata.getVersion()).thenReturn(version);
        when(metadata.getDisplayName()).thenReturn(new I18nMap(displayName));
        Module result = mock(Module.class);
        when(result.getMetadata()).thenReturn(metadata);
        return result;
    }

}
