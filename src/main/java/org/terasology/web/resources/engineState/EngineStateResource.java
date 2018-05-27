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
package org.terasology.web.resources.engineState;

import org.terasology.engine.GameEngine;
import org.terasology.registry.In;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.AbstractSimpleResource;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.resources.base.ResourcePath;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;
import static org.terasology.web.resources.base.ResourceMethodFactory.createVoidParameterlessMethod;

/**
 * This class determines what methods can be used to access the engine state.
 */
public class EngineStateResource extends AbstractSimpleResource {

    @In
    private GameEngine gameEngine;

    @Override
    protected ResourceMethod<Void, EngineStateMetadata> getGetMethod(ResourcePath path) throws ResourceAccessException {
        return createParameterlessMethod(path, ClientSecurityRequirements.PUBLIC, Void.class,
                (data, client) -> EngineStateMetadata.build(gameEngine.getState()));
    }

    @Override
    protected ResourceMethod<EngineStateMetadata, Void> getPutMethod(ResourcePath path) throws ResourceAccessException {
        return createVoidParameterlessMethod(path, ClientSecurityRequirements.REQUIRE_ADMIN, EngineStateMetadata.class,
                (data, client) -> data.switchEngineToThisState(gameEngine));
    }
}
