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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.modes.GameState;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.registry.InjectionHelper;
import org.terasology.web.client.HeadlessClient;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.base.InputParser;
import org.terasology.web.resources.base.Resource;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.resources.base.ResourceMethodName;
import org.terasology.web.resources.base.ResourceObserver;
import org.terasology.web.resources.base.ResourcePath;
import org.terasology.web.resources.base.RouterResource;
import org.terasology.web.resources.config.ServerMotdResource;
import org.terasology.web.resources.config.ServerPortResource;
import org.terasology.web.resources.console.ConsoleResource;
import org.terasology.web.resources.engineState.EngineStateResource;
import org.terasology.web.resources.games.GamesResource;
import org.terasology.web.resources.modules.AvailableModulesResource;
import org.terasology.web.resources.modules.ModuleInstallerResource;
import org.terasology.web.resources.onlinePlayers.OnlinePlayersResource;
import org.terasology.web.resources.serverAdmins.AdminPermissionListResource;
import org.terasology.web.resources.serverAdmins.AdminPermissionResource;
import org.terasology.web.resources.serverAdmins.ServerAdminsResource;
import org.terasology.web.resources.systemStatus.SystemResource;
import org.terasology.web.resources.worldGenerators.AvailableWorldGeneratorsResource;
import org.terasology.web.serverAdminManagement.AdminPermissionManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ResourceManager implements ResourceObserver {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);
    private static final ResourceManager INSTANCE = new ResourceManager();

    private RouterResource rootResource;
    private Map<ResourcePath, Set<ResourcePath>> additionalResourcesToUpdate;
    private Map<EntityRef, BiConsumer<ResourcePath, Object>> eventListeners = new HashMap<>();
    private Map<EntityRef, BiConsumer<ResourcePath, Object>> updateSubscribers = new HashMap<>();
    private Map<EntityRef, HeadlessClient> clientLookup = new HashMap<>();

    private ResourceManager() {
    }

    public static ResourceManager getInstance() {
        return INSTANCE;
    }

    public void initialize(TerasologyEngine gameEngine) {
        GameState gameState = gameEngine.getState();
        Context context = gameState.getContext();

        Consumer<Resource> resourceInitializer = (resource) -> initializeResource(context, resource);
        SystemResource systemResource = new SystemResource();
        rootResource = new RouterResource.Builder(resourceInitializer)
                .addSubResource("onlinePlayers", new OnlinePlayersResource())
                .addSubResource("console", new ConsoleResource())
                .addSubResource("games", new GamesResource())
                .addSubResource("engineState", new EngineStateResource())
                .addSubResource("modules", new RouterResource.Builder(resourceInitializer)
                        .addSubResource("available", new AvailableModulesResource())
                        .addSubResource("installer", new ModuleInstallerResource())
                        .build())
                .addSubResource("worldGenerators", new AvailableWorldGeneratorsResource())
                .addSubResource("config", new RouterResource.Builder(resourceInitializer)
                        .addSubResource("serverPort", new ServerPortResource())
                        .addSubResource("MOTD", new ServerMotdResource())
                        .build())
                .addSubResource("serverAdmins", new ServerAdminsResource())
                .addSubResource("serverAdminPermissions", new AdminPermissionListResource())
                .addSubResource("system", systemResource)
                .build();
        systemResource.startSystemInfoRefreshService();
        InjectionHelper.inject(AdminPermissionManager.getInstance(), context);
        rootResource.setObserver(this);
        additionalResourcesToUpdate = new HashMap<>();
        // when /modules/installer changes, also update /modules/available and /worldGenerators
        additionalResourcesToUpdate.put(new ResourcePath("modules", "installer"), new HashSet<>(Arrays.asList(
                new ResourcePath("modules", "available"),
                new ResourcePath("worldGenerators"))));
        rootResource.notifyChangedForAllClients();
    }

    private void initializeResource(Context context, Resource resource) {
        ComponentSystemManager componentSystemManager = context.get(ComponentSystemManager.class);
        if (resource instanceof ComponentSystem && componentSystemManager != null) {
            // this will both inject fields and register event handlers
            componentSystemManager.register((ComponentSystem) resource);
        } else {
            // if it's not a ComponentSystem (thus no need to receive events) only field injection is performed
            InjectionHelper.inject(resource, context);
        }
    }

    private ResourceMethod getResourceMethod(Resource resource, ResourcePath path, ResourceMethodName methodName, HeadlessClient client) throws ResourceAccessException {
        ResourceMethod method = resource.getMethod(methodName, path);
        if (!method.clientIsAllowed(client.getSecurityInfoWithAdminPermission(method.getPermissionType()))) {
            // TODO: possibly provide a way to explain a reason for denied access (unauthenticated or not admin)
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.FORBIDDEN, "You are not allowed to access this resource."));
        }
        return method;
    }

    private ResourceMethod getResourceMethod(ResourcePath path, ResourceMethodName methodName, HeadlessClient client) throws ResourceAccessException {
        return getResourceMethod(rootResource, path, methodName, client);
    }

    private ResourceMethod getResourceMethod(Resource resource, ResourceMethodName methodName, HeadlessClient client) throws ResourceAccessException {
        return getResourceMethod(resource, ResourcePath.createEmpty(), methodName, client);
    }

    public <T> Object performAction(ResourcePath path, ResourceMethodName methodName, T inputData, InputParser<T> inputParser, HeadlessClient client)
            throws ResourceAccessException {
        ResourceMethod method = getResourceMethod(path, methodName, client);
        return method.perform(inputParser.parse(inputData, method.getInType()), client);
    }

    public void addClient(HeadlessClient client, BiConsumer<ResourcePath, Object> updateSubscriber, BiConsumer<ResourcePath, Object> eventListener) {
        eventListeners.put(client.getEntity(), eventListener);
        updateSubscribers.put(client.getEntity(), updateSubscriber);
        clientLookup.put(client.getEntity(), client);
    }

    public void removeClient(HeadlessClient client) {
        eventListeners.remove(client.getEntity());
        updateSubscribers.remove(client.getEntity());
        clientLookup.remove(client.getEntity());
    }

    @Override
    public void onEvent(ResourcePath senderPath, Object eventData, EntityRef targetClientEntity) {
        eventListeners.get(targetClientEntity).accept(senderPath, eventData);
    }

    @Override
    public void onChangedForClient(ResourcePath senderPath, Resource sender, EntityRef targetClientEntity) {
        HeadlessClient client = clientLookup.get(targetClientEntity);
        if (client == null) {
            logger.warn("Failed to send update to client with entity ID" + targetClientEntity.getId() + " (corresponding client not registered)");
        }
        Set<ResourcePath> updatesToSend = new HashSet<>();
        updatesToSend.add(senderPath);
        updatesToSend.addAll(additionalResourcesToUpdate.getOrDefault(senderPath, Collections.emptySet()));
        for (ResourcePath path: updatesToSend) {
            try {
                ResourceMethod resourceGetMethod = getResourceMethod(path.clone(), ResourceMethodName.GET, client);
                if (!resourceGetMethod.getInType().equals(Void.class)) {
                    throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR, "This resource's GET method requires input data"));
                }
                updateSubscribers.get(targetClientEntity).accept(path, resourceGetMethod.perform(null, client));
            } catch (ResourceAccessException ex) {
                logger.warn("Failed to send update for resource at path " + path.toString(), ex);
            }
        }
    }

    @Override
    public void onChangedForAllClients(ResourcePath senderPath, Resource sender) {
        updateSubscribers.keySet().forEach((clientEntity) -> onChangedForClient(senderPath, sender, clientEntity));
    }
}
