/*
 * Copyright 2018 MovingBlocks
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

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.ConsoleColors;
import org.terasology.engine.logic.console.ConsoleMessageEvent;
import org.terasology.engine.logic.console.MessageEvent;
import org.terasology.engine.logic.console.commandSystem.ConsoleCommand;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.events.ConnectedEvent;
import org.terasology.engine.registry.In;
import org.terasology.naming.Name;
import org.terasology.nui.FontColor;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.DefaultComponentSystem;
import org.terasology.web.resources.base.*;
import org.terasology.web.serverAdminManagement.AdminPermissionManager;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.terasology.web.resources.base.ResourceMethodFactory.createVoidParameterlessMethod;
import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;

/**
 * This resource is used to access the in-game console and send commands to it.
 */
@RegisterSystem
public class ConsoleResource extends AbstractSimpleResource implements DefaultComponentSystem {

    @In
    private Console console;

    @ReceiveEvent
    public void onConnected(ConnectedEvent event, EntityRef entityRef) {
        AdminPermissionManager.getInstance().updateAdminConsolePermissions(event.getPlayerStore().getId(), entityRef);
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onMessage(MessageEvent event, EntityRef entityRef) {
        notifyEvent(entityRef, event.getFormattedMessage());
    }

    @Override
    protected ResourceMethod<Void, Collection<String>> getGetMethod(ResourcePath path) throws ResourceAccessException {
        return createParameterlessMethod(path, ClientSecurityRequirements.PUBLIC, Void.class, (data, client) ->
                getConsoleCommands());
    }

    @Override
    protected ResourceMethod<String, Void> getPostMethod(ResourcePath path) throws ResourceAccessException {
        // No permission because console permissions are handled separately.
        return createVoidParameterlessMethod(path, ClientSecurityRequirements.REQUIRE_AUTH, String.class, (data, client) -> {
            String command = !data.contains(" ") ? data : data.substring(0, data.indexOf(" "));
            if (command.equals("help")) {
                onMessage(new ConsoleMessageEvent(getHelpMessage()), client.getEntity());
            } else if (getConsoleCommands().contains(command)) {
                console.execute(data, client.getEntity());
            } else {
                throw new ResourceAccessException(new ActionResult(ActionResult.Status.BAD_REQUEST, "Invalid command"));
            }
        });
    }

    private Collection<String> getConsoleCommands() {
        return console.getCommands().stream().filter(ConsoleCommand::isRunOnServer).sorted().map(ConsoleCommand::getName)
                .map(Name::toString).collect(Collectors.toList());
    }

    private String getHelpMessage() {
        return "\n" + console.getCommands().stream().filter(ConsoleCommand::isRunOnServer).sorted().map(this::appendDescriptionToCommand).collect(Collectors.joining("\n"));
    }

    private String appendDescriptionToCommand(ConsoleCommand command) {
        return FontColor.getColored(command.getUsage(), ConsoleColors.COMMAND) + " - " + command.getDescription();
    }
}
