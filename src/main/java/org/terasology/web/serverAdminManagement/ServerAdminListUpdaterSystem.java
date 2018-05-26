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
package org.terasology.web.serverAdminManagement;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.network.events.ConnectedEvent;

/**
 * This class serves as an abstraction layer between ServerAdminsManager and other classes.
 * It is used when the admins list needs to be saved or when a client connects without any admins previously set.
 */
public class ServerAdminListUpdaterSystem extends BaseComponentSystem {

    @ReceiveEvent
    public void onConnected(ConnectedEvent event, EntityRef entityRef) {
        ServerAdminsManager.getInstance().addFirstAdminIfNecessary(event.getPlayerStore().getId());
    }

    @Override
    public void postSave() {
        ServerAdminsManager.getInstance().saveAdminList();
    }
}
