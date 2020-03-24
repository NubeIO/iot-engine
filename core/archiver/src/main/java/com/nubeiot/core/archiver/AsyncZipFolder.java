package com.nubeiot.core.archiver;

import java.io.File;

import com.nubeiot.core.utils.ExecutorHelpers;

import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;

@SuperBuilder
public final class AsyncZipFolder extends AbstractAsyncArchiver implements AsyncZip {

    @Override
    public void zip(@NonNull ZipArgument argument, @NonNull File destFolder, @NonNull File tobeZipped) {
        ExecutorHelpers.blocking(transporter().getVertx(), () -> execute(argument, destFolder, tobeZipped));
    }

    @Override
    protected ZipFile createZipFile(@NonNull ZipArgument argument, @NonNull File destination,
                                    @NonNull File originFile) {
        return new ZipFile(destination.toPath().resolve(computeZipName(argument, originFile)).toString(),
                           argument.toPassword());
    }

    @Override
    protected void run(@NonNull ZipArgument argument, ZipFile zipFile, @NonNull File destination,
                       @NonNull File originFile) throws ZipException {
        zipFile.addFolder(originFile, argument.zipParameters());
    }

    @Override
    protected String action() {
        return "Compressing";
    }

    @Override
    protected ZipOutput createOutput(@NonNull ZipArgument argument, @NonNull ZipFile zipFile,
                                     @NonNull ProgressMonitor progressMonitor, @NonNull File originFile) {
        return ZipOutput.builder()
                        .inputPath(originFile.getPath())
                        .outputPath(zipFile.getFile().toString())
                        .size(zipFile.getFile().length())
                        .lastModified(zipFile.getFile().lastModified())
                        .trackingInfo(argument.trackingInfo())
                        .build();
    }

}
