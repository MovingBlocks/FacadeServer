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
package org.terasology.web.resources.games;


import org.terasology.engine.core.paths.PathManager;
import org.terasology.engine.network.Client;
import org.terasology.engine.utilities.FilesUtil;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceMethodImpl;
import org.terasology.web.serverAdminManagement.PermissionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * {@link org.terasology.web.resources.base.ResourceMethod} used for deleting games.
 */
public class DeleteGameMethod extends ResourceMethodImpl<Void, Void> {

    private PathManager pathManager;
    private String gameName;

    public DeleteGameMethod(PathManager pathManager, String gameName) {
        super(Void.class, ClientSecurityRequirements.requireAdminPermission(PermissionType.DELETE_GAMES), null);
        this.pathManager = pathManager;
        this.gameName = gameName;
    }

    @Override
    public Void perform(Void data, Client client) throws ResourceAccessException {
        Path gamePath = pathManager.getSavePath(gameName);
        if (!Files.isDirectory(gamePath)) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.NOT_FOUND, "The specified path does not exist or isn't a valid savegame."));
        }
        try {
            FilesUtil.recursiveDelete(gamePath);
        } catch (IOException ex) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR, ex.getMessage()));
        }
        return null;
    }
}
