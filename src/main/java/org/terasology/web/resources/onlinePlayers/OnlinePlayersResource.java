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
package org.terasology.web.resources.onlinePlayers;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.network.NetworkSystem;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.registry.In;
import org.terasology.web.resources.DefaultComponentSystem;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.AbstractSimpleResource;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.resources.base.ResourcePath;
import org.terasology.web.serverAdminManagement.PermissionType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;

@RegisterSystem
public class OnlinePlayersResource extends AbstractSimpleResource implements DefaultComponentSystem {

    @In
    private NetworkSystem networkSystem;

    @ReceiveEvent
    public void onConnected(ConnectedEvent event, EntityRef entityRef) {
        notifyChangedForAllClients();
    }

    @ReceiveEvent
    public void onDisconnected(DisconnectedEvent event, EntityRef entityRef) {
        notifyChangedForAllClients();
    }

    @Override
    protected ResourceMethod<Void, List<OnlinePlayerMetadata>> getGetMethod(ResourcePath path) throws ResourceAccessException {
        return createParameterlessMethod(path, ClientSecurityRequirements.PUBLIC, PermissionType.NO_PERMISSION, Void.class, (data, client) ->
                StreamSupport.stream(networkSystem.getPlayers().spliterator(), true)
                    .map(OnlinePlayerMetadata::new)
                    .collect(Collectors.toList()));
    }
}
