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
package org.terasology.web;

import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.TerasologyEngineBuilder;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.modes.StateIngame;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.subsystem.common.hibernation.HibernationSubsystem;
import org.terasology.engine.subsystem.headless.HeadlessAudio;
import org.terasology.engine.subsystem.headless.HeadlessGraphics;
import org.terasology.engine.subsystem.headless.HeadlessInput;
import org.terasology.engine.subsystem.headless.HeadlessTimer;
import org.terasology.engine.subsystem.headless.mode.StateHeadlessSetup;
import org.terasology.game.Game;
import org.terasology.web.resources.ResourceManager;
import org.terasology.web.serverAdminManagement.ServerAdminListUpdaterSystem;

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
            if (engine.getState() instanceof StateMainMenu) {
                engine.shutdown();
            } else if (engine.getState() instanceof StateIngame) {
                engine.getState().getContext().get(ComponentSystemManager.class).register(new ServerAdminListUpdaterSystem());
            }
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
