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

import org.terasology.engine.SimpleUri;
import org.terasology.naming.Name;

import java.util.List;

public class NewGameMetadata {

    private String gameName;
    private String seed;
    private List<Name> modules;
    private SimpleUri worldGenerator;

    public NewGameMetadata(String gameName, String seed, List<Name> modules, SimpleUri worldGenerator) {
        this.gameName = gameName;
        this.seed = seed;
        this.modules = modules;
        this.worldGenerator = worldGenerator;
    }

    public String getGameName() {
        return gameName;
    }

    public String getSeed() {
        return seed;
    }

    public List<Name> getModules() {
        return modules;
    }

    public SimpleUri getWorldGenerator() {
        return worldGenerator;
    }
}
