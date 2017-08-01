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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.web.resources.ResourceAccessException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BackupGameActionTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private PathManager pathManagerMock;

    @Before
    public void setUp() throws IOException {
        pathManagerMock = mock(PathManager.class);
        Path game1Path = tempFolder.getRoot().toPath().resolve("game1");
        Path game2Path = tempFolder.getRoot().toPath().resolve("game2");
        when(pathManagerMock.getSavePath("game1")).thenReturn(game1Path);
        when(pathManagerMock.getSavePath("game2")).thenReturn(game2Path);
        when(pathManagerMock.getSavePath(startsWith("game1_backup"))).thenReturn(tempFolder.getRoot().toPath().resolve("game1_backup"));
        Files.createDirectory(game1Path);
        Files.createFile(game1Path.resolve("someFile"));
        GameManifest gameManifest = new GameManifest("game1", "", 0);
        GameManifest.save(game1Path.resolve(GameManifest.DEFAULT_FILE_NAME), gameManifest);
    }

    @Test
    public void testBackupOk() throws ResourceAccessException, IOException {
        Path gamePath = tempFolder.getRoot().toPath().resolve("game1");
        Path tempFolderPath = tempFolder.getRoot().toPath();
        Path backupPath = tempFolderPath.resolve("game1_backup");

        assertTrue(Files.exists(gamePath));
        assertFalse(Files.exists(backupPath));

        new BackupGameAction().perform(pathManagerMock, "game1");

        assertTrue(Files.exists(gamePath));
        assertTrue(Files.exists(backupPath));
        assertTrue(Files.exists(backupPath.resolve("someFile")));
        assertTrue(Files.exists(backupPath.resolve(GameManifest.DEFAULT_FILE_NAME)));
    }

    @Test(expected = ResourceAccessException.class)
    public void testBackupNotExisting() throws ResourceAccessException {
        new BackupGameAction().perform(pathManagerMock, "game2");
    }
}
