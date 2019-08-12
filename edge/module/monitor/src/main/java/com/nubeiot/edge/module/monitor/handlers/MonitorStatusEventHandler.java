package com.nubeiot.edge.module.monitor.handlers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OperatingSystem.ProcessSort;
import oshi.util.FormatUtil;

@NoArgsConstructor
public class MonitorStatusEventHandler implements EventListener {

    private static JsonObject getProcessor(CentralProcessor processor) {
        return new JsonObject().put("processor", processor.toString())
                               .put("physical_cpu_package_count", processor.getPhysicalPackageCount())
                               .put("physical_cpu_core_count", processor.getPhysicalProcessorCount())
                               .put("logical_cpu_count", processor.getLogicalProcessorCount())
                               .put("identifier", processor.getIdentifier())
                               .put("processor_id", processor.getProcessorID());
    }

    private static JsonObject getMemory(GlobalMemory memory) {
        return new JsonObject().put("total_memory", FormatUtil.formatBytes(memory.getTotal()))
                               .put("available_memory", FormatUtil.formatBytes(memory.getAvailable()))
                               .put("total_swap", FormatUtil.formatBytes(memory.getSwapTotal()))
                               .put("swap_used", FormatUtil.formatBytes(memory.getSwapUsed()));
    }

    private static JsonObject getCpu(CentralProcessor processor) {
        long freq = processor.getVendorFreq();
        if (freq > 0) {
            System.out.println("Vendor Frequency: " + FormatUtil.formatHertz(freq));
        }
        if (freq > 0) {
            System.out.println("Max Frequency: " + FormatUtil.formatHertz(freq));
        }
        return new JsonObject().put("uptime", FormatUtil.formatElapsedSecs(processor.getSystemUptime()))
                               .put("cup_load", String.format("%.1f%% (OS MXBean)", processor.getSystemCpuLoad() * 100))
                               .put("frequency", FormatUtil.formatHertz(freq));
    }

    private static JsonObject getProcesses(OperatingSystem os, GlobalMemory memory) {
        // Sort by highest CPU
        List<OSProcess> processes = Arrays.asList(os.getProcesses(10, ProcessSort.CPU));
        JsonArray processesList = new JsonArray();
        for (int i = 0; i < processes.size() && i < 5; i++) {
            OSProcess p = processes.get(i);
            processesList.add(new JsonObject().put("pid", p.getProcessID())
                                              .put("%cpu", 100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime())
                                              .put("%mem", 100d * p.getResidentSetSize() / memory.getTotal())
                                              .put("virtual_size", FormatUtil.formatBytes(p.getVirtualSize()))
                                              .put("resident_set_size", FormatUtil.formatBytes(p.getResidentSetSize()))
                                              .put("name", p.getName()));
        }
        return new JsonObject().put("process_count", os.getProcessCount())
                               .put("thread", os.getThreadCount())
                               .put("processes", processesList);
    }

    private static JsonArray getFileSystem(FileSystem fileSystem) {
        OSFileStore[] fsArray = fileSystem.getFileStores();
        JsonArray fsList = new JsonArray();
        for (OSFileStore fs : fsArray) {
            long usable = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            fsList.add(new JsonObject().put("name", fs.getName())
                                       .put("description",
                                            fs.getDescription().isEmpty() ? "file system" : fs.getDescription())
                                       .put("type", fs.getType())
                                       .put("usable", FormatUtil.formatBytes(usable))
                                       .put("total_space", FormatUtil.formatBytes(total))
                                       .put("%free", String.format("%.1f%%", 100d * usable / total))
                                       .put("mount", fs.getMount()));
        }

        return fsList;
    }

    @EventContractor(action = EventAction.GET_LIST)
    public JsonObject getList(RequestData data) {
        JsonObject jsonObject = new JsonObject();

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        jsonObject.put("os", os.toString());
        jsonObject.put("processor", getProcessor(hal.getProcessor()));
        jsonObject.put("memory", getMemory(hal.getMemory()));
        jsonObject.put("cpu_load", getCpu(hal.getProcessor()));
        jsonObject.put("processes", getProcesses(os, hal.getMemory()));
        jsonObject.put("file_system", getFileSystem(os.getFileSystem()));

        return jsonObject;
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.GET_LIST);
    }

}
