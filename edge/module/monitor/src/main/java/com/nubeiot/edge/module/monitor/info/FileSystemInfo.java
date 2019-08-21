package com.nubeiot.edge.module.monitor.info;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;

@Getter
@RequiredArgsConstructor
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonDeserialize(builder = FileSystemInfo.Builder.class)
public class FileSystemInfo implements JsonData {

    final String name;
    final String description;
    final String type;
    final String usable;
    final String totalSpace;
    final String freePercent;
    final String mount;

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
