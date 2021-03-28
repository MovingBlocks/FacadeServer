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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.paths.PathManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;
import org.terasology.web.resources.DefaultComponentSystem;

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
public final class AdminPermissionManager implements DefaultComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(AdminPermissionManager.class);
    private static final Gson GSON = new Gson();
    private static AdminPermissionManager instance;

    @In
    private NetworkSystem networkSystem;
    @In
    private PermissionManager permissionManager;

    private final Path adminPermissionsFilePath;
    private final Type typeOfServerAdminPermissions = new TypeToken<Set<IdPermissionPair>>() {
    }.getType();
    private boolean autoSave;
    private Set<IdPermissionPair> serverAdminPermissions;
    private Runnable onListChanged = () -> {
    };

    AdminPermissionManager(Path adminPermissionsFilePath, boolean autoSave) {
        this.adminPermissionsFilePath = adminPermissionsFilePath;
        this.autoSave = autoSave;
        setServerAdminPermissions(new HashSet<>());
    }

    public static AdminPermissionManager getInstance() {
        if (instance == null) {
            instance = new AdminPermissionManager(PathManager.getInstance().getHomePath().resolve("serverAdminPermissions.json"), true);
        }
        return instance;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public void updateAdminConsolePermissions(String adminId, EntityRef entityRef) {
        Map<PermissionType, Boolean> permissions = getPermissionsOfAdmin(adminId);
        EntityRef clientInfo = entityRef.getComponent(ClientComponent.class).clientInfo;
        if (permissions != null) {
            Map<PermissionType, String> consolePermissionsMap = PermissionType.getConsolePermissionsMap();
            for (PermissionType permissionType : consolePermissionsMap.keySet()) {
                if (permissions.get(permissionType)) {
                    permissionManager.addPermission(clientInfo, consolePermissionsMap.get(permissionType));
                } else {
                    permissionManager.removePermission(clientInfo, consolePermissionsMap.get(permissionType));
                }
            }
        }
    }

    public void giveAllPermissionsToAdmin(String adminId) {
        setAdminPermissions(adminId, new IdPermissionPair(adminId, PermissionType.generatePermissionMap(true)));
    }

    @SuppressWarnings({"SuspiciousToArrayCall", "SuspiciousMethodCalls"})
    public void setAdminPermissions(String adminId, IdPermissionPair newPermissions) {
        Map<PermissionType, Boolean> permission = getPermissionsOfAdmin(adminId);
        serverAdminPermissions.remove(new IdPermissionPair(adminId, permission));
        // Hack: somewhere along the line, the values of newPermissions get changed to Strings instead of PermissionTypes.
        // This causes casting errors unless they are all turned back into PermissionTypes.
        // However, adding the first admin works correctly, so we need to check it.
        Map<PermissionType, Boolean> fixedNewPermissions = newPermissions.getPermissions();
        if (newPermissions.getPermissions().get(PermissionType.NO_PERMISSION) == null) {
            fixedNewPermissions = new HashMap<>();
            for (String permissionType : newPermissions.getPermissions().keySet().toArray(new String[0])) {
                fixedNewPermissions.put(PermissionType.valueOf(permissionType), newPermissions.getPermissions().get(permissionType));
            }
        }
        serverAdminPermissions.add(new IdPermissionPair(adminId, fixedNewPermissions));
        if (autoSave) {
            try {
                saveAdminPermissionList();
            } catch (IOException e) {
                logger.error("cannot save the admin permission list after setting a permission", e);
            }
        }
        EntityRef playerToChange = EntityRef.NULL;
        if (networkSystem != null) {
            for (Client player : networkSystem.getPlayers()) {
                if (player.getId().equals(adminId)) {
                    playerToChange = player.getEntity();
                }
            }
        }
        if (playerToChange != EntityRef.NULL) {
            updateAdminConsolePermissions(adminId, playerToChange);
        }
    }

    public void addAdmin(String id) {
        serverAdminPermissions.add(new IdPermissionPair(id, PermissionType.generatePermissionMap(false)));
        onListChanged.run();
    }

    public void removeAdmin(String id) {
        for (IdPermissionPair adminPermission : serverAdminPermissions) {
            if (adminPermission.getId().equals(id)) {
                serverAdminPermissions.remove(adminPermission);
            }
        }
        IdPermissionPair adminPermission = new IdPermissionPair(id, getPermissionsOfAdmin(id));
        serverAdminPermissions.remove(adminPermission);
        onListChanged.run();
    }

    public Map<PermissionType, Boolean> getPermissionsOfAdmin(String id) {
        for (IdPermissionPair adminPermission : serverAdminPermissions) {
            if (adminPermission.getId().equals(id)) {
                return adminPermission.getPermissions();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void loadAdminPermissionList() {
        Set<IdPermissionPair> newValue;
        try {
            newValue = GSON.fromJson(Files.newBufferedReader(adminPermissionsFilePath), typeOfServerAdminPermissions);
        } catch (IOException ex) {
            logger.warn("Failed to load the admin permissions list, resetting all permissions to false!");
            newValue = new HashSet<>();
            for (String adminId : ServerAdminsManager.getInstance().getAdminIds()) {
                newValue.add(new IdPermissionPair(adminId, PermissionType.generatePermissionMap(false)));
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

    public Set<IdPermissionPair> getAdminPermissions() {
        return serverAdminPermissions;
    }

    private void setServerAdminPermissions(Set<IdPermissionPair> permissions) {
        serverAdminPermissions = Collections.synchronizedSet(permissions);
    }

}
