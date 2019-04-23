package com.nubeiot.core.http.mock;

import java.util.Arrays;
import java.util.List;

import io.vertx.reactivex.core.eventbus.EventBus;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.base.event.WebsocketServerEventMetadata;

public class MockWebsocketEvent {

    public static final EventModel SERVER_PROCESSOR = EventModel.builder()
                                                                .address("server.processor")
                                                                .pattern(EventPattern.REQUEST_RESPONSE)
                                                                .events(Arrays.asList(EventAction.GET_LIST,
                                                                                      EventAction.GET_ONE))
                                                                .build();
    public static final EventModel SERVER_LISTENER = EventModel.clone(SERVER_PROCESSOR, "socket.client2server",
                                                                      EventPattern.REQUEST_RESPONSE);
    public static final EventModel SERVER_PUBLISHER = EventModel.builder()
                                                                .address("socket.server2client")
                                                                .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                                                .event(EventAction.RETURN)
                                                                .build();
    public static final WebsocketServerEventMetadata ALL_EVENTS = WebsocketServerEventMetadata.create(SERVER_LISTENER,
                                                                                                      SERVER_PROCESSOR,
                                                                                                      SERVER_PUBLISHER);
    public static final WebsocketServerEventMetadata NO_PUBLISHER = WebsocketServerEventMetadata.create(SERVER_LISTENER,
                                                                                                        SERVER_PROCESSOR);
    public static final WebsocketServerEventMetadata ONLY_PUBLISHER = WebsocketServerEventMetadata.create("rtc",
                                                                                                          SERVER_PUBLISHER);


    public static class MockWebsocketEventServerHandler extends MockEventBusHandler {

        public MockWebsocketEventServerHandler(EventBus eventBus) {
            super(eventBus, SERVER_PROCESSOR);
        }

        @EventContractor(action = EventAction.GET_LIST, returnType = List.class)
        public List<String> list(RequestData data) {
            return Arrays.asList("1", "2", "3");
        }

        @EventContractor(action = EventAction.GET_ONE, returnType = String.class)
        public String one(RequestData data) {
            return "1";
        }

    }

}
