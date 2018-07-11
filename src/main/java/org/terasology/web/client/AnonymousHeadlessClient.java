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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.rendering.nui.Color;

/**
 * This class is used to make an anonymous client, which is used to get read-only access to resources.
 * @see HeadlessClientFactory
 */
public class AnonymousHeadlessClient implements HeadlessClient {

    private EntityRef entity = EntityRef.NULL;

    public AnonymousHeadlessClient() {
    }

    public void connect(EntityManager entityManager) {
        //only the client entity is created to identify the client in the engine and receive events
        //no clientInfo entity is created because this client's data must not be persistent
        entity = entityManager.create("engine:client");
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Override
    public void disconnect() {
        entity.destroy();
        entity = EntityRef.NULL;
    }

    @Override
    public EntityRef getEntity() {
        return entity;
    }

    @Override
    public String getName() {
        return "anonymousHeadlessClient";
    }

    @Override
    public String getId() {
        return "anonymousHeadlessClient";
    }

    @Override
    public Color getColor() {
        return Color.BLACK;
    }
}
