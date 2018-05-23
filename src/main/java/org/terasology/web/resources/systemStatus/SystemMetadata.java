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
import oshi.hardware.HardwareAbstractionLayer;

import java.lang.management.*;


/**
 * This class contains the data for system resources
 */

public final class SystemMetadata {

    private static SystemMetadata instance;
    private static HardwareAbstractionLayer hardware;
    private static RuntimeMXBean runtimeBean;
    private static MemoryMXBean memoryBean;

    private double cpuUsage;
    private double memoryUsagePercentage;
    private long memoryUsed;
    private long memoryTotal;
    private long memoryAvailable;
    private long serverUptime;
    private long jvmMemoryUsed;
    private long jvmMemoryTotal;

    private SystemMetadata() {
        hardware = new SystemInfo().getHardware();
        runtimeBean = ManagementFactory.getRuntimeMXBean();
        memoryBean = ManagementFactory.getMemoryMXBean();
    }

    public static SystemMetadata getInstance() {
        if (instance == null) {
            instance = new SystemMetadata();
        }
        instance.refresh();
        return instance;
    }

    private void refresh() {
        //Runtime
        cpuUsage = hardware.getProcessor().getSystemCpuLoad() * 100;
        memoryAvailable = hardware.getMemory().getAvailable();
        memoryTotal = hardware.getMemory().getTotal();
        memoryUsed = memoryTotal - memoryAvailable;
        memoryUsagePercentage = ((double) memoryUsed / memoryTotal) * 100;
        // system uptime in milliseconds
        serverUptime = runtimeBean.getUptime();
        jvmMemoryUsed = Runtime.getRuntime().totalMemory();
        jvmMemoryTotal = Runtime.getRuntime().maxMemory();
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

    public long getServerUptime() {
        return serverUptime;
    }
}
