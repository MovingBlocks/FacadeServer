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
package org.terasology.web.webSocket;

import com.google.gson.JsonElement;
import org.terasology.web.io.gsonUtils.InvalidClientMessageException;
import org.terasology.web.io.gsonUtils.Validable;
import org.terasology.web.resources.base.ResourceMethodName;

import java.util.Collection;

public class ResourceRequestClientMessage implements Validable {

    private ResourceMethodName method;
    private Collection<String> resourcePath;
    private JsonElement data;

    public ResourceMethodName getMethod() {
        return method;
    }

    public Collection<String> getResourcePath() {
        return resourcePath;
    }

    public JsonElement getData() {
        return data;
    }

    @Override
    public void validate() throws InvalidClientMessageException {
        if (method == null) {
            throw new InvalidClientMessageException("an action must be specified");
        } else if (resourcePath == null) {
            throw new InvalidClientMessageException("a resource path must be specified");
        }
    }
}
