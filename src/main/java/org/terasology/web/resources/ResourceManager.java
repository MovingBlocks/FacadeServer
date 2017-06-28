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

import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.registry.InjectionHelper;
import org.terasology.web.io.ActionResult;

import java.util.HashMap;
import java.util.HashSet;
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
            resources = new HashMap<>();
            putResource(new ConsoleResource());
            putResource(new OnlinePlayersResource());
        }
        injectContext(context);
        registerEventHandlers(context);
    }

    private void injectContext(Context context) {
        for (Resource resource: resources.values()) {
            InjectionHelper.inject(resource, context);
        }
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
            resources.put(resource.getName(), resource);
        } else {
            throw new IllegalArgumentException("This type of resource has already been registered");
        }
    }

    public <T extends Resource> T getAs(String name, Class<T> type) throws ResourceAccessException {
        Resource resource = resources.get(name);
        if (resource == null) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.NOT_FOUND, "Resource not found."));
        }
        if (type.isAssignableFrom(resource.getClass())) {
            return type.cast(resource);
        }
        throw new ResourceAccessException(new ActionResult(ActionResult.Status.ACTION_NOT_ALLOWED, "This resource does not support the requested action."));
    }

    public <T extends Resource> Set<T> getAll(Class<T> type) {
        Set<T> result = new HashSet<>();
        for (Map.Entry<String, Resource> entry: resources.entrySet()) {
            if (type.isAssignableFrom(entry.getValue().getClass())) {
                result.add(type.cast(entry.getValue()));
            }
        }
        return result;
    }
}
