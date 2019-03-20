package com.nubeiot.core.http.utils;

import java.util.Objects;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.base.HttpUtils.HttpParams;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

//TODO should convert only useful HEADER also
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestDataConverter {

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
        Pagination pagination = HttpParams.pagination(context.request());
        if (Objects.nonNull(pagination)) {
            builder.pagination(pagination);
        }
        return builder.headers(JsonObject.mapFrom(context.request().headers()))
                      .body(mergeInput)
                      .filter(HttpParams.query(context.request()))
                      .build();
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

}
