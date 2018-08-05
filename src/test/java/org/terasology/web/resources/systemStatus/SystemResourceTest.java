/*
 * Copyright 2018 MovingBlocks
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

import org.junit.Before;
import org.junit.Test;
import org.terasology.web.resources.base.ResourceObserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

public class SystemResourceTest {

    private SystemResource systemResource;
    private SystemMetadata systemMetadata;

    @Before
    public void setup() {
        systemResource = new SystemResource();
        systemMetadata = SystemMetadata.getInstance();
    }

    @Test
    public void testSystemInfoRefreshService() {
        ResourceObserver resourceObserverMock = mock(ResourceObserver.class);
        systemResource.setObserver(resourceObserverMock);
        systemResource.startSystemInfoRefreshService();
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(resourceObserverMock).onChangedForAllClients(any(), any());
    }

    @Test
    public void testSystemValuesInAcceptableRange() {
        assertTrue(systemMetadata.getCpuUsage() >= 0);
        assertTrue(systemMetadata.getCpuUsage() <= 100);
        assertTrue(systemMetadata.getServerUptime() > 0);
        assertTrue(systemMetadata.getJvmMemoryMax() > 0);
        assertTrue(systemMetadata.getJvmMemoryUsed() > 0);
        assertTrue(systemMetadata.getMemoryMax() > 0);
        assertTrue(systemMetadata.getMemoryUsed() > 0);
        assertTrue(systemMetadata.getJvmMemoryMax() >= systemMetadata.getJvmMemoryUsed());
        assertTrue(systemMetadata.getMemoryMax() >= systemMetadata.getMemoryUsed());
    }
}
