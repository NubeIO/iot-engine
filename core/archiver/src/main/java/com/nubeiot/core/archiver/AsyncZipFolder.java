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
    @NonNull
    private final ZipArgument zipArgument;

    @Override
    public void run(@NonNull File destFolder, @NonNull File tobeZipped) {
        ExecutorHelpers.blocking(transporter.getVertx(), () -> doRun(destFolder, tobeZipped));
    }

    private void doRun(@NonNull File destFolder, @NonNull File tobeZipped) {
        try {
            final ZipFile zipFile = createZipFile(destFolder, tobeZipped);
            final ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
            zipFile.addFolder(tobeZipped, zipArgument().zipParameters());
            transporter.getVertx()
                       .setPeriodic(zipArgument().watcherDelayInMilli(),
                                    id -> watch(id, progressMonitor, zipFile.getFile()));
        } catch (ZipException e) {
            onError(e);
        }
    }

    private ZipFile createZipFile(@NonNull File destinationFolder, @NonNull File origin) {
        final String destination = destinationFolder.toPath().resolve(zipArgument().getZipFileName(origin)).toString();
        final String password = zipArgument().password();
        final ZipFile zipFile = new ZipFile(AsyncZip.ext(destination),
                                            Strings.isBlank(password) ? null : password.toCharArray());
        zipFile.setRunInThread(true);
        return zipFile;
    }

    private void watch(long timerId, @NonNull ProgressMonitor progressMonitor, @NonNull File archiveFile) {
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
                               .build());
            return;
        }
        onError(progressMonitor.getException());
    }

}
