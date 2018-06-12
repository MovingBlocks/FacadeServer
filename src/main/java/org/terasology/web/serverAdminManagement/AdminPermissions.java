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

    public boolean getConsoleCheat() {
        return permissions.consoleCheat;
    }

    public boolean getConsoleUserManagement() {
        return permissions.consoleUserManagement;
    }

    public boolean getConsoleServerManagement() {
        return permissions.consoleServerManagement;
    }

    public boolean getConsoleDebug() {
        return permissions.consoleDebug;
    }

    public boolean getInstallModules() {
        return permissions.installModules;
    }

    public boolean getCreateBackupRenameGames() {
        return permissions.createBackupRenameGames;
    }

    public boolean getDeleteGames() {
        return permissions.deleteGames;
    }

    public boolean getStartStopGames() {
        return permissions.startStopGames;
    }

    public boolean getChangeSettings() {
        return permissions.changeSettings;
    }

    public boolean getAdminManagement() {
        return permissions.adminManagement;
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
