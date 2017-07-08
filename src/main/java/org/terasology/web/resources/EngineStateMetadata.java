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

import com.google.common.collect.ImmutableMap;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.modes.StateIngame;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.subsystem.headless.mode.StateHeadlessSetup;
import org.terasology.web.StateEngineIdle;

import java.util.Map;
import java.util.function.Function;

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
                    .put(StateLoading.class, (state) -> new EngineStateMetadata(State.LOADING, state.getLoggingPhase()))
                    .put(StateIngame.class, (state) -> new EngineStateMetadata(State.RUNNING, state.getLoggingPhase()))
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
}
