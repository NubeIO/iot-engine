package com.nubeiot.core.archiver;

import java.io.File;
import java.nio.file.Paths;

import com.nubeiot.core.utils.ExecutorHelpers;

import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;

@SuperBuilder
public final class DefaultAsyncUnzip extends AbstractAsyncArchiver implements AsyncUnzip {

    @Override
    public void extract(@NonNull ZipArgument argument, @NonNull File destFolder, @NonNull File zipFile) {
        ExecutorHelpers.blocking(transporter().getVertx(), () -> execute(argument, destFolder, zipFile));
    }

    @Override
    protected ZipFile createZipFile(@NonNull ZipArgument argument, @NonNull File destination,
                                    @NonNull File originFile) {
        return new ZipFile(originFile, argument.toPassword());
    }

    @Override
    protected void run(@NonNull ZipArgument argument, @NonNull ZipFile zipFile, @NonNull File destination,
                       @NonNull File originFile) throws ZipException {
        zipFile.extractAll(destination.toPath().resolve(computeExtractedFolder(argument, originFile)).toString());
    }

    @Override
    protected String action() {
        return "Extracting";
    }

    @Override
    protected ZipOutput createOutput(@NonNull ZipArgument argument, @NonNull ZipFile zipFile,
                                     @NonNull ProgressMonitor progressMonitor, @NonNull File originFile) {
        final File outputFile = Paths.get(progressMonitor.getFileName()).getParent().toFile();
        return ZipOutput.builder()
                        .inputPath(originFile.getPath())
                        .outputPath(outputFile.getPath())
                        .lastModified(outputFile.lastModified())
                        .trackingInfo(argument.trackingInfo())
                        .build();
    }

}
