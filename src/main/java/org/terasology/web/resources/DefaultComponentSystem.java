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
package org.terasology.web.resources;

import org.terasology.entitySystem.systems.ComponentSystem;

/**
 * Extension of {@link org.terasology.entitySystem.systems.ComponentSystem} which provides default
 * do-nothing implementations of the various preBegin(), postSave(), etc methods, so that,
 * for convenience, implementor classes only need to override the necessary methods
 */
public interface DefaultComponentSystem extends ComponentSystem {

    @Override
    default void initialise() {
    }

    @Override
    default void preBegin() {
    }

    @Override
    default void postBegin() {
    }

    @Override
    default void preSave() {
    }

    @Override
    default void postSave() {
    }

    @Override
    default void shutdown() {
    }
}
