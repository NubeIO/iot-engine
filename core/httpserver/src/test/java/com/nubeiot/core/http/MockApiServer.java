package com.nubeiot.core.http;

import java.util.Arrays;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.nubeiot.core.component.IComponent;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventType;
import com.nubeiot.core.exceptions.EngineException;
import com.nubeiot.core.exceptions.NubeException;
import com.zandero.rest.annotation.Get;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockApiServer {

    @Path("/api/test")
    public static class MockAPI {

        @Get
        @Produces(ApiConstants.DEFAULT_CONTENT_TYPE)
        public JsonObject get() {
            return new JsonObject().put("abc", "xxx");
        }

        @Get("/error")
        @Produces(ApiConstants.DEFAULT_CONTENT_TYPE)
        public JsonObject error() {
            throw new NubeException("error");
        }

    }


    public static class MockEventBusAPI extends AbstractEventBusRestApi {

        @Override
        protected void initRoute() {
            EventModel model = new EventModel("http.server.test").add(EventType.GET_LIST, EventType.GET_ONE,
                                                                      EventType.CREATE, EventType.UPDATE,
                                                                      EventType.PATCH);
            this.addRouter(model, "/test/event", "event_id");
        }

    }


    @RequiredArgsConstructor
    private static abstract class MockEventBusHandler implements IComponent, EventHandler {

        private final EventBus eventBus;

        @Override
        public void start() throws NubeException {
            this.eventBus.consumer("http.server.test", this::handleMessage);
        }

        @Override
        public void stop() throws NubeException {

        }

        @Override
        public List<EventType> getAvailableEvents() {
            return Arrays.asList(EventType.GET_LIST, EventType.GET_ONE, EventType.CREATE, EventType.UPDATE,
                                 EventType.PATCH);
        }

    }


    public static class MockEventBusErrorHandler extends MockEventBusHandler {

        public MockEventBusErrorHandler(EventBus eventBus) {
            super(eventBus);
        }

        @EventContractor(events = EventType.GET_LIST)
        public JsonObject list(RequestData data) {
            throw new RuntimeException("xxx");
        }

        @EventContractor(events = EventType.CREATE)
        public JsonObject create(RequestData data) {
            throw new EngineException("Engine error");
        }

        @EventContractor(events = EventType.UPDATE)
        public JsonObject update(RequestData data) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "invalid");
        }

    }


    public static class MockEventBusSuccessHandler extends MockEventBusHandler {

        public MockEventBusSuccessHandler(EventBus eventBus) {
            super(eventBus);
        }

        @EventContractor(events = EventType.GET_LIST, returnType = List.class)
        public List<String> list(RequestData data) {
            return Arrays.asList("1", "2", "3");
        }

        @EventContractor(events = EventType.GET_ONE, returnType = Integer.class)
        public int get(RequestData data) {
            return Integer.valueOf(data.getBody().getString("event_id"));
        }

        @EventContractor(events = EventType.CREATE)
        public JsonObject create(RequestData data) {
            return new JsonObject().put("create", "success");
        }

        @EventContractor(events = EventType.UPDATE, returnType = Single.class)
        public Single<String> update(RequestData data) {
            return Single.just("success");
        }

        @EventContractor(events = EventType.PATCH, returnType = Single.class)
        public Single<JsonObject> patch(RequestData data) {
            return Single.just(new JsonObject().put("patch", "success")
                                               .put("event_id", Integer.valueOf(data.getBody().getString("event_id"))));
        }

    }

}
