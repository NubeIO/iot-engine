package com.nubeiot.core.http.mock;

import java.util.Arrays;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.AbstractRestEventApi;
import com.nubeiot.core.http.ApiConstants;
import com.zandero.rest.annotation.Get;

import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockApiDefinition {

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


    public static class MockRestEventApi extends AbstractRestEventApi {

        @Override
        protected void initRoute() {
            EventModel model = EventModel.builder()
                                         .address("http.server.test")
                                         .events(Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE,
                                                               EventAction.CREATE, EventAction.UPDATE,
                                                               EventAction.PATCH))
                                         .build();
            this.addRouter(model, "/test/event", "event_id");
        }

    }

}
