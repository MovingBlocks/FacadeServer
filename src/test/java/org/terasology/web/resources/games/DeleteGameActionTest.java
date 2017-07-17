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
import org.terasology.web.resources.ResourceAccessException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteGameActionTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private DeleteGameAction deleteGameAction = new DeleteGameAction();

    @Before
    public void setUp() throws IOException {
        PathManager pathManagerMock = mock(PathManager.class);
        Path game1Path = tempFolder.getRoot().toPath().resolve("game1");
        Path game2Path = tempFolder.getRoot().toPath().resolve("game2");
        when(pathManagerMock.getSavePath("game1")).thenReturn(game1Path);
        when(pathManagerMock.getSavePath("game2")).thenReturn(game2Path);
        Files.createDirectory(game1Path);
        Files.createFile(game1Path.resolve(Paths.get("someFile")));
        deleteGameAction.setPathManager(pathManagerMock);
    }

    @Test
    public void testDeleteOk() throws ResourceAccessException {
        Path gamePath = tempFolder.getRoot().toPath().resolve("game1");
        assertTrue(Files.exists(gamePath));
        deleteGameAction.perform("game1");
        assertFalse(Files.exists(gamePath));
    }

    @Test(expected = ResourceAccessException.class)
    public void testDeleteNotExisting() throws ResourceAccessException {
        deleteGameAction.perform("game2");
    }
}
