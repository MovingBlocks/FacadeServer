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
package org.terasology.web.client;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.Client;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.world.chunks.Chunk;

public class AnonymousHeadlessClient implements Client {

    private EntityRef entity = EntityRef.NULL;

    AnonymousHeadlessClient() {
    }

    void connect(EntityManager entityManager) {
        //only the client entity is created to identify the client in the engine and receive events
        //no clientInfo entity is created because this client's data must not be persistent
        if (entityManager != null) {
            entity = entityManager.create("engine:client");
        }
        //if entityManager == null, we are in the StateEngineIdle state and the resources available
        //in this state are not bound to client entities
    }

    @Override
    public EntityRef getEntity() {
        return entity;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void onChunkRelevant(Vector3i pos, Chunk chunk) {

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

    @Override
    public void onChunkIrrelevant(Vector3i pos) {

    }

    @Override
    public void update(boolean netTick) {

    }

    @Override
    public void send(Event event, EntityRef target) {

    }

    @Override
    public ViewDistance getViewDistance() {
        return ViewDistance.LEGALLY_BLIND;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public void setViewDistanceMode(ViewDistance viewDistance) {

    }
}
