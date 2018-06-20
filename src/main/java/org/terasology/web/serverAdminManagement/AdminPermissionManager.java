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
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;
import org.terasology.web.resources.DefaultComponentSystem;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages the serverAdminPermissions.json file in the server directory.
 */
public final class AdminPermissionManager implements DefaultComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(AdminPermissionManager.class);
    private static final Gson GSON = new Gson();
    private static AdminPermissionManager instance;

    @In
    private NetworkSystem networkSystem;
    @In
    private PermissionManager permissionManager;

    private final Path adminPermissionsFilePath;
    private final Type typeOfServerAdminPermissions = new TypeToken<Set<Pair<String, Map<PermissionType, Boolean>>>>() {
    }.getType();
    private Set<Pair<String, Map<PermissionType, Boolean>>> serverAdminPermissions;
    private Runnable onListChanged = () -> {
    };

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
    public void updateAdminConsolePermissions(String adminId, EntityRef entityRef) {
        Pair<String, Map<PermissionType, Boolean>> permissions = getPermissionsOfAdmin(adminId);
        EntityRef clientInfo = entityRef.getComponent(ClientComponent.class).clientInfo;
        if (permissions != null) {
            Map<PermissionType, String> consolePermissionsMap = PermissionType.getConsolePermissionsMap();
            for (PermissionType permissionType : consolePermissionsMap.keySet()) {
                if (permissions.getValue().get(permissionType)) {
                    permissionManager.addPermission(clientInfo, consolePermissionsMap.get(permissionType));
                } else {
                    permissionManager.removePermission(clientInfo, consolePermissionsMap.get(permissionType));
                }
            }
        }
    }

    public void giveAllPermissionsToAdmin(String adminId) {
        setAdminPermissions(adminId, new Pair<>(adminId, generatePermissionMap(true)));
    }

    @SuppressWarnings({"SuspiciousToArrayCall", "SuspiciousMethodCalls"})
    public void setAdminPermissions(String adminId, Pair<String, Map<PermissionType, Boolean>> newPermissions) {
        Pair<String, Map<PermissionType, Boolean>> permission = getPermissionsOfAdmin(adminId);
        serverAdminPermissions.remove(permission);
        // Hack: somewhere along the line, the values of newPermissions get changed to Strings instead of PermissionTypes.
        // This causes casting errors unless they are all turned back into PermissionTypes.
        Map<PermissionType, Boolean> fixedNewPermissions = new HashMap<>();
        for (String permissionType : newPermissions.getValue().keySet().toArray(new String[0])) {
            fixedNewPermissions.put(PermissionType.valueOf(permissionType), newPermissions.getValue().get(permissionType));
        }
        serverAdminPermissions.add(new Pair<>(adminId, fixedNewPermissions));
        try {
            saveAdminPermissionList();
        } catch (IOException e) {
            logger.error("cannot save the admin permission list after setting a permission", e);
        }
        EntityRef playerToChange = EntityRef.NULL;
        for (Client player : networkSystem.getPlayers()) {
            if (player.getId().equals(adminId)) {
                playerToChange = player.getEntity();
            }
        }
        if (playerToChange != EntityRef.NULL) {
            updateAdminConsolePermissions(adminId, playerToChange);
        }
    }

    public void addAdmin(String id) {
        serverAdminPermissions.add(new Pair<>(id, generatePermissionMap(false)));
        onListChanged.run();
    }

    public void removeAdmin(String id) {
        for (Pair<String, Map<PermissionType, Boolean>> adminPermission : serverAdminPermissions) {
            if (adminPermission.getKey().equals(id)) {
                serverAdminPermissions.remove(adminPermission);
            }
        }
        Pair<String, Map<PermissionType, Boolean>> adminPermission = getPermissionsOfAdmin(id);
        serverAdminPermissions.remove(adminPermission);
        onListChanged.run();
    }

    public Pair<String, Map<PermissionType, Boolean>> getPermissionsOfAdmin(String id) {
        for (Pair<String, Map<PermissionType, Boolean>> adminPermission : serverAdminPermissions) {
            if (adminPermission.getKey().equals(id)) {
                return adminPermission;
            }
        }
        return null;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public List<PermissionType> getOwnedPermissions(String id) {
        Pair<String, Map<PermissionType, Boolean>> permissions = getPermissionsOfAdmin(id);
        List<PermissionType> permissionList = new ArrayList<>();
        if (permissions != null) {
            for (PermissionType permissionType : permissions.getValue().keySet()) {
                if (permissions.getValue().get(permissionType)) {
                    permissionList.add(permissionType);
                }
            }
        }
        return permissionList;
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

    private Map<PermissionType, Boolean> generatePermissionMap(boolean initialValues) {
        Map<PermissionType, Boolean> permissionMap = new HashMap<>();
        for (PermissionType permissionType : PermissionType.values()) {
            permissionMap.put(permissionType, initialValues);
        }
        return permissionMap;
    }

}
