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
package org.terasology.web.io;

import com.google.gson.JsonElement;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.web.resources.EventEmittingResource;
import org.terasology.web.resources.EventEmittingResourceObserver;
import org.terasology.web.resources.ObservableReadableResource;
import org.terasology.web.resources.ReadableResourceObserver;

import java.util.function.BiConsumer;

class JsonSessionResourceObserver implements ReadableResourceObserver, EventEmittingResourceObserver {

    private JsonSession session;
    private BiConsumer<EntityRef, JsonElement> readableResourceObserver;
    private BiConsumer<EntityRef, JsonElement> eventResourceObserver;

    JsonSessionResourceObserver(JsonSession session) {
        this.session = session;
    }

    void setReadableResourceObserver(BiConsumer<EntityRef, JsonElement> readableResourceObserver) {
        this.readableResourceObserver = readableResourceObserver;
    }

    void setEventResourceObserver(BiConsumer<EntityRef, JsonElement> eventResourceObserver) {
        this.eventResourceObserver = eventResourceObserver;
    }

    //for readable resource updates
    @Override
    public void update(EntityRef client, ObservableReadableResource resource) {
        if (readableResourceObserver != null) {
            readableResourceObserver.accept(client, session.readResource(resource));
        }
    }

    //for events
    @Override
    public void update(EntityRef client, EventEmittingResource resource, Object eventData) {
        if (eventResourceObserver != null) {
            eventResourceObserver.accept(client, session.serializeEvent(resource, eventData));
        }
    }
}
