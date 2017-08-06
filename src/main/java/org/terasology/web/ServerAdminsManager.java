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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ServerAdminsManager {

    private static final Logger logger = LoggerFactory.getLogger(ServerAdminsManager.class);
    private static final Gson GSON = new Gson();
    private static List<String> serverAdminIds;

    private ServerAdminsManager() {
    }

    public static void setAdminList(List<String> ids) {
        serverAdminIds = ids;
    }

    @SuppressWarnings("unchecked")
    public static void loadAdminList() {
        Path filePath = PathManager.getInstance().getHomePath().resolve("serverAdmins.json");
        try {
            serverAdminIds = GSON.fromJson(Files.newBufferedReader(filePath), List.class);
        } catch (IOException ex) {
            logger.warn("Failed to load serverAdmins.json, no client will be able to change the engine state");
            serverAdminIds = new ArrayList<>();
        }
    }

    public static void checkClientIsServerAdmin(String clientId) throws ResourceAccessException {
        if (!serverAdminIds.contains(clientId)) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.UNAUTHORIZED, "Only server admins can perform this action"));
        }
    }
}
