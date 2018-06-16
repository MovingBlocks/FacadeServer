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
package org.terasology.web.resources.base;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.network.Client;
import org.terasology.web.client.ClientSecurityInfo;
import org.terasology.web.serverAdminManagement.PermissionType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class RouterResource implements Resource {

    private final Resource rootResource;
    private final Map<String, Resource> subResources = new HashMap<>();

    // must use builder
    private RouterResource(Resource rootResource) {
        this.rootResource = rootResource;
    }

    @Override
    public void setObserver(ResourceObserver observer) {
        rootResource.setObserver(observer);
        subResources.forEach((name, resource) ->
            resource.setObserver(new ResourceObserver() {
                @Override
                public void onEvent(ResourcePath senderPath, Object eventData, EntityRef targetClientEntity) {
                    observer.onEvent(senderPath.pushItem(name), eventData, targetClientEntity);
                }

                @Override
                public void onChangedForClient(ResourcePath senderPath, Resource sender, EntityRef targetClientEntity) {
                    observer.onChangedForClient(senderPath.pushItem(name), sender, targetClientEntity);
                }

                @Override
                public void onChangedForAllClients(ResourcePath senderPath, Resource sender) {
                    observer.onChangedForAllClients(senderPath.pushItem(name), sender);
                }
            }));
    }

    @Override
    public void notifyChangedForAllClients() {
        rootResource.notifyChangedForAllClients();
        subResources.values().forEach(Resource::notifyChangedForAllClients);
    }

    @Override
    public ResourceMethod getMethod(ResourceMethodName methodName, ResourcePath path) throws ResourceAccessException {
        if (path.isEmpty()) {
            return rootResource.getMethod(methodName, path);
        } else {
            String selector = path.consumeNextItem();
            Resource res = subResources.getOrDefault(selector, NullResource.getInstance());
            return res.getMethod(methodName, path);
        }
    }

    private static final class NullResource extends AbstractObservableResource {

        private static final NullResource INSTANCE = new NullResource();

        private NullResource() {
        }

        public static NullResource getInstance() {
            return INSTANCE;
        }

        @Override
        public ResourceMethod getMethod(ResourceMethodName methodName, ResourcePath path) throws ResourceAccessException {
            return new ResourceMethod<Void, Void>() {
                @Override
                public Class<Void> getInType() {
                    return Void.class;
                }

                @Override
                public PermissionType getPermissionType() {
                    return PermissionType.NO_PERMISSION;
                }

                @Override
                public boolean clientIsAllowed(ClientSecurityInfo securityInfo) {
                    return true;
                }

                @Override
                public Void perform(Void data, Client client) throws ResourceAccessException {
                    throw ResourceAccessException.NOT_FOUND;
                }
            };
        }

        @Override
        public void notifyChangedForAllClients() {
            // do nothing (avoid sending updates for this non-meaningful resource)
        }
    }

    public static final class Builder {

        private final RouterResource result;
        private final Consumer<Resource> resourceInitializer;

        public Builder(Resource rootResource, Consumer<Resource> resourceInitializer) {
            this.resourceInitializer = resourceInitializer;
            this.result = new RouterResource(rootResource);
            resourceInitializer.accept(rootResource);
        }

        public Builder(Resource rootResource) {
            this(rootResource, (resource) -> { });
        }

        public Builder(Consumer<Resource> resourceInitializer) {
            this(new NullResource(), resourceInitializer);
        }

        public Builder() {
            this(NullResource.getInstance(), (resource) -> { });
        }

        public Builder addSubResource(String name, Resource resource) {
            resourceInitializer.accept(resource);
            result.subResources.put(name, resource);
            return this;
        }

        public RouterResource build() {
            return result;
        }
    }
}
