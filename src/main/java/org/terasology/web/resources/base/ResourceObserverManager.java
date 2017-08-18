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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ResourceObserverManager {

    private Resource observedResource;
    private Map<EntityRef, Consumer<Resource>> observers = new HashMap<>();

    public ResourceObserverManager(Resource observerdResource) {
        this.observedResource = observerdResource;
    }

    public final void setObserver(EntityRef clientEntityRef, Consumer<Resource> observer) {
        observers.put(clientEntityRef, observer);
    }

    public final void removeObserver(EntityRef clientEntityRef) {
        observers.remove(clientEntityRef);
    }

    public final void notifyChanged(EntityRef clientEntity) {
        Consumer<Resource> observer = observers.get(clientEntity);
        if (observer != null) {
            observer.accept(observedResource);
        }
    }

    public final void notifyChangedAll() {
        for (EntityRef client: observers.keySet()) {
            observers.get(client).accept(observedResource);
        }
    }
}
