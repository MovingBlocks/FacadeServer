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

import org.terasology.engine.core.module.DependencyResolutionFailedException;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.download.MultiFileTransferProgressListener;
import org.terasology.engine.world.generator.internal.WorldGeneratorManager;
import org.terasology.module.Module;
import org.terasology.naming.Name;
import org.terasology.web.io.ActionResult;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.AbstractSimpleResource;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.resources.base.ResourcePath;
import org.terasology.web.serverAdminManagement.PermissionType;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;
import static org.terasology.web.resources.base.ResourceMethodFactory.createVoidParameterlessMethod;

public class ModuleInstallerResource extends AbstractSimpleResource {

    @In
    private ModuleManager moduleManager;
    @In
    private WorldGeneratorManager worldGeneratorManager;

    private ExecutorService installExecutor = Executors.newSingleThreadExecutor();
    private String status = "Ready";

    @Override
    protected ResourceMethod<Void, String> getGetMethod(ResourcePath path) throws ResourceAccessException {
        return createParameterlessMethod(path, ClientSecurityRequirements.PUBLIC, Void.class,
                (data, client) -> status);
    }

    @Override
    protected ResourceMethod<Name[], Void> getPutMethod(ResourcePath path) throws ResourceAccessException {
        return createVoidParameterlessMethod(path, ClientSecurityRequirements.requireAdminPermission(PermissionType.INSTALL_MODULES), Name[].class,
                (data, client) -> installModules(data));
    }

    private void installModules(Name[] moduleNames) throws ResourceAccessException {
        executeCallable(moduleManager.getInstallManager().updateRemoteRegistry());
        Set<Module> allModules;
        try {
            allModules = moduleManager.getInstallManager().getAllModulesToDownloadFor(moduleNames);
        } catch (DependencyResolutionFailedException ex) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR, ex.getMessage()));
        }
        installExecutor.submit(() -> setStatus("Initializing installation"));
        Future<List<Module>> installResult = installExecutor.submit(moduleManager.getInstallManager().createInstaller(allModules, new MultiFileTransferProgressListener() {
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
        // the executor is single-threaded and tasks run sequentially, so the following one sets the status message after the install finishes
        installExecutor.submit(() -> {
            String newStatus = "Ready. Last operation result: ";
            try {
                List<Module> installedModules = installResult.get();
                newStatus += "successfully installed " + installedModules.size() + " modules.";
                worldGeneratorManager.refresh();
            } catch (CancellationException | ExecutionException | InterruptedException ex) {
                newStatus += "installation failed for this reason: " + ex.getMessage();
            }
            setStatus(newStatus);
        });
    }

    private void setStatus(String value) {
        status = value;
        notifyChangedForAllClients();
    }

    private void executeCallable(Callable process) throws ResourceAccessException {
        try {
            process.call();
        } catch (Exception ex) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR, ex.getMessage()));
        }
    }
}
