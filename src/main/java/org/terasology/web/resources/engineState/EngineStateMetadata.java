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
package org.terasology.web.resources.engineState;

import com.google.common.collect.ImmutableMap;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.modes.StateIngame;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.subsystem.headless.mode.StateHeadlessSetup;
import org.terasology.game.Game;
import org.terasology.network.NetworkMode;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.web.StateEngineIdle;
import org.terasology.web.io.ActionResult;
import org.terasology.web.io.JsonSession;
import org.terasology.web.resources.base.ResourceAccessException;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public final class EngineStateMetadata {

    public enum State {
        IDLE,
        LOADING,
        RUNNING
    }

    private static final Function<GameState, EngineStateMetadata> RETURN_IDLE = (state) -> new EngineStateMetadata(State.IDLE, null);

    private static final Map<Class<? extends GameState>, Function<GameState, EngineStateMetadata>> METADATA_BUILDERS =
            ImmutableMap.<Class<? extends GameState>, Function<GameState, EngineStateMetadata>>builder()
                    .put(StateHeadlessSetup.class, RETURN_IDLE)
                    .put(StateEngineIdle.class, RETURN_IDLE)
                    .put(StateLoading.class, (state) -> new EngineStateMetadata(State.LOADING, state.getContext().get(Game.class).getName()))
                    .put(StateIngame.class, (state) -> new EngineStateMetadata(State.RUNNING, state.getContext().get(Game.class).getName()))
                    .build();

    private State state;
    private String gameName;

    private EngineStateMetadata(State state, String gameName) {
        this.state = state;
        this.gameName = gameName;
    }

    public static EngineStateMetadata build(GameState state) {
        return METADATA_BUILDERS.get(state.getClass()).apply(state);
    }

    public State getState() {
        return state;
    }

    public String getGameName() {
        return gameName;
    }

    public void switchEngineToThisState(GameEngine engine) throws ResourceAccessException {
        // if the supplied string is a savegame name, the engine will switch to run this game;
        // if it's empty, it will switch to the idle state.
        GameState newState;
        switch (state) {
            case IDLE:
                newState = new StateEngineIdle();
                break;
            case LOADING:
                Stream<GameInfo> saveGames = GameProvider.getSavedGames().stream();
                Optional<GameInfo> game = saveGames.filter((gameInfo) -> gameInfo.getManifest().getTitle().equals(gameName)).findFirst();
                if (!game.isPresent()) {
                    throw new ResourceAccessException(new ActionResult(ActionResult.Status.BAD_REQUEST, "No savegame with the specified name exists on this server."));
                }
                newState = new StateLoading(game.get().getManifest(), NetworkMode.LISTEN_SERVER);
                break;
            default:
                throw new ResourceAccessException(new ActionResult(ActionResult.Status.BAD_REQUEST, "It's not possible to switch the engine to the requested state."));
        }
        JsonSession.disconnectAllClients();
        engine.changeState(newState);
    }
}
