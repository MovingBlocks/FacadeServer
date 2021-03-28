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


import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * Interface for a resource observer. A resource observer looks at a particular resource
 * and performs an action whenever the resource changes.
 */
public interface ResourceObserver {

    /**
     *
     * @param senderPath
     * @param eventData
     * @param targetClientEntity
     */
    void onEvent(ResourcePath senderPath, Object eventData, EntityRef targetClientEntity);

    /**
     *
     * @param senderPath
     * @param sender
     * @param targetClientEntity
     */
    void onChangedForClient(ResourcePath senderPath, Resource sender, EntityRef targetClientEntity);

    /**
     *
     * @param senderPath
     * @param sender
     */
    void onChangedForAllClients(ResourcePath senderPath, Resource sender);
}
