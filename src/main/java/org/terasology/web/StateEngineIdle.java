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

import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.LoggingContext;
import org.terasology.engine.modes.GameState;

public class StateEngineIdle implements GameState {

    private Context context;

    @Override
    public void init(GameEngine engine) {
        context = engine.createChildContext();
    }

    @Override
    public void dispose(boolean shuttingDown) {

    }

    @Override
    public void handleInput(float delta) {

    }

    @Override
    public void update(float delta) {
        Thread.yield();
    }

    @Override
    public void render() {

    }

    @Override
    public boolean isHibernationAllowed() {
        return false;
    }

    @Override
    public String getLoggingPhase() {
        return LoggingContext.INIT_PHASE;
    }

    @Override
    public Context getContext() {
        return context;
    }
}
