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
package org.terasology.web.resources.connectLists;

import org.terasology.network.internal.ServerConnectListManager;
import org.terasology.web.resources.base.AbstractItemCollectionResource;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.serverAdminManagement.PermissionType;

import java.util.Set;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;
import static org.terasology.web.resources.base.ResourceMethodFactory.createVoidParameterlessMethod;

public class ConnectListResource extends AbstractItemCollectionResource {

    public enum ConnectListType {
        BLACKLIST,
        WHITELIST
    }

    private ServerConnectListManager serverConnectListManager;
    private ConnectListType typeOfList;

    public ConnectListResource(ServerConnectListManager serverConnectListManager, ConnectListType connectListType) {
        this.serverConnectListManager = serverConnectListManager;
        this.typeOfList = connectListType;
    }

    @Override
    protected ResourceMethod<Void, Set> getGetCollectionMethod() throws ResourceAccessException {
        return createParameterlessMethod(ClientSecurityRequirements.PUBLIC, Void.class,
                (data, client) -> typeOfList == ConnectListType.BLACKLIST ? serverConnectListManager.getBlacklist() : serverConnectListManager.getWhitelist());
    }

    // TODO: validate input (check that the input is in the form of a player id: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx) where x is an alphanumeric character
    @Override
    protected ResourceMethod<Void, Void> getPostItemMethod(String itemId) throws ResourceAccessException {
        return createVoidParameterlessMethod(ClientSecurityRequirements.requireAdminPermission(PermissionType.CONSOLE_USER_MANAGEMENT), Void.class,
                (data, client) -> {
                    if (typeOfList == ConnectListType.BLACKLIST) {
                        serverConnectListManager.addToBlacklist(itemId);
                    } else {
                        serverConnectListManager.addToWhitelist(itemId);
                    }
                    notifyChangedForAllClients();
                });
    }

    @Override
    protected ResourceMethod<Void, Void> getDeleteItemMethod(String itemId) throws ResourceAccessException {
        return createVoidParameterlessMethod(ClientSecurityRequirements.requireAdminPermission(PermissionType.CONSOLE_USER_MANAGEMENT), Void.class,
                (data, client) -> {
                    if (typeOfList == ConnectListType.BLACKLIST) {
                        serverConnectListManager.removeFromBlacklist(itemId);
                    } else {
                        serverConnectListManager.removeFromWhitelist(itemId);
                    }
                    notifyChangedForAllClients();
                });
    }

}
