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
package org.terasology.web.resources.base;

import org.terasology.engine.network.Client;
import org.terasology.web.client.ClientSecurityInfo;

/**
 * Implementation of {@link ResourceMethod}.
 * @param <INTYPE> the type of data sent to the server from the client.
 * @param <OUTTYPE> the type of data sent from the server to the client.
 */
public class ResourceMethodImpl<INTYPE, OUTTYPE> implements ResourceMethod<INTYPE, OUTTYPE> {

    private final Class<INTYPE> inType;
    private final ClientSecurityRequirements securityRequirements;
    private final ParameterlessMethodHandler<INTYPE, OUTTYPE> handler;

    public ResourceMethodImpl(Class<INTYPE> inType, ClientSecurityRequirements securityRequirements, ParameterlessMethodHandler<INTYPE, OUTTYPE> handler) {
        this.inType = inType;
        this.securityRequirements = securityRequirements;
        this.handler = handler;
    }

    @Override
    public Class<INTYPE> getInType() {
        return inType;
    }

    @Override
    public boolean clientIsAllowed(ClientSecurityInfo securityInfo) {
        return securityRequirements.clientIsAllowed(securityInfo);
    }

    @Override
    public OUTTYPE perform(INTYPE data, Client client) throws ResourceAccessException {
        return handler.perform(data, client);
    }
}
