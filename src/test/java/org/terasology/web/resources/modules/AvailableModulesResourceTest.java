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
import org.junit.Before;
import org.junit.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.i18n.I18nMap;
import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleRegistry;
import org.terasology.naming.Name;
import org.terasology.naming.Version;
import org.terasology.web.resources.base.ResourceAccessException;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AvailableModulesResourceTest {

    private Module moduleMock2;
    private Module moduleMock1;
    private AvailableModulesResource availableModulesResource;

    @Before
    public void setUp() {
        moduleMock2 = mockModule(new Name("module2"), new Version("1.1.0"), "Module 2");
        moduleMock1 = mockModule(new Name("module1"), new Version("1.0.0"), "Module 1");
        Set<Module> modules = ImmutableSet.of(moduleMock1, moduleMock2);

        ModuleRegistry moduleRegistryMock = mock(ModuleRegistry.class);
        when(moduleRegistryMock.stream()).thenReturn(modules.stream());
        ModuleManager moduleManagerMock = mock(ModuleManager.class);
        when(moduleManagerMock.getRegistry()).thenReturn(moduleRegistryMock);
        Context contextMock = mock(Context.class);
        when(contextMock.get(ModuleManager.class)).thenReturn(moduleManagerMock);

        availableModulesResource = new AvailableModulesResource();
        InjectionHelper.inject(availableModulesResource, contextMock);
    }

    @Test
    public void testReadCollection() throws ResourceAccessException {
        List<ModuleMetadata> result = availableModulesResource.getGetCollectionMethod().perform(null, null);
        assertEquals(2, result.size());
        assertEquals(moduleMock1.getMetadata(), result.get(0));
        assertEquals(moduleMock2.getMetadata(), result.get(1));
    }

    @Test
    public void testReadItemOk() throws ResourceAccessException {
        ModuleMetadata result = availableModulesResource.getGetItemMethod("module1").perform(null, null);
        assertEquals(moduleMock1.getMetadata(), result);
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
