package com.nubeiot.core.micro.discovery;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.ReplyEventHandler;

import lombok.NonNull;

public interface RemoteDiscoverService {

    /**
     * Gateway index address
     *
     * @return gateway index address
     */
    String gatewayAddress();

    /**
     * Request service name
     *
     * @return request service name
     */
    String requestService();

    /**
     * Event client
     *
     * @return event client
     */
    EventController eventClient();

    /**
     * Destination address
     *
     * @return destination address
     */
    String destination();

    EventAction action();

    default EventPattern pattern() {
        return EventPattern.REQUEST_RESPONSE;
    }

    default Single<JsonObject> execute(JsonObject data) {
        return execute(RequestData.builder().body(data).build());
    }

    default Single<JsonObject> execute(@NonNull RequestData requestData) {
        requestData.headers().put(Headers.X_REQUEST_BY, requestService());
        return Single.<EventMessage>create(emitter -> {
            eventClient().request(DeliveryEvent.builder()
                                               .address(destination())
                                               .action(action())
                                               .pattern(EventPattern.REQUEST_RESPONSE)
                                               .addPayload(requestData)
                                               .build(), ReplyEventHandler.builder()
                                                                          .system("REMOTE_DISCOVER")
                                                                          .action(action())
                                                                          .address(destination())
                                                                          .success(emitter::onSuccess)
                                                                          .exception(emitter::onError)
                                                                          .build());
        }).map(EventMessage::getData);
    }

}
