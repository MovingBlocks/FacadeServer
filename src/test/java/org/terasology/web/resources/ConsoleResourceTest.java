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

import org.junit.Test;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.MessageEvent;

import java.util.function.BiConsumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConsoleResourceTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testMessageNotification() {
        ConsoleResource consoleResource = new ConsoleResource(mock(Console.class));
        BiConsumer<EventEmittingResource<Message>, Message> observer = mock(BiConsumer.class);
        MessageEvent testEvent = mock(MessageEvent.class);
        when(testEvent.getFormattedMessage()).thenReturn(new Message("testMessage"));
        EntityRef client = mock(EntityRef.class);
        consoleResource.setObserver(client, observer);
        consoleResource.onMessage(testEvent, client);
        verify(observer).accept(consoleResource, testEvent.getFormattedMessage());
    }

    @Test
    public void testCommandExecution() {
        Console consoleMock = mock(Console.class);
        ConsoleResource consoleResource = new ConsoleResource(consoleMock);
        EntityRef client = mock(EntityRef.class);
        consoleResource.write(client, "testCommand testArg");
        verify(consoleMock).execute("testCommand testArg", client);
    }
}
