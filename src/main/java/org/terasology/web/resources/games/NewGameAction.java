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

import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.ResourceAccessException;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.time.WorldTime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.terasology.web.resources.InputCheckUtils.checkNotNull;
import static org.terasology.web.resources.InputCheckUtils.checkNotNullOrEmpty;

public class NewGameAction implements Action {

    private String gameName;
    private String seed;
    private List<Name> modules;
    private SimpleUri worldGenerator;

    @Override
    public void perform(ModuleManager moduleManager) throws ResourceAccessException {
        checkNotNullOrEmpty(gameName, "A name for the new game must be specified.");
        checkNotNullOrEmpty(seed, "A seed must be specified.");
        checkNotNull(modules, "A list of modules must be specified");
        checkNotNull(worldGenerator, "A world generator must be specified.");
        if (Files.exists(PathManager.getInstance().getSavePath(gameName))) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.CONFLICT, "A game with the specified name already exists"));
        }
        GameManifest newGameManifest = buildGameManifest(moduleManager);
        Path saveDir = PathManager.getInstance().getSavePath(newGameManifest.getTitle());
        Path saveFile = saveDir.resolve(GameManifest.DEFAULT_FILE_NAME);
        try {
            Files.createDirectory(saveDir);
            GameManifest.save(saveFile, newGameManifest);
        } catch (IOException ex) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR, "Failed to save new game manifest: " + ex.getMessage()));
        }
    }

    private GameManifest buildGameManifest(ModuleManager moduleManager) throws ResourceAccessException {
        GameManifest newGameManifest = new GameManifest();
        newGameManifest.setTitle(gameName);
        newGameManifest.setSeed(seed);

        ResolutionResult result = new DependencyResolver(moduleManager.getRegistry()).resolve(modules);
        if (!result.isSuccess()) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR, "Failed to resolve all module dependencies"));
        }
        for (Module m: result.getModules()) {
            newGameManifest.addModule(m.getId(), m.getVersion());
        }

        WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, newGameManifest.getSeed(),
                (long) (WorldTime.DAY_LENGTH * 0.025f), worldGenerator);
        newGameManifest.addWorld(worldInfo);

        return newGameManifest;
    }

}
