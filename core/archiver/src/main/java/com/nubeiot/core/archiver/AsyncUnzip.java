package com.nubeiot.core.archiver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public interface AsyncUnzip extends AsyncArchiver {

    default void extract(@NonNull ZipArgument argument, @NonNull String destFolder, @NonNull String zipFile) {
        extract(argument, Paths.get(destFolder), Paths.get(zipFile));
    }

    default void extract(@NonNull ZipArgument argument, @NonNull Path destFolder, @NonNull Path zipFile) {
        extract(argument, destFolder.toFile(), zipFile.toFile());
    }

    void extract(@NonNull ZipArgument argument, @NonNull File destFolder, @NonNull File zipFile);

    default String computeExtractedFolder(@NonNull ZipArgument argument, @NonNull File zippedFile) {
        if (Strings.isNotBlank(argument.overriddenDestFileName())) {
            return argument.overriddenDestFileName();
        }
        return FileUtils.withoutExtension(zippedFile.toPath().getFileName().toString());
    }

}
