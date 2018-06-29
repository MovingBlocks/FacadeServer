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
package org.terasology.web.client;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.web.EngineRunner;

/**
 * This factory is used to generate instances of headless clients
 */
public class HeadlessClientFactory {

    private final EntityManager entityManager;
    private final EngineRunner engineRunner;

    public HeadlessClientFactory(EntityManager entityManager, EngineRunner engineRunner) {
        this.entityManager = entityManager;
        this.engineRunner = engineRunner;
    }

    public HeadlessClientFactory(EntityManager entityManager) {
        this(entityManager, EngineRunner.getInstance());
    }

    /**
     * @return a new instance of {@link AuthenticatedHeadlessClient} connected to the {@link EntityManager}
     * passed to the constructor of this class.
     */
    public AuthenticatedHeadlessClient connectNewHeadlessClient(String id) {
        AuthenticatedHeadlessClient result = new AuthenticatedHeadlessClient(id);
        connectIfGameIsRunning(result);
        return result;
    }

    /**
     * @return a new instance of {@link AnonymousHeadlessClient}, which entity has no correspondent clientInfo entity and components;
     * thus, it's data is not persisted in the save files. Used to provide anonymous read-only access to certain resources.
     */
    public AnonymousHeadlessClient connectNewAnonymousHeadlessClient() {
        AnonymousHeadlessClient result = new AnonymousHeadlessClient();
        connectIfGameIsRunning(result);
        return result;
    }

    private void connectIfGameIsRunning(HeadlessClient client) {
        if (engineRunner.isRunningGame()) {
            client.connect(entityManager);
        }
    }
}
