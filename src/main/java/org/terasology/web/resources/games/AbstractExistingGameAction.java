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

import org.terasology.engine.module.ModuleManager;
import org.terasology.web.EngineRunner;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.ResourceAccessException;

import static org.terasology.web.resources.InputCheckUtils.checkNotNullOrEmpty;

public abstract class AbstractExistingGameAction extends AbstractAction {

    private String gameName;

    @Override
    public final void perform(ModuleManager moduleManager) throws ResourceAccessException {
        checkNotNullOrEmpty(gameName, "A game name must be specified.");
        checkGameIsNotRunningOrLoading();
        perform(gameName);
    }

    private void checkGameIsNotRunningOrLoading() throws ResourceAccessException {
        if (gameName.equals(EngineRunner.getInstance().getRunningOrLoadingGameName())) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR,
                    "This action cannot be performed on a game which is running or loading."));
        }
    }

    protected abstract void perform(String savegameName) throws ResourceAccessException;
}
