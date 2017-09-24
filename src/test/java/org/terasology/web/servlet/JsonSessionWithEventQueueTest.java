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
import com.google.gson.JsonPrimitive;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.terasology.web.io.JsonSession;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JsonSessionWithEventQueueTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testEventQueue() {
        JsonSession jsonSessionMock = mock(JsonSession.class);
        JsonSessionWithEventQueue eventSession = new JsonSessionWithEventQueue(jsonSessionMock);
        ArgumentCaptor<BiConsumer<Collection<String>, JsonElement>> observerArgument = ArgumentCaptor.forClass(BiConsumer.class);
        verify(jsonSessionMock).setResourceEventListener(observerArgument.capture());
        assertTrue(eventSession.drainEventQueue().isEmpty());
        observerArgument.getValue().accept(Arrays.asList("parent1", "resource1"), new JsonPrimitive("testEventData1"));
        observerArgument.getValue().accept(Arrays.asList("parent2", "resource2"), new JsonPrimitive("testEventData2"));
        List<JsonSessionWithEventQueue.ResourceEvent> returnedEventList = eventSession.drainEventQueue();
        assertEquals(2, returnedEventList.size());
        assertEventEquals(new JsonSessionWithEventQueue.ResourceEvent(Arrays.asList("parent1", "resource1"), new JsonPrimitive("testEventData1")), returnedEventList.get(0));
        assertEventEquals(new JsonSessionWithEventQueue.ResourceEvent(Arrays.asList("parent2", "resource2"), new JsonPrimitive("testEventData2")), returnedEventList.get(1));
    }

    private void assertEventEquals(JsonSessionWithEventQueue.ResourceEvent expected, JsonSessionWithEventQueue.ResourceEvent actual) {
        assertEquals(expected.getResourcePath(), actual.getResourcePath());
        assertEquals(expected.getEventData(), actual.getEventData());
    }
}
