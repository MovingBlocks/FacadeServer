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

import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.naming.NameVersion;
import org.terasology.naming.Version;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.world.internal.WorldInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NewGameMethodTest {

    private static final SimpleUri DEFAULT_WORLD_GENERATOR = new SimpleUri("testModule", "testGenerator");

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private PathManager pathManagerMock;
    private DependencyResolver dependencyResolverMock;

    @Before
    public void setUp() throws IOException {
        pathManagerMock = mock(PathManager.class);
        Path gamePath = tempFolder.getRoot().toPath().resolve("game1");
        when(pathManagerMock.getSavePath("game1")).thenReturn(gamePath);

        dependencyResolverMock = mock(DependencyResolver.class);
        Module moduleMock1 = mock(Module.class);
        Module moduleMock2 = mock(Module.class);
        when(moduleMock1.getId()).thenReturn(new Name("module1"));
        when(moduleMock1.getVersion()).thenReturn(new Version(1, 0, 0));
        when(moduleMock2.getId()).thenReturn(new Name("module2"));
        when(moduleMock2.getVersion()).thenReturn(new Version(2, 0, 1));
        Set<Module> moduleMockSet = new HashSet<>(Arrays.asList(moduleMock1, moduleMock2));
        when(dependencyResolverMock.resolve(any())).thenReturn(new ResolutionResult(true, moduleMockSet));
    }

    private void performAction(String name, String seed, List<Name> modules, SimpleUri worldGenerator) throws ResourceAccessException {
        NewGameMethod newGameMethod = new NewGameMethod(pathManagerMock, dependencyResolverMock);
        newGameMethod.perform(new NewGameMetadata(name, seed, modules, worldGenerator), null);
    }

    @Test
    public void testNewOk() throws ResourceAccessException, IOException {
        List<Name> inputModuleList = Arrays.asList(new Name("a"), new Name("b"));
        performAction("game1", "gameSeed", inputModuleList, DEFAULT_WORLD_GENERATOR);
        verify(dependencyResolverMock, times(1)).resolve(inputModuleList);

        Path gamePath = tempFolder.getRoot().toPath().resolve("game1");
        Path manifestPath = gamePath.resolve(GameManifest.DEFAULT_FILE_NAME);
        assertTrue(Files.isDirectory(gamePath));
        assertTrue(Files.isRegularFile(manifestPath));
        GameManifest gameManifest = GameManifest.load(manifestPath);
        assertEquals("game1", gameManifest.getTitle());
        assertEquals("gameSeed", gameManifest.getSeed());
        assertEquals(2, gameManifest.getModules().size());
        assertTrue(gameManifest.getModules().contains(new NameVersion(new Name("module1"), new Version(1, 0, 0))));
        assertEquals(1, Iterables.size(gameManifest.getWorlds()));
        WorldInfo worldInfo = gameManifest.getWorldInfo("main");
        assertEquals("gameSeed", worldInfo.getSeed());
        assertEquals("testModule", worldInfo.getWorldGenerator().getModuleName().toString());
        assertEquals("testGenerator", worldInfo.getWorldGenerator().getObjectName().toString());
    }

    @Test(expected = ResourceAccessException.class)
    public void testNewEmptyName() throws ResourceAccessException {
        performAction("", "gameSeed", Collections.emptyList(), DEFAULT_WORLD_GENERATOR);
    }

    @Test(expected = ResourceAccessException.class)
    public void testNewEmptySeed() throws ResourceAccessException {
        performAction("game1", "", Collections.emptyList(), DEFAULT_WORLD_GENERATOR);
    }

    @Test(expected = ResourceAccessException.class)
    public void testNewNullModules() throws ResourceAccessException {
        performAction("game1", "gameSeed", null, DEFAULT_WORLD_GENERATOR);
    }

    @Test(expected = ResourceAccessException.class)
    public void testNewNullWorldGenerator() throws ResourceAccessException {
        performAction("game1", "gameSeed", Collections.emptyList(), null);
    }

}
