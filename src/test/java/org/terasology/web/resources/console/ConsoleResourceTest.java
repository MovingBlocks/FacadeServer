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
package org.terasology.web.resources.console;

import org.junit.Test;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.MessageEvent;
import org.terasology.network.Client;
import org.terasology.registry.InjectionHelper;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceObserver;
import org.terasology.web.resources.base.ResourcePath;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConsoleResourceTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testMessageNotification() {
        ConsoleResource consoleResource = new ConsoleResource();
        ResourceObserver observer = mock(ResourceObserver.class);
        MessageEvent testEvent = mock(MessageEvent.class);
        when(testEvent.getFormattedMessage()).thenReturn(new Message("testMessage"));
        EntityRef clientEntity = mock(EntityRef.class);
        consoleResource.setObserver(observer);
        consoleResource.onMessage(testEvent, clientEntity);
        verify(observer).onEvent(ResourcePath.EMPTY, testEvent.getFormattedMessage(), clientEntity);
    }

    @Test
    public void testCommandExecution() throws ResourceAccessException {
        Console consoleMock = mock(Console.class);
        Context context = new ContextImpl();
        context.put(Console.class, consoleMock);
        ConsoleResource consoleResource = new ConsoleResource();
        InjectionHelper.inject(consoleResource, context);

        Client client = mock(Client.class);
        EntityRef clientEntityMock = mock(EntityRef.class);
        when(client.getEntity()).thenReturn(clientEntityMock);

        consoleResource.getPostMethod(ResourcePath.EMPTY).perform("testCommand testArg", null);
        verify(consoleMock).execute("testCommand testArg", clientEntityMock);
    }
}
