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
package org.terasology.web.resources.base;

import java.lang.reflect.Field;

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
            setConsoleCheat(true);
            setConsoleUserManagement(true);
            setConsoleServerManagement(true);
            setConsoleDebug(true);
            setInstallModules(true);
            setCreateBackupRenameGames(true);
            setDeleteGames(true);
            setStartStopGames(true);
            setChangeSettings(true);
            setAdminManagement(true);
        }
    }

    public String getId() {
        return id;
    }

    public boolean hasConsoleCheat() {
        return permissions.consoleCheat;
    }

    public void setConsoleCheat(boolean consoleCheat) {
        this.permissions.consoleCheat = consoleCheat;
    }

    public boolean hasConsoleUserManagement() {
        return permissions.consoleUserManagement;
    }

    public void setConsoleUserManagement(boolean consoleUserManagement) {
        this.permissions.consoleUserManagement = consoleUserManagement;
    }

    public boolean hasConsoleServerManagement() {
        return permissions.consoleServerManagement;
    }

    public void setConsoleServerManagement(boolean consoleServerManagement) {
        this.permissions.consoleServerManagement = consoleServerManagement;
    }

    public boolean hasConsoleDebug() {
        return permissions.consoleDebug;
    }

    public void setConsoleDebug(boolean consoleDebug) {
        this.permissions.consoleDebug = consoleDebug;
    }

    public boolean hasInstallModules() {
        return permissions.installModules;
    }

    public void setInstallModules(boolean installModules) {
        this.permissions.installModules = installModules;
    }

    public boolean hasCreateBackupRenameGames() {
        return permissions.createBackupRenameGames;
    }

    public void setCreateBackupRenameGames(boolean createBackupRenameGames) {
        this.permissions.createBackupRenameGames = createBackupRenameGames;
    }

    public boolean hasDeleteGames() {
        return permissions.deleteGames;
    }

    public void setDeleteGames(boolean deleteGames) {
        this.permissions.deleteGames = deleteGames;
    }

    public boolean hasStartStopGames() {
        return permissions.startStopGames;
    }

    public void setStartStopGames(boolean startStopGames) {
        this.permissions.startStopGames = startStopGames;
    }

    public boolean hasChangeSettings() {
        return permissions.changeSettings;
    }

    public void setChangeSettings(boolean changeSettings) {
        this.permissions.changeSettings = changeSettings;
    }

    public boolean hasAdminManagement() {
        return permissions.adminManagement;
    }

    public void setAdminManagement(boolean adminManagement) {
        this.permissions.adminManagement = adminManagement;
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
