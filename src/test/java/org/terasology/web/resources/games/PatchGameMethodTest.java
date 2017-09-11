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
import org.terasology.web.resources.base.ResourceAccessException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PatchGameMethodTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private PathManager pathManagerMock;

    @Before
    public void setUp() throws IOException {
        pathManagerMock = mock(PathManager.class);
        Path game1Path = tempFolder.getRoot().toPath().resolve("game1");
        Path game1NewPath = tempFolder.getRoot().toPath().resolve("game1New");
        Path game2Path = tempFolder.getRoot().toPath().resolve("game2");
        Path game2NewPath = tempFolder.getRoot().toPath().resolve("game2");
        Path game3Path = tempFolder.getRoot().toPath().resolve("game3");
        when(pathManagerMock.getSavePath("game1")).thenReturn(game1Path);
        when(pathManagerMock.getSavePath("game1New")).thenReturn(game1NewPath);
        when(pathManagerMock.getSavePath("game2")).thenReturn(game2Path);
        when(pathManagerMock.getSavePath("game2New")).thenReturn(game2NewPath);
        when(pathManagerMock.getSavePath("game3")).thenReturn(game3Path);
        Files.createDirectory(game1Path);
        GameManifest gameManifest = new GameManifest("game1", "testSeed", 0);
        GameManifest.save(game1Path.resolve(GameManifest.DEFAULT_FILE_NAME), gameManifest);
        Files.createDirectory(game3Path);
    }

    @Test
    public void testRenameOk() throws ResourceAccessException, IOException {
        Path oldPath = tempFolder.getRoot().toPath().resolve("game1");
        Path newPath = tempFolder.getRoot().toPath().resolve("game1New");

        assertTrue(Files.exists(oldPath));
        assertFalse(Files.exists(newPath));

        new PatchGameMethod(pathManagerMock, "game1").perform(getRenameData("game1New"), null);

        assertFalse(Files.exists(oldPath));
        assertTrue(Files.exists(newPath));
        GameManifest newManifest = GameManifest.load(newPath.resolve(GameManifest.DEFAULT_FILE_NAME));
        assertEquals("game1New", newManifest.getTitle());
        assertEquals("testSeed", newManifest.getSeed());
    }

    @Test(expected = ResourceAccessException.class)
    public void testRenameNotExisting() throws ResourceAccessException {
        new PatchGameMethod(pathManagerMock, "game2New").perform(getRenameData("game2"), null);
    }

    @Test(expected = ResourceAccessException.class)
    public void testRenameConflict() throws ResourceAccessException {
        new PatchGameMethod(pathManagerMock, "game3").perform(getRenameData("game1"), null);
    }

    @Test(expected = ResourceAccessException.class)
    public void testRenameEmptyName() throws ResourceAccessException {
        new PatchGameMethod(pathManagerMock, "game1").perform(getRenameData(""), null);
    }

    private NewGameMetadata getRenameData(String newName) {
        return new NewGameMetadata(newName, null, null, null);
    }
}
