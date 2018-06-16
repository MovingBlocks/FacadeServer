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
package org.terasology.web.resources.serverAdmins;

import org.terasology.web.resources.base.AbstractItemCollectionResource;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.serverAdminManagement.PermissionType;
import org.terasology.web.serverAdminManagement.ServerAdminsManager;

import java.util.Collections;
import java.util.Set;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;
import static org.terasology.web.resources.base.ResourceMethodFactory.createVoidParameterlessMethod;

public class ServerAdminsResource extends AbstractItemCollectionResource {

    public ServerAdminsResource() {
        super(Collections.singletonMap("permissions", AdminPermissionResource::new));
        ServerAdminsManager.getInstance().setOnListChangedCallback(this::notifyChangedForAllClients);
    }

    @Override
    protected ResourceMethod<Void, Set<String>> getGetCollectionMethod() throws ResourceAccessException {
        return createParameterlessMethod(ClientSecurityRequirements.PUBLIC, PermissionType.NO_PERMISSION, Void.class,
                (data, client) -> ServerAdminsManager.getInstance().getAdminIds());
    }

    @Override
    protected ResourceMethod<Void, Void> getPostItemMethod(String itemId) throws ResourceAccessException {
        return createVoidParameterlessMethod(ClientSecurityRequirements.REQUIRE_ADMIN_PERMISSION, PermissionType.ADMIN_MANAGEMENT, Void.class,
                (data, client) -> ServerAdminsManager.getInstance().addAdmin(itemId));
    }

    @Override
    protected ResourceMethod<Void, Void> getDeleteItemMethod(String itemId) throws ResourceAccessException {
        return createVoidParameterlessMethod(ClientSecurityRequirements.REQUIRE_ADMIN_PERMISSION, PermissionType.ADMIN_MANAGEMENT, Void.class,
                (data, client) -> ServerAdminsManager.getInstance().removeAdmin(itemId));
    }
}
