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
package org.terasology.web.client;

import org.terasology.web.serverAdminManagement.PermissionType;

import java.util.Map;

public class ClientSecurityInfo {

    private boolean isAuthenticated;
    private boolean isAdmin;
    private Map<PermissionType, Boolean> clientPermissions;

    public ClientSecurityInfo(boolean isAuthenticated, boolean isAdmin, Map<PermissionType, Boolean> clientPermissions) {
        this.isAuthenticated = isAuthenticated;
        this.isAdmin = isAdmin;
        this.clientPermissions = clientPermissions;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean ownsPermission(PermissionType permissionType) {
        return clientPermissions.get(permissionType);
    }
}
