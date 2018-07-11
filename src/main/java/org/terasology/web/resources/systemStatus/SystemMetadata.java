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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;


/**
 * This class contains the data for system resources.
 */

public final class SystemMetadata {

    private static SystemMetadata instance;
    private static HardwareAbstractionLayer hardware = new SystemInfo().getHardware();
    private static RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

    private double cpuUsage;
    private long memoryUsed;
    private long memoryMax;
    private long serverUptime;
    private long jvmMemoryUsed;
    private long jvmMemoryMax;

    private SystemMetadata() {
    }

    public static SystemMetadata getInstance() {
        if (instance == null) {
            instance = new SystemMetadata();
        }
        instance.refresh();
        return instance;
    }

    private void refresh() {
        cpuUsage = hardware.getProcessor().getSystemCpuLoad() * 100;
        memoryMax = hardware.getMemory().getTotal();
        memoryUsed = memoryMax - hardware.getMemory().getAvailable();
        // system uptime in milliseconds
        serverUptime = runtimeBean.getUptime();
        jvmMemoryUsed = Runtime.getRuntime().totalMemory();
        jvmMemoryMax = Runtime.getRuntime().maxMemory();
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public long getMemoryMax() {
        return memoryMax;
    }

    public long getServerUptime() {
        return serverUptime;
    }

    public long getJvmMemoryUsed() {
        return jvmMemoryUsed;
    }

    public long getJvmMemoryMax() {
        return jvmMemoryMax;
    }

}
