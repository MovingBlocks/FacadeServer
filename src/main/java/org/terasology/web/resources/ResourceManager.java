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
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.registry.InjectionHelper;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.config.ServerMotdResource;
import org.terasology.web.resources.config.ServerPortResource;
import org.terasology.web.resources.games.GamesResource;
import org.terasology.web.resources.modules.AvailableModulesResource;
import org.terasology.web.resources.modules.ModuleInstallerResource;

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
        Context context = gameState.getContext();

        resources = new HashMap<>();
        registerAndPutResource(context, new EngineStateResource());
        registerAndPutResource(context, new GamesResource());
        registerAndPutResource(context, new AvailableModulesResource());
        registerAndPutResource(context, new ServerAdminsResource());
        registerAndPutResource(context, new ServerMotdResource());
        registerAndPutResource(context, new ServerPortResource());
        registerAndPutResource(context, new ModuleInstallerResource());
        if (gameState instanceof StateIngame) {
            registerAndPutResource(context, new ConsoleResource());
            registerAndPutResource(context, new OnlinePlayersResource());
        }
        //all the resources have been re-initialized, so notify all the clients
        updateAllClients(gameState);
    }

    private void registerAndPutResource(Context context, Resource resource) {
        if (resources.containsKey(resource.getName())) {
            throw new IllegalArgumentException("This type of resource has already been registered");
        }
        if (resource instanceof ComponentSystem) {
            // this will both inject fields and register event handlers
            context.get(ComponentSystemManager.class).register((ComponentSystem) resource);
        } else {
            // if it's not a ComponentSystem (thus no need to receive events) only field injection is performed
            InjectionHelper.inject(resource, context);
        }
        resources.put(resource.getName(), resource);
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
