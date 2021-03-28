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

import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.internal.AbstractClient;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.nui.Color;

/**
 * This class is used to make an authenticated client, which is able to connect to the
 * server. Note that authenticated does not necessarily mean that the client can perform admin
 * actions on the server.
 * @see HeadlessClientFactory
 */
public class AuthenticatedHeadlessClient extends AbstractClient implements HeadlessClient {

    private String id;
    private boolean connectedToEntityManager;

    public AuthenticatedHeadlessClient(String id) {
        this.id = id;
    }

    @Override
    public void connect(EntityManager entityManager) {
        createEntity(id, Color.BLACK, entityManager);
        connectedToEntityManager = true;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public void disconnect() {
        if (connectedToEntityManager && getEntity() != EntityRef.NULL) {
            super.disconnect();
            connectedToEntityManager = false;
        }
    }

    @Override
    public String getName() {
        return id; //TODO temporary
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Color getColor() {
        return Color.BLACK; //TODO temporary default
    }

    @Override
    public void onChunkRelevant(Vector3ic pos, Chunk chunk) {

    }

    @Override
    public void onChunkIrrelevant(Vector3ic pos) {

    }
}
