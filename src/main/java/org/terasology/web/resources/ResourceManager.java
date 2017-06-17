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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.logic.console.Console;

import java.util.Map;
import java.util.Set;

public class ResourceManager {

    private static final ResourceManager INSTANCE = new ResourceManager();
    private Map<String, Resource> resources;

    public static ResourceManager getInstance() {
        return INSTANCE;
    }

    public void initialize(Context context) {
        if (resources == null) {
            resources = Maps.newHashMap();
            putResource(new ConsoleResource(context.get(Console.class)));
        }
        registerEventHandlers(context);
    }

    private void registerEventHandlers(Context context) {
        EventSystem eventSystem = context.get(EntityManager.class).getEventSystem();
        for (Map.Entry<String, Resource> entry: resources.entrySet()) {
            Resource resource = entry.getValue();
            if (resource instanceof ComponentSystem) {
                eventSystem.registerEventHandler((ComponentSystem) resource);
            }
        }
    }

    private void putResource(Resource resource) {
        if (!resources.containsValue(resource)) {
            resources.put(resource.getClass().getSimpleName(), resource);
        } else {
            throw new IllegalArgumentException("This type of resource has already been registered");
        }
    }

    public <T extends Resource> T getAs(String name, Class<T> type) throws UnsupportedResourceTypeException {
        Resource resource = resources.get(name);
        if (type.isAssignableFrom(resource.getClass())) {
            return type.cast(resource);
        }
        throw new UnsupportedResourceTypeException();
    }

    public <T extends Resource> Set<T> getAllAs(Class<T> type) {
        Set<T> result = Sets.newHashSet();
        for (Map.Entry<String, Resource> entry: resources.entrySet()) {
            if (type.isAssignableFrom(entry.getValue().getClass())) {
                result.add(type.cast(entry.getValue()));
            }
        }
        return result;
    }
}
