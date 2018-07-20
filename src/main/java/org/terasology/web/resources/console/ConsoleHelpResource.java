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

import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleColors;
import org.terasology.logic.console.commandSystem.ConsoleCommand;
import org.terasology.registry.In;
import org.terasology.rendering.FontColor;
import org.terasology.web.resources.base.AbstractSimpleResource;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.resources.base.ResourcePath;

import java.util.stream.Collectors;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;

/**
 * This resource is used to get the help text for the web interface frontend. It only shows commands that can run on the server.
 */
public class ConsoleHelpResource extends AbstractSimpleResource {

    @In
    private Console console;

    @Override
    protected ResourceMethod<Void, String> getGetMethod(ResourcePath path) throws ResourceAccessException {
        return createParameterlessMethod(path, ClientSecurityRequirements.PUBLIC, Void.class, (data, client) ->
                "\n" + console.getCommands().stream().filter(ConsoleCommand::isRunOnServer).sorted().map(this::appendDescriptionToCommand).collect(Collectors.joining("\n")));
    }

    private String appendDescriptionToCommand(ConsoleCommand command) {
        return FontColor.getColored(command.getUsage(), ConsoleColors.COMMAND) + " - " + command.getDescription();
    }

}
