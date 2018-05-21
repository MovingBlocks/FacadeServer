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

import oshi.SystemInfo;

public final class SystemMetadata {

    private double cpuUsage;
    private double memoryUsagePercentage;
    private long memoryUsed;
    private long memoryTotal;
    private long memoryAvailable;
    private long systemUptime;

    public static SystemMetadata build() {
        SystemMetadata systemMetadata = new SystemMetadata();
        systemMetadata.getStatus();
        return systemMetadata;
    }

    private void getStatus() {
        SystemInfo systemInfo = new SystemInfo();
        cpuUsage = systemInfo.getHardware().getProcessor().getSystemCpuLoad() * 100;
        memoryAvailable = systemInfo.getHardware().getMemory().getAvailable();
        memoryTotal = systemInfo.getHardware().getMemory().getTotal();
        memoryUsed = memoryTotal - memoryAvailable;
        memoryUsagePercentage = ((double) memoryUsed / memoryTotal) * 100;
        // system uptime in seconds
        systemUptime = systemInfo.getHardware().getProcessor().getSystemUptime();
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public double getMemoryUsagePercentage() {
        return memoryUsagePercentage;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public long getMemoryTotal() {
        return memoryTotal;
    }

    public long getMemoryAvailable() {
        return memoryAvailable;
    }

    public long getSystemUptime() {
        return systemUptime;
    }
}
