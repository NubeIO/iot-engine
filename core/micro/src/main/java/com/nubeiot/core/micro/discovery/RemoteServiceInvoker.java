package com.nubeiot.core.micro.discovery;

import io.reactivex.Single;

import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.exceptions.ServiceNotFoundException;

import lombok.NonNull;

/**
 * Remote procedure call by eventbus mechanism
 *
 * @see <a href="https://en.wikipedia.org/wiki/Remote_procedure_call">Remote procedure call</a>
 */
public interface RemoteServiceInvoker {

    static String requestBy(@NonNull String serviceName) {
        return "service/" + serviceName;
    }

    static String notFoundMessage(String serviceLabel) {
        return serviceLabel + " is not found or out of service. Try again later";
    }

    /**
     * Request service name
     *
     * @return request service name
     */
    String requester();

    /**
     * Event client
     *
     * @return event client
     */
    @NonNull EventbusClient eventClient();

    /**
     * Defines remote service label that is used in case making  an intuitive error message
     *
     * @return remote service label. Default: {@code Remote service}
     */
    default String serviceLabel() {
        return "Remote service";
    }

    default Single<EventMessage> invoke(@NonNull String address, @NonNull EventAction action,
                                        @NonNull RequestData reqData) {
        reqData.headers().put(Headers.X_REQUEST_BY, RemoteServiceInvoker.requestBy(requester()));
        return invoke(address, EventMessage.initial(action, reqData));
    }

    default Single<EventMessage> invoke(@NonNull String address, @NonNull EventMessage message) {
        return eventClient().request(address, message).onErrorReturn(t -> {
            throw new ServiceNotFoundException(notFoundMessage(serviceLabel()), t);
        });
    }

}
