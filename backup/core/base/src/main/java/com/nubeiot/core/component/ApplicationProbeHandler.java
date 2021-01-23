package com.nubeiot.core.component;

import java.util.Arrays;
import java.util.Collection;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.ErrorData;

import lombok.NonNull;

/**
 * Application probe handler
 * <p>
 * It is handler by pattern {@link EventPattern#PUBLISH_SUBSCRIBE}
 */
public interface ApplicationProbeHandler extends EventListener {

    @Override
    default @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.NOTIFY, EventAction.NOTIFY_ERROR);
    }

    @EventContractor(action = EventAction.NOTIFY, returnType = boolean.class)
    boolean success(@NonNull RequestData requestData);

    @EventContractor(action = EventAction.NOTIFY_ERROR, returnType = boolean.class)
    boolean error(@NonNull ErrorData error);

    /**
     * Application readiness handler
     *
     * @see ApplicationProbe#readiness()
     */
    interface ApplicationReadinessHandler extends ApplicationProbeHandler {}


    /**
     * Application liveness handler
     *
     * @see ApplicationProbe#liveness()
     */
    interface ApplicationLivenessHandler extends ApplicationProbeHandler {}

}
