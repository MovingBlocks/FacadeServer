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
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.AbstractItemCollectionResource;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceMethod;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;

// TODO: make observable and update after installation (how to link the two resources?)
public class AvailableModulesResource extends AbstractItemCollectionResource {

    @In
    private ModuleManager moduleManager;

    @Override
    protected ResourceMethod<Void, List<ModuleMetadata>> getGetCollectionMethod() throws ResourceAccessException {
        return createParameterlessMethod(ClientSecurityRequirements.PUBLIC, Void.class,
                (data, client) -> getMetadataStream()
                        .sorted(Comparator.comparing(ModuleMetadata::getDisplayName, Comparator.comparing(I18nMap::value)))
                        .collect(Collectors.toList()));
    }

    @Override
    protected ResourceMethod<Void, ModuleMetadata> getGetItemMethod(String itemId) throws ResourceAccessException {
        return createParameterlessMethod(ClientSecurityRequirements.PUBLIC, Void.class, (data, client) -> {
            Optional<ModuleMetadata> result = getMetadataStream()
                    .filter(metadata -> metadata.getId().equals(new Name(itemId)))
                    .findFirst();
            if (!result.isPresent()) {
                throw ResourceAccessException.NOT_FOUND;
            }
            return result.get();
        });
    }

    // TODO: implement DELETE method

    private Stream<ModuleMetadata> getMetadataStream() {
        return moduleManager.getRegistry().stream().map(Module::getMetadata);
    }
}
