package com.nubeiot.core.http.mock;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.rest.AbstractRestEventApi;
import com.nubeiot.core.http.rest.RestApi;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockApiDefinition {

    @Path("/test")
    public static class MockAPI implements RestApi {

        @GET
        @Produces(HttpUtils.DEFAULT_CONTENT_TYPE)
        public JsonObject get() {
            return new JsonObject().put("abc", "xxx");
        }

        @GET
        @Path("/error")
        @Produces(HttpUtils.DEFAULT_CONTENT_TYPE)
        public JsonObject error() {
            throw new NubeException("error");
        }

    }


    public static class MockRestEventApi extends AbstractRestEventApi {

        @Override
        protected void initRoute() {
            EventModel model = EventModel.builder()
                                         .address("http.server.test")
                                         .addEvents(EventAction.GET_LIST, EventAction.GET_ONE, EventAction.CREATE,
                                                    EventAction.UPDATE, EventAction.PATCH)
                                         .build();
            this.addRouter(model, "/test/events", "/:event_id");
        }

    }

}
