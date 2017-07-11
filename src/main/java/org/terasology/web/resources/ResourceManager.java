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
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.modes.StateIngame;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.web.io.ActionResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ResourceManager {

    private static final ResourceManager INSTANCE = new ResourceManager();
    private Map<String, Resource> resources;
    private Set<Consumer<ReadableResource>> stateChangeObservers = new HashSet<>();

    private ResourceManager() {
    }

    public static ResourceManager getInstance() {
        return INSTANCE;
    }

    public void initialize(GameEngine gameEngine) {
        GameState gameState = gameEngine.getState();
        Context context = gameState != null ? gameState.getContext() : null;
        resources = new HashMap<>();
        registerAndPutResource(context, new EngineStateResource(gameEngine));
        if (gameState instanceof StateIngame) {
            registerAndPutResource(context, new ConsoleResource());
            registerAndPutResource(context, new GamesResource());
            registerAndPutResource(context, new OnlinePlayersResource());
        } else {
            registerAndPutResource(context, new GamesResource());
            //TODO: add server config resource
        }
        //all the resources have been re-initialize, so notify all the clients
        updateAllClients();
    }

    private void registerAndPutResource(Context context, Resource resource) {
        if (!resources.containsValue(resource)) {
            if (context != null && resource instanceof ComponentSystem) {
                context.get(ComponentSystemManager.class).register((ComponentSystem) resource);
            }
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

    public void addEngineStateChangeObserver(Consumer<ReadableResource> observer) {
        stateChangeObservers.add(observer);
    }

    public void removeEngineStateChangeObserver(Consumer<ReadableResource> observer) {
        stateChangeObservers.remove(observer);
    }

    private void updateAllClients() {
        for (Consumer<ReadableResource> client: stateChangeObservers) {
            for (ReadableResource resource: getAll(ReadableResource.class)) {
                client.accept(resource);
            }
        }
    }
}
