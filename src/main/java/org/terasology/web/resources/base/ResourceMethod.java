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
package org.terasology.web.resources.base;

import org.terasology.network.Client;
import org.terasology.web.client.ClientSecurityInfo;

/**
 * Interface used to perform a resource request.
 * @param <INTYPE> the parameter received from the client.
 * @param <OUTTYPE> the parameter given to the client.
 */
public interface ResourceMethod<INTYPE, OUTTYPE> {

    /**
     * Get the type of INTYPE.
     * @return the type of INTYPE.
     */
    Class<INTYPE> getInType();

    /**
     * Determine if the client has sufficient permission to execute the method.
     * @param securityInfo the client requesting the command's security permissions.
     * @return whether the client has the permissions required.
     */
    boolean clientIsAllowed(ClientSecurityInfo securityInfo);

    /**
     * Perform the HTTP request for the client.
     * @param data the data to send to the server (if any).
     * @param client the client that requested the perform.
     * @return the data requested by the server.
     * @throws ResourceAccessException the specified resource cannot be retrieved.
     */
    OUTTYPE perform(INTYPE data, Client client) throws ResourceAccessException;
}
