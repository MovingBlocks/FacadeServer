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

import org.terasology.game.GameManifest;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.ResourceAccessException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.terasology.web.resources.InputCheckUtils.checkNotNullOrEmpty;

public class RenameGameAction extends AbstractExistingGameAction {

    private String newGameName;

    @Override
    protected void perform(String oldGameName) throws ResourceAccessException {
        checkNotNullOrEmpty(newGameName, "A new name must be specified.");
        Path oldGameDir = getPathManager().getSavePath(oldGameName);
        Path newGameDir = getPathManager().getSavePath(newGameName);
        Path newManifestFile = newGameDir.resolve(GameManifest.DEFAULT_FILE_NAME);
        if (Files.exists(newGameDir)) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.CONFLICT, "A game with the specified name already exists"));
        }
        try {
            Files.move(oldGameDir, newGameDir);
            GameManifest gameManifest = GameManifest.load(newManifestFile);
            gameManifest.setTitle(newGameName);
            GameManifest.save(newManifestFile, gameManifest);
        } catch (IOException ex) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR, ex.getMessage()));
        }
    }

    void setNewGameName(String newGameName) {
        this.newGameName = newGameName;
    }
}
