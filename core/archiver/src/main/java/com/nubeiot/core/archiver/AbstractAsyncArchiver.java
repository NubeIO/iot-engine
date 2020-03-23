package com.nubeiot.core.archiver;

import java.io.File;
import java.nio.file.Path;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.event.EventbusClient;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.progress.ProgressMonitor.Result;
import net.lingala.zip4j.progress.ProgressMonitor.State;

@Getter
@Accessors(fluent = true)
@SuperBuilder
public abstract class AbstractAsyncArchiver implements AsyncArchiver {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NonNull
    private final EventbusClient transporter;
    @NonNull
    private final String notifiedAddress;

    protected void execute(@NonNull ZipArgument argument, @NonNull File destFolder, @NonNull File originFile) {
        try {
            final ZipFile zipFile = createZipFile(argument, destFolder, originFile);
            zipFile.setRunInThread(true);
            run(argument, zipFile, destFolder, originFile);
            transporter().getVertx()
                         .setPeriodic(argument.watcherDelayInMilli(), id -> watch(id, argument, zipFile, originFile));
        } catch (ZipException e) {
            onError(argument, new IllegalArgumentException(e));
        }
    }

    protected abstract ZipFile createZipFile(@NonNull ZipArgument argument, @NonNull File destination,
                                             @NonNull File originFile);

    protected abstract void run(@NonNull ZipArgument argument, @NonNull ZipFile zipFile, @NonNull File destination,
                                @NonNull File originFile) throws ZipException;

    protected void watch(long timerId, @NonNull ZipArgument argument, @NonNull ZipFile zipFile,
                         @NonNull File originFile) {
        final ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
        final Path path = zipFile.getFile().toPath();
        if (progressMonitor.getState().equals(State.BUSY)) {
            logger.debug("{} {} | Progress: {}% | Current file: {} | Current task: {}", action(), path,
                         progressMonitor.getPercentDone(), progressMonitor.getFileName(),
                         progressMonitor.getCurrentTask());
            return;
        }
        transporter().getVertx().cancelTimer(timerId);
        final Result result = progressMonitor.getResult();
        if (result == Result.SUCCESS) {
            onSuccess(createOutput(argument, zipFile, progressMonitor, originFile));
            return;
        }
        onError(argument, new IllegalArgumentException(progressMonitor.getException()));
    }

    protected abstract String action();

    protected abstract ZipOutput createOutput(@NonNull ZipArgument argument, @NonNull ZipFile zipFile,
                                              @NonNull ProgressMonitor progressMonitor, @NonNull File originFile);

}
