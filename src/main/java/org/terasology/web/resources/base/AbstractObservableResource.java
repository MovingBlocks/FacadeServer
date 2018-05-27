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

import org.terasology.entitySystem.entity.EntityRef;

/**
 * Basic implementation of the resource interface, taking all methods from the resource observer.
 */
public abstract class AbstractObservableResource implements Resource {

    private ResourceObserver observer;

    @Override
    public void setObserver(ResourceObserver observer) {
        this.observer = observer;
    }

    protected ResourceObserver getObserver() {
        return observer;
    }

    protected void notifyEvent(EntityRef clientEntity, Object eventData) {
        observer.onEvent(ResourcePath.createEmpty(), eventData, clientEntity);
    }

    protected void notifyChangedForClient(EntityRef clientEntity) {
        observer.onChangedForClient(ResourcePath.createEmpty(), this, clientEntity);
    }

    @Override
    public void notifyChangedForAllClients() {
        observer.onChangedForAllClients(ResourcePath.createEmpty(), this);
    }
}
