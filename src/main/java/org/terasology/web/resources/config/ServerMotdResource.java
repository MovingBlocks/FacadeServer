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
package org.terasology.web.resources.config;

import org.terasology.config.Config;

public class ServerMotdResource extends AbstractConfigEntryResource<String> {

    @Override
    public String getName() {
        return "serverMotd";
    }

    @Override
    public Class<String> getDataType() {
        return String.class;
    }

    @Override
    void set(Config targetConfig, String value) {
        targetConfig.getNetwork().setServerMOTD(value);
    }

    @Override
    String get(Config sourceConfig) {
        return sourceConfig.getNetwork().getServerMOTD();
    }
}
