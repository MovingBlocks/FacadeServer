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

import org.terasology.engine.module.DependencyResolutionFailedException;
import org.terasology.engine.module.ModuleManager;
import org.terasology.i18n.I18nMap;
import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;
import org.terasology.naming.Name;
import org.terasology.network.Client;
import org.terasology.registry.In;
import org.terasology.utilities.download.MultiFileTransferProgressListener;
import org.terasology.web.ServerAdminsManager;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.ReadableResource;
import org.terasology.web.resources.ResourceAccessException;
import org.terasology.web.resources.WritableResource;
import org.terasology.world.generator.internal.WorldGeneratorManager;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AvailableModulesResource implements ReadableResource<AvailableModulesData>, WritableResource<Name[]> {

    @In
    private ModuleManager moduleManager;

    @In
    private WorldGeneratorManager worldGeneratorManager;

    @Override
    public String getName() {
        return "availableModules";
    }

    @Override
    public AvailableModulesData read(Client requestingClient) throws ResourceAccessException {
        Stream<ModuleMetadata> modules = moduleManager.getRegistry().stream().map(Module::getMetadata)
                .sorted(Comparator.comparing(ModuleMetadata::getDisplayName, Comparator.comparing(I18nMap::value)));
        return new AvailableModulesData(modules.collect(Collectors.toList()), worldGeneratorManager.getWorldGenerators());
    }

    @Override
    public Class<Name[]> getDataType() {
        return Name[].class;
    }

    @Override
    public void write(Client requestingClient, Name[] data) throws ResourceAccessException {
        ServerAdminsManager.checkClientIsServerAdmin(requestingClient.getId());
        executeCallable(moduleManager.getInstallManager().updateRemoteRegistry());
        Set<Module> allModules;
        try {
            allModules = moduleManager.getInstallManager().getAllModulesToDownloadFor(data);
        } catch (DependencyResolutionFailedException ex) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR, ex.getMessage()));
        }
        executeCallable(moduleManager.getInstallManager().createInstaller(allModules, new MultiFileTransferProgressListener() {
            @Override
            public void onSizeMetadataProgress(int index, int totalUrls) {
            }
            @Override
            public void onDownloadProgress(long totalTransferredBytes, long totalBytes, int completedFiles, int nFiles) {
            }
        }));
    }

    private void executeCallable(Callable process) throws ResourceAccessException {
        try {
            process.call();
        } catch (Exception ex) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR, ex.getMessage()));
        }
    }
}
