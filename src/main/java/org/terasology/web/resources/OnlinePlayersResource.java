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
package org.terasology.web.resources;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.network.Client;
import org.terasology.network.NetworkSystem;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.registry.In;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RegisterSystem
public class OnlinePlayersResource extends ObservableReadableResource<List<OnlinePlayerMetadata>> implements DefaultComponentSystem {

    @In
    private NetworkSystem networkSystem;

    OnlinePlayersResource() {
    }

    @Override
    public String getName() {
        return "onlinePlayers";
    }

    @ReceiveEvent
    public void onConnected(ConnectedEvent event, EntityRef entityRef) {
        notifyChangedAll();
    }

    @ReceiveEvent
    public void onDisconnected(DisconnectedEvent event, EntityRef entityRef) {
        notifyChangedAll();
    }

    @Override
    public List<OnlinePlayerMetadata> read(Client requestingClient) {
        return StreamSupport.stream(networkSystem.getPlayers().spliterator(), true)
                .map(OnlinePlayerMetadata::new)
                .collect(Collectors.toList());
    }
}
