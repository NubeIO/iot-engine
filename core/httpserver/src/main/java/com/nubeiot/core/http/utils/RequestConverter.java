package com.nubeiot.core.http.utils;

import java.util.Objects;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

//TODO should convert HEADER also
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestConverter {

    public static RequestData convert(RoutingContext context) {
        JsonObject mergeInput = JsonObject.mapFrom(context.pathParams());
        String body = context.getBodyAsString();
        if (Strings.isNotBlank(body)) {
            final JsonObject bodyAsJson = context.getBodyAsJson();
            if (Objects.nonNull(bodyAsJson)) {
                mergeInput.mergeIn(bodyAsJson, true);
            }
        }
        final RequestData.Builder builder = RequestData.builder();
        if (context.request().method() == HttpMethod.GET) {
            Pagination pagination = Pagination.builder()
                                              .page(context.queryParams().get("page"))
                                              .perPage(context.queryParams().get("per_page"))
                                              .build();
            builder.pagination(pagination);
        }

        final String query = context.request().query();
        if (Strings.isBlank(query)) {
            return builder.body(mergeInput).build();
        }
        return builder.body(mergeInput).filter(JsonObject.mapFrom(HttpUtils.deserializeQuery(query))).build();
    }

    public static RequestData convert(io.vertx.reactivex.ext.web.RoutingContext context) {
        return convert(context.getDelegate());
    }

    public static RequestData convert(ServerWebSocket context) {
        final RequestData.Builder builder = RequestData.builder();
        final String query = context.query();
        if (Strings.isBlank(query)) {
            return builder.build();
        }
        return builder.filter(JsonObject.mapFrom(HttpUtils.deserializeQuery(query))).build();
    }

    public static RequestData convert(EventMessage msg) {
        return RequestData.builder().body(msg.getData()).build();
    }

}
