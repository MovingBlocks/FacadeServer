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

import org.terasology.engine.module.ModuleManager;
import org.terasology.i18n.I18nMap;
import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;
import org.terasology.network.Client;
import org.terasology.web.resources.ReadableResource;
import org.terasology.web.resources.ResourceAccessException;
import org.terasology.world.generator.internal.WorldGeneratorManager;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AvailableModulesResource implements ReadableResource<AvailableModulesData> {

    private ModuleManager moduleManager;
    private WorldGeneratorManager worldGeneratorManager;

    public AvailableModulesResource(ModuleManager moduleManager, WorldGeneratorManager worldGeneratorManager) {
        this.moduleManager = moduleManager;
        this.worldGeneratorManager = worldGeneratorManager;
    }

    @Override
    public String getName() {
        return "availableModules";
    }

    @Override
    public AvailableModulesData read(Client requestingClient) throws ResourceAccessException {
        Stream<ModuleMetadata> modules = moduleManager.getRegistry().stream().map(Module::getMetadata)
                .sorted(Comparator.comparing(ModuleMetadata::getDisplayName, Comparator.comparing(I18nMap::value)));
        return new AvailableModulesData(modules.collect(Collectors.toList()), worldGeneratorManager.getWorldGenerators());
    }
}
