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
import org.terasology.engine.paths.PathManager;
import org.terasology.web.resources.ResourceAccessException;

public abstract class AbstractAction {

    private transient PathManager pathManager;

    public AbstractAction() {
        pathManager = PathManager.getInstance();
    }

    public abstract void perform(ModuleManager moduleManager) throws ResourceAccessException;

    // for use by subclasses
    public PathManager getPathManager() {
        return pathManager;
    }

    //for unit testing
    void setPathManager(PathManager pathManager) {
        this.pathManager = pathManager;
    }
}
