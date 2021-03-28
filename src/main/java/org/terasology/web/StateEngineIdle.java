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


import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.LoggingContext;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.ConsoleImpl;
import org.terasology.engine.logic.console.ConsoleSystem;
import org.terasology.engine.logic.console.commands.CoreCommands;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.internal.NUIManagerInternal;
import org.terasology.engine.rendering.nui.internal.TerasologyCanvasRenderer;
import org.terasology.nui.canvas.CanvasRenderer;

/**
 * An engine state similar to {@link org.terasology.engine.subsystem.headless.mode.StateHeadlessSetup} which doesn't
 * automatically load or generate a savegame
 */
public class StateEngineIdle implements GameState {

    private EngineEntityManager entityManager;
    private EventSystem eventSystem;
    private ComponentSystemManager componentSystemManager;
    private Context context;

    @Override
    public void init(GameEngine engine) {
        context = engine.createChildContext();

        CoreRegistry.setContext(context);

        // let's get the entity event system running
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = context.get(EngineEntityManager.class);

        eventSystem = context.get(EventSystem.class);
        context.put(Console.class, new ConsoleImpl(context));

        NUIManager nuiManager = new NUIManagerInternal(context.get(TerasologyCanvasRenderer.class), context);
        context.put(NUIManager.class, nuiManager);

        componentSystemManager = new ComponentSystemManager(context);
        context.put(ComponentSystemManager.class, componentSystemManager);

        componentSystemManager.register(new ConsoleSystem(), "engine:ConsoleSystem");
        componentSystemManager.register(new CoreCommands(), "engine:CoreCommands");
        componentSystemManager.register(context.get(InputSystem.class), "engine:InputSystem");

        EntityRef localPlayerEntity = entityManager.create(new ClientComponent());
        LocalPlayer localPlayer = new LocalPlayer();
        context.put(LocalPlayer.class, localPlayer);
        localPlayer.setClientEntity(localPlayerEntity);

        componentSystemManager.initialise();
    }

    @Override
    public void dispose(boolean shuttingDown) {
        eventSystem.process();

        componentSystemManager.shutdown();

        entityManager.clear();
    }

    @Override
    public void handleInput(float delta) {

    }

    @Override
    public void update(float delta) {
        eventSystem.process();
        Thread.yield();
    }

    @Override
    public void render() {

    }

    @Override
    public boolean isHibernationAllowed() {
        return true;
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
