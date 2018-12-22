package com.nubeiot.core.http.mock;

import java.util.Arrays;
import java.util.List;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.WebsocketEventMetadata;

import io.vertx.reactivex.core.eventbus.EventBus;

public class MockWebsocketEvent {

    public static final EventModel SERVER_PROCESSOR = EventModel.builder()
                                                                .address("server.processor")
                                                                .pattern(EventPattern.REQUEST_RESPONSE)
                                                                .event(EventAction.GET_LIST)
                                                                .build();
    public static final EventModel SERVER_LISTENER = EventModel.clone(SERVER_PROCESSOR, "socket.client2server",
                                                                      EventPattern.PUBLISH_SUBSCRIBE);
    public static final EventModel SERVER_PUBLISHER = EventModel.builder()
                                                                .address("socket.server2client")
                                                                .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                                                .event(EventAction.GET_LIST)
                                                                .build();
    public static final WebsocketEventMetadata DEFAULT_METADATA = WebsocketEventMetadata.builder()
                                                                                        .listener(SERVER_LISTENER)
                                                                                        .publisher(SERVER_PUBLISHER)
                                                                                        .processor(SERVER_PROCESSOR)
                                                                                        .build();


    public static class MockWebsocketEventServerHandler extends MockEventBusHandler {

        public MockWebsocketEventServerHandler(EventBus eventBus) {
            super(eventBus, SERVER_PROCESSOR);
        }

        @EventContractor(events = EventAction.GET_LIST, returnType = List.class)
        public List<String> list(RequestData data) {
            return Arrays.asList("1", "2", "3");
        }

    }

}
