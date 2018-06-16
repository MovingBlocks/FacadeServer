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

import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.network.Client;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceMethodImpl;
import org.terasology.web.serverAdminManagement.PermissionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.terasology.web.resources.InputCheckUtils.checkNotNullOrEmpty;
import static org.terasology.web.resources.InputCheckUtils.checkNull;

public class PatchGameMethod extends ResourceMethodImpl<NewGameMetadata, Void> {

    private PathManager pathManager;
    private String gameName;

    public PatchGameMethod(PathManager pathManager, String gameName) {
        super(NewGameMetadata.class, ClientSecurityRequirements.REQUIRE_ADMIN_PERMISSION, PermissionType.CREATE_BACKUP_RENAME_GAMES, null);
        this.pathManager = pathManager;
        this.gameName = gameName;
    }

    @Override
    public Void perform(NewGameMetadata data, Client client) throws ResourceAccessException {
        checkNull(data.getSeed(), "It's not possible to change the seed of an existing game.");
        checkNull(data.getWorldGenerator(), "It's not possible to change the world generator of an existing game.");
        checkNull(data.getModules(), "It's not possible to change modules of an existing game"); // TODO: make it possible
        checkNotNullOrEmpty(data.getGameName(), "A new name must be specified.");
        Path oldGameDir = pathManager.getSavePath(gameName);
        Path newGameDir = pathManager.getSavePath(data.getGameName());
        Path newManifestFile = newGameDir.resolve(GameManifest.DEFAULT_FILE_NAME);
        if (Files.exists(newGameDir)) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.CONFLICT, "A game with the specified name already exists"));
        }
        try {
            Files.move(oldGameDir, newGameDir);
            GameManifest gameManifest = GameManifest.load(newManifestFile);
            gameManifest.setTitle(data.getGameName());
            GameManifest.save(newManifestFile, gameManifest);
        } catch (IOException ex) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR, ex.getMessage()));
        }
        return null;
    }
}
