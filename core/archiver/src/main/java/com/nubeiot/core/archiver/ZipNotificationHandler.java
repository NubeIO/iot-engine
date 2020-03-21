package com.nubeiot.core.archiver;

import java.util.Arrays;
import java.util.Collection;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.exceptions.ErrorData;

import lombok.NonNull;

public interface ZipNotificationHandler extends EventListener {

    @Override
    default @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.NOTIFY, EventAction.NOTIFY_ERROR);
    }

    @EventContractor(action = EventAction.NOTIFY, returnType = boolean.class)
    boolean success(@NonNull ZipOutput result);

    @EventContractor(action = EventAction.NOTIFY_ERROR, returnType = boolean.class)
    boolean error(@NonNull ErrorData error);

}
