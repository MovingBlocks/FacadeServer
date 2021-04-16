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

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.Client;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.math.geom.Vector3i;
import org.terasology.web.serverAdminManagement.AdminPermissionManager;
import org.terasology.web.serverAdminManagement.ServerAdminsManager;

/**
 * Interface for clients attempting to access resources or connect to the web interface frontend.
 */
public interface HeadlessClient extends Client {

    void connect(EntityManager entityManager);
    boolean isAnonymous();

    default ClientSecurityInfo getSecurityInfo() {
        return new ClientSecurityInfo(!isAnonymous(), ServerAdminsManager.getInstance().clientHasAdminPermissions(getId()),
                AdminPermissionManager.getInstance().getPermissionsOfAdmin(getId()));
    }

    @Override
    default void update(boolean netTick) {
    }

    @Override
    default void send(Event event, EntityRef target) {
    }

    @Override
    default ViewDistance getViewDistance() {
        return ViewDistance.LEGALLY_BLIND;
    }

    @Override
    default boolean isLocal() {
        return false;
    }

    @Override
    default void setViewDistanceMode(ViewDistance viewDistance) {
    }
}
