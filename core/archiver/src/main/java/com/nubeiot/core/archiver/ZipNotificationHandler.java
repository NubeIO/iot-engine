package com.nubeiot.core.archiver;

import java.util.Arrays;
import java.util.Collection;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.exceptions.ErrorData;

import lombok.NonNull;

/**
 * Represents Zip notification handler.
 *
 * @since 1.0.0
 */
public interface ZipNotificationHandler extends EventListener {

    @Override
    default @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.NOTIFY, EventAction.NOTIFY_ERROR);
    }

    /**
     * Handles {@code success}.
     *
     * @param result the result
     * @return the boolean
     * @since 1.0.0
     */
    @EventContractor(action = EventAction.NOTIFY, returnType = boolean.class)
    boolean success(@NonNull ZipOutput result);

    /**
     * Handles {@code error} case.
     *
     * @param error the error
     * @return the boolean
     * @since 1.0.0
     */
    @EventContractor(action = EventAction.NOTIFY_ERROR, returnType = boolean.class)
    boolean error(@NonNull ErrorData error);

}
