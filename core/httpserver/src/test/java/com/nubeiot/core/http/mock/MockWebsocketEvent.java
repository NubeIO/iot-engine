package com.nubeiot.core.http.mock;

import java.util.Arrays;
import java.util.List;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.ws.WebsocketEventMetadata;

import io.vertx.reactivex.core.eventbus.EventBus;

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
    public static final WebsocketEventMetadata ALL_EVENTS = WebsocketEventMetadata.create(SERVER_LISTENER,
                                                                                          SERVER_PROCESSOR,
                                                                                          SERVER_PUBLISHER);
    public static final WebsocketEventMetadata NO_PUBLISHER = WebsocketEventMetadata.create(SERVER_LISTENER,
                                                                                            SERVER_PROCESSOR);
    public static final WebsocketEventMetadata ONLY_PUBLISHER = WebsocketEventMetadata.create("rtc", SERVER_PUBLISHER);


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
