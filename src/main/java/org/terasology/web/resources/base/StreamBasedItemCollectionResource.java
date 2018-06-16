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

import org.terasology.web.serverAdminManagement.PermissionType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;

public abstract class StreamBasedItemCollectionResource<T> extends AbstractItemCollectionResource {

    protected StreamBasedItemCollectionResource(Map<String, Function<String, Resource>> itemSubResourceProviders) {
        super(itemSubResourceProviders);
    }

    protected StreamBasedItemCollectionResource() {
        super();
    }

    @Override
    public final ResourceMethod<Void, List<T>> getGetCollectionMethod() throws ResourceAccessException {
        return createParameterlessMethod(getGetMethodSecurityRequirements(), getPermissionType(), Void.class,
                (data, client) -> getDataSourceStream().collect(Collectors.toList()));
    }

    @Override
    public final ResourceMethod<Void, T> getGetItemMethod(String itemId) throws ResourceAccessException {
        return createParameterlessMethod(getGetMethodSecurityRequirements(), getPermissionType(), Void.class, (data, client) -> {
            Optional<T> result = getDataSourceStream()
                    .filter(item -> itemMatchesId(itemId, item))
                    .findFirst();
            if (!result.isPresent()) {
                throw ResourceAccessException.NOT_FOUND;
            }
            return result.get();
        });
    }

    protected ClientSecurityRequirements getGetMethodSecurityRequirements() {
        return ClientSecurityRequirements.PUBLIC;
    }

    private PermissionType getPermissionType() {
        return PermissionType.NO_PERMISSION;
    }

    protected abstract Stream<T> getDataSourceStream();
    protected abstract boolean itemMatchesId(String itemId, T item);
}
