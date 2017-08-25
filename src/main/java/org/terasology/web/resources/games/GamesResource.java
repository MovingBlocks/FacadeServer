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

import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.registry.In;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.web.EngineRunner;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.resources.base.StreamBasedItemCollectionResource;

import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.terasology.web.resources.base.ResourceMethodFactory.decorateMethod;

public class GamesResource extends StreamBasedItemCollectionResource<GameInfo> {

    @In
    private ModuleManager moduleManager;

    public GamesResource() {
        super(Collections.singletonMap("backup", (gameName) -> new GamesBackupsResource(PathManager.getInstance(), gameName)));
    }

    @Override
    protected Stream<GameInfo> getDataSourceStream() {
        return GameProvider.getSavedGames().stream()
                .sorted(Comparator.comparing(GameInfo::getManifest, Comparator.comparing(GameManifest::getTitle)));
    }

    @Override
    protected boolean itemMatchesId(String itemId, GameInfo item) {
        return false;
    }

    @Override
    protected void beforeSubResourceAccess(String subResourceName, String itemId) throws ResourceAccessException {
        checkGameIsNotRunningOrLoading(itemId);
    }

    @Override
    protected void afterSubResourceAccess(String subResourceName, String itemId) {
        notifyChangedForAllClients();
    }

    @Override
    protected ResourceMethod<NewGameMetadata, Void> getPostCollectionMethod() throws ResourceAccessException {
        return decorateMethodWithNotifier(new NewGameMethod(PathManager.getInstance(), moduleManager));
    }

    @Override
    protected ResourceMethod<Void, Void> getDeleteItemMethod(String itemId) throws ResourceAccessException {
        return decorateMethodForExistingGame(itemId, new DeleteGameMethod(PathManager.getInstance(), itemId));
    }

    @Override
    protected ResourceMethod<NewGameMetadata, Void> getPatchItemMethod(String itemId) throws ResourceAccessException {
        return decorateMethodForExistingGame(itemId, new PatchGameMethod(PathManager.getInstance(), itemId));
    }

    private <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> decorateMethodWithNotifier(ResourceMethod<INTYPE, OUTTYPE> base) {
        return decorateMethod(base, () -> { }, this::notifyChangedForAllClients);
    }

    private <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> decorateMethodForExistingGame(String gameName, ResourceMethod<INTYPE, OUTTYPE> base) {
        return decorateMethod(base, () -> checkGameIsNotRunningOrLoading(gameName), this::notifyChangedForAllClients);
    }

    private void checkGameIsNotRunningOrLoading(String gameName) throws ResourceAccessException {
        if (gameName.equals(EngineRunner.getInstance().getRunningOrLoadingGameName())) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR,
                    "This action cannot be performed on a game which is running or loading."));
        }
    }
}
