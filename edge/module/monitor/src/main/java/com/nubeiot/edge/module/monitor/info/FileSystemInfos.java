package com.nubeiot.edge.module.monitor.info;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

@Getter
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public final class FileSystemInfos implements JsonData {

    private final List<FileSystemInfo> fileSystemInfos;

    public static FileSystemInfos from(FileSystem fileSystem) {
        List<FileSystemInfo> fileSystemInfoList = new ArrayList<>();
        OSFileStore[] fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {
            fileSystemInfoList.add(FileSystemInfo.from(fs));
        }

        return FileSystemInfos.builder().fileSystemInfos(fileSystemInfoList).build();
    }

}
