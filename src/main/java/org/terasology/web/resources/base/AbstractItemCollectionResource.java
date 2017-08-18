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

public abstract class AbstractItemCollectionResource implements Resource {

    @Override
    public final ResourceMethod getMethod(ResourceMethodName methodName, ResourcePath path) throws ResourceAccessException {
        String itemId = null;
        if (!path.isEmpty()) {
            itemId = path.consumeNextItem();
        }
        switch (methodName) {
            case GET:
                return itemId == null ? getGetCollectionMethod() : getGetItemMethod(itemId);
            case POST:
                return itemId == null ? getPostCollectionMethod() : getPostItemMethod(itemId);
            case PUT:
                return itemId == null ? getPutCollectionMethod() : getPutItemMethod(itemId);
            case DELETE:
                return itemId == null ? getDeleteCollectionMethod() : getDeleteItemMethod(itemId);
            case PATCH:
                return itemId == null ? getPatchCollectionMethod() : getPatchItemMethod(itemId);
        }
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    // in subclasses, override the methods supported by the resource

    protected ResourceMethod getGetCollectionMethod() throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    protected ResourceMethod getGetItemMethod(String itemId) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    protected ResourceMethod getPostCollectionMethod() throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    protected ResourceMethod getPostItemMethod(String itemId) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    protected ResourceMethod getPutCollectionMethod() throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    protected ResourceMethod getPutItemMethod(String itemId) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    protected ResourceMethod getDeleteCollectionMethod() throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    protected ResourceMethod getDeleteItemMethod(String itemId) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    protected ResourceMethod getPatchCollectionMethod() throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }

    protected ResourceMethod getPatchItemMethod(String itemId) throws ResourceAccessException {
        throw ResourceAccessException.METHOD_NOT_ALLOWED;
    }
}
