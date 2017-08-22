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
import org.terasology.web.resources.base.StreamBasedItemCollectionResource;

import java.util.Comparator;
import java.util.stream.Stream;

public class AvailableModulesResource extends StreamBasedItemCollectionResource<ModuleMetadata> {

    @In
    private ModuleManager moduleManager;

    @Override
    protected Stream<ModuleMetadata> getDataSourceStream() {
        return moduleManager.getRegistry().stream()
                .map(Module::getMetadata)
                .sorted(Comparator.comparing(ModuleMetadata::getDisplayName, Comparator.comparing(I18nMap::value)));
    }

    @Override
    protected boolean itemMatchesId(String itemId, ModuleMetadata item) {
        return item.getId().equals(new Name(itemId));
    }

    // TODO: implement DELETE method to uninstall modules
}
