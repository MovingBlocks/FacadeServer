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

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static org.terasology.web.resources.base.ResourceMethodFactory.decorateMethod;

public abstract class AbstractItemCollectionResource extends AbstractObservableResource {

    private Map<String, Function<String, Resource>> itemSubResourceProviders;

    protected AbstractItemCollectionResource(Map<String, Function<String, Resource>> itemSubResourceProviders) {
        this.itemSubResourceProviders = itemSubResourceProviders;
    }

    protected AbstractItemCollectionResource() {
        this(Collections.emptyMap());
    }

    @Override
    public final ResourceMethod getMethod(ResourceMethodName methodName, ResourcePath path) throws ResourceAccessException {
        String itemId = null;
        if (!path.isEmpty()) {
            itemId = path.consumeNextItem();
            if (!path.isEmpty()) {
                return getItemSubResourceMethod(methodName, path, itemId);
            }
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

    private ResourceMethod getItemSubResourceMethod(ResourceMethodName methodName, ResourcePath path, String itemId) throws ResourceAccessException {
        String subResourceName = path.consumeNextItem();
        Function<String, Resource> subResourceProvider = itemSubResourceProviders.get(subResourceName);
        if (subResourceProvider != null) {
            return decorateMethod(subResourceProvider.apply(itemId).getMethod(methodName, path),
                    () -> beforeSubResourceAccess(subResourceName, itemId),
                    () -> afterSubResourceAccess(subResourceName, itemId));
        } else {
            throw ResourceAccessException.NOT_FOUND;
        }
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

    // override to do additional actions before or after a sub-resource is accessed (e.g. notify observers)

    protected void beforeSubResourceAccess(String subResourceName, String itemId) throws ResourceAccessException {
        // by default, do nothing
    }

    protected void afterSubResourceAccess(String subResourceName, String itemId) throws ResourceAccessException {
        // by default, do nothing
    }
}
