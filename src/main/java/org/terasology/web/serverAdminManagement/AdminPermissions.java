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

public class AdminPermissions {

    private String id;
    private Permissions permissions;

    AdminPermissions(String id) {
        this.id = id;
        permissions = new Permissions();
    }

    AdminPermissions(String id, boolean allPermissions) {
        this.id = id;
        permissions = new Permissions();
        if (allPermissions) {
            permissions.consoleCheat = true;
            permissions.consoleUserManagement = true;
            permissions.consoleServerManagement = true;
            permissions.consoleDebug = true;
            permissions.installModules = true;
            permissions.createBackupRenameGames = true;
            permissions.deleteGames = true;
            permissions.startStopGames = true;
            permissions.changeSettings = true;
            permissions.adminManagement = true;
        }
    }

    public String getId() {
        return id;
    }

    public boolean getPermission(PermissionType permission) {
        switch (permission) {
            case CONSOLE_CHEAT:
                return permissions.consoleCheat;
            case CONSOLE_USER_MANAGEMENT:
                return permissions.consoleUserManagement;
            case CONSOLE_SERVER_MANAGEMENT:
                return permissions.consoleServerManagement;
            case CONSOLE_DEBUG:
                return permissions.consoleDebug;
            case INSTALL_MODULES:
                return permissions.installModules;
            case CREATE_BACKUP_RENAME_GAMES:
                return permissions.createBackupRenameGames;
            case DELETE_GAMES:
                return permissions.deleteGames;
            case START_STOP_GAMES:
                return permissions.startStopGames;
            case CHANGE_SETTINGS:
                return permissions.changeSettings;
            case ADMIN_MANAGEMENT:
                return permissions.adminManagement;
            default:
                return false;
        }
    }

    private static class Permissions {
        private boolean consoleCheat;
        private boolean consoleUserManagement;
        private boolean consoleServerManagement;
        private boolean consoleDebug;
        private boolean installModules;
        private boolean createBackupRenameGames;
        private boolean deleteGames;
        private boolean startStopGames;
        private boolean changeSettings;
        private boolean adminManagement;
    }

}
