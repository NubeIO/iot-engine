package com.nubeiot.edge.module.monitor.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OperatingSystem.ProcessSort;
import oshi.util.FormatUtil;

@Getter
@RequiredArgsConstructor
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProcessesUsage implements JsonData {

    final int processCount;
    final int thread;
    final List<ProcessUsage> processes;

    public static ProcessesUsage from(OperatingSystem os, GlobalMemory memory) {
        // Sort by highest CPU
        List<OSProcess> processesList = Arrays.asList(os.getProcesses(10, ProcessSort.CPU));
        List<ProcessUsage> processes = new ArrayList<>();
        for (int i = 0; i < processesList.size() && i < 5; i++) {
            OSProcess p = processesList.get(i);
            processes.add(ProcessUsage.builder()
                                      .pid(p.getProcessID())
                                      .cpuPercent(100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime())
                                      .memPercent(100d * p.getResidentSetSize() / memory.getTotal())
                                      .virtualSize(FormatUtil.formatBytes(p.getVirtualSize()))
                                      .residentSetSize(FormatUtil.formatBytes(p.getResidentSetSize()))
                                      .name(p.getName())
                                      .build());
        }

        return ProcessesUsage.builder()
                             .processCount(os.getProcessCount())
                             .thread(os.getThreadCount())
                             .processes(processes)
                             .build();
    }

    @Getter
    @RequiredArgsConstructor
    @lombok.Builder(builderClassName = "Builder")
    @JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
    static class ProcessUsage implements JsonData {

        final int pid;
        final double cpuPercent;
        final double memPercent;
        final String virtualSize;
        final String residentSetSize;
        final String name;

    }

}
