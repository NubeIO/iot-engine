package com.nubeiot.core.archiver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.nubeiot.core.component.EventClientProxy;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;

import lombok.NonNull;

public interface AsyncZip extends EventClientProxy {

    String EXT_ZIP_FILE = ".zip";

    @NonNull
    static String ext(@NonNull String fileName) {
        return fileName + EXT_ZIP_FILE;
    }

    @NonNull
    static String ext(@NonNull Path fileName) {
        return ext(fileName.toString());
    }

    @NonNull String notifiedAddress();

    default void run(@NonNull ZipArgument argument, @NonNull String destFolder, @NonNull String tobeZipped) {
        run(argument, Paths.get(destFolder), Paths.get(tobeZipped));
    }

    default void run(@NonNull ZipArgument argument, @NonNull Path destFolder, @NonNull Path tobeZipped) {
        run(argument, destFolder.toFile(), tobeZipped.toFile());
    }

    void run(@NonNull ZipArgument argument, @NonNull File destFolder, @NonNull File tobeZipped);

    default void onSuccess(@NonNull ZipOutput information) {
        transporter().publish(notifiedAddress(), EventMessage.success(EventAction.NOTIFY, information.toJson()));
    }

    default void onError(@NonNull Throwable throwable) {
        transporter().publish(notifiedAddress(), EventMessage.error(EventAction.NOTIFY_ERROR, throwable));
    }

}
