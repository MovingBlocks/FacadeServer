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
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.modes.StateIngame;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.games.GamesResource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ResourceManager {

    private static final ResourceManager INSTANCE = new ResourceManager();
    private Map<String, Resource> resources;
    private Set<EngineStateChangeObserver> stateChangeObservers = new HashSet<>();

    private ResourceManager() {
    }

    public static ResourceManager getInstance() {
        return INSTANCE;
    }

    public void initialize(TerasologyEngine gameEngine) {
        GameState gameState = gameEngine.getState();
        ModuleManager moduleManager = gameEngine.getFromEngineContext(ModuleManager.class);
        Context context = gameState != null ? gameState.getContext() : null;

        resources = new HashMap<>();
        registerAndPutResource(context, new EngineStateResource(gameEngine));
        if (gameState instanceof StateIngame) {
            registerAndPutResource(context, new ConsoleResource());
            registerAndPutResource(context, new GamesResource(moduleManager));
            registerAndPutResource(context, new AvailableModulesResource());
            registerAndPutResource(context, new OnlinePlayersResource());
        } else {
            registerAndPutResource(context, new GamesResource(moduleManager));
            registerAndPutResource(context, new AvailableModulesResource());
            //TODO: add server config resource
        }
        if (gameState != null) {
            //all the resources have been re-initialize, so notify all the clients
            updateAllClients(gameState);
        }
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

    public void addEngineStateChangeObserver(EngineStateChangeObserver observer) {
        stateChangeObservers.add(observer);
    }

    public void removeEngineStateChangeObserver(EngineStateChangeObserver observer) {
        stateChangeObservers.remove(observer);
    }

    private void updateAllClients(GameState newState) {
        Set<ReadableResource> readableResources = getAll(ReadableResource.class);
        for (EngineStateChangeObserver observer: stateChangeObservers) {
            observer.notifyUpdate(newState, readableResources);
        }
    }
}
