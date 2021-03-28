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
package org.terasology.web.serverAdminManagement;

import org.terasology.engine.logic.permission.PermissionManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This enum is used for determining admin permissions.
 */
public enum PermissionType {
    NO_PERMISSION,
    CONSOLE_CHEAT,
    CONSOLE_USER_MANAGEMENT,
    CONSOLE_SERVER_MANAGEMENT,
    CONSOLE_DEBUG,
    INSTALL_MODULES,
    CREATE_BACKUP_RENAME_GAMES,
    DELETE_GAMES,
    START_STOP_GAMES,
    CHANGE_SETTINGS,
    ADMIN_MANAGEMENT;

    public static Map<PermissionType, String> getConsolePermissionsMap() {
        Map<PermissionType, String> consolePermissionsMap = new HashMap<>();
        consolePermissionsMap.put(CONSOLE_CHEAT, PermissionManager.CHEAT_PERMISSION);
        consolePermissionsMap.put(CONSOLE_USER_MANAGEMENT, PermissionManager.USER_MANAGEMENT_PERMISSION);
        consolePermissionsMap.put(CONSOLE_SERVER_MANAGEMENT, PermissionManager.SERVER_MANAGEMENT_PERMISSION);
        consolePermissionsMap.put(CONSOLE_DEBUG, PermissionManager.DEBUG_PERMISSION);
        return Collections.unmodifiableMap(consolePermissionsMap);
    }

    protected static Map<PermissionType, Boolean> generatePermissionMap(boolean initialValues) {
        Map<PermissionType, Boolean> permissionMap = new HashMap<>();
        for (PermissionType permissionType : PermissionType.values()) {
            permissionMap.put(permissionType, initialValues);
        }
        return permissionMap;
    }
}
