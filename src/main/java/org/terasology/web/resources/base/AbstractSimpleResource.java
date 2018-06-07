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

/**
 * Simple resource class which provides access to a variable through REST.
 */
public abstract class AbstractSimpleResource extends AbstractObservableResource {

    @Override
    public final ResourceMethod getMethod(ResourceMethodName methodName, ResourcePath path) throws ResourceAccessException {
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

    // in subclasses, override the methods supported by the resource

    protected ResourceMethod getGetMethod(ResourcePath path) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    protected ResourceMethod getPostMethod(ResourcePath path) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    protected ResourceMethod getPutMethod(ResourcePath path) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    protected ResourceMethod getDeleteMethod(ResourcePath path) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    protected ResourceMethod getPatchMethod(ResourcePath path) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }
}
