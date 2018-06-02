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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.MessageEvent;
import org.terasology.logic.console.commandSystem.ConsoleCommand;
import org.terasology.naming.Name;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.web.resources.DefaultComponentSystem;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.AbstractSimpleResource;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.resources.base.ResourcePath;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.terasology.web.resources.base.ResourceMethodFactory.createVoidParameterlessMethod;
import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;

@RegisterSystem
public class ConsoleResource extends AbstractSimpleResource implements DefaultComponentSystem {

    @In
    private Console console;

    @ReceiveEvent(components = ClientComponent.class)
    public void onMessage(MessageEvent event, EntityRef entityRef) {
        notifyEvent(entityRef, event.getFormattedMessage());
    }

    @Override
    protected ResourceMethod<Void, Collection<String>> getGetMethod(ResourcePath path) throws ResourceAccessException {
        return createParameterlessMethod(path, ClientSecurityRequirements.PUBLIC, Void.class, (data, client) ->
            console.getCommands().stream().filter(ConsoleCommand::isRunOnServer).map(ConsoleCommand::getName)
                .map(Name::toString).collect(Collectors.toList()));
    }

    @Override
    protected ResourceMethod<String, Void> getPostMethod(ResourcePath path) throws ResourceAccessException {
        return createVoidParameterlessMethod(path, ClientSecurityRequirements.REQUIRE_AUTH, String.class,
                (data, client) -> console.execute(data, client.getEntity()));
    }
}
