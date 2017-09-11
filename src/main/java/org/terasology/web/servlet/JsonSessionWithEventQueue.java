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
package org.terasology.web.servlet;

import com.google.gson.JsonElement;
import org.terasology.web.io.JsonSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class JsonSessionWithEventQueue {

    private final JsonSession session;
    private final Queue<ResourceEvent> eventQueue = new LinkedList<>();

    public JsonSessionWithEventQueue(JsonSession session) {
        this.session = session;
        session.setResourceEventListener((resourcePath, eventData) -> {
                synchronized (eventQueue) {
                    eventQueue.offer(new ResourceEvent(resourcePath, eventData));
                }
        });
    }

    public JsonSessionWithEventQueue() {
        this(new JsonSession());
    }

    public List<ResourceEvent> drainEventQueue() {
        List<ResourceEvent> result = new ArrayList<>();
        synchronized (eventQueue) {
            ResourceEvent item;
            while ((item = eventQueue.poll()) != null) {
                result.add(item);
            }
        }
        return result;
    }

    public JsonSession getSession() {
        return session;
    }

    public static final class ResourceEvent {
        private Collection<String> resourcePath;
        private JsonElement eventData;

        ResourceEvent(Collection<String> resourcePath, JsonElement eventData) {
            this.resourcePath = resourcePath;
            this.eventData = eventData;
        }

        public Collection<String> getResourcePath() {
            return resourcePath;
        }

        public JsonElement getEventData() {
            return eventData;
        }
    }
}
