package com.nubeiot.edge.module.monitor;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.edge.module.monitor.info.CpuInfo;
import com.nubeiot.edge.module.monitor.info.FileSystemInfo;
import com.nubeiot.edge.module.monitor.info.MemoryInfo;
import com.nubeiot.edge.module.monitor.info.ProcessesInfo;
import com.nubeiot.edge.module.monitor.info.ProcessorInfo;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

@Getter
@RequiredArgsConstructor
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MonitorStatus implements JsonData {

    final String os;
    final ProcessorInfo processor;
    final MemoryInfo memory;
    final CpuInfo cpu;
    final ProcessesInfo processes;
    final List<FileSystemInfo> fileSystems;

    public static MonitorStatus from(SystemInfo si) {
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        return MonitorStatus.builder()
                            .os(os.toString())
                            .processor(ProcessorInfo.from(hal.getProcessor()))
                            .memory(MemoryInfo.from(hal.getMemory()))
                            .cpu(CpuInfo.from(hal.getProcessor()))
                            .processes(ProcessesInfo.from(os, hal.getMemory()))
                            .fileSystems(getFileSystem(os.getFileSystem()))
                            .build();
    }

    private static List<FileSystemInfo> getFileSystem(FileSystem fileSystem) {
        List<FileSystemInfo> fileSystemInfoList = new ArrayList<>();
        OSFileStore[] fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {
            fileSystemInfoList.add(FileSystemInfo.from(fs));
        }

        return fileSystemInfoList;
    }

}
