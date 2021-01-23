package com.nubeiot.edge.module.monitor.info;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;

@Getter
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public final class FileSystemInfo implements JsonData {

    private final String name;
    private final String description;
    private final String type;
    private final String usable;
    private final String totalSpace;
    private final String freePercent;
    private final String mount;

    public static FileSystemInfo from(OSFileStore fs) {
        long usable = fs.getUsableSpace();
        long total = fs.getTotalSpace();

        return FileSystemInfo.builder()
                             .name(fs.getName())
                             .description(fs.getDescription().isEmpty() ? "file system" : fs.getDescription())
                             .type(fs.getType())
                             .usable(FormatUtil.formatBytes(usable))
                             .totalSpace(FormatUtil.formatBytes(total))
                             .freePercent(String.format("%.1f%%", 100d * usable / total))
                             .mount(fs.getMount())
                             .build();
    }

}
