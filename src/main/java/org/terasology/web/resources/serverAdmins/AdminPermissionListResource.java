/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.web.resources.serverAdmins;

import javafx.util.Pair;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;
import org.terasology.web.resources.base.AbstractSimpleResource;
import org.terasology.web.serverAdminManagement.AdminPermissionManager;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.resources.base.ResourcePath;
import org.terasology.web.serverAdminManagement.PermissionType;

import java.util.Map;
import java.util.Set;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;

/**
 * This resource is used to get a list of admin permissions.
 */
public class AdminPermissionListResource extends AbstractSimpleResource {

    public AdminPermissionListResource() {
        AdminPermissionManager.getInstance().setOnListChangedCallback(this::notifyChangedForAllClients);
    }

    @Override
    protected ResourceMethod<Void, Set<Pair<String, Map<PermissionType, Boolean>>>> getGetMethod(ResourcePath path) throws ResourceAccessException {
        return createParameterlessMethod(ClientSecurityRequirements.PUBLIC, Void.class,
                (data, client) -> AdminPermissionManager.getInstance().getAdminPermissions());
    }

}
