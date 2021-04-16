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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.paths.PathManager;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class loads, adds, and removes admins by changing the serverAdmins.json file.
 * As a special case, when no admins are on the list, the first client to connect is given admin permissions.
 */
public final class ServerAdminsManager {

    private static final Logger logger = LoggerFactory.getLogger(ServerAdminsManager.class);
    private static final Gson GSON = new Gson();
    private static final ServerAdminsManager INSTANCE = new ServerAdminsManager(PathManager.getInstance().getHomePath().resolve("serverAdmins.json"), true);
    private static AdminPermissionManager adminPermissionManager = AdminPermissionManager.getInstance();

    private final Path adminListFilePath;
    private final boolean autoSave;
    private Set<String> serverAdminIds;
    private Runnable onListChanged = () -> { };

    ServerAdminsManager(Path adminListFilePath, boolean autoSave) {
        this.adminListFilePath = adminListFilePath;
        this.autoSave = autoSave;
        setServerAdminIds(new HashSet<>());
    }

    public static ServerAdminsManager getInstance() {
        return INSTANCE;
    }

    private void setServerAdminIds(Set<String> value) {
        serverAdminIds = Collections.synchronizedSet(value);
    }

    public void setOnListChangedCallback(Runnable callback) {
        onListChanged = callback;
    }

    @SuppressWarnings("unchecked")
    public void loadAdminList() {
        Set<String> newValue;
        try {
            newValue = GSON.fromJson(Files.newBufferedReader(adminListFilePath), Set.class);
        } catch (IOException ex) {
            logger.warn("Failed to load serverAdmins.json. " +
                    "WARNING: anonymous admin access is now allowed until an user (which will be registered as admin) connects using the regular Terasology client!");
            newValue = new HashSet<>();
        }
        setServerAdminIds(newValue);
        adminPermissionManager.loadAdminPermissionList();
    }

    public void saveAdminList() {
        try {
            try (Writer writer = Files.newBufferedWriter(adminListFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                GSON.toJson(serverAdminIds, writer);
            }
            adminPermissionManager.saveAdminPermissionList();
        } catch (IOException ex) {
            logger.warn("Failed to save serverAdmins.json", ex);
        }
    }

    public boolean clientHasAdminPermissions(String clientId) {
        return isAnonymousAdminAccessEnabled() || clientIsInAdminList(clientId);
    }

    /**
     * Determine if anyone can become an admin simply by joining the game.
     * @return whether the admin list is empty or not.
     */
    public boolean isAnonymousAdminAccessEnabled() {
        return serverAdminIds.isEmpty();
    }

    private boolean clientIsInAdminList(String clientId) {
        return serverAdminIds.contains(clientId);
    }

    public void addAdmin(String id) {
        serverAdminIds.add(id);
        adminPermissionManager.addAdmin(id);
        if (autoSave) {
            saveAdminList();
        }
        onListChanged.run();
    }

    public void removeAdmin(String id) {
        serverAdminIds.remove(id);
        adminPermissionManager.removeAdmin(id);
        if (autoSave) {
            saveAdminList();
        }
        onListChanged.run();
    }

    public Set<String> getAdminIds() {
        return Collections.unmodifiableSet(serverAdminIds);
    }

    /**
     * Add the first admin if none are currently set.
     * @param id the client id of the new admin
     */
    public void addFirstAdminIfNecessary(String id) {
        if (isAnonymousAdminAccessEnabled()) {
            addAdmin(id);
            adminPermissionManager.giveAllPermissionsToAdmin(id);
        }
    }
}
