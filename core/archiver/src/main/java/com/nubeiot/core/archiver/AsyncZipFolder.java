package com.nubeiot.core.archiver;

import java.io.File;
import java.nio.file.Path;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.utils.ExecutorHelpers;
import com.nubeiot.core.utils.Strings;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.progress.ProgressMonitor.Result;
import net.lingala.zip4j.progress.ProgressMonitor.State;

@Getter
@Accessors(fluent = true)
@Builder(builderClassName = "Builder")
public final class AsyncZipFolder implements AsyncZip {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncZipFolder.class);

    @NonNull
    private final EventbusClient transporter;
    @NonNull
    private final String notifiedAddress;

    @Override
    public void run(@NonNull ZipArgument argument, @NonNull File destFolder, @NonNull File tobeZipped) {
        ExecutorHelpers.blocking(transporter.getVertx(), () -> doRun(argument, destFolder, tobeZipped));
    }

    private void doRun(@NonNull ZipArgument argument, @NonNull File destFolder, @NonNull File tobeZipped) {
        try {
            final ZipFile zipFile = createZipFile(argument, destFolder, tobeZipped);
            final ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
            zipFile.addFolder(tobeZipped, argument.zipParameters());
            transporter.getVertx()
                       .setPeriodic(argument.watcherDelayInMilli(),
                                    id -> watch(id, argument, progressMonitor, zipFile.getFile()));
        } catch (ZipException e) {
            onError(argument, e);
        }
    }

    private ZipFile createZipFile(@NonNull ZipArgument argument, @NonNull File destination, @NonNull File origin) {
        final String dest = destination.toPath().resolve(argument.getZipFileName(origin)).toString();
        final String password = argument.password();
        final ZipFile zipFile = new ZipFile(AsyncZip.ext(dest),
                                            Strings.isBlank(password) ? null : password.toCharArray());
        zipFile.setRunInThread(true);
        return zipFile;
    }

    private void watch(long timerId, @NonNull ZipArgument argument, @NonNull ProgressMonitor progressMonitor,
                       @NonNull File archiveFile) {
        final Path path = archiveFile.toPath();
        if (progressMonitor.getState().equals(State.BUSY)) {
            LOGGER.info("Zipping: {} | Progress: {}% | Current file: {} | Current task: {}", path,
                        progressMonitor.getPercentDone(), progressMonitor.getFileName(),
                        progressMonitor.getCurrentTask());
            return;
        }
        transporter.getVertx().cancelTimer(timerId);
        final Result result = progressMonitor.getResult();
        if (result == Result.SUCCESS) {
            onSuccess(ZipOutput.builder()
                               .originFile(progressMonitor.getFileName())
                               .zipFile(path.toString())
                               .size(archiveFile.length())
                               .lastModified(archiveFile.lastModified())
                               .trackingInfo(argument.trackingInfo())
                               .build());
            return;
        }
        onError(argument, progressMonitor.getException());
    }

}
