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

import java.util.HashMap;
import java.util.Map;

public final class RouterResource implements Resource {

    private final Resource rootResource;
    private final Map<String, Resource> subResources = new HashMap<>();

    // must use builder
    private RouterResource(Resource rootResource) {
        this.rootResource = rootResource;
    }

    @Override
    public <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> getMethod(ResourceMethodName methodName, ResourcePath path) throws ResourceAccessException {
        if (path.isEmpty()) {
            return rootResource.getMethod(methodName, path);
        } else {
            String selector = path.consumeNextItem();
            Resource res = subResources.get(selector);
            return res.getMethod(methodName, path);
        }
    }

    private static final class NullResource implements Resource {

        @Override
        public <INTYPE, OUTTYPE> ResourceMethod<INTYPE, OUTTYPE> getMethod(ResourceMethodName methodName, ResourcePath path) throws ResourceAccessException {
            return new ResourceMethod<INTYPE, OUTTYPE>() {
                @Override
                public Class<INTYPE> getInType() {
                    return null;
                }

                @Override
                public OUTTYPE perform(INTYPE data) throws ResourceAccessException {
                    throw ResourceAccessException.NOT_FOUND;
                }
            };
        }
    }

    public static final class Builder {
        private final RouterResource result;

        public Builder() {
            result = new RouterResource(new NullResource());
        }

        public Builder(Resource rootResource) {
            result = new RouterResource(rootResource);
        }

        public void addSubResource(String name, Resource resource) {
            result.subResources.put(name, resource);
        }

        public RouterResource build() {
            return result;
        }
    }
}
