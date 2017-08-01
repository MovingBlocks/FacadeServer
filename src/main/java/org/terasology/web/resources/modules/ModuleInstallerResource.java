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
import org.terasology.module.Module;
import org.terasology.naming.Name;
import org.terasology.network.Client;
import org.terasology.registry.In;
import org.terasology.utilities.download.MultiFileTransferProgressListener;
import org.terasology.web.ServerAdminsManager;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.ObservableReadableResource;
import org.terasology.web.resources.ResourceAccessException;
import org.terasology.web.resources.WritableResource;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModuleInstallerResource extends ObservableReadableResource<String> implements WritableResource<Name[]> {

    @In
    private ModuleManager moduleManager;

    private ExecutorService installExecutor = Executors.newSingleThreadExecutor();
    private String status = "Idle";

    @Override
    public String getName() {
        return "moduleInstaller";
    }

    @Override
    public Class<Name[]> getDataType() {
        return Name[].class;
    }

    @Override
    public String read(Client requestingClient) throws ResourceAccessException {
        return status;
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
        installExecutor.submit(() -> setStatus("Initializing installation"));
        installExecutor.submit(moduleManager.getInstallManager().createInstaller(allModules, new MultiFileTransferProgressListener() {
            @Override
            public void onSizeMetadataProgress(int index, int totalUrls) {
                setStatus(String.format("Retrieving file size information - %d of %d", index, totalUrls));
            }
            @Override
            public void onDownloadProgress(long totalTransferredBytes, long totalBytes, int completedFiles, int nFiles) {
                int globalPercentage = (int) (totalTransferredBytes * 100f / totalBytes);
                setStatus(String.format("Downloaded modules: %d of %d\nDownloaded bytes: %d of %d\nGlobal progress: %d%%",
                        completedFiles, nFiles, totalTransferredBytes, totalBytes, globalPercentage));
            }
        }));
        // the executor is single-threaded and tasks run sequentially, so the following one sets the status back to "Idle" after the install finishes
        installExecutor.submit(() -> setStatus("Idle"));
    }

    private void setStatus(String value) {
        status = value;
        notifyChangedAll();
    }

    private void executeCallable(Callable process) throws ResourceAccessException {
        try {
            process.call();
        } catch (Exception ex) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR, ex.getMessage()));
        }
    }
}
