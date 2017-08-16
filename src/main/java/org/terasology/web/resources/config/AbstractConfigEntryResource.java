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
import org.terasology.network.Client;
import org.terasology.registry.In;
import org.terasology.web.ServerAdminsManager;
import org.terasology.web.resources.ObservableReadableResource;
import org.terasology.web.resources.ResourceAccessException;
import org.terasology.web.resources.WritableResource;

public abstract class AbstractConfigEntryResource<T> extends ObservableReadableResource<T> implements WritableResource<T> {

    @In
    private Config config;

    @Override
    public T read(Client requestingClient) throws ResourceAccessException {
        return get(config);
    }

    @Override
    public void write(Client requestingClient, T data) throws ResourceAccessException {
        ServerAdminsManager.checkClientIsServerAdmin(requestingClient.getId());
        set(config, data);
        config.save();
        notifyChangedAll();
    }

    abstract void set(Config targetConfig, T value);
    abstract T get(Config sourceConfig);
}
