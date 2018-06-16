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
package org.terasology.web.resources.systemStatus;

import org.terasology.web.resources.base.AbstractSimpleResource;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.resources.base.ResourcePath;
import org.terasology.web.serverAdminManagement.PermissionType;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;

public class SystemResource extends AbstractSimpleResource {

    private final ScheduledExecutorService refreshSystemInfoService = Executors.newSingleThreadScheduledExecutor();

    public void startSystemInfoRefreshService() {
        refreshSystemInfoService.scheduleAtFixedRate(this::notifyChangedForAllClients, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    protected ResourceMethod<Void, SystemMetadata> getGetMethod(ResourcePath path) throws ResourceAccessException {
        return createParameterlessMethod(path, ClientSecurityRequirements.PUBLIC, PermissionType.NO_PERMISSION, Void.class,
                (data, client) -> SystemMetadata.getInstance());
    }

}
