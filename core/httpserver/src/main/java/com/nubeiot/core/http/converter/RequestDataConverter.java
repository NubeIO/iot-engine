package com.nubeiot.core.http.converter;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.base.HttpUtils.HttpHeaderUtils;
import com.nubeiot.core.http.base.HttpUtils.HttpRequests;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

//TODO should convert only useful HEADER also
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestDataConverter {

    public static RequestData convert(RoutingContext context) {
        return RequestData.builder()
                          .headers(HttpHeaderUtils.serializeHeaders(context.request()))
                          .body(body(context)).sort(HttpRequests.sort(context.request()))
                          .filter(HttpRequests.query(context.request()))
                          .pagination(HttpRequests.pagination(context.request()))
                          .build();
    }

    public static JsonObject body(RoutingContext context) {
        JsonObject body = JsonObject.mapFrom(context.pathParams());
        return body.mergeIn(JsonData.tryParse(context.getBody()).toJson(), true);
    }

    public static RequestData convert(ServerWebSocket context) {
        final RequestData.Builder builder = RequestData.builder();
        final String query = context.query();
        if (Strings.isBlank(query)) {
            return builder.build();
        }
        return builder.filter(JsonObject.mapFrom(HttpRequests.deserializeQuery(query))).build();
    }

}
