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

import org.terasology.network.Client;
import org.terasology.web.ThrowingRunnable;
import org.terasology.web.client.ClientSecurityInfo;
import org.terasology.web.serverAdminManagement.PermissionType;

public final class ResourceMethodFactory {

    private ResourceMethodFactory() {
    }

    public static <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> createParametrizedMethod(
            ResourcePath path, ClientSecurityRequirements securityRequirements, Class<INTYPE> inType,
            ParametrizedMethodHandler<INTYPE, OUTTYPE> handler) throws ResourceAccessException {
        String parameter = path.assertAndConsumeLastItem();
        return createParametrizedMethod(parameter, securityRequirements, inType, handler);
    }

    public static <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> createParametrizedMethod(
            String parameter, ClientSecurityRequirements securityRequirements, Class<INTYPE> inType,
            ParametrizedMethodHandler<INTYPE, OUTTYPE> handler) throws ResourceAccessException {
        return new ResourceMethodImpl<>(inType, securityRequirements, (data, client) -> handler.perform(data, parameter, client));
    }

    public static <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> createParameterlessMethod(
            ClientSecurityRequirements securityRequirements, Class<INTYPE> inType,
            ParameterlessMethodHandler<INTYPE, OUTTYPE> handler) throws ResourceAccessException {
        return new ResourceMethodImpl<>(inType, securityRequirements, handler);
    }

    public static <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> createParameterlessMethod(
            ResourcePath path, ClientSecurityRequirements securityRequirements, Class<INTYPE> inType,
            ParameterlessMethodHandler<INTYPE, OUTTYPE> handler) throws ResourceAccessException {
        path.assertEmpty();
        return createParameterlessMethod(securityRequirements, inType, handler);
    }

    public static <INTYPE> ResourceMethod<INTYPE, Void> createVoidParameterlessMethod(
            ClientSecurityRequirements securityRequirements, Class<INTYPE> inType,
            VoidParameterlessMethodHandler<INTYPE> handler) throws ResourceAccessException {
        return createParameterlessMethod(securityRequirements, inType, (data, client) -> {
            handler.perform(data, client);
            return null;
        });
    }

    public static <INTYPE> ResourceMethod<INTYPE, Void> createVoidParameterlessMethod(
            ResourcePath path, ClientSecurityRequirements securityRequirements, Class<INTYPE> inType,
            VoidParameterlessMethodHandler<INTYPE> handler) throws ResourceAccessException {
        path.assertEmpty();
        return createVoidParameterlessMethod(securityRequirements, inType, handler);
    }

    public static <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> decorateMethod(
            ResourceMethod<INTYPE, OUTTYPE> base, ThrowingRunnable<ResourceAccessException> before, ThrowingRunnable<ResourceAccessException> after) {
        return new ResourceMethod<INTYPE, OUTTYPE>() {
            @Override
            public Class<INTYPE> getInType() {
                return base.getInType();
            }

            @Override
            public boolean clientIsAllowed(ClientSecurityInfo securityInfo) {
                return base.clientIsAllowed(securityInfo);
            }

            @Override
            public OUTTYPE perform(INTYPE data, Client client) throws ResourceAccessException {
                before.run();
                OUTTYPE result = base.perform(data, client);
                after.run();
                return result;
            }
        };
    }
}
