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
package org.terasology.web.resources;

import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateLoading;
import org.terasology.network.Client;
import org.terasology.network.NetworkMode;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.web.ServerAdminsManager;
import org.terasology.web.StateEngineIdle;
import org.terasology.web.io.ActionResult;
import org.terasology.web.io.JsonSession;

import java.util.Optional;
import java.util.stream.Stream;

public class EngineStateResource implements ReadableResource<EngineStateMetadata>, WritableResource<String> {

    private GameEngine gameEngine;

    EngineStateResource(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    @Override
    public String getName() {
        return "engineState";
    }

    @Override
    public EngineStateMetadata read(Client requestingClient) {
        return EngineStateMetadata.build(gameEngine.getState());
    }

    @Override
    public Class<String> getDataType() {
        return String.class;
    }

    @Override
    public void write(Client requestingClient, String data) throws ResourceAccessException {
        // if the supplied string is a savegame name, the engine will switch to run this game;
        // if it's empty, it will switch to the idle state.
        ServerAdminsManager.checkClientIsServerAdmin(requestingClient.getId());
        JsonSession.disconnectAllClients();
        if (data == null || data.length() == 0) {
            gameEngine.changeState(new StateEngineIdle());
        } else {
            Stream<GameInfo> saveGames = GameProvider.getSavedGames().stream();
            Optional<GameInfo> game = saveGames.filter((gameInfo) -> gameInfo.getManifest().getTitle().equals(data)).findFirst();
            if (game.isPresent()) {
                gameEngine.changeState(new StateLoading(game.get().getManifest(), NetworkMode.LISTEN_SERVER));
            } else {
                throw new ResourceAccessException(new ActionResult(ActionResult.Status.NOT_FOUND, "No savegame with the specified name exists on this server"));
            }
        }
    }

}
