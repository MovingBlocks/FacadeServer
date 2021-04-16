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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.entitySystem.entity.EntityRef;

import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleImpl;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.MessageEvent;
import org.terasology.logic.console.commandSystem.MethodCommand;
import org.terasology.logic.console.commands.ServerCommands;
import org.terasology.engine.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.InjectionHelper;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceObserver;
import org.terasology.web.resources.base.ResourcePath;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConsoleResourceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Console consoleMock;
    private Context context;
    private Client client;
    private EntityRef clientEntityMock;
    private ConsoleResource consoleResource;

    @Before
    public void setupConsole() {
        consoleMock = mock(Console.class);
        context = new ContextImpl();
        context.put(Console.class, consoleMock);
        consoleResource = new ConsoleResource();
        InjectionHelper.inject(consoleResource, context);

        client = mock(Client.class);
        clientEntityMock = mock(EntityRef.class);
        when(client.getEntity()).thenReturn(clientEntityMock);
    }

    @Test
    public void testGetConsoleCommands() throws ResourceAccessException {
        assertNotNull(consoleResource.getGetMethod(ResourcePath.createEmpty()).perform(null, client));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMessageNotification() {
        ResourceObserver observer = mock(ResourceObserver.class);
        MessageEvent testEvent = mock(MessageEvent.class);
        Message message = new Message("testMessage");
        when(testEvent.getFormattedMessage()).thenReturn(message);
        EntityRef clientEntity = mock(EntityRef.class);
        consoleResource.setObserver(observer);
        consoleResource.onMessage(testEvent, clientEntity);
        verify(observer).onEvent(argThat(ResourcePath::isEmpty), eq(message), eq(clientEntity));
    }

    @Test
    public void testNonexistantCommandExecutionThrowsException() throws ResourceAccessException {
        expectedException.expect(ResourceAccessException.class);
        consoleResource.getPostMethod(ResourcePath.createEmpty()).perform("testCommand testArg", client);
    }

    @Test
    public void testCommandExecution() throws ResourceAccessException {
        context = new ContextImpl();
        consoleResource = new ConsoleResource();
        NetworkSystem networkSystemMock = mock(NetworkSystem.class);
        when(networkSystemMock.getMode()).thenReturn(NetworkMode.NONE);
        context.put(NetworkSystem.class, networkSystemMock);
        Console consoleWithCommandsSpy = spy(new ConsoleImpl(context));
        context.put(Console.class, consoleWithCommandsSpy);
        InjectionHelper.inject(consoleResource, context);

        client = mock(Client.class);
        clientEntityMock = mock(EntityRef.class);
        when(clientEntityMock.getComponent(ClientComponent.class)).thenReturn(new ClientComponent());
        when(client.getEntity()).thenReturn(clientEntityMock);

        ServerCommands serverCommands = new ServerCommands();
        MethodCommand.registerAvailable(serverCommands, consoleWithCommandsSpy, context);

        consoleResource.getPostMethod(ResourcePath.createEmpty()).perform("save", client);
        verify(consoleWithCommandsSpy).execute("save", clientEntityMock);
    }

    @Test
    public void testHelpCommand() throws ResourceAccessException {
        ResourceObserver resourceObserverMock = mock(ResourceObserver.class);
        consoleResource.setObserver(resourceObserverMock);
        consoleResource.getPostMethod(ResourcePath.createEmpty()).perform("help", client);
        verify(consoleMock, never()).execute(anyString(), eq(clientEntityMock));
    }
}
