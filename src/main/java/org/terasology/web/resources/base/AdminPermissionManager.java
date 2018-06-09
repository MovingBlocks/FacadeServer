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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.paths.PathManager;
import org.terasology.web.serverAdminManagement.ServerAdminsManager;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class AdminPermissionManager {

    private static final Logger logger = LoggerFactory.getLogger(AdminPermissionManager.class);
    private static final Gson GSON = new Gson();
    private static AdminPermissionManager instance;

    private final Path adminPermissionsFilePath;
    private final Type typeOfServerAdminPermissions = new TypeToken<Set<AdminPermissions>>() { }.getType();
    private Set<AdminPermissions> serverAdminPermissions;

    private AdminPermissionManager(Path adminPermissionsFilePath) {
        this.adminPermissionsFilePath = adminPermissionsFilePath;
        setServerAdminPermissions(new HashSet<>());
    }

    public static AdminPermissionManager getInstance() {
        if (instance == null) {
            instance = new AdminPermissionManager(PathManager.getInstance().getHomePath().resolve("serverAdminPermissions.json"));
        }
        return instance;
    }

    public boolean adminHasPermission(String adminID, ResourcePath path, ResourceMethodName resourceMethodName) {
        AdminPermissions permissions = findAdmin(adminID);
        if (permissions != null) {
            switch (resourceMethodName) {
                case GET:
                    return true;
                case POST:
                    if (path.toString().compareTo("console") == 0) {
                        // TODO: figure out how to determine what permission the command requires
                        return true;
                    } else if (path.toString().contains("games")) {
                        return permissions.hasCreateBackupRenameGames();
                    } else if (path.toString().contains("serverAdmins")) {
                        return permissions.hasAdminManagement();
                    }
                    return true;
                case PUT:
                    if (path.toString().compareTo("engineState") == 0) {
                        return permissions.hasStartStopGames();
                    } else if (path.toString().compareTo("modules/installer") == 0) {
                        return permissions.hasInstallModules();
                    } else if (path.toString().contains("config")) {
                        return permissions.hasChangeSettings();
                    }
                    return true;
                case PATCH:
                    if (path.toString().contains("games")) {
                        return permissions.hasCreateBackupRenameGames();
                    } else if (path.toString().contains("serverAdmins")) {
                        return permissions.hasAdminManagement();
                    }
                    return true;
                case DELETE:
                    if (path.toString().contains("games")) {
                        return permissions.hasDeleteGames();
                    } else if (path.toString().contains("serverAdmins")) {
                        return permissions.hasAdminManagement();
                    }
                }
            }
        return true;
    }

    public void giveAllPermissionsToAdmin(String adminId) {
        setAdminPermission(adminId, new AdminPermissions(adminId, true));
    }

    public void setAdminPermission(String adminId, AdminPermissions newPermissions) {
        AdminPermissions permission = findAdmin(adminId);
        serverAdminPermissions.remove(permission);
        serverAdminPermissions.add(newPermissions);
    }


    public void addAdmin(String id) {
        serverAdminPermissions.add(new AdminPermissions(id));
    }

    public void removeAdmin(String id) {
        for (AdminPermissions adminPermission : serverAdminPermissions) {
            if (adminPermission.getId().compareTo(id) == 0) {
                serverAdminPermissions.remove(adminPermission);
            }
        }
        AdminPermissions adminPermission = findAdmin(id);
        serverAdminPermissions.remove(adminPermission);
    }

    public AdminPermissions getPermissionsOfAdmin(String id) {
        for (AdminPermissions adminPermission: serverAdminPermissions) {
            if (adminPermission.getId().compareTo(id) == 0) {
                return adminPermission;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void loadAdminPermissionList() {
        Set<AdminPermissions> newValue;
        try {
            newValue = GSON.fromJson(Files.newBufferedReader(adminPermissionsFilePath), typeOfServerAdminPermissions);
            System.out.println(newValue + "nv");
        } catch (IOException ex) {
            logger.warn("Failed to load the admin permissions list, resetting all permissions to false!");
            newValue = new HashSet<>();
            for (String admin : ServerAdminsManager.getInstance().getAdminIds()) {
                newValue.add(new AdminPermissions(admin));
            }
        }
        setServerAdminPermissions(newValue);
    }

    public void saveAdminPermissionList() throws IOException {
        try (Writer writer = Files.newBufferedWriter(adminPermissionsFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            GSON.toJson(serverAdminPermissions, typeOfServerAdminPermissions, writer);
        }
    }

    public Set<AdminPermissions> getAdminPermissions() {
        return serverAdminPermissions;
    }

    private void setServerAdminPermissions(Set<AdminPermissions> permissions) {
        serverAdminPermissions = Collections.synchronizedSet(permissions);
    }

    private AdminPermissions findAdmin(String id) {
        for (AdminPermissions adminPermission : serverAdminPermissions) {
            if (adminPermission.getId().compareTo(id) == 0) {
                serverAdminPermissions.remove(adminPermission);
            }
        }
        return null;
    }

}
