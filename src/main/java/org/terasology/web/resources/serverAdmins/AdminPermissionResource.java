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

import org.terasology.web.resources.DefaultComponentSystem;
import org.terasology.web.resources.base.AbstractItemCollectionResource;
import org.terasology.web.serverAdminManagement.AdminPermissionManager;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.serverAdminManagement.IdPermissionPair;
import org.terasology.web.serverAdminManagement.PermissionType;

import java.util.Map;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;
import static org.terasology.web.resources.base.ResourceMethodFactory.createVoidParameterlessMethod;

/**
 * Resource used for getting/setting the permissions of a specific admin.
 */
public class AdminPermissionResource extends AbstractItemCollectionResource implements DefaultComponentSystem {

    private String adminID;

    public AdminPermissionResource(String adminID) {
        this.adminID = adminID;
    }

    @Override
    protected ResourceMethod<Void, IdPermissionPair<String, Map<PermissionType, Boolean>>> getGetCollectionMethod() throws ResourceAccessException {
        return createParameterlessMethod(ClientSecurityRequirements.PUBLIC, Void.class,
                (data, client) -> new IdPermissionPair<>(adminID, AdminPermissionManager.getInstance().getPermissionsOfAdmin(adminID)));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ResourceMethod<IdPermissionPair, Void> getPatchCollectionMethod() throws ResourceAccessException {
        return createVoidParameterlessMethod(ClientSecurityRequirements.requireAdminPermission(PermissionType.ADMIN_MANAGEMENT), IdPermissionPair.class,
                (data, client) -> AdminPermissionManager.getInstance().setAdminPermissions(adminID, data));
    }

}
