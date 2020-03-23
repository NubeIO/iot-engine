package com.nubeiot.core.archiver;

import java.nio.file.Path;

import com.nubeiot.core.component.EventClientProxy;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;

import lombok.NonNull;

/**
 * Represents Async archvier.
 *
 * @since 1.0.0
 */
public interface AsyncArchiver extends EventClientProxy {

    /**
     * The constant EXT_ZIP_FILE.
     */
    String EXT_ZIP_FILE = ".zip";

    /**
     * Appends {@code ext} in file name.
     *
     * @param fileName the file name
     * @return the string
     * @since 1.0.0
     */
    @NonNull
    static String ext(@NonNull String fileName) {
        return fileName + EXT_ZIP_FILE;
    }

    /**
     * Appends {@code ext} in file name.
     *
     * @param fileName the file name
     * @return the string
     * @since 1.0.0
     */
    @NonNull
    static String ext(@NonNull Path fileName) {
        return ext(fileName.toString());
    }

    /**
     * Notified address string.
     *
     * @return the string
     * @since 1.0.0
     */
    @NonNull String notifiedAddress();

    /**
     * On success.
     *
     * @param information the information
     * @since 1.0.0
     */
    default void onSuccess(@NonNull ZipOutput information) {
        transporter().publish(notifiedAddress(), EventMessage.success(EventAction.NOTIFY, information.toJson()));
    }

    /**
     * On error.
     *
     * @param argument  the argument
     * @param throwable the throwable
     * @since 1.0.0
     */
    default void onError(@NonNull ZipArgument argument, @NonNull Throwable throwable) {
        transporter().publish(notifiedAddress(),
                              EventMessage.error(EventAction.NOTIFY_ERROR, throwable, argument.trackingInfo()));
    }

}
