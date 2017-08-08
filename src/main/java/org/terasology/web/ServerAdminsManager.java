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
package org.terasology.web;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.paths.PathManager;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.ResourceAccessException;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class ServerAdminsManager {

    private static final Logger logger = LoggerFactory.getLogger(ServerAdminsManager.class);
    private static final String ADMIN_LIST_FILE_NAME = "serverAdmins.json";
    private static final Gson GSON = new Gson();
    private static List<String> serverAdminIds;

    private ServerAdminsManager() {
    }

    private static Path getAdminListFilePath() {
        return PathManager.getInstance().getHomePath().resolve(ADMIN_LIST_FILE_NAME);
    }

    @SuppressWarnings("unchecked")
    public static void loadAdminList() {
        try {
            serverAdminIds = GSON.fromJson(Files.newBufferedReader(getAdminListFilePath()), List.class);
        } catch (IOException ex) {
            logger.warn("Failed to load serverAdmins.json");
            serverAdminIds = new ArrayList<>();
        }
    }

    public static void saveAdminList() {
        try {
            try (Writer writer = Files.newBufferedWriter(getAdminListFilePath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                GSON.toJson(serverAdminIds, writer);
            }
        } catch (IOException ex) {
            logger.warn("Failed to save serverAdmins.json", ex);
        }
    }

    public static void checkClientHasAdminPermissions(String clientId) throws ResourceAccessException {
        if (!(isAnonymousAdminAccessEnabled() || clientIsInAdminList(clientId))) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.UNAUTHORIZED, "Only server admins can perform this action"));
        }
    }

    public static boolean isAnonymousAdminAccessEnabled() {
        return serverAdminIds.isEmpty();
    }

    private static boolean clientIsInAdminList(String clientId) {
        return serverAdminIds.contains(clientId);
    }

    public static void addAdmin(String id) {
        serverAdminIds.add(id);
    }

    public static void removeAdmin(String id) {
        serverAdminIds.remove(id);
    }
}
