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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.permission.PermissionSetComponent;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages the serverAdminPermissions.json file in the server directory.
 */
public final class AdminPermissionManager extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(AdminPermissionManager.class);
    private static final Gson GSON = new Gson();
    private static AdminPermissionManager instance;

    private final Path adminPermissionsFilePath;
    private final Type typeOfServerAdminPermissions = new TypeToken<Set<Pair<String, Map<PermissionType, Boolean>>>>() { }.getType();
    private Set<Pair<String, Map<PermissionType, Boolean>>> serverAdminPermissions;
    private Runnable onListChanged = () -> { };

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

    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean adminHasPermission(String adminId, PermissionType permission) {
        if (permission == PermissionType.NO_PERMISSION) {
            return true;
        }
        Pair<String, Map<PermissionType, Boolean>> permissions = getPermissionsOfAdmin(adminId);
        if (permissions != null) {
            if (permissions.getValue().get(permission) == null) {
                return permissions.getValue().get(permission.toString());
            } else {
                return permissions.getValue().get(permission);
            }
        }
        return true;
    }

    public void updateAdminConsolePermissions(String adminId, EntityRef entityRef) {
        Pair<String, Map<PermissionType, Boolean>> permission = getPermissionsOfAdmin(adminId);
        EntityRef clientInfo = entityRef.getComponent(ClientComponent.class).clientInfo;
        if (permission != null) {
            if (permission.getValue().get(PermissionType.CONSOLE_CHEAT)) {
                addPermission(clientInfo, PermissionManager.CHEAT_PERMISSION);
            } else {
                removePermission(clientInfo, PermissionManager.CHEAT_PERMISSION);
            }
            if (permission.getValue().get(PermissionType.CONSOLE_USER_MANAGEMENT)) {
                addPermission(clientInfo, PermissionManager.USER_MANAGEMENT_PERMISSION);
            } else {
                removePermission(clientInfo, PermissionManager.USER_MANAGEMENT_PERMISSION);
            }
            if (permission.getValue().get(PermissionType.CONSOLE_SERVER_MANAGEMENT)) {
                addPermission(clientInfo, PermissionManager.SERVER_MANAGEMENT_PERMISSION);
            } else {
                removePermission(clientInfo, PermissionManager.SERVER_MANAGEMENT_PERMISSION);
            }
            if (permission.getValue().get(PermissionType.CONSOLE_DEBUG)) {
                addPermission(clientInfo, PermissionManager.DEBUG_PERMISSION);
            } else {
                removePermission(clientInfo, PermissionManager.DEBUG_PERMISSION);
            }
        }
    }

    public void giveAllPermissionsToAdmin(String adminId) {
        setAdminPermissions(adminId, new Pair<>(adminId, generatePermissionMap(true)));
    }

    public void setAdminPermissionsAndUpdateConsolePermissions(String adminId, Pair<String, Map<PermissionType, Boolean>> newPermissions, NetworkSystem networkSystem) {
        EntityRef playerToChange = EntityRef.NULL;
        System.out.println("ns1: " + networkSystem);
        for (Client player : networkSystem.getPlayers()) {
            if (player.getId().equals(adminId)) {
                playerToChange = player.getEntity();
            }
        }
        System.out.println("ptc: " + playerToChange);
        setAdminPermissions(adminId, newPermissions);
        updateAdminConsolePermissions(adminId, playerToChange);
    }

    private void setAdminPermissions(String adminId, Pair<String, Map<PermissionType, Boolean>> newPermissions) {
        Pair<String, Map<PermissionType, Boolean>> permission = getPermissionsOfAdmin(adminId);
        serverAdminPermissions.remove(permission);
        serverAdminPermissions.add(newPermissions);
        try {
            saveAdminPermissionList();
        } catch (IOException e) {
            logger.error("cannot save the admin permission list after setting a permission", e);
        }
    }

    public void addAdmin(String id) {
        serverAdminPermissions.add(new Pair<>(id, generatePermissionMap(false)));
        onListChanged.run();
    }

    public void removeAdmin(String id) {
        for (Pair<String, Map<PermissionType, Boolean>> adminPermission: serverAdminPermissions) {
            if (adminPermission.getKey().equals(id)) {
                serverAdminPermissions.remove(adminPermission);
            }
        }
        Pair<String, Map<PermissionType, Boolean>> adminPermission = getPermissionsOfAdmin(id);
        serverAdminPermissions.remove(adminPermission);
        onListChanged.run();
    }

    public Pair<String, Map<PermissionType, Boolean>> getPermissionsOfAdmin(String id) {
        for (Pair<String, Map<PermissionType, Boolean>> adminPermission: serverAdminPermissions) {
            if (adminPermission.getKey().equals(id)) {
                return adminPermission;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void loadAdminPermissionList() {
        Set<Pair<String, Map<PermissionType, Boolean>>> newValue;
        try {
            newValue = GSON.fromJson(Files.newBufferedReader(adminPermissionsFilePath), typeOfServerAdminPermissions);
        } catch (IOException ex) {
            logger.warn("Failed to load the admin permissions list, resetting all permissions to false!");
            newValue = new HashSet<>();
            for (String adminId : ServerAdminsManager.getInstance().getAdminIds()) {
                newValue.add(new Pair<>(adminId, generatePermissionMap(false)));
            }
        }
        setServerAdminPermissions(newValue);
    }

    public void saveAdminPermissionList() throws IOException {
        try (Writer writer = Files.newBufferedWriter(adminPermissionsFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            GSON.toJson(serverAdminPermissions, typeOfServerAdminPermissions, writer);
        }
    }

    public void setOnListChangedCallback(Runnable callback) {
        onListChanged = callback;
    }

    public Set<Pair<String, Map<PermissionType, Boolean>>> getAdminPermissions() {
        return serverAdminPermissions;
    }

    private void setServerAdminPermissions(Set<Pair<String, Map<PermissionType, Boolean>>> permissions) {
        serverAdminPermissions = Collections.synchronizedSet(permissions);
    }

    private void addPermission(EntityRef clientInfo, String permission) {
        PermissionSetComponent permissionSet = clientInfo.getComponent(PermissionSetComponent.class);
        if (permissionSet != null) {
            permissionSet.permissions.add(permission);
            clientInfo.saveComponent(permissionSet);
        }
        onListChanged.run();
    }

    private void removePermission(EntityRef clientInfo, String permission) {
        PermissionSetComponent permissionSet = clientInfo.getComponent(PermissionSetComponent.class);
        if (permissionSet != null) {
            permissionSet.permissions.remove(permission);
            clientInfo.saveComponent(permissionSet);
        }
        onListChanged.run();
    }

    private Map<PermissionType, Boolean> generatePermissionMap(boolean initialValues) {
        Map<PermissionType, Boolean> permissionMap = new HashMap<>();
        for (PermissionType permissionType : PermissionType.values()) {
            permissionMap.put(permissionType, initialValues);
        }
        return permissionMap;
    }

}
