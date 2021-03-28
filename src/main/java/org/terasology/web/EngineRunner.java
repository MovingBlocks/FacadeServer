/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.web;

import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.TerasologyEngineBuilder;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.modes.StateIngame;
import org.terasology.engine.core.modes.StateLoading;
import org.terasology.engine.core.modes.StateMainMenu;
import org.terasology.engine.core.subsystem.common.hibernation.HibernationSubsystem;
import org.terasology.engine.core.subsystem.headless.HeadlessAudio;
import org.terasology.engine.core.subsystem.headless.HeadlessGraphics;
import org.terasology.engine.core.subsystem.headless.HeadlessInput;
import org.terasology.engine.core.subsystem.headless.HeadlessTimer;
import org.terasology.engine.core.subsystem.headless.mode.StateHeadlessSetup;
import org.terasology.engine.game.Game;
import org.terasology.web.io.JsonSession;
import org.terasology.web.resources.ResourceManager;
import org.terasology.web.serverAdminManagement.ServerAdminListUpdaterSystem;

/**
 * This class starts and runs the engine, similarly to the PC facade's Terasology class.
 */
public final class EngineRunner {

    private static final EngineRunner INSTANCE = new EngineRunner();

    private TerasologyEngine engine;

    private EngineRunner() {
    }

    public static EngineRunner getInstance() {
        return INSTANCE;
    }

    void runEngine(boolean autoStart) {
        TerasologyEngineBuilder builder = new TerasologyEngineBuilder();
        populateSubsystems(builder);
        engine = builder.build();
        engine.subscribeToStateChange(() -> {
            ResourceManager.getInstance().initialize(engine);
            GameState newState = engine.getState();
            if (newState instanceof StateMainMenu) {
                engine.shutdown();
            } else if (engine.getState() instanceof StateIngame) {
                newState.getContext().get(ComponentSystemManager.class).register(new ServerAdminListUpdaterSystem());
            }
            JsonSession.handleEngineStateChanged(newState);
        });
        engine.run(autoStart ? new StateHeadlessSetup() : new StateEngineIdle());
    }

    private void populateSubsystems(TerasologyEngineBuilder builder) {
        builder.add(new HeadlessGraphics())
                .add(new HeadlessTimer())
                .add(new HeadlessAudio())
                .add(new HeadlessInput())
                .add(new HibernationSubsystem());
    }

    public <T> T getFromCurrentContext(Class<T> clazz) {
        GameState state = engine.getState();
        return state == null ? engine.getFromEngineContext(clazz) : engine.getState().getContext().get(clazz);
    }

    public boolean isRunningGame() {
        return engine.getState() instanceof StateIngame;
    }

    public String getRunningOrLoadingGameName() {
        GameState currentState = engine.getState();
        if (currentState instanceof StateIngame || currentState instanceof StateLoading) {
            return currentState.getContext().get(Game.class).getName();
        }
        return null;
    }

}
