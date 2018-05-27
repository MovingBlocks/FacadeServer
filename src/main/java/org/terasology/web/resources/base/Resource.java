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

/**
 * Base interface for the resource implementation.
 */
public interface Resource {

    /**
     * Determine the method used to request a resource.
     * @param methodName type of http request (GET, PUT, etc.).
     * @param path path of the URL to the resource (api/resources/console for example).
     * @return the method used to request the resource.
     * @throws ResourceAccessException the requested method is not supported by this resource.
     */
    ResourceMethod getMethod(ResourceMethodName methodName, ResourcePath path) throws ResourceAccessException;

    /**
     * Set the resource observer.
     * @param observer the observer to set.
     */
    void setObserver(ResourceObserver observer);

    /**
     * Tell clients that a value has been changed. Clients who are subscribed to changes in a specified resource
     * will access the REST API to get any new values that have been changed.
     */
    void notifyChangedForAllClients();
}
