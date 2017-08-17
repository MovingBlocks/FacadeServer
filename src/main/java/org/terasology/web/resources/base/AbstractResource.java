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

public abstract class AbstractResource implements Resource {

    @Override
    public final <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> getMethod(ResourceMethodName methodName, ResourcePath path) throws ResourceAccessException {
        switch (methodName) {
            case GET:
                return getGetMethod(path);
            case POST:
                return getPostMethod(path);
            case PUT:
                return getPutMethod(path);
            case DELETE:
                return getDeleteMethod(path);
            case PATCH:
                return getPatchMethod(path);
        }
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    public <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> getGetMethod(ResourcePath path) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    public <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> getPostMethod(ResourcePath path) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    public <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> getPutMethod(ResourcePath path) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    public <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> getDeleteMethod(ResourcePath path) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    public <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> getPatchMethod(ResourcePath path) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }
}
