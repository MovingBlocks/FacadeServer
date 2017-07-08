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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.paths.PathManager;
import org.terasology.network.Client;
import org.terasology.network.NetworkMode;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.web.StateEngineIdle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class EngineStateResource implements ReadableResource<EngineStateMetadata>, WritableResource<String> {

    private static final Logger logger = LoggerFactory.getLogger(EngineStateResource.class);

    private GameEngine gameEngine;
    private List<String> serverAdmins;
    private EngineStateMetadata state;

    EngineStateResource(GameEngine gameEngine, List<String> serverAdmins) {
        this.gameEngine = gameEngine;
        this.serverAdmins = serverAdmins;
        this.state = EngineStateMetadata.build(gameEngine.getState());
    }

    EngineStateResource(GameEngine gameEngine) {
        this(gameEngine, loadServerAdminList());
    }

    @SuppressWarnings("unchecked")
    private static List<String> loadServerAdminList() {
        Path filePath = PathManager.getInstance().getHomePath().resolve("serverAdmins.json");
        try {
            return new Gson().fromJson(Files.newBufferedReader(filePath), List.class);
        } catch (IOException ex) {
            logger.warn("Failed to load serverAdmins.json, no client will be able to change the engine state");
            return new ArrayList<>();
        }
    }

    @Override
    public String getName() {
        return "engineState";
    }

    @Override
    public EngineStateMetadata read(Client requestingClient) {
        return state;
    }

    @Override
    public Class<String> getDataType() {
        return String.class;
    }

    @Override
    public void write(Client requestingClient, String data) {
        // if the supplied string is a savegame name, the engine will switch to run this game;
        // if it's empty, it will switch to the idle state.
        if (serverAdmins.contains(requestingClient.getId())) {
            if (data == null || data.length() == 0) {
                gameEngine.changeState(new StateEngineIdle());
            } else {
                Stream<GameInfo> saveGames = GameProvider.getSavedGames().stream();
                Optional<GameInfo> game = saveGames.filter((gameInfo) -> gameInfo.getManifest().getTitle().equals(data)).findFirst();
                if (game.isPresent()) {
                    gameEngine.changeState(new StateLoading(game.get().getManifest(), NetworkMode.LISTEN_SERVER));
                } else {
                    // TODO throw appropriate exception
                }
            }
        } else {
            //TODO throw appropriate exception
        }
    }
}
