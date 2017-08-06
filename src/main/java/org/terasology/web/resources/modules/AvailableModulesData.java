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
package org.terasology.web.resources.modules;

import org.terasology.module.ModuleMetadata;
import org.terasology.world.generator.internal.WorldGeneratorInfo;

import java.util.List;

public class AvailableModulesData {

    private List<ModuleMetadata> modules;
    private List<WorldGeneratorInfo> worldGenerators;

    public AvailableModulesData(List<ModuleMetadata> modules, List<WorldGeneratorInfo> worldGenerators) {
        this.modules = modules;
        this.worldGenerators = worldGenerators;
    }

    public List<ModuleMetadata> getModules() {
        return modules;
    }

    public List<WorldGeneratorInfo> getWorldGenerators() {
        return worldGenerators;
    }
}
