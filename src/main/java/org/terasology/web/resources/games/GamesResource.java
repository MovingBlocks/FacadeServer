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
package org.terasology.web.resources.games;

import org.terasology.network.Client;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.web.resources.ReadableResource;
import org.terasology.web.resources.ResourceAccessException;
import org.terasology.web.resources.WritableResource;

import java.util.List;

public class GamesResource implements ReadableResource<List<GameInfo>>, WritableResource<Action> {

    @Override
    public String getName() {
        return "games";
    }

    @Override
    public List<GameInfo> read(Client requestingClient) {
        return GameProvider.getSavedGames();
    }

    @Override
    public Class<Action> getDataType() {
        return Action.class;
    }

    @Override
    public void write(Client requestingClient, Action data) throws ResourceAccessException {
        // STOPSHIP: 13/07/2017 TODO: check for permissions!
        data.perform();
        //TODO: throw exception on failure, or refactor perform() to return void and throw ResourceAccessException(s)
    }

}
