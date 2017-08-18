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

import org.terasology.web.resources.ResourceAccessException;

public final class ResourceMethodFactory {

    private ResourceMethodFactory() {
    }

    public static <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> createParametrizedMethod(
            ResourcePath path, Class<INTYPE> inType, ParametrizedMethodHandler<INTYPE, OUTTYPE> handler) throws ResourceAccessException {
        String parameter = path.assertAndConsumeLastItem();
        return new ResourceMethod<INTYPE, OUTTYPE>() {
            @Override
            public Class<INTYPE> getInType() {
                return inType;
            }

            @Override
            public OUTTYPE perform(INTYPE data) throws ResourceAccessException {
                return handler.perform(data, parameter);
            }
        };
    }

    public static <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> createParameterlessMethod(
            ResourcePath path, Class<INTYPE> inType, ParameterlessMethodHandler<INTYPE, OUTTYPE> handler) throws ResourceAccessException {
        path.assertEmpty();
        return new ResourceMethod<INTYPE, OUTTYPE>() {
            @Override
            public Class<INTYPE> getInType() {
                return inType;
            }

            @Override
            public OUTTYPE perform(INTYPE data) throws ResourceAccessException {
                return handler.perform(data);
            }
        };
    }
}
