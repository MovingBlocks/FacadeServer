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
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.ResourceAccessException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BackupGameAction implements Action {

    private String gameName;

    @Override
    public void perform() throws ResourceAccessException {
        String backupName = gameName + "_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        Path srcGamePath = PathManager.getInstance().getSavePath(gameName);
        Path dstGamePath = PathManager.getInstance().getSavePath(backupName);
        try {
            copyRecursive(srcGamePath, dstGamePath);
            Path backupManifestPath = dstGamePath.resolve(GameManifest.DEFAULT_FILE_NAME);
            GameManifest backupManifest = GameManifest.load(backupManifestPath);
            backupManifest.setTitle(backupName);
            GameManifest.save(backupManifestPath, backupManifest);
        } catch (IOException ex) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR, ex.getMessage()));
        }
    }

    private void copyRecursive(Path src, Path dst) throws IOException {
        List<Path> files = Files.walk(src).collect(Collectors.toList());
        for (Path file: files) {
            Path relative = src.relativize(file);
            Files.copy(file, dst.resolve(relative));
        }
    }
}
