package com.nubeiot.core.archiver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * Represents Async zip.
 *
 * @since 1.0.0
 */
public interface AsyncZip extends AsyncArchiver {

    /**
     * Do zip folder.
     *
     * @param argument   the argument
     * @param destFolder the dest folder
     * @param tobeZipped the tobe zipped
     * @see ZipArgument
     * @since 1.0.0
     */
    default void zip(@NonNull ZipArgument argument, @NonNull String destFolder, @NonNull String tobeZipped) {
        zip(argument, Paths.get(destFolder), Paths.get(tobeZipped));
    }

    /**
     * Do zip folder.
     *
     * @param argument   the argument
     * @param destFolder the dest folder
     * @param tobeZipped the tobe zipped
     * @see ZipArgument
     * @since 1.0.0
     */
    default void zip(@NonNull ZipArgument argument, @NonNull Path destFolder, @NonNull Path tobeZipped) {
        zip(argument, destFolder.toFile(), tobeZipped.toFile());
    }

    /**
     * Do zip folder.
     *
     * @param argument   the argument
     * @param destFolder the dest folder
     * @param tobeZipped the tobe zipped
     * @see ZipArgument
     * @since 1.0.0
     */
    void zip(@NonNull ZipArgument argument, @NonNull File destFolder, @NonNull File tobeZipped);

    default String computeZipName(@NonNull ZipArgument argument, @NonNull File toZippedFile) {
        if (Strings.isNotBlank(argument.overriddenDestFileName())) {
            return AsyncArchiver.ext(argument.overriddenDestFileName());
        }
        final String fileName = toZippedFile.toPath().getFileName().toString();
        return AsyncArchiver.ext(fileName + (argument.appendTimestamp() ? "-" + DateTimes.nowMilli() : ""));
    }

}
