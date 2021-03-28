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
package org.terasology.web.resources.config;

import org.terasology.engine.config.Config;
import org.terasology.engine.registry.In;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.AbstractSimpleResource;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.resources.base.ResourcePath;
import org.terasology.web.serverAdminManagement.PermissionType;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;
import static org.terasology.web.resources.base.ResourceMethodFactory.createVoidParameterlessMethod;

/**
 * This {@link org.terasology.web.resources.base.Resource} provides methods common to all simple config settings.
 * @param <T> the type to be sent to or received by the server.
 */
public abstract class AbstractConfigEntryResource<T> extends AbstractSimpleResource {

    @In
    private Config config;

    @Override
    protected ResourceMethod<Void, T> getGetMethod(ResourcePath path) throws ResourceAccessException {
        return createParameterlessMethod(path, ClientSecurityRequirements.PUBLIC, Void.class, (data, client) -> get(config));
    }

    @Override
    protected ResourceMethod<T, Void> getPutMethod(ResourcePath path) throws ResourceAccessException {
        return createVoidParameterlessMethod(path, ClientSecurityRequirements.requireAdminPermission(PermissionType.CHANGE_SETTINGS), getDataType(), (data, client) -> {
            set(config, data);
            config.save();
            notifyChangedForAllClients();
        });
    }

    protected abstract Class<T> getDataType();
    protected abstract void set(Config targetConfig, T value);
    protected abstract T get(Config sourceConfig);
}
