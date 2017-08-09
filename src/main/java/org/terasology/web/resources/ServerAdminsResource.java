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

import org.terasology.network.Client;
import org.terasology.web.serverAdminManagement.ServerAdminsManager;

import java.util.Set;

public class ServerAdminsResource extends ObservableReadableResource<Set<String>> implements WritableResource<ServerAdminsResourceAction> {

    public ServerAdminsResource() {
        ServerAdminsManager.getInstance().setOnListChangedCallback(this::notifyChangedAll);
    }

    @Override
    public String getName() {
        return "serverAdmins";
    }

    @Override
    public Set<String> read(Client requestingClient) throws ResourceAccessException {
        return ServerAdminsManager.getInstance().getAdminIds();
    }

    @Override
    public Class<ServerAdminsResourceAction> getDataType() {
        return ServerAdminsResourceAction.class;
    }

    @Override
    public boolean writeRequiresAuthentication() {
        return false;
    }

    @Override
    public boolean writeIsAdminRestricted() {
        return true;
    }

    @Override
    public void write(Client requestingClient, ServerAdminsResourceAction data) throws ResourceAccessException {
        switch(data.getAction()) {
            case ADD:
                ServerAdminsManager.getInstance().addAdmin(data.getClientId());
                break;
            case REMOVE:
                ServerAdminsManager.getInstance().removeAdmin(data.getClientId());
        }
        ServerAdminsManager.getInstance().saveAdminList();
    }
}
