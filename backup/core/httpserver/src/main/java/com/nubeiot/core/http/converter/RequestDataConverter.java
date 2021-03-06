package com.nubeiot.core.http.converter;

import java.util.Optional;

import io.github.zero88.utils.Strings;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.base.HttpUtils.HttpHeaderUtils;
import com.nubeiot.core.http.base.HttpUtils.HttpRequests;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

//TODO should convert only useful HEADER also
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestDataConverter {

    public static RequestData convert(@NonNull RoutingContext context) {
        return RequestData.builder()
                          .headers(HttpHeaderUtils.serializeHeaders(context.request()))
                          .body(body(context))
                          .sort(HttpRequests.sort(context.request()))
                          .filter(HttpRequests.query(context.request()))
                          .pagination(HttpRequests.pagination(context.request()))
                          .build();
    }

    public static JsonObject body(@NonNull RoutingContext context) {
        final JsonObject params = JsonObject.mapFrom(context.pathParams());
        final JsonObject body = Optional.ofNullable(context.getBody())
                                        .map(b -> JsonData.tryParse(b).toJson())
                                        .orElseGet(JsonObject::new);
        return params.mergeIn(body, true);
    }

    public static RequestData convert(@NonNull ServerWebSocket context) {
        final RequestData.Builder builder = RequestData.builder();
        final String query = context.query();
        if (Strings.isBlank(query)) {
            return builder.build();
        }
        return builder.filter(HttpRequests.deserializeQuery(query)).build();
    }

}
