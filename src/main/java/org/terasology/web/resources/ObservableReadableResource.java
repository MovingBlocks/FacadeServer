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
package org.terasology.web.resources;

import org.terasology.entitySystem.entity.EntityRef;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class ObservableReadableResource<T> implements ReadableResource<T> {

    private Set<Consumer<EntityRef>> observers = new HashSet<>();

    public final void addObserver(Consumer<EntityRef> observer) {
        observers.add(observer);
    }

    public final void removeObserver(Consumer<EntityRef> observer) {
        observers.remove(observer);
    }

    protected final void notifyChanged(EntityRef clientEntity) {
        for (Consumer<EntityRef> observer: observers) {
            observer.accept(clientEntity);
        }
    }
}
